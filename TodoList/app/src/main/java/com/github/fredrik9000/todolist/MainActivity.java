package com.github.fredrik9000.todolist;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.github.fredrik9000.todolist.add_edit_todo.GeofenceRadiusFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavController controller = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, controller);

        // We have to dynamically set the title for each fragment, because the label of any fragment will stick around and override setTitle
        controller.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                int destinationId = destination.getId();
                if (destinationId == R.id.mainFragment) {
                    setTitle(R.string.title_main);
                } else if (destinationId == R.id.addEditTodoFragment) {
                    if (arguments == null) {
                        setTitle(R.string.title_add_todo);
                    } else {
                        setTitle(R.string.title_edit_todo);
                    }
                } else if (destinationId == R.id.todoGeofenceMapFragment) {
                    setTitle(R.string.title_set_geofence);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        FragmentManager fragmentManager = getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager();

        if (fragmentManager.findFragmentByTag(GeofenceRadiusFragment.TAG) != null) {
            GeofenceRadiusFragment.disableAnimations = true;
            fragmentManager.popBackStackImmediate();
            GeofenceRadiusFragment.disableAnimations = false;
        }

        NavController controller = Navigation.findNavController(this, R.id.nav_host_fragment);
        return controller.navigateUp();
    }

    @Override
    public void onBackPressed() {
        // Fix for memory leak in the Android framework that happens on Android 10:
        // https://issuetracker.google.com/issues/139738913
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && isTaskRoot()
                && getSupportFragmentManager().getPrimaryNavigationFragment() != null
                && getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager().getBackStackEntryCount() == 0
                && getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finishAfterTransition();
        } else {
            super.onBackPressed();
        }
    }
}
