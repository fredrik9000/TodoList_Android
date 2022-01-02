package com.github.fredrik9000.todolist.add_edit_todo.add_edit_geofence

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.databinding.FragmentGeofenceMapBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.parcelize.Parcelize
import kotlin.math.log

class GeofenceMapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentGeofenceMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var geofenceMapViewModel: GeofenceMapViewModel
    private lateinit var map: GoogleMap
    private lateinit var userLocationButton: FloatingActionButton
    private lateinit var confirmGeofenceButton: FloatingActionButton
    private lateinit var cancelGeofenceButton: FloatingActionButton
    private lateinit var radiusContainer: FragmentContainerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGeofenceMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geofenceMapViewModel = ViewModelProvider(this).get(GeofenceMapViewModel::class.java)
        geofenceMapViewModel.setValuesFromArgumentsOrSavedState(requireArguments())

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        radiusContainer = binding.geofenceRadiusFragmentContainer
        userLocationButton = binding.userLocationButton
        confirmGeofenceButton = binding.confirmGeofenceButton
        cancelGeofenceButton = binding.cancelGeofenceButton

        userLocationButton.setOnClickListener { moveToCurrentLocation() }
        cancelGeofenceButton.setOnClickListener { parentFragmentManager.popBackStack() }
        confirmGeofenceButton.setOnClickListener {
            if (parentFragmentManager.findFragmentByTag(GeofenceRadiusFragment.TAG) != null) {
                // Remove the geofence radius fragment without any animations
                GeofenceRadiusFragment.disableAnimations = true
                parentFragmentManager.popBackStackImmediate()
                GeofenceRadiusFragment.disableAnimations = false

                // Pass data to parent fragment and navigate back
                Navigation.findNavController(it).previousBackStackEntry!!.savedStateHandle.apply {
                    set(GeofenceData.GEOFENCE_DATA, GeofenceData(geofenceMapViewModel.geofenceRadius, geofenceMapViewModel.geofenceCenter.latitude, geofenceMapViewModel.geofenceCenter.longitude))
                }
                Navigation.findNavController(it).navigateUp()
            }
        }
    }

    private fun cancelGeoFence() {
        map.clear()

        confirmGeofenceButton.visibility = View.GONE
        cancelGeofenceButton.visibility = View.GONE
        radiusContainer.visibility = View.GONE
        userLocationButton.visibility = View.VISIBLE

        // Fade in the location button
        userLocationButton.startAnimation(AnimationUtils.loadAnimation(userLocationButton.context, android.R.anim.fade_in).apply {
            duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        })
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapClickListener(OnMapClickListener { point ->
            // If the radius fragment is showing, don't handle map clicks
            if (parentFragmentManager.findFragmentByTag(GeofenceRadiusFragment.TAG) != null) {
                return@OnMapClickListener
            }
            map.clear()
            geofenceMapViewModel.geofenceCenter = point
            geofenceMapViewModel.hasSetGeofence = true
            setGeofence(true)
        })

        // Set the geofence in case the task already has one
        if (geofenceMapViewModel.hasSetGeofence) {
            setGeofence(false)
            moveToGeofenceLocation()
        } else {
            moveToCurrentLocation()
        }
    }

    private fun setGeofence(fadeInFabs: Boolean) {
        map.addMarker(MarkerOptions().position(geofenceMapViewModel.geofenceCenter))
        adjustGeofenceCircle()

        confirmGeofenceButton.visibility = View.VISIBLE
        cancelGeofenceButton.visibility = View.VISIBLE
        userLocationButton.visibility = View.GONE
        radiusContainer.visibility = View.VISIBLE

        // Add and animate the geofence radius fragment, but there's no need to do this after rotating (in which case findFragmentByTag will return null).
        if (parentFragmentManager.findFragmentByTag(GeofenceRadiusFragment.TAG) == null) {
            val geofenceRadiusFragment = GeofenceRadiusFragment().apply {
                arguments = bundleOf(GeofenceRadiusFragment.RADIUS_ARGUMENT to geofenceMapViewModel.geofenceRadius)
            }

            parentFragmentManager.commit {
                addToBackStack(null)
                add(R.id.geofence_radius_fragment_container, geofenceRadiusFragment, GeofenceRadiusFragment.TAG)
            }

            parentFragmentManager.setFragmentResultListener(GeofenceRadiusFragment.SET_GEOFENCE_RADIUS_REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
                geofenceMapViewModel.geofenceRadius = bundle.getInt(GeofenceRadiusFragment.BUNDLE_RADIUS_KEY)

                // If the app dies due to a process death while the geofence radius fragment is open,then map won't be initialized when reopening the app.
                // Even so, the geofence will still be drawn, since onMapReady will handle this.
                if (this::map.isInitialized) {
                    map.clear()
                    adjustGeofenceCircle()
                }
            }

            parentFragmentManager.setFragmentResultListener(GeofenceRadiusFragment.EXIT_ANIMATION_STARTED_REQUEST_KEY, viewLifecycleOwner) { _, _ ->
                val duration = resources.getInteger(R.integer.expand_collapse_animation_duration)

                // Fade out all the fabs at their current position
                confirmGeofenceButton.startAnimation(AnimationUtils.loadAnimation(confirmGeofenceButton.context, android.R.anim.fade_out).apply {
                    this.duration = duration.toLong()
                })
                cancelGeofenceButton.startAnimation(AnimationUtils.loadAnimation(cancelGeofenceButton.context, android.R.anim.fade_out).apply {
                    this.duration = duration.toLong()
                })
            }

            parentFragmentManager.setFragmentResultListener(GeofenceRadiusFragment.EXIT_ANIMATION_FINISHED_REQUEST_KEY, viewLifecycleOwner) { _, _ ->
                cancelGeoFence()
            }
        }

        if (fadeInFabs) {
            confirmGeofenceButton.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in).apply {
                duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            })

            cancelGeofenceButton.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in).apply {
                duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            })
        }
    }

    // Location permissions are checked before navigating to this fragment.
    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation() {
        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), DEFAULT_MAP_ZOOM_LEVEL))
            }
        }
    }

    private fun moveToGeofenceLocation() {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(geofenceMapViewModel.geofenceCenter, DEFAULT_MAP_ZOOM_LEVEL))
    }

    private fun adjustGeofenceCircle() {
        map.addCircle(CircleOptions()
                .center(geofenceMapViewModel.geofenceCenter)
                .radius(geofenceMapViewModel.geofenceRadius.toDouble())
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.geofence_stroke))
                .fillColor(ContextCompat.getColor(requireContext(), R.color.geofence_fill)))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        geofenceMapViewModel.saveState()
    }

    @Parcelize
    data class GeofenceData(var radius: Int, var latitude: Double, var longitude: Double) : Parcelable {
        companion object {
            const val GEOFENCE_DATA: String = "GEOFENCE_DATA"
        }
    }

    companion object {
        const val ARGUMENT_HAS_GEOFENCE_NOTIFICATION = "ARGUMENT_HAS_GEOFENCE_NOTIFICATION"
        const val ARGUMENT_GEOFENCE_RADIUS = "ARGUMENT_GEOFENCE_RADIUS"
        const val ARGUMENT_GEOFENCE_LATITUDE = "ARGUMENT_GEOFENCE_LATITUDE"
        const val ARGUMENT_GEOFENCE_LONGITUDE = "ARGUMENT_GEOFENCE_LONGITUDE"

        private const val DEFAULT_MAP_ZOOM_LEVEL = 12f
    }
}