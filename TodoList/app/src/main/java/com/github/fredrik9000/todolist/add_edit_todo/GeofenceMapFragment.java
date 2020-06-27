package com.github.fredrik9000.todolist.add_edit_todo;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.databinding.FragmentGeofenceMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GeofenceMapFragment extends Fragment implements OnMapReadyCallback, GeofenceRadiusFragment.GeofenceRadiusToFragmentInteractionListener {

    public static final String ARGUMENT_HAS_GEOFENCE_NOTIFICATION = "ARGUMENT_HAS_GEOFENCE_NOTIFICATION";
    public static final String ARGUMENT_GEOFENCE_RADIUS = "ARGUMENT_GEOFENCE_RADIUS";
    public static final String ARGUMENT_GEOFENCE_LATITUDE = "ARGUMENT_GEOFENCE_LATITUDE";
    public static final String ARGUMENT_GEOFENCE_LONGITUDE = "ARGUMENT_GEOFENCE_LONGITUDE";

    FragmentGeofenceMapBinding binding;
    private GoogleMap map;
    private FloatingActionButton userLocationButton;
    private FloatingActionButton confirmGeofenceButton;
    private FloatingActionButton cancelGeofenceButton;
    private FragmentContainerView radius_container;
    private FragmentManager fragmentManager;
    private GeofenceMapViewModel geofenceMapViewModel;

    private static final float DEFAULT_MAP_ZOOM_LEVEL = 12;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGeofenceMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        geofenceMapViewModel = new ViewModelProvider(this).get(GeofenceMapViewModel.class);
        geofenceMapViewModel.setValuesFromArgumentsOrSavedState(getArguments());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fragmentManager = getParentFragmentManager();
        radius_container = binding.geofenceRadiusFragmentContainer;

        userLocationButton = binding.userLocationButton;
        confirmGeofenceButton = binding.confirmGeofenceButton;
        cancelGeofenceButton = binding.cancelGeofenceButton;

        userLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToCurrentLocation();
            }
        });

        cancelGeofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.popBackStack();
            }
        });

        confirmGeofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fragmentManager.findFragmentByTag(GeofenceRadiusFragment.TAG) != null) {
                    // Remove the geofence radius fragment without any animations
                    GeofenceRadiusFragment.disableAnimations = true;
                    fragmentManager.popBackStackImmediate();
                    GeofenceRadiusFragment.disableAnimations = false;

                    // Pass data to parent fragment and navigate
                    SavedStateHandle navBackStackEntrySavedStateHandle = Navigation.findNavController(getView()).getPreviousBackStackEntry().getSavedStateHandle();
                    navBackStackEntrySavedStateHandle.set(GeofenceMapViewModel.GEOFENCE_RADIUS_STATE, geofenceMapViewModel.getGeofenceRadius());
                    navBackStackEntrySavedStateHandle.set(GeofenceMapViewModel.GEOFENCE_CENTER_LAT_STATE, geofenceMapViewModel.getGeofenceCenter().latitude);
                    navBackStackEntrySavedStateHandle.set(GeofenceMapViewModel.GEOFENCE_CENTER_LONG_STATE, geofenceMapViewModel.getGeofenceCenter().longitude);
                    navBackStackEntrySavedStateHandle.set(GeofenceMapViewModel.HAS_SET_GEOFENCE_STATE, geofenceMapViewModel.hasSetGeofence());
                    Navigation.findNavController(getView()).navigateUp();
                }
            }
        });
    }

    private void cancelGeoFence() {
        map.clear();
        confirmGeofenceButton.setVisibility(View.GONE);
        cancelGeofenceButton.setVisibility(View.GONE);
        radius_container.setVisibility(View.GONE);

        userLocationButton.setVisibility(View.VISIBLE);
        // Fade in the location button
        Animation fadeInLocationButtonAnimation = android.view.animation.AnimationUtils.loadAnimation(userLocationButton.getContext(), android.R.anim.fade_in);
        fadeInLocationButtonAnimation.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        userLocationButton.startAnimation(fadeInLocationButtonAnimation);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                // If the fragment is already showing, don't handle map clicks
                if (fragmentManager.findFragmentByTag(GeofenceRadiusFragment.TAG) != null) {
                    return;
                }

                map.clear();
                geofenceMapViewModel.setGeofenceCenter(point);
                geofenceMapViewModel.setHasSetGeofence(true);
                setGeofence(true);
            }
        });

        // Set the geofence in case the task already has one
        if (geofenceMapViewModel.hasSetGeofence()) {
            setGeofence(false);
            moveToGeofenceLocation();
        } else {
            moveToCurrentLocation();
        }
    }

    private void setGeofence(boolean fadeInFabs) {
        map.addMarker(new MarkerOptions().position(geofenceMapViewModel.getGeofenceCenter()));
        adjustGeofenceCircle();

        confirmGeofenceButton.setVisibility(View.VISIBLE);
        cancelGeofenceButton.setVisibility(View.VISIBLE);
        userLocationButton.setVisibility(View.GONE);
        radius_container.setVisibility(View.VISIBLE);

        // Add and animate the geofence radius fragment, but no need after rotating
        if (fragmentManager.findFragmentByTag(GeofenceRadiusFragment.TAG) == null) {
            Bundle arguments = new Bundle();
            arguments.putInt(GeofenceRadiusFragment.RADIUS_ARGUMENT, geofenceMapViewModel.getGeofenceRadius());
            GeofenceRadiusFragment geofenceRadiusFragment = new GeofenceRadiusFragment();
            geofenceRadiusFragment.setArguments(arguments);
            geofenceRadiusFragment.setTargetFragment(GeofenceMapFragment.this, 0);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.geofence_radius_fragment_container, geofenceRadiusFragment, GeofenceRadiusFragment.TAG)
                    .addToBackStack(null)
                    .commit();
        }

        if (fadeInFabs) {
            Animation fadeInConfirmButtonAnimation = android.view.animation.AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
            confirmGeofenceButton.startAnimation(fadeInConfirmButtonAnimation);
            fadeInConfirmButtonAnimation.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
            Animation fadeInCancelButtonAnimation = android.view.animation.AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
            cancelGeofenceButton.startAnimation(fadeInCancelButtonAnimation);
            fadeInCancelButtonAnimation.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        }
    }

    // TODO: Location permissions are checked before navigating to this fragment, but it would be better to also check it here.
    @SuppressLint("MissingPermission")
    private void moveToCurrentLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_MAP_ZOOM_LEVEL));
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void moveToGeofenceLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(geofenceMapViewModel.getGeofenceCenter(), DEFAULT_MAP_ZOOM_LEVEL));
                }
            }
        });
    }

    private void adjustGeofenceCircle() {
        LatLng center = geofenceMapViewModel.getGeofenceCenter();
        if (center != null) {
            map.addCircle(new CircleOptions()
                    .center(center)
                    .radius(geofenceMapViewModel.getGeofenceRadius())
                    .strokeColor(ContextCompat.getColor(getContext(), R.color.geofence_stroke))
                    .fillColor(ContextCompat.getColor(getContext(), R.color.geofence_fill)));
        }
    }

    @Override
    public void setGeofenceRadius(int radius) {
        geofenceMapViewModel.setGeofenceRadius(radius);
        map.clear();
        adjustGeofenceCircle();
    }

    @Override
    public void exitAnimationFinished() {
        cancelGeoFence();
    }

    @Override
    public void exitAnimationStarted() {
        // Fade out all the fabs at their current position
        final int duration = 270;  // Same value as in the geofence radius fragment animation
        Animation fadeOutConfirmButtonAnimation = android.view.animation.AnimationUtils.loadAnimation(confirmGeofenceButton.getContext(), android.R.anim.fade_out);
        fadeOutConfirmButtonAnimation.setDuration(duration);
        confirmGeofenceButton.startAnimation(fadeOutConfirmButtonAnimation);
        Animation fadeOutCancelButtonAnimation = android.view.animation.AnimationUtils.loadAnimation(cancelGeofenceButton.getContext(), android.R.anim.fade_out);
        fadeOutCancelButtonAnimation.setDuration(duration);
        cancelGeofenceButton.startAnimation(fadeOutCancelButtonAnimation);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        geofenceMapViewModel.saveState();
    }
}
