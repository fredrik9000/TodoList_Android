package com.github.fredrik9000.todolist

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.github.fredrik9000.todolist.add_edit_todo.add_edit_geofence.GeofenceRadiusFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val controller = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, controller)

        // We have to dynamically set the title for each fragment because the label of any fragment will stick around and override setTitle
        controller.addOnDestinationChangedListener { _, destination, arguments ->
            val destinationId = destination.id
            if (destinationId == R.id.mainFragment) {
                setTitle(R.string.title_main)
            } else if (destinationId == R.id.addEditTodoFragment) {
                if (arguments == null) {
                    setTitle(R.string.title_add_todo)
                } else {
                    setTitle(R.string.title_edit_todo)
                }
            } else if (destinationId == R.id.todoGeofenceMapFragment) {
                setTitle(R.string.title_set_geofence)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val fragmentManager = supportFragmentManager.primaryNavigationFragment?.childFragmentManager
        if (fragmentManager?.findFragmentByTag(GeofenceRadiusFragment.TAG) != null) {
            GeofenceRadiusFragment.disableAnimations = true
            fragmentManager.popBackStackImmediate()
            GeofenceRadiusFragment.disableAnimations = false
        }
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp()
    }

    override fun onBackPressed() {
        // Fix for memory leak in the Android framework that happens on Android 10:
        // https://issuetracker.google.com/issues/139738913
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && isTaskRoot &&
            supportFragmentManager.primaryNavigationFragment != null &&
            supportFragmentManager.primaryNavigationFragment!!.childFragmentManager.backStackEntryCount == 0 &&
            supportFragmentManager.backStackEntryCount == 0
        ) {
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }
}