package com.github.fredrik9000.todolist.add_edit_todo.add_edit_geofence

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.databinding.FragmentGeofenceRadiusBinding

class GeofenceRadiusFragment : Fragment() {

    private var _binding: FragmentGeofenceRadiusBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // It is needed to handle the back button, otherwise the fragment wont get popped and the app will navigate back to AddEditTodoFragment
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGeofenceRadiusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radiusArgumentValue = arguments?.getInt(RADIUS_ARGUMENT) ?: DEFAULT_RADIUS_IN_METERS
        val radiusDescription = binding.radiusDescription
        radiusDescription.text = getString(R.string.radius_description, radiusArgumentValue.toString())

        // Seekbar that maps progress to geofence radius. When the progress is changed the listener will redraw the circle.
        val seekBar = binding.geofenceRadiusSeekBar
        seekBar.progress = radiusToProgress(radiusArgumentValue)
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val radius = progressToRadius(progress)
                radiusDescription.text = getString(R.string.radius_description, radius.toString())
                setFragmentResult(SET_GEOFENCE_RADIUS_REQUEST_KEY, bundleOf(BUNDLE_RADIUS_KEY to radius))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (disableAnimations) {
            return super.onCreateAnimation(transit, enter, nextAnim)
        }

        return if (enter) {
            AnimationUtils.loadAnimation(activity, R.anim.enter_bottom_to_top)
        } else {
            AnimationUtils.loadAnimation(activity, R.anim.exit_top_to_bottom).apply {
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(animation: Animation?) {
                        setFragmentResult(EXIT_ANIMATION_FINISHED_REQUEST_KEY, Bundle())
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationStart(animation: Animation?) {
                        setFragmentResult(EXIT_ANIMATION_STARTED_REQUEST_KEY, Bundle())
                    }
                })
            }
        }
    }

    // TODO: Not ideal to hard code the progress and radius values like this. Max value for seekbar is set to 4, which needs to be in sync with these values.
    private fun progressToRadius(progress: Int): Int {
        return when (progress) {
            0 -> 100
            1 -> 300
            2 -> 800
            3 -> 2000
            4 -> 5000
            else -> DEFAULT_RADIUS_IN_METERS
        }
    }

    private fun radiusToProgress(radius: Int): Int {
        return when (radius) {
            100 -> 0
            300 -> 1
            800 -> 2
            2000 -> 3
            5000 -> 4
            else -> 1
        }
    }

    companion object {
        var disableAnimations = false
        const val TAG = "GeofenceRadiusFragment"
        const val RADIUS_ARGUMENT = "RADIUS_ARGUMENT"
        const val DEFAULT_RADIUS_IN_METERS = 300
        const val SET_GEOFENCE_RADIUS_REQUEST_KEY = "SET_GEOFENCE_RADIUS_REQUEST"
        const val EXIT_ANIMATION_FINISHED_REQUEST_KEY = "EXIT_ANIMATION_FINISHED_REQUEST"
        const val EXIT_ANIMATION_STARTED_REQUEST_KEY = "EXIT_ANIMATION_STARTED_REQUEST"
        const val BUNDLE_RADIUS_KEY = "radius"
    }
}