package at.shockbytes.corey.ui.activity

import android.os.Bundle
import android.support.wear.widget.drawer.WearableNavigationDrawerView
import android.support.wearable.activity.WearableActivity
import at.shockbytes.corey.R
import at.shockbytes.corey.adapter.CoreyNavigationAdapter
import at.shockbytes.corey.common.core.workout.model.Workout
import at.shockbytes.corey.core.CommunicationManager
import at.shockbytes.corey.core.WearCoreyApp
import at.shockbytes.corey.ui.fragment.RunningFragment
import at.shockbytes.corey.ui.fragment.WorkoutOverviewFragment
import kotterknife.bindView
import java.util.Arrays
import javax.inject.Inject
import kotlin.collections.ArrayList

class MainActivity : WearableActivity() {

    interface OnWorkoutsLoadedListener {

        fun onWorkoutLoaded(workouts: List<Workout>)
    }

    @Inject
    protected lateinit var communicationManager: CommunicationManager

    private val navigationDrawer: WearableNavigationDrawerView by bindView(R.id.main_navigation_drawer)

    private var workoutListener: OnWorkoutsLoadedListener? = null

    private val navigationItems: List<CoreyNavigationAdapter.NavigationItem>
        get() = Arrays.asList(
                CoreyNavigationAdapter.NavigationItem(R.string.navigation_workout,
                        R.drawable.ic_workout),
                CoreyNavigationAdapter.NavigationItem(R.string.navigation_running,
                        R.drawable.ic_tab_running),
                CoreyNavigationAdapter.NavigationItem(R.string.navigation_settings,
                        R.drawable.ic_settings))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as WearCoreyApp).appComponent.inject(this)

        setAmbientEnabled()
        setupNavigationDrawer()

        communicationManager.connectIfDeviceAvailable {
            workoutListener?.onWorkoutLoaded(it)
        }

        onNavigationItemSelected(0)
    }

    override fun onStart() {
        super.onStart()
        communicationManager.onStart()
    }

    override fun onPause() {
        super.onPause()
        communicationManager.onPause()
    }

    private fun onNavigationItemSelected(index: Int) {
        when (index) {
            0 -> showWorkoutFragment()
            1 -> showRunningFragment()
            2 -> showSettings()
        }
    }

    private fun showWorkoutFragment() {
        val workoutOverviewFragment = WorkoutOverviewFragment
                .newInstance(ArrayList(communicationManager.cachedWorkouts))
        workoutListener = workoutOverviewFragment
        fragmentManager.beginTransaction()
                .replace(R.id.main_content, workoutOverviewFragment)
                .commit()
    }

    private fun showRunningFragment() {
        fragmentManager.beginTransaction()
                .replace(R.id.main_content, RunningFragment.newInstance())
                .commit()
    }

    private fun showSettings() {
        startActivity(CoreyPreferenceActivity.newIntent(this))
    }

    private fun setupNavigationDrawer() {
        navigationDrawer.setAdapter(CoreyNavigationAdapter(this, navigationItems))
        navigationDrawer.addOnItemSelectedListener { onNavigationItemSelected(it) }
        navigationDrawer.controller.peekDrawer()
        navigationDrawer.setCurrentItem(0, true)
    }

}
