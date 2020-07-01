package com.github.fredrik9000.todolist.add_edit_todo

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.add_edit_todo.DatePickerFragment.OnSelectDateDialogInteractionListener
import com.github.fredrik9000.todolist.add_edit_todo.TimePickerFragment.OnSelectTimeDialogInteractionListener
import com.github.fredrik9000.todolist.databinding.FragmentAddEditTodoBinding
import com.github.fredrik9000.todolist.model.Todo
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.text.DateFormat
import java.util.*

class AddEditTodoFragment : Fragment(), OnSelectDateDialogInteractionListener, OnSelectTimeDialogInteractionListener {

    private lateinit var binding: FragmentAddEditTodoBinding
    private lateinit var addEditTodoViewModel: AddEditTodoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentAddEditTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addEditTodoViewModel = ViewModelProvider(this).get(AddEditTodoViewModel::class.java)

        // If arguments is not null we are editing an existing task
        if (arguments != null) {
            addEditTodoViewModel.setValuesFromArgumentsOrSavedState(requireArguments())
        }

        if (addEditTodoViewModel.isDescriptionEmpty()) {
            binding.saveTodoButton.isEnabled = false
            binding.saveTodoButton.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
        } else {
            binding.saveTodoButton.isEnabled = true
            binding.saveTodoButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            binding.todoDescriptionEditText.setText(addEditTodoViewModel.description)
        }

        binding.todoNoteEditText.setText(addEditTodoViewModel.note)

        setupPriorityPicker()
        setupNotificationState()
        setupGeofenceNotificationState()

        binding.todoDescriptionEditText.addTextChangedListener(descriptionTextWatcher)
        binding.saveTodoButton.setOnClickListener(saveButtonListener)
        binding.removeNotificationButton.setOnClickListener(removeNotificationButtonListener)
        binding.addUpdateNotificationButton.setOnClickListener(addNotificationButtonListener)
        binding.removeGeofenceNotificationButton.setOnClickListener(removeGeofenceNotificationButtonListener)
        binding.addUpdateGeofenceNotificationButton.setOnClickListener(addGeofenceNotificationButtonListener)

        setGeofenceObservers()
    }

    private fun setGeofenceObservers() {
        val savedStateHandle = NavHostFragment.findNavController(this).currentBackStackEntry!!.savedStateHandle

        val radiusLiveData = savedStateHandle.getLiveData<Int>(GeofenceMapViewModel.GEOFENCE_RADIUS_STATE)
        radiusLiveData.observe(viewLifecycleOwner, Observer { radius ->
            addEditTodoViewModel.geofenceRadius = radius
            savedStateHandle.remove<Int>(GeofenceMapViewModel.GEOFENCE_RADIUS_STATE)
        })

        val latitudeLiveData = savedStateHandle.getLiveData<Double>(GeofenceMapViewModel.GEOFENCE_CENTER_LAT_STATE)
        latitudeLiveData.observe(viewLifecycleOwner, Observer { latitude ->
            addEditTodoViewModel.geofenceLatitude = latitude
            savedStateHandle.remove<Double>(GeofenceMapViewModel.GEOFENCE_CENTER_LAT_STATE)
        })

        val longitudeLiveData = savedStateHandle.getLiveData<Double>(GeofenceMapViewModel.GEOFENCE_CENTER_LONG_STATE)
        longitudeLiveData.observe(viewLifecycleOwner, Observer { longitude ->
            addEditTodoViewModel.geofenceLongitude = longitude
            savedStateHandle.remove<Double>(GeofenceMapViewModel.GEOFENCE_CENTER_LONG_STATE)
        })

        val hasGeofenceLiveData = savedStateHandle.getLiveData<Boolean>(GeofenceMapViewModel.HAS_SET_GEOFENCE_STATE)
        hasGeofenceLiveData.observe(viewLifecycleOwner, Observer { hasGeofence ->
            addEditTodoViewModel.hasGeofenceNotification = hasGeofence
            addEditTodoViewModel.geofenceNotificationUpdateState = NotificationUpdateState.ADDED_NOTIFICATION
            savedStateHandle.remove<Boolean>(GeofenceMapViewModel.HAS_SET_GEOFENCE_STATE)
            displayGeofenceNotificationAddedState()
        })
    }

    private fun setupPriorityPicker() {
        binding.priorityPickerButton.text = addEditTodoViewModel.getLabelForCurrentPriority()
        binding.priorityPickerButton.setTextColor(addEditTodoViewModel.getColorForCurrentPriority())
        binding.priorityPickerButton.setOnClickListener {
            addEditTodoViewModel.togglePriorityValue()
            binding.priorityPickerButton.text = addEditTodoViewModel.getLabelForCurrentPriority()
            binding.priorityPickerButton.setTextColor(addEditTodoViewModel.getColorForCurrentPriority())
        }
    }

    private fun setupNotificationState() {
        addEditTodoViewModel.setupNotificationState(arguments)
        if (addEditTodoViewModel.hasNotification && !addEditTodoViewModel.isNotificationExpired()) {
            displayNotificationAddedState(addEditTodoViewModel.createNotificationCalendar())
        }
    }

    private fun displayNotificationAddedState(notificationCalendar: Calendar) {
        constrainAddNotificationButtonToAddedState(R.id.add_update_notification_button)
        binding.notificationTextView.text = getString(R.string.notification_by_time_set_heading,
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US).format(notificationCalendar.time))
        binding.removeNotificationButton.visibility = View.VISIBLE
        binding.addUpdateNotificationButton.setText(R.string.update_notification)
    }

    private fun displayNotificationNotAddedState() {
        constrainAddNotificationButtonToRemovedState(R.id.add_update_notification_button)
        binding.notificationTextView.text = getString(R.string.notification_by_time_not_set_heading)
        binding.addUpdateNotificationButton.setText(R.string.add_timed_notification)
        binding.removeNotificationButton.visibility = View.GONE
    }

    private fun setupGeofenceNotificationState() {
        addEditTodoViewModel.setupGeofenceNotificationState(arguments)
        if (addEditTodoViewModel.hasGeofenceNotification) {
            displayGeofenceNotificationAddedState()
        }
    }

    private fun displayGeofenceNotificationAddedState() {
        constrainAddNotificationButtonToAddedState(R.id.add_update_geofence_notification_button)
        binding.geofenceNotificationTextView.text = getString(R.string.notification_by_location_set_heading, getAddressFromLatLong(addEditTodoViewModel.geofenceLatitude, addEditTodoViewModel.geofenceLongitude))
        binding.removeGeofenceNotificationButton.visibility = View.VISIBLE
        binding.addUpdateGeofenceNotificationButton.setText(R.string.update_notification)
    }

    private fun displayGeofenceNotificationNotAddedState() {
        constrainAddNotificationButtonToRemovedState(R.id.add_update_geofence_notification_button)
        binding.geofenceNotificationTextView.text = getString(R.string.notification_by_location_not_set_heading)
        binding.addUpdateGeofenceNotificationButton.setText(R.string.add_geofence_notification)
        binding.removeGeofenceNotificationButton.visibility = View.GONE
    }

    private fun getAddressFromLatLong(latitude: Double, longitude: Double): String? {
        var address: String? = null
        try {
            address = Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1)[0].getAddressLine(0)
        } catch (e: IOException) {
            Log.w(TAG, "Could not get city from latitude and longitude: " + e.message)
        }
        return address ?: "Unknown"
    }

    // When both the add and remove buttons are showing, show each button on each side of the centered vertical guideline, moved towards the center
    private fun constrainAddNotificationButtonToAddedState(addNotificationButton: Int) {
        ConstraintSet().apply {
            clone(binding.addEditTodoConstraintLayout)
            connect(addNotificationButton, ConstraintSet.END, R.id.centered_vertical_guideline, ConstraintSet.END, resources.getDimension(R.dimen.notification_buttons_space_divided_by_2).toInt())
            setHorizontalBias(addNotificationButton, 1.0f)
            applyTo(binding.addEditTodoConstraintLayout)
        }
    }

    // When only the add button is showing, center it horizontally
    private fun constrainAddNotificationButtonToRemovedState(addNotificationButton: Int) {
        ConstraintSet().apply {
            clone(binding.addEditTodoConstraintLayout)
            connect(addNotificationButton, ConstraintSet.END, R.id.add_edit_todo_constraint_layout, ConstraintSet.END, 0)
            setHorizontalBias(addNotificationButton, 0.5f)
            applyTo(binding.addEditTodoConstraintLayout)
        }
    }

    private val saveButtonListener: View.OnClickListener? = View.OnClickListener {
        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        addEditTodoViewModel.saveTodoItem(alarmManager, binding.todoDescriptionEditText.text.toString(), binding.todoNoteEditText.text.toString())
        Navigation.findNavController(requireView()).navigateUp()
    }

    private val addNotificationButtonListener: View.OnClickListener? = View.OnClickListener {
        val datePickerFragment = DatePickerFragment()
        datePickerFragment.setTargetFragment(this@AddEditTodoFragment, 1)
        datePickerFragment.show(parentFragmentManager, "datePicker")
    }

    private val removeNotificationButtonListener: View.OnClickListener? = View.OnClickListener {
        displayNotificationNotAddedState()
        addEditTodoViewModel.hasNotification = false
        val tempNotificationUpdateState = addEditTodoViewModel.notificationUpdateState
        addEditTodoViewModel.notificationUpdateState = NotificationUpdateState.REMOVED_NOTIFICATION

        Snackbar.make(
                binding.addEditTodoCoordinatorLayout,
                R.string.notification_removed,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.undo) {
            if (addEditTodoViewModel.isUndoDoubleClicked()) {
                return@setAction
            }
            addEditTodoViewModel.updateLastClickedUndoTime()

            // When undoing, set the notification update state to what it was previously.
            addEditTodoViewModel.notificationUpdateState = tempNotificationUpdateState
            addEditTodoViewModel.hasNotification = true
            displayNotificationAddedState(addEditTodoViewModel.createNotificationCalendar())

            Snackbar.make(
                    binding.addEditTodoCoordinatorLayout,
                    R.string.undo_successful,
                    Snackbar.LENGTH_SHORT
            ).show()
        }.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                navigateToGeofenceMap()
            }
        }
    }

    private val addGeofenceNotificationButtonListener: View.OnClickListener? = View.OnClickListener {
        if (ContextCompat.checkSelfPermission(this@AddEditTodoFragment.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this@AddEditTodoFragment.requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            navigateToGeofenceMap()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(arrayOf<String?>(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION), LOCATION_REQUEST_CODE)
            } else {
                requestPermissions(arrayOf<String?>(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            }
        }
    }

    private fun navigateToGeofenceMap() {
        val bundle = Bundle()
        if (addEditTodoViewModel.hasGeofenceNotification) {
            bundle.putBoolean(GeofenceMapFragment.ARGUMENT_HAS_GEOFENCE_NOTIFICATION, addEditTodoViewModel.hasGeofenceNotification)
            bundle.putInt(GeofenceMapFragment.ARGUMENT_GEOFENCE_RADIUS, addEditTodoViewModel.geofenceRadius)
            bundle.putDouble(GeofenceMapFragment.ARGUMENT_GEOFENCE_LATITUDE, addEditTodoViewModel.geofenceLatitude)
            bundle.putDouble(GeofenceMapFragment.ARGUMENT_GEOFENCE_LONGITUDE, addEditTodoViewModel.geofenceLongitude)
        }
        Navigation.findNavController(requireView()).navigate(R.id.action_addEditTodoFragment_to_geofenceMapFragment, bundle)
    }

    private val removeGeofenceNotificationButtonListener: View.OnClickListener? = View.OnClickListener {
        displayGeofenceNotificationNotAddedState()
        addEditTodoViewModel.hasGeofenceNotification = false
        val tempNotificationUpdateState = addEditTodoViewModel.geofenceNotificationUpdateState
        addEditTodoViewModel.geofenceNotificationUpdateState = NotificationUpdateState.REMOVED_NOTIFICATION

        Snackbar.make(
                binding.addEditTodoCoordinatorLayout,
                R.string.notification_removed,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.undo) {
            if (addEditTodoViewModel.isUndoDoubleClicked()) {
                return@setAction
            }
            addEditTodoViewModel.updateLastClickedUndoTime()

            // When undoing, set the notification update state to what it was previously.
            addEditTodoViewModel.geofenceNotificationUpdateState = tempNotificationUpdateState
            addEditTodoViewModel.hasGeofenceNotification = true
            displayGeofenceNotificationAddedState()

            Snackbar.make(
                    binding.addEditTodoCoordinatorLayout,
                    R.string.undo_successful,
                    Snackbar.LENGTH_SHORT
            ).show()
        }.show()
    }

    private val descriptionTextWatcher: TextWatcher? = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
            if (charSequence.toString().trim().isEmpty()) {
                binding.saveTodoButton.isEnabled = false
                binding.saveTodoButton.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
            } else {
                binding.saveTodoButton.isEnabled = true
                binding.saveTodoButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@AddEditTodoFragment.requireContext(), R.color.colorAccent))
            }
        }

        override fun afterTextChanged(editable: Editable?) {}
    }

    override fun onSelectDateDialogInteraction(year: Int, month: Int, day: Int) {
        addEditTodoViewModel.setTemporaryNotificationDateValues(year, month, day)
        val timePickerFragment = TimePickerFragment()
        timePickerFragment.setTargetFragment(this@AddEditTodoFragment, 2)
        timePickerFragment.show(parentFragmentManager, "timePicker")
    }

    override fun onSelectTimeDialogInteraction(hour: Int, minute: Int) {
        addEditTodoViewModel.setTemporaryNotificationTimeValues(hour, minute)
        val notificationCalendar = addEditTodoViewModel.createTemporaryNotificationCalendar()
        if (notificationCalendar.timeInMillis < Calendar.getInstance().timeInMillis) {
            Toast.makeText(requireActivity().applicationContext, R.string.invalid_time, Toast.LENGTH_LONG).show()
        } else {
            addEditTodoViewModel.setFinallySelectedNotificationValues()
            displayNotificationAddedState(notificationCalendar)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // If one has navigated further, then the ViewModel will be null on the second rotation
        addEditTodoViewModel.description = binding.todoDescriptionEditText.text.toString().trim()
        addEditTodoViewModel.note = binding.todoNoteEditText.text.toString()
        addEditTodoViewModel.saveState()
    }

    companion object {
        private const val TAG: String = "AddEditTodoFragment"
        private const val LOCATION_REQUEST_CODE = 1

        const val ARGUMENT_TODO_ID: String = "ARGUMENT_TODO_ID"
        const val ARGUMENT_DESCRIPTION: String = "ARGUMENT_DESCRIPTION"
        const val ARGUMENT_NOTE: String = "ARGUMENT_NOTE"
        const val ARGUMENT_PRIORITY: String = "ARGUMENT_PRIORITY"
        const val ARGUMENT_HAS_NOTIFICATION: String = "ARGUMENT_HAS_NOTIFICATION"
        const val ARGUMENT_NOTIFICATION_ID: String = "ARGUMENT_NOTIFICATION_ID"
        const val ARGUMENT_NOTIFICATION_YEAR: String = "ARGUMENT_NOTIFICATION_YEAR"
        const val ARGUMENT_NOTIFICATION_MONTH: String = "ARGUMENT_NOTIFICATION_MONTH"
        const val ARGUMENT_NOTIFICATION_DAY: String = "ARGUMENT_NOTIFICATION_DAY"
        const val ARGUMENT_NOTIFICATION_HOUR: String = "ARGUMENT_NOTIFICATION_HOUR"
        const val ARGUMENT_NOTIFICATION_MINUTE: String = "ARGUMENT_NOTIFICATION_MINUTE"
        const val ARGUMENT_HAS_GEOFENCE_NOTIFICATION: String = "ARGUMENT_HAS_GEOFENCE_NOTIFICATION"
        const val ARGUMENT_GEOFENCE_NOTIFICATION_ID: String = "ARGUMENT_GEOFENCE_NOTIFICATION_ID"
        const val ARGUMENT_GEOFENCE_RADIUS: String = "ARGUMENT_GEOFENCE_RADIUS"
        const val ARGUMENT_GEOFENCE_LATITUDE: String = "ARGUMENT_GEOFENCE_LATITUDE"
        const val ARGUMENT_GEOFENCE_LONGITUDE: String = "ARGUMENT_GEOFENCE_LONGITUDE"

        fun createBundleForTodoItem(todo: Todo): Bundle? {
            // Double cannot be passed as safe args, which is used for latitude and longitude, so must create a bundle instead
            val bundle = Bundle()
            bundle.putInt(ARGUMENT_TODO_ID, todo.id)
            bundle.putString(ARGUMENT_DESCRIPTION, todo.description)
            bundle.putString(ARGUMENT_NOTE, todo.note)
            bundle.putInt(ARGUMENT_PRIORITY, todo.priority)
            bundle.putBoolean(ARGUMENT_HAS_NOTIFICATION, todo.notificationEnabled)
            if (todo.notificationEnabled) {
                bundle.putInt(ARGUMENT_NOTIFICATION_ID, todo.notificationId)
                bundle.putInt(ARGUMENT_NOTIFICATION_YEAR, todo.notifyYear)
                bundle.putInt(ARGUMENT_NOTIFICATION_MONTH, todo.notifyMonth)
                bundle.putInt(ARGUMENT_NOTIFICATION_DAY, todo.notifyDay)
                bundle.putInt(ARGUMENT_NOTIFICATION_HOUR, todo.notifyHour)
                bundle.putInt(ARGUMENT_NOTIFICATION_MINUTE, todo.notifyMinute)
            }
            bundle.putBoolean(ARGUMENT_HAS_GEOFENCE_NOTIFICATION, todo.geofenceNotificationEnabled)
            if (todo.geofenceNotificationEnabled) {
                bundle.putInt(ARGUMENT_GEOFENCE_NOTIFICATION_ID, todo.geofenceNotificationId)
                bundle.putInt(ARGUMENT_GEOFENCE_RADIUS, todo.geofenceRadius)
                bundle.putDouble(ARGUMENT_GEOFENCE_LATITUDE, todo.geofenceLatitude)
                bundle.putDouble(ARGUMENT_GEOFENCE_LONGITUDE, todo.geofenceLongitude)
            }
            return bundle
        }
    }
}