package com.github.fredrik9000.todolist.add_edit_todo;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.databinding.FragmentAddEditTodoBinding;
import com.github.fredrik9000.todolist.model.Todo;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditTodoFragment extends Fragment implements DatePickerFragment.OnSelectDateDialogInteractionListener, TimePickerFragment.OnSelectTimeDialogInteractionListener {

    private static final String TAG = "AddEditTodoFragment";

    public static final String ARGUMENT_TODO_ID = "ARGUMENT_TODO_ID";
    public static final String ARGUMENT_DESCRIPTION = "ARGUMENT_DESCRIPTION";
    public static final String ARGUMENT_NOTE = "ARGUMENT_NOTE";
    public static final String ARGUMENT_PRIORITY = "ARGUMENT_PRIORITY";
    public static final String ARGUMENT_HAS_NOTIFICATION = "ARGUMENT_HAS_NOTIFICATION";
    public static final String ARGUMENT_NOTIFICATION_ID = "ARGUMENT_NOTIFICATION_ID";
    public static final String ARGUMENT_NOTIFICATION_YEAR = "ARGUMENT_NOTIFICATION_YEAR";
    public static final String ARGUMENT_NOTIFICATION_MONTH = "ARGUMENT_NOTIFICATION_MONTH";
    public static final String ARGUMENT_NOTIFICATION_DAY = "ARGUMENT_NOTIFICATION_DAY";
    public static final String ARGUMENT_NOTIFICATION_HOUR = "ARGUMENT_NOTIFICATION_HOUR";
    public static final String ARGUMENT_NOTIFICATION_MINUTE = "ARGUMENT_NOTIFICATION_MINUTE";
    public static final String ARGUMENT_HAS_GEOFENCE_NOTIFICATION = "ARGUMENT_HAS_GEOFENCE_NOTIFICATION";
    public static final String ARGUMENT_GEOFENCE_NOTIFICATION_ID = "ARGUMENT_GEOFENCE_NOTIFICATION_ID";
    public static final String ARGUMENT_GEOFENCE_RADIUS = "ARGUMENT_GEOFENCE_RADIUS";
    public static final String ARGUMENT_GEOFENCE_LATITUDE = "ARGUMENT_GEOFENCE_LATITUDE";
    public static final String ARGUMENT_GEOFENCE_LONGITUDE = "ARGUMENT_GEOFENCE_LONGITUDE";

    private static final int LOCATION_REQUEST_CODE = 1;

    private FragmentAddEditTodoBinding binding;
    private AddEditTodoViewModel addEditTodoViewModel;

    public AddEditTodoFragment() {
        // Required empty public constructor
    }

    public static Bundle createBundleForTodoItem(Todo todo) {
        // Double cannot be passed as safe args, which is used for latitude and longitude, so must create a bundle instead
        Bundle bundle = new Bundle();
        bundle.putInt(AddEditTodoFragment.ARGUMENT_TODO_ID, todo.getId());
        bundle.putString(AddEditTodoFragment.ARGUMENT_DESCRIPTION, todo.getDescription());
        bundle.putString(AddEditTodoFragment.ARGUMENT_NOTE, todo.getNote());
        bundle.putInt(AddEditTodoFragment.ARGUMENT_PRIORITY, todo.getPriority());

        bundle.putBoolean(AddEditTodoFragment.ARGUMENT_HAS_NOTIFICATION, todo.isNotificationEnabled());
        if (todo.isNotificationEnabled()) {
            bundle.putInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_ID, todo.getNotificationId());
            bundle.putInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_YEAR, todo.getNotifyYear());
            bundle.putInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_MONTH, todo.getNotifyMonth());
            bundle.putInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_DAY, todo.getNotifyDay());
            bundle.putInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_HOUR, todo.getNotifyHour());
            bundle.putInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_MINUTE, todo.getNotifyMinute());
        }

        bundle.putBoolean(AddEditTodoFragment.ARGUMENT_HAS_GEOFENCE_NOTIFICATION, todo.isGeofenceNotificationEnabled());
        if (todo.isGeofenceNotificationEnabled()) {
            bundle.putInt(AddEditTodoFragment.ARGUMENT_GEOFENCE_NOTIFICATION_ID, todo.getGeofenceNotificationId());
            bundle.putInt(AddEditTodoFragment.ARGUMENT_GEOFENCE_RADIUS, todo.getGeofenceRadius());
            bundle.putDouble(AddEditTodoFragment.ARGUMENT_GEOFENCE_LATITUDE, todo.getGeofenceLatitude());
            bundle.putDouble(AddEditTodoFragment.ARGUMENT_GEOFENCE_LONGITUDE, todo.getGetGeofenceLongitude());
        }

        return bundle;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddEditTodoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addEditTodoViewModel = new ViewModelProvider(this).get(AddEditTodoViewModel.class);

        // If arguments is not null we are editing an existing task
        if (getArguments() != null) {
            addEditTodoViewModel.setValuesFromArgumentsOrSavedState(getArguments());
        }

        if (addEditTodoViewModel.isDescriptionEmpty()) {
            binding.saveTodoButton.setEnabled(false);
            binding.saveTodoButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        } else {
            binding.saveTodoButton.setEnabled(true);
            binding.saveTodoButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this.getContext(), R.color.colorAccent)));
            binding.todoDescriptionEditText.setText(addEditTodoViewModel.getDescription());
        }

        binding.todoNoteEditText.setText(addEditTodoViewModel.getNote());

        setupPriorityPicker();
        setupNotificationState();
        setupGeofenceNotificationState();

        binding.todoDescriptionEditText.addTextChangedListener(descriptionTextWatcher);
        binding.saveTodoButton.setOnClickListener(saveButtonListener);
        binding.removeNotificationButton.setOnClickListener(removeNotificationButtonListener);
        binding.addUpdateNotificationButton.setOnClickListener(addNotificationButtonListener);
        binding.removeGeofenceNotificationButton.setOnClickListener(removeGeofenceNotificationButtonListener);
        binding.addUpdateGeofenceNotificationButton.setOnClickListener(addGeofenceNotificationButtonListener);

        setGeofenceObservers();
    }

    private void setGeofenceObservers() {
        final SavedStateHandle savedStateHandle = NavHostFragment.findNavController(this).getCurrentBackStackEntry().getSavedStateHandle();
        MutableLiveData<Integer> radiusLiveData = savedStateHandle.getLiveData(GeofenceMapViewModel.GEOFENCE_RADIUS_STATE);
        radiusLiveData.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer radius) {
                addEditTodoViewModel.setGeofenceRadius(radius);
                savedStateHandle.remove(GeofenceMapViewModel.GEOFENCE_RADIUS_STATE);
            }
        });

        MutableLiveData<Double> latitudeLiveData = savedStateHandle.getLiveData(GeofenceMapViewModel.GEOFENCE_CENTER_LAT_STATE);
        latitudeLiveData.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double latitude) {
                addEditTodoViewModel.setGeofenceLatitude(latitude);
                savedStateHandle.remove(GeofenceMapViewModel.GEOFENCE_CENTER_LAT_STATE);
            }
        });

        MutableLiveData<Double> longitudeLiveData = savedStateHandle.getLiveData(GeofenceMapViewModel.GEOFENCE_CENTER_LONG_STATE);
        longitudeLiveData.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double longitude) {
                addEditTodoViewModel.setGeofenceLongitude(longitude);
                savedStateHandle.remove(GeofenceMapViewModel.GEOFENCE_CENTER_LONG_STATE);
            }
        });

        MutableLiveData<Boolean> hasGeofenceLiveData = savedStateHandle.getLiveData(GeofenceMapViewModel.HAS_SET_GEOFENCE_STATE);
        hasGeofenceLiveData.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean hasGeofence) {
                addEditTodoViewModel.setHasGeofenceNotification(hasGeofence);
                addEditTodoViewModel.setGeofenceNotificationUpdateState(NotificationUpdateState.ADDED_NOTIFICATION);
                savedStateHandle.remove(GeofenceMapViewModel.HAS_SET_GEOFENCE_STATE);
                displayGeofenceNotificationAddedState();
            }
        });
    }

    private void setupPriorityPicker() {
        binding.priorityPickerButton.setText(addEditTodoViewModel.getLabelForCurrentPriority());
        binding.priorityPickerButton.setTextColor(addEditTodoViewModel.getColorForCurrentPriority());
        binding.priorityPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEditTodoViewModel.togglePriorityValue();
                binding.priorityPickerButton.setText(addEditTodoViewModel.getLabelForCurrentPriority());
                binding.priorityPickerButton.setTextColor(addEditTodoViewModel.getColorForCurrentPriority());
            }
        });
    }

    private void setupNotificationState() {
        addEditTodoViewModel.setupNotificationState(getArguments());
        if (addEditTodoViewModel.hasNotification() && !addEditTodoViewModel.isNotificationExpired()) {
            displayNotificationAddedState(addEditTodoViewModel.createNotificationCalendar());
        }
    }

    private void displayNotificationAddedState(Calendar notificationCalendar) {
        constrainAddNotificationButtonToAddedState(R.id.add_update_notification_button);
        binding.notificationTextView.setText(getString(R.string.notification_by_time_set_heading,
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US).format(notificationCalendar.getTime())));
        binding.removeNotificationButton.setVisibility(View.VISIBLE);
        binding.addUpdateNotificationButton.setText(R.string.update_notification);
    }

    private void displayNotificationNotAddedState() {
        constrainAddNotificationButtonToRemovedState(R.id.add_update_notification_button);
        binding.notificationTextView.setText(getString(R.string.notification_by_time_not_set_heading));
        binding.addUpdateNotificationButton.setText(R.string.add_timed_notification);
        binding.removeNotificationButton.setVisibility(View.GONE);
    }

    private void setupGeofenceNotificationState() {
        addEditTodoViewModel.setupGeofenceNotificationState(getArguments());
        if (addEditTodoViewModel.hasGeofenceNotification()) {
            displayGeofenceNotificationAddedState();
        }
    }

    private void displayGeofenceNotificationAddedState() {
        constrainAddNotificationButtonToAddedState(R.id.add_update_geofence_notification_button);
        binding.geofenceNotificationTextView.setText(getString(R.string.notification_by_location_set_heading, getAddressFromLatLong(addEditTodoViewModel.getGeofenceLatitude(), addEditTodoViewModel.getGeofenceLongitude())));
        binding.removeGeofenceNotificationButton.setVisibility(View.VISIBLE);
        binding.addUpdateGeofenceNotificationButton.setText(R.string.update_notification);
    }

    private void displayGeofenceNotificationNotAddedState() {
        constrainAddNotificationButtonToRemovedState(R.id.add_update_geofence_notification_button);
        binding.geofenceNotificationTextView.setText(getString(R.string.notification_by_location_not_set_heading));
        binding.addUpdateGeofenceNotificationButton.setText(R.string.add_geofence_notification);
        binding.removeGeofenceNotificationButton.setVisibility(View.GONE);
    }

    private String getAddressFromLatLong(double latitude, double longitude) {
        String address = null;
        try {
            address = new Geocoder(getContext(), Locale.getDefault()).getFromLocation(latitude, longitude, 1).get(0).getAddressLine(0);
        } catch (IOException e) {
            Log.w(TAG, "Could not get city from latitude and longitude: " + e.getMessage());
        }
        return address != null ? address : "Unknown";
    }

    // When both the add and remove buttons are showing, show each button on each side of the centered vertical guideline, moved towards the center
    private void constrainAddNotificationButtonToAddedState(int addNotificationButton) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.addEditTodoConstraintLayout);
        constraintSet.connect(addNotificationButton, ConstraintSet.END, R.id.centered_vertical_guideline, ConstraintSet.END, (int) getResources().getDimension(R.dimen.notification_buttons_space_divided_by_2));
        constraintSet.setHorizontalBias(addNotificationButton, 1.0f);
        constraintSet.applyTo(binding.addEditTodoConstraintLayout);
    }

    // When only the add button is showing, center it horizontally
    private void constrainAddNotificationButtonToRemovedState(int addNotificationButton) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.addEditTodoConstraintLayout);
        constraintSet.connect(addNotificationButton, ConstraintSet.END, R.id.add_edit_todo_constraint_layout, ConstraintSet.END, 0);
        constraintSet.setHorizontalBias(addNotificationButton, 0.5f);
        constraintSet.applyTo(binding.addEditTodoConstraintLayout);
    }

    private View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            addEditTodoViewModel.saveTodoItem(alarmManager, binding.todoDescriptionEditText.getText().toString(), binding.todoNoteEditText.getText().toString());
            Navigation.findNavController(getView()).navigateUp();
        }
    };

    private View.OnClickListener addNotificationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            datePickerFragment.setTargetFragment(AddEditTodoFragment.this, 1);
            datePickerFragment.show(getParentFragmentManager(), "datePicker");
        }
    };

    private View.OnClickListener removeNotificationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            displayNotificationNotAddedState();
            addEditTodoViewModel.setHasNotification(false);
            final NotificationUpdateState tempNotificationUpdateState = addEditTodoViewModel.getNotificationUpdateState();
            addEditTodoViewModel.setNotificationUpdateState(NotificationUpdateState.REMOVED_NOTIFICATION);

            Snackbar snackbar = Snackbar.make(
                    binding.addEditTodoCoordinatorLayout,
                    R.string.notification_removed,
                    Snackbar.LENGTH_LONG
            ).setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (addEditTodoViewModel.isUndoDoubleClicked()) {
                        return;
                    }

                    addEditTodoViewModel.updateLastClickedUndoTime();

                    // When undoing, set the notification update state to what it was previously.
                    addEditTodoViewModel.setNotificationUpdateState(tempNotificationUpdateState);
                    addEditTodoViewModel.setHasNotification(true);
                    displayNotificationAddedState(addEditTodoViewModel.createNotificationCalendar());

                    Snackbar snackbar2 = Snackbar.make(
                            binding.addEditTodoCoordinatorLayout,
                            R.string.undo_successful,
                            Snackbar.LENGTH_SHORT
                    );
                    snackbar2.show();
                }
            });
            snackbar.show();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                navigateToGeofenceMap();
            }
        }
    }

    private View.OnClickListener addGeofenceNotificationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (ContextCompat.checkSelfPermission(AddEditTodoFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(AddEditTodoFragment.this.getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                navigateToGeofenceMap();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, LOCATION_REQUEST_CODE);
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                }
            }
        }
    };

    private void navigateToGeofenceMap() {
        Bundle bundle = new Bundle();
        if (addEditTodoViewModel.hasGeofenceNotification()) {
            bundle.putBoolean(GeofenceMapFragment.ARGUMENT_HAS_GEOFENCE_NOTIFICATION, addEditTodoViewModel.hasGeofenceNotification());
            bundle.putInt(GeofenceMapFragment.ARGUMENT_GEOFENCE_RADIUS, addEditTodoViewModel.getGeofenceRadius());
            bundle.putDouble(GeofenceMapFragment.ARGUMENT_GEOFENCE_LATITUDE, addEditTodoViewModel.getGeofenceLatitude());
            bundle.putDouble(GeofenceMapFragment.ARGUMENT_GEOFENCE_LONGITUDE, addEditTodoViewModel.getGeofenceLongitude());
        }
        Navigation.findNavController(getView()).navigate(R.id.action_addEditTodoFragment_to_geofenceMapFragment, bundle);
    }

    private View.OnClickListener removeGeofenceNotificationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            displayGeofenceNotificationNotAddedState();
            addEditTodoViewModel.setHasGeofenceNotification(false);
            final NotificationUpdateState tempNotificationUpdateState = addEditTodoViewModel.getGeofenceNotificationUpdateState();
            addEditTodoViewModel.setGeofenceNotificationUpdateState(NotificationUpdateState.REMOVED_NOTIFICATION);

            Snackbar snackbar = Snackbar.make(
                    binding.addEditTodoCoordinatorLayout,
                    R.string.notification_removed,
                    Snackbar.LENGTH_LONG
            ).setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (addEditTodoViewModel.isUndoDoubleClicked()) {
                        return;
                    }

                    addEditTodoViewModel.updateLastClickedUndoTime();

                    // When undoing, set the notification update state to what it was previously.
                    addEditTodoViewModel.setGeofenceNotificationUpdateState(tempNotificationUpdateState);
                    addEditTodoViewModel.setHasGeofenceNotification(true);
                    displayGeofenceNotificationAddedState();

                    Snackbar snackbar2 = Snackbar.make(
                            binding.addEditTodoCoordinatorLayout,
                            R.string.undo_successful,
                            Snackbar.LENGTH_SHORT
                    );
                    snackbar2.show();
                }
            });
            snackbar.show();
        }
    };

    private TextWatcher descriptionTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.toString().trim().length() == 0) {
                binding.saveTodoButton.setEnabled(false);
                binding.saveTodoButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            } else {
                binding.saveTodoButton.setEnabled(true);
                binding.saveTodoButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(AddEditTodoFragment.this.getContext(), R.color.colorAccent)));
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    public void onSelectDateDialogInteraction(int year, int month, int day) {
        addEditTodoViewModel.setTemporaryNotificationDateValues(year, month, day);
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setTargetFragment(AddEditTodoFragment.this, 2);
        timePickerFragment.show(getParentFragmentManager(), "timePicker");
    }

    @Override
    public void onSelectTimeDialogInteraction(int hour, int minute) {
        addEditTodoViewModel.setTemporaryNotificationTimeValues(hour, minute);
        Calendar notificationCalendar = addEditTodoViewModel.createTemporaryNotificationCalendar();
        if (notificationCalendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.invalid_time, Toast.LENGTH_LONG).show();
        } else {
            addEditTodoViewModel.setFinallySelectedNotificationValues();
            displayNotificationAddedState(notificationCalendar);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // If one has navigated further, then the ViewModel will be null on the second rotation
        if (addEditTodoViewModel != null) {
            addEditTodoViewModel.setDescription(binding.todoDescriptionEditText.getText().toString().trim());
            addEditTodoViewModel.setNote(binding.todoNoteEditText.getText().toString());
            addEditTodoViewModel.saveState();
        }
    }
}
