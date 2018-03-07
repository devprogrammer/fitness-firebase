package at.shockbytes.corey.ui.fragment


import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.wear.widget.WearableLinearLayoutManager
import android.support.wear.widget.WearableRecyclerView
import android.view.View
import at.shockbytes.corey.R
import at.shockbytes.corey.adapter.WearWorkoutOverviewAdapter
import at.shockbytes.corey.common.core.util.WorkoutNameComparator
import at.shockbytes.corey.common.core.workout.model.Workout
import at.shockbytes.corey.dagger.WearAppComponent
import at.shockbytes.corey.ui.activity.MainActivity
import at.shockbytes.corey.ui.activity.WorkoutActivity
import at.shockbytes.util.adapter.BaseAdapter
import kotterknife.bindView
import java.util.*

class WorkoutOverviewFragment : WearableBaseFragment(),
        BaseAdapter.OnItemClickListener<Workout>, MainActivity.OnWorkoutsLoadedListener {

    private lateinit var workouts: List<Workout>

    private val recyclerView: WearableRecyclerView by bindView(R.id.fragment_workout_overview_rv)

    override val layoutId = R.layout.fragment_workout_overview

    override fun setupViews() {

        val adapter = WearWorkoutOverviewAdapter(context, workouts.sortedWith(WorkoutNameComparator()))

        recyclerView.isEdgeItemsCenteringEnabled = true
        recyclerView.layoutManager = WearableLinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.onItemClickListener = this
    }

    override fun injectToGraph(appComponent: WearAppComponent) {
        // Do nothing...
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workouts = arguments.getParcelableArrayList(ARG_WORKOUTS)
    }

    override fun onItemClick(t: Workout, v: View) {
        startActivity(WorkoutActivity.newIntent(context, t),
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle())
    }

    override fun onWorkoutLoaded(workouts: List<Workout>) {
        this.workouts = workouts
        setupViews()
    }

    companion object {

        private const val ARG_WORKOUTS = "arg_workouts"

        fun newInstance(workouts: ArrayList<Workout>): WorkoutOverviewFragment {
            val fragment = WorkoutOverviewFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_WORKOUTS, workouts)
            fragment.arguments = args
            return fragment
        }
    }
}