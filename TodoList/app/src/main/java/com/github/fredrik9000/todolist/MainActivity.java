package com.github.fredrik9000.todolist;

import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
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
