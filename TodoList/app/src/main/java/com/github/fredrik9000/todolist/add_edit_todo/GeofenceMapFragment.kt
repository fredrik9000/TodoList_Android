package com.github.fredrik9000.todolist.add_edit_todo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.add_edit_todo.GeofenceRadiusFragment.GeofenceRadiusToFragmentInteractionListener
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

class GeofenceMapFragment : Fragment(), OnMapReadyCallback, GeofenceRadiusToFragmentInteractionListener {

    private lateinit var binding: FragmentGeofenceMapBinding
    private lateinit var geofenceMapViewModel: GeofenceMapViewModel
    private lateinit var map: GoogleMap
    private lateinit var userLocationButton: FloatingActionButton
    private lateinit var confirmGeofenceButton: FloatingActionButton
    private lateinit var cancelGeofenceButton: FloatingActionButton
    private lateinit var radiusContainer: FragmentContainerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGeofenceMapBinding.inflate(inflater, container, false)
        return binding.root
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
                    set(GeofenceMapViewModel.GEOFENCE_RADIUS_STATE, geofenceMapViewModel.geofenceRadius)
                    set(GeofenceMapViewModel.GEOFENCE_CENTER_LAT_STATE, geofenceMapViewModel.geofenceCenter.latitude)
                    set(GeofenceMapViewModel.GEOFENCE_CENTER_LONG_STATE, geofenceMapViewModel.geofenceCenter.longitude)
                    set(GeofenceMapViewModel.HAS_SET_GEOFENCE_STATE, geofenceMapViewModel.hasSetGeofence)
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
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapClickListener(OnMapClickListener { point ->
            // If the fragment is already showing, don't handle map clicks
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

        // Add and animate the geofence radius fragment, but there's no need to do this after rotating
        if (parentFragmentManager.findFragmentByTag(GeofenceRadiusFragment.TAG) == null) {
            val geofenceRadiusFragment = GeofenceRadiusFragment().apply {
                arguments = Bundle().apply {
                    putInt(GeofenceRadiusFragment.RADIUS_ARGUMENT, geofenceMapViewModel.geofenceRadius)
                }
                setTargetFragment(this@GeofenceMapFragment, 0)
            }

            parentFragmentManager.beginTransaction()
                    .add(R.id.geofence_radius_fragment_container, geofenceRadiusFragment, GeofenceRadiusFragment.TAG)
                    .addToBackStack(null)
                    .commit()
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

    // TODO: Location permissions are checked before navigating to this fragment, but it would be better to also check it here.
    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location -> // Got last known location. In some rare situations this can be null.
            if (location != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), DEFAULT_MAP_ZOOM_LEVEL))
            }
        }
    }

    private fun moveToGeofenceLocation() {
         map.animateCamera(CameraUpdateFactory.newLatLngZoom(geofenceMapViewModel.geofenceCenter, DEFAULT_MAP_ZOOM_LEVEL))
    }

    private fun adjustGeofenceCircle() {
        val center = geofenceMapViewModel.geofenceCenter
        map.addCircle(CircleOptions()
                .center(center)
                .radius(geofenceMapViewModel.geofenceRadius.toDouble())
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.geofence_stroke))
                .fillColor(ContextCompat.getColor(requireContext(), R.color.geofence_fill)))
    }

    override fun setGeofenceRadius(radius: Int) {
        geofenceMapViewModel.geofenceRadius = radius
        map.clear()
        adjustGeofenceCircle()
    }

    override fun exitAnimationFinished() {
        cancelGeoFence()
    }

    // Fade out all the fabs at their current position
    override fun exitAnimationStarted() {
        val duration = 270 // Same value as in the geofence radius fragment animation

        confirmGeofenceButton.startAnimation(AnimationUtils.loadAnimation(confirmGeofenceButton.context, android.R.anim.fade_out).apply {
            this.duration = duration.toLong()
        })

        cancelGeofenceButton.startAnimation(AnimationUtils.loadAnimation(cancelGeofenceButton.context, android.R.anim.fade_out).apply {
            this.duration = duration.toLong()
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        geofenceMapViewModel.saveState()
    }

    companion object {
        const val ARGUMENT_HAS_GEOFENCE_NOTIFICATION: String = "ARGUMENT_HAS_GEOFENCE_NOTIFICATION"
        const val ARGUMENT_GEOFENCE_RADIUS: String = "ARGUMENT_GEOFENCE_RADIUS"
        const val ARGUMENT_GEOFENCE_LATITUDE: String = "ARGUMENT_GEOFENCE_LATITUDE"
        const val ARGUMENT_GEOFENCE_LONGITUDE: String = "ARGUMENT_GEOFENCE_LONGITUDE"

        private const val DEFAULT_MAP_ZOOM_LEVEL = 12f
    }
}