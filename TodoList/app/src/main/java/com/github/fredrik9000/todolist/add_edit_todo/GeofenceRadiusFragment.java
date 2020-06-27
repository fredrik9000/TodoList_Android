package com.github.fredrik9000.todolist.add_edit_todo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.databinding.FragmentGeofenceRadiusBinding;

public class GeofenceRadiusFragment extends Fragment {

    // When navigating away from the parent fragment with this fragment visible, don't show animations
    public static boolean disableAnimations = false;

    public static final String TAG = "GeofenceRadiusFragment";

    static final String RADIUS_ARGUMENT = "RADIUS_ARGUMENT";
    static final int DEFAULT_RADIUS_IN_METERS = 300;

    FragmentGeofenceRadiusBinding binding;

    private GeofenceRadiusToFragmentInteractionListener listener;

    public interface GeofenceRadiusToFragmentInteractionListener {
        void setGeofenceRadius(int radius);

        void exitAnimationFinished();

        void exitAnimationStarted();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // It is needed to handle the back button, otherwise the fragment wont get popped and one will navigate back to AddEditTodoFragment
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                getParentFragmentManager().popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGeofenceRadiusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final int radiusArgumentValue = getArguments().getInt(RADIUS_ARGUMENT);
        final TextView radiusDescription = binding.radiusDescription;
        radiusDescription.setText(getString(R.string.radius_description, Integer.toString(radiusArgumentValue)));

        SeekBar seekBar = binding.geofenceRadiusSeekBar;
        seekBar.setProgress(radiusToProgress(radiusArgumentValue));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int radius = progressToRadius(progress);
                radiusDescription.setText(getString(R.string.radius_description, Integer.toString(radius)));
                listener.setGeofenceRadius(radius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (disableAnimations) {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }

        Animation animation;
        if (enter) {
            animation = AnimationUtils.loadAnimation(getActivity(), R.anim.enter_bottom_to_top);
        } else {
            animation = AnimationUtils.loadAnimation(getActivity(), R.anim.exit_top_to_bottom);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    listener.exitAnimationFinished();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

                @Override
                public void onAnimationStart(Animation animation) {
                    listener.exitAnimationStarted();
                }
            });
        }
        return animation;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (GeofenceRadiusToFragmentInteractionListener) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private int progressToRadius(int progress) {
        int radius;
        switch (progress) {
            case 0:
                radius = 100;
                break;
            case 1:
                radius = 300;
                break;
            case 2:
                radius = 800;
                break;
            case 3:
                radius = 2000;
                break;
            case 4:
                radius = 5000;
                break;
            default:
                radius = 300;
        }
        return radius;
    }

    private int radiusToProgress(int radius) {
        int progress;
        switch (radius) {
            case 100:
                progress = 0;
                break;
            case 300:
                progress = 1;
                break;
            case 800:
                progress = 2;
                break;
            case 2000:
                progress = 3;
                break;
            case 5000:
                progress = 5000;
                break;
            default:
                progress = 1;
        }
        return progress;
    }
}
