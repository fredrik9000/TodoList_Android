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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.add_edit_todo.add_edit_geofence.GeofenceMapFragment
import com.github.fredrik9000.todolist.databinding.FragmentAddEditTodoBinding
import com.github.fredrik9000.todolist.model.Todo
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.DateFormat
import java.util.*

class AddEditTodoFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentAddEditTodoBinding? = null
    private val binding get() = _binding!!

    private lateinit var addEditTodoViewModel: AddEditTodoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        // Since onViewCreated will run when navigating back when using the Navigation Component
        // there is no need to override an initialized view model with supplied arguments or saved state
        if (!this::addEditTodoViewModel.isInitialized) {
            addEditTodoViewModel = ViewModelProvider(this).get(AddEditTodoViewModel::class.java)
            addEditTodoViewModel.setValuesFromArgumentsOrSavedState(arguments)

            // Need to set up the notification state for both new and existing tasks,
            // since tasks without an existing notification will be given a new notification id
            addEditTodoViewModel.setupNotificationState(arguments)
            addEditTodoViewModel.setupGeofenceNotificationState(arguments)
        }

        // If there is no title, don't allow saving the task
        if (addEditTodoViewModel.title.isEmpty()) {
            binding.saveTodoButton.isEnabled = false
            binding.saveTodoButton.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
        } else {
            binding.saveTodoButton.isEnabled = true
            binding.saveTodoButton.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.colorSecondary)
            )

            binding.todoTitleEditText.setText(addEditTodoViewModel.title)
        }

        binding.todoDescriptionEditText.setText(addEditTodoViewModel.description)

        setupPriorityPicker()

        if (addEditTodoViewModel.hasActiveTimedNotification) {
            displayNotificationAddedState(addEditTodoViewModel.createNotificationCalendar())
        }

        if (addEditTodoViewModel.hasGeofenceNotification) {
            displayGeofenceNotificationAddedState()
        }

        binding.todoTitleEditText.addTextChangedListener(titleTextWatcher)
        binding.titleMic.setOnClickListener(titleSpeechToTextListener)
        binding.descriptionMic.setOnClickListener(descriptionSpeechToTextListener)
        binding.saveTodoButton.setOnClickListener(saveButtonListener)
        binding.removeNotificationButton.setOnClickListener(removeNotificationButtonListener)
        binding.addUpdateNotificationButton.setOnClickListener(addNotificationButtonListener)
        binding.removeGeofenceNotificationButton.setOnClickListener(removeGeofenceNotificationButtonListener)
        binding.addUpdateGeofenceNotificationButton.setOnClickListener(addGeofenceNotificationButtonListener)

        setupConfirmGeofenceObserver()
    }

    private fun setupConfirmGeofenceObserver() {
        val savedStateHandle = NavHostFragment.findNavController(this).currentBackStackEntry!!.savedStateHandle
        savedStateHandle.getLiveData<GeofenceMapFragment.GeofenceData>(
            GeofenceMapFragment.GeofenceData.GEOFENCE_DATA
        ).observe(viewLifecycleOwner) {
            addEditTodoViewModel.geofenceRadius = it.radius
            addEditTodoViewModel.geofenceLatitude = it.latitude
            addEditTodoViewModel.geofenceLongitude = it.longitude
            addEditTodoViewModel.hasGeofenceNotification = true
            addEditTodoViewModel.geofenceNotificationUpdateState = NotificationUpdateState.ADDED_NOTIFICATION

            savedStateHandle.remove<GeofenceMapFragment.GeofenceData>(GeofenceMapFragment.GeofenceData.GEOFENCE_DATA)

            displayGeofenceNotificationAddedState()
        }
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
        binding.priorityPickerLabel.text = resources.getString(
            R.string.priority_label, addEditTodoViewModel.getLabelForCurrentPriority()
        )

        val priorityColorId = addEditTodoViewModel.getColorForCurrentPriority()
        binding.priorityPickerLabel.setTextColor(priorityColorId)
        binding.priorityPickerSeekbar.thumbTintList = ColorStateList.valueOf(priorityColorId)
        binding.priorityPickerSeekbar.progressTintList = ColorStateList.valueOf(priorityColorId)
    }

    private fun displayNotificationAddedState(notificationCalendar: Calendar) {
        binding.removeNotificationButton.visibility = View.VISIBLE
        binding.addUpdateNotificationButton.text = DateFormat.getDateTimeInstance(
            DateFormat.LONG, DateFormat.SHORT, Locale.US
        ).format(notificationCalendar.time)
    }

    private fun displayNotificationNotAddedState() {
        binding.addUpdateNotificationButton.setText(R.string.add_timed_notification)
        binding.removeNotificationButton.visibility = View.GONE
    }

    private fun displayGeofenceNotificationAddedState() {
        binding.removeGeofenceNotificationButton.visibility = View.VISIBLE
        binding.addUpdateGeofenceNotificationButton.text =
            Todo.getAddressFromLatLong(
                context = requireContext(),
                latitude = addEditTodoViewModel.geofenceLatitude,
                longitude = addEditTodoViewModel.geofenceLongitude,
                geofenceNotificationEnabled = true
            )
    }

    private fun displayGeofenceNotificationNotAddedState() {
        binding.addUpdateGeofenceNotificationButton.setText(R.string.add_geofence_notification)
        binding.removeGeofenceNotificationButton.visibility = View.GONE
    }

    private val titleSpeechToTextListener: View.OnClickListener = View.OnClickListener {
        initiateSpeechToText(registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK && activityResult.data != null) {
                activityResult.data!!.getStringArrayListExtra(EXTRA_RESULTS)?.let {
                    binding.todoTitleEditText.append(
                        it[0].replaceFirstChar { firstChar -> firstChar.titlecase(Locale.getDefault()) }
                    )
                }
            }
        })
    }

    private val descriptionSpeechToTextListener: View.OnClickListener = View.OnClickListener {
        initiateSpeechToText(registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK && activityResult.data != null) {
                activityResult.data!!.getStringArrayListExtra(EXTRA_RESULTS)?.let {
                    binding.todoDescriptionEditText.append(
                        it[0].replaceFirstChar { firstChar -> firstChar.titlecase(Locale.getDefault()) }
                    )
                }
            }
        })
    }

    private fun initiateSpeechToText(launcher: ActivityResultLauncher<Intent>) {
        try {
            launcher.launch(Intent((ACTION_RECOGNIZE_SPEECH)).apply {
                putExtra(EXTRA_LANGUAGE_MODEL, LANGUAGE_MODEL_FREE_FORM)
                putExtra(EXTRA_LANGUAGE, Locale.getDefault())
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.cant_initiate_speech_recognition, Toast.LENGTH_SHORT).show()
        }
    }

    private val saveButtonListener: View.OnClickListener = View.OnClickListener {
        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        addEditTodoViewModel.saveTodoItem(
            alarmManager = alarmManager,
            title = binding.todoTitleEditText.text.toString(),
            description = binding.todoDescriptionEditText.text.toString()
        )

        Navigation.findNavController(requireView()).navigateUp()
    }

    private val addNotificationButtonListener: View.OnClickListener = View.OnClickListener {
        MaterialDatePicker.Builder.datePicker().setTitleText("Pick a date")
            .setCalendarConstraints(CalendarConstraints.Builder().apply {
                setValidator(DateValidatorPointForward.now())
            }.build()).build().apply {
                addOnPositiveButtonClickListener { selectedDate ->
                    val clockFormat = if (android.text.format.DateFormat.is24HourFormat(requireContext())) {
                        TimeFormat.CLOCK_24H
                    } else {
                        TimeFormat.CLOCK_12H
                    }

                    MaterialTimePicker.Builder().setTitleText("Pick a time").setTimeFormat(clockFormat).build().apply {
                        addOnPositiveButtonClickListener { _ ->
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = selectedDate
                            val selectedYear = cal.get(Calendar.YEAR)
                            val selectedMonth = cal.get(Calendar.MONTH)
                            val selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                            val selectedHour = this.hour
                            val selectedMinute = this.minute

                            if (selectedDate < Calendar.getInstance().timeInMillis) {
                                Toast.makeText(
                                    requireActivity().applicationContext,
                                    R.string.invalid_time,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                addEditTodoViewModel.setSelectedNotificationValues(
                                    year = selectedYear,
                                    month = selectedMonth,
                                    day = selectedDay,
                                    hour = selectedHour,
                                    minute = selectedMinute
                                )

                                displayNotificationAddedState(Calendar.getInstance().also {
                                    it[selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute] = 0
                                })
                            }
                        }
                    }.show(parentFragmentManager, NOTIFICATION_TIME_PICKER_TAG)
                }
            }.show(parentFragmentManager, NOTIFICATION_DATE_PICKER_TAG)
    }

    private val removeNotificationButtonListener: View.OnClickListener = View.OnClickListener {
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
        if ((requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) ||
            requestCode == ACCESS_BACKGROUND_LOCATION_REQUEST_CODE
        ) {
            navigateToGeofenceMap()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private val addGeofenceNotificationButtonListener: View.OnClickListener = View.OnClickListener {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !EasyPermissions.hasPermissions(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            EasyPermissions.requestPermissions(
                this,
                resources.getString(R.string.fine_location_rationale_message),
                ACCESS_FINE_LOCATION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            requestBackgroundLocationPermission()
        } else {
            navigateToGeofenceMap()
        }
    }

    @AfterPermissionGranted(ACCESS_FINE_LOCATION_REQUEST_CODE)
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            EasyPermissions.requestPermissions(
                this,
                resources.getString(R.string.background_location_rationale_message),
                ACCESS_BACKGROUND_LOCATION_REQUEST_CODE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    private fun navigateToGeofenceMap() {
        val bundle = Bundle()

        if (addEditTodoViewModel.hasGeofenceNotification) {
            bundle.putBoolean(
                GeofenceMapFragment.ARGUMENT_HAS_GEOFENCE_NOTIFICATION,
                addEditTodoViewModel.hasGeofenceNotification
            )
            bundle.putInt(GeofenceMapFragment.ARGUMENT_GEOFENCE_RADIUS, addEditTodoViewModel.geofenceRadius)
            bundle.putDouble(GeofenceMapFragment.ARGUMENT_GEOFENCE_LATITUDE, addEditTodoViewModel.geofenceLatitude)
            bundle.putDouble(GeofenceMapFragment.ARGUMENT_GEOFENCE_LONGITUDE, addEditTodoViewModel.geofenceLongitude)
        }

        Navigation.findNavController(requireView()).navigate(R.id.action_addEditTodoFragment_to_geofenceMapFragment, bundle)
    }

    private val removeGeofenceNotificationButtonListener: View.OnClickListener = View.OnClickListener {
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

    private val titleTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}

        override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
            if (charSequence.toString().trim().isEmpty()) {
                binding.saveTodoButton.isEnabled = false
                binding.saveTodoButton.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
            } else {
                binding.saveTodoButton.isEnabled = true
                binding.saveTodoButton.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@AddEditTodoFragment.requireContext(),
                            R.color.colorSecondary
                        )
                    )
            }
        }

        override fun afterTextChanged(editable: Editable?) {}
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
        const val NOTIFICATION_DATE_PICKER_TAG = "NOTIFICATION_DATE_PICKER"
        const val NOTIFICATION_TIME_PICKER_TAG = "NOTIFICATION_TIME_PICKER"

        const val ARGUMENT_TODO_ID = "ARGUMENT_TODO_ID"
        const val ARGUMENT_TITLE = "ARGUMENT_TITLE"
        const val ARGUMENT_DESCRIPTION = "ARGUMENT_DESCRIPTION"
        const val ARGUMENT_PRIORITY = "ARGUMENT_PRIORITY"
        const val ARGUMENT_HAS_NOTIFICATION = "ARGUMENT_HAS_NOTIFICATION"
        const val ARGUMENT_NOTIFICATION_ID = "ARGUMENT_NOTIFICATION_ID"
        const val ARGUMENT_NOTIFICATION_YEAR = "ARGUMENT_NOTIFICATION_YEAR"
        const val ARGUMENT_NOTIFICATION_MONTH = "ARGUMENT_NOTIFICATION_MONTH"
        const val ARGUMENT_NOTIFICATION_DAY = "ARGUMENT_NOTIFICATION_DAY"
        const val ARGUMENT_NOTIFICATION_HOUR = "ARGUMENT_NOTIFICATION_HOUR"
        const val ARGUMENT_NOTIFICATION_MINUTE = "ARGUMENT_NOTIFICATION_MINUTE"
        const val ARGUMENT_HAS_GEOFENCE_NOTIFICATION = "ARGUMENT_HAS_GEOFENCE_NOTIFICATION"
        const val ARGUMENT_GEOFENCE_NOTIFICATION_ID = "ARGUMENT_GEOFENCE_NOTIFICATION_ID"
        const val ARGUMENT_GEOFENCE_RADIUS = "ARGUMENT_GEOFENCE_RADIUS"
        const val ARGUMENT_GEOFENCE_LATITUDE = "ARGUMENT_GEOFENCE_LATITUDE"
        const val ARGUMENT_GEOFENCE_LONGITUDE = "ARGUMENT_GEOFENCE_LONGITUDE"

        fun createBundleForTodoItem(todo: Todo): Bundle {
            // Double cannot be passed as safe args, which is used for latitude and longitude, so must create a bundle instead
            return Bundle().apply {
                putInt(ARGUMENT_TODO_ID, todo.id)
                putString(ARGUMENT_TITLE, todo.title)
                putString(ARGUMENT_DESCRIPTION, todo.description)
                putInt(ARGUMENT_PRIORITY, todo.priority)
                putBoolean(ARGUMENT_HAS_NOTIFICATION, todo.notificationEnabled)
                putBoolean(ARGUMENT_HAS_GEOFENCE_NOTIFICATION, todo.geofenceNotificationEnabled)

                if (todo.notificationEnabled) {
                    putInt(ARGUMENT_NOTIFICATION_ID, todo.notificationId)
                    putInt(ARGUMENT_NOTIFICATION_YEAR, todo.notifyYear)
                    putInt(ARGUMENT_NOTIFICATION_MONTH, todo.notifyMonth)
                    putInt(ARGUMENT_NOTIFICATION_DAY, todo.notifyDay)
                    putInt(ARGUMENT_NOTIFICATION_HOUR, todo.notifyHour)
                    putInt(ARGUMENT_NOTIFICATION_MINUTE, todo.notifyMinute)
                }

                if (todo.geofenceNotificationEnabled) {
                    putInt(ARGUMENT_GEOFENCE_NOTIFICATION_ID, todo.geofenceNotificationId)
                    putInt(ARGUMENT_GEOFENCE_RADIUS, todo.geofenceRadius)
                    putDouble(ARGUMENT_GEOFENCE_LATITUDE, todo.geofenceLatitude)
                    putDouble(ARGUMENT_GEOFENCE_LONGITUDE, todo.geofenceLongitude)
                }
            }
        }
    }
}