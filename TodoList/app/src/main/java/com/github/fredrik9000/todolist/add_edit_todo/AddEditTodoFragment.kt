package com.github.fredrik9000.todolist.add_edit_todo

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent.*
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.add_edit_todo.DatePickerFragment.OnSelectDateDialogInteractionListener
import com.github.fredrik9000.todolist.add_edit_todo.TimePickerFragment.OnSelectTimeDialogInteractionListener
import com.github.fredrik9000.todolist.databinding.FragmentAddEditTodoBinding
import com.github.fredrik9000.todolist.model.Todo
import com.google.android.material.snackbar.Snackbar
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.DateFormat
import java.util.*

class AddEditTodoFragment : Fragment(), OnSelectDateDialogInteractionListener, OnSelectTimeDialogInteractionListener, EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentAddEditTodoBinding? = null
    private val binding get() = _binding!!

    private lateinit var addEditTodoViewModel: AddEditTodoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentAddEditTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addEditTodoViewModel.title = binding.todoTitleEditText.text.toString().trim()
        addEditTodoViewModel.description = binding.todoDescriptionEditText.text.toString()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Since onViewCreated will run when navigating back with Navigation Component we don't want to override the view model with arguments or saved state
        if (!this::addEditTodoViewModel.isInitialized) {
            addEditTodoViewModel = ViewModelProvider(this).get(AddEditTodoViewModel::class.java)
            addEditTodoViewModel.setValuesFromArgumentsOrSavedState(arguments)

            // Need to set up the notification state for both new and existing tasks, since tasks without a notification will be given a new notification id
            addEditTodoViewModel.setupNotificationState(arguments)
            addEditTodoViewModel.setupGeofenceNotificationState(arguments)
        }

        if (addEditTodoViewModel.title.isEmpty()) {
            binding.saveTodoButton.isEnabled = false
            binding.saveTodoButton.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
        } else {
            binding.saveTodoButton.isEnabled = true
            binding.saveTodoButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            binding.todoTitleEditText.setText(addEditTodoViewModel.title)
        }

        binding.todoDescriptionEditText.setText(addEditTodoViewModel.description)

        setupPriorityPicker()

        displayNotificationAddedStateIfActive()
        displayGeofenceNotificationAddedStateIfActive()

        binding.todoTitleEditText.addTextChangedListener(titleTextWatcher)
        binding.titleMic.setOnClickListener(titleSpeechToTextListener)
        binding.descriptionMic.setOnClickListener(descriptionSpeechToTextListener)
        binding.saveTodoButton.setOnClickListener(saveButtonListener)
        binding.removeNotificationButton.setOnClickListener(removeNotificationButtonListener)
        binding.addUpdateNotificationButton.setOnClickListener(addNotificationButtonListener)
        binding.removeGeofenceNotificationButton.setOnClickListener(removeGeofenceNotificationButtonListener)
        binding.addUpdateGeofenceNotificationButton.setOnClickListener(addGeofenceNotificationButtonListener)

        setConfirmGeofenceObserver()
    }

    private fun setConfirmGeofenceObserver() {
        val savedStateHandle = NavHostFragment.findNavController(this).currentBackStackEntry!!.savedStateHandle
        val geofenceLiveData = savedStateHandle.getLiveData<GeofenceMapFragment.GeofenceData>(GeofenceMapFragment.GeofenceData.GEOFENCE_DATA)
        geofenceLiveData.observe(viewLifecycleOwner, { geofenceData ->
            addEditTodoViewModel.geofenceRadius = geofenceData.radius
            addEditTodoViewModel.geofenceLatitude = geofenceData.latitude
            addEditTodoViewModel.geofenceLongitude = geofenceData.longitude
            addEditTodoViewModel.hasGeofenceNotification = true
            addEditTodoViewModel.geofenceNotificationUpdateState = NotificationUpdateState.ADDED_NOTIFICATION
            savedStateHandle.remove<GeofenceMapFragment.GeofenceData>(GeofenceMapFragment.GeofenceData.GEOFENCE_DATA)
            displayGeofenceNotificationAddedState()
        })
    }

    private fun setupPriorityPicker() {
        binding.priorityPickerSeekbar.progress = addEditTodoViewModel.priority
        configurePriorityPickerWithCurrentValues()
        binding.priorityPickerSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                addEditTodoViewModel.priority = progress
                configurePriorityPickerWithCurrentValues()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun configurePriorityPickerWithCurrentValues() {
        binding.priorityPickerLabel.text = resources.getString(R.string.priority_label, addEditTodoViewModel.getLabelForCurrentPriority())
        val priorityColorId = addEditTodoViewModel.getColorForCurrentPriority()
        binding.priorityPickerLabel.setTextColor(priorityColorId)
        binding.priorityPickerSeekbar.thumbTintList = ColorStateList.valueOf(priorityColorId)
        binding.priorityPickerSeekbar.progressTintList = ColorStateList.valueOf(priorityColorId)
    }

    private fun displayNotificationAddedStateIfActive() {
        if (addEditTodoViewModel.hasActiveTimedNotification) {
            displayNotificationAddedState(addEditTodoViewModel.createNotificationCalendar())
        }
    }

    private fun displayNotificationAddedState(notificationCalendar: Calendar) {
        binding.removeNotificationButton.visibility = View.VISIBLE
        binding.addUpdateNotificationButton.text = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US).format(notificationCalendar.time)
    }

    private fun displayNotificationNotAddedState() {
        binding.addUpdateNotificationButton.setText(R.string.add_timed_notification)
        binding.removeNotificationButton.visibility = View.GONE
    }

    private fun displayGeofenceNotificationAddedStateIfActive() {
        if (addEditTodoViewModel.hasGeofenceNotification) {
            displayGeofenceNotificationAddedState()
        }
    }

    private fun displayGeofenceNotificationAddedState() {
        binding.removeGeofenceNotificationButton.visibility = View.VISIBLE
        binding.addUpdateGeofenceNotificationButton.text = Todo.getAddressFromLatLong(requireContext(), addEditTodoViewModel.geofenceLatitude, addEditTodoViewModel.geofenceLongitude, true)
    }

    private fun displayGeofenceNotificationNotAddedState() {
        binding.addUpdateGeofenceNotificationButton.setText(R.string.add_geofence_notification)
        binding.removeGeofenceNotificationButton.visibility = View.GONE
    }

    private val titleSpeechToTextListener: View.OnClickListener? = View.OnClickListener {
        initiateSpeechToText(TITLE_SPEECH_REQUEST_CODE)
    }

    private val descriptionSpeechToTextListener: View.OnClickListener? = View.OnClickListener {
        initiateSpeechToText(DESCRIPTION_SPEECH_REQUEST_CODE)
    }

    private fun initiateSpeechToText(requestCode: Int) {
        val intent = Intent((ACTION_RECOGNIZE_SPEECH)).apply {
            putExtra(EXTRA_LANGUAGE_MODEL, LANGUAGE_MODEL_FREE_FORM)
            putExtra(EXTRA_LANGUAGE, Locale.getDefault())
        }

        try {
            startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.cant_initiate_speech_recognition, Toast.LENGTH_SHORT).show()
        }
    }

    @ExperimentalStdlibApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == TITLE_SPEECH_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                data.getStringArrayListExtra(EXTRA_RESULTS)?.let {
                    binding.todoTitleEditText.append(it[0].capitalize(Locale.getDefault()))
                }
            }
        } else if (requestCode == DESCRIPTION_SPEECH_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                data.getStringArrayListExtra(EXTRA_RESULTS)?.let {
                    binding.todoDescriptionEditText.append(it[0].capitalize(Locale.getDefault()))
                }
            }
        }
    }

    private val saveButtonListener: View.OnClickListener? = View.OnClickListener {
        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        addEditTodoViewModel.saveTodoItem(alarmManager, binding.todoTitleEditText.text.toString(), binding.todoDescriptionEditText.text.toString())
        Navigation.findNavController(requireView()).navigateUp()
    }

    private val addNotificationButtonListener: View.OnClickListener? = View.OnClickListener {
        val datePickerFragment = DatePickerFragment()
        datePickerFragment.setTargetFragment(this@AddEditTodoFragment, DATE_PICKER_FRAGMENT_REQUEST_CODE)
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
            if (addEditTodoViewModel.isUndoDoubleClicked) {
                return@setAction
            }
            addEditTodoViewModel.updateLastClickedUndoTime()

            // When undoing, set the notification update state to what it was previously.
            addEditTodoViewModel.notificationUpdateState = tempNotificationUpdateState
            addEditTodoViewModel.hasNotification = true
            displayNotificationAddedState(addEditTodoViewModel.createNotificationCalendar())

            showUndoSuccessfulSnackbar()
        }.show()
    }

    private fun showUndoSuccessfulSnackbar() {
        Snackbar.make(
                binding.addEditTodoCoordinatorLayout,
                R.string.undo_successful,
                Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if ((requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) || requestCode == ACCESS_BACKGROUND_LOCATION_REQUEST_CODE) {
            navigateToGeofenceMap()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private val addGeofenceNotificationButtonListener: View.OnClickListener? = View.OnClickListener {
        if (!hasLocationPermissions()) {
            requestLocationPermissions()
        } else {
            navigateToGeofenceMap()
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            true
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    @AfterPermissionGranted(ACCESS_FINE_LOCATION_REQUEST_CODE)
    private fun requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestBackgroundLocationPermission()
        } else {
            EasyPermissions.requestPermissions(this, resources.getString(R.string.fine_location_rationale_message), ACCESS_FINE_LOCATION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @AfterPermissionGranted(ACCESS_BACKGROUND_LOCATION_REQUEST_CODE)
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            EasyPermissions.requestPermissions(this, resources.getString(R.string.background_location_rationale_message), ACCESS_BACKGROUND_LOCATION_REQUEST_CODE, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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
                R.string.geofence_removed,
                Snackbar.LENGTH_LONG
        ).setAction(R.string.undo) {
            if (addEditTodoViewModel.isUndoDoubleClicked) {
                return@setAction
            }
            addEditTodoViewModel.updateLastClickedUndoTime()

            // When undoing, set the notification update state to what it was previously.
            addEditTodoViewModel.geofenceNotificationUpdateState = tempNotificationUpdateState
            addEditTodoViewModel.hasGeofenceNotification = true
            displayGeofenceNotificationAddedState()

            showUndoSuccessfulSnackbar()
        }.show()
    }

    private val titleTextWatcher: TextWatcher? = object : TextWatcher {
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
        timePickerFragment.setTargetFragment(this@AddEditTodoFragment, TIME_PICKER_FRAGMENT_REQUEST_CODE)
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
        if (this::addEditTodoViewModel.isInitialized) {
            addEditTodoViewModel.saveState()
        }
    }

    companion object {
        private const val ACCESS_FINE_LOCATION_REQUEST_CODE = 1
        private const val ACCESS_BACKGROUND_LOCATION_REQUEST_CODE = 2
        private const val TITLE_SPEECH_REQUEST_CODE = 3
        private const val DESCRIPTION_SPEECH_REQUEST_CODE = 4
        private const val TIME_PICKER_FRAGMENT_REQUEST_CODE = 1001
        private const val DATE_PICKER_FRAGMENT_REQUEST_CODE = 1002

        const val ARGUMENT_TODO_ID: String = "ARGUMENT_TODO_ID"
        const val ARGUMENT_TITLE: String = "ARGUMENT_TITLE"
        const val ARGUMENT_DESCRIPTION: String = "ARGUMENT_DESCRIPTION"
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
            bundle.putString(ARGUMENT_TITLE, todo.title)
            bundle.putString(ARGUMENT_DESCRIPTION, todo.description)
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