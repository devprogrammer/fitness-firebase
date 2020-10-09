package at.shockbytes.corey.ui.fragment.tab

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import at.shockbytes.corey.R
import at.shockbytes.corey.ui.adapter.DaysScheduleAdapter
import at.shockbytes.corey.ui.adapter.ScheduleAdapter
import at.shockbytes.corey.common.addTo
import at.shockbytes.corey.dagger.AppComponent
import at.shockbytes.corey.data.schedule.ScheduleItem
import at.shockbytes.corey.data.schedule.weather.ScheduleWeatherResolver
import at.shockbytes.corey.ui.fragment.dialog.InsertScheduleDialogFragment
import at.shockbytes.corey.ui.viewmodel.ScheduleViewModel
import at.shockbytes.corey.util.isPortrait
import at.shockbytes.corey.util.viewModelOf
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import at.shockbytes.util.adapter.BaseItemTouchHelper
import at.shockbytes.util.view.EqualSpaceItemDecoration
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_schedule.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Author:  Martin Macheiner
 * Date:    26.10.2015
 */
class ScheduleFragment : TabBaseFragment<AppComponent>(),
        BaseAdapter.OnItemMoveListener<ScheduleItem>,
        BaseAdapter.OnItemClickListener<ScheduleItem>
{

    override val snackBarBackgroundColorRes: Int = R.color.sb_background
    override val snackBarForegroundColorRes: Int = R.color.sb_foreground

    @Inject
    lateinit var weatherResolver: ScheduleWeatherResolver

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ScheduleViewModel

    private val scheduleAdapter: ScheduleAdapter by lazy {
        ScheduleAdapter(
                requireContext(),
                onItemClickListener = this,
                onItemMoveListener = this,
                weatherResolver
        )
    }

    private val recyclerViewLayoutManager: RecyclerView.LayoutManager
        get() = if (isPortrait()) {
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        } else {
            GridLayoutManager(context, ScheduleAdapter.MAX_SCHEDULES)
        }

    override val layoutId = R.layout.fragment_schedule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelOf(vmFactory)
    }

    override fun injectToGraph(appComponent: AppComponent?) {
        appComponent?.inject(this)
    }

    override fun bindViewModel() {
        viewModel.requestSchedule()
        viewModel.getSchedule().observe(this, Observer(scheduleAdapter::updateData))
    }

    override fun unbindViewModel() {
        viewModel.getSchedule().removeObservers(this)
    }

    override fun setupViews() {

        fragment_schedule_rv_days.apply {
            layoutManager = recyclerViewLayoutManager
            adapter = DaysScheduleAdapter(
                    requireContext(),
                    resources.getStringArray(R.array.days).toList(),
                    object: BaseAdapter.OnItemLongClickListener<String> {
                        override fun onItemLongClick(content: String, position: Int, v: View) {
                            v.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            scheduleAdapter.getItemAt(position)?.let { item ->
                                if (!item.isEmpty) {
                                    viewModel.deleteScheduleItem(item)
                                }
                            }
                        }
                    }
            )
            addItemDecoration(EqualSpaceItemDecoration(AppUtils.convertDpInPixel(4, requireContext())))
        }

        fragment_schedule_rv.apply {
            layoutManager = recyclerViewLayoutManager
            isNestedScrollingEnabled = false
            adapter = scheduleAdapter

            addItemDecoration(EqualSpaceItemDecoration(AppUtils.convertDpInPixel(4, requireContext())))

            BaseItemTouchHelper(scheduleAdapter, false, BaseItemTouchHelper.DragAccess.ALL)
                    .let(::ItemTouchHelper)
                    .attachToRecyclerView(this)
        }
    }

    override fun onItemClick(content: ScheduleItem, position: Int, v: View) {
        if (content.isEmpty) {
            InsertScheduleDialogFragment.newInstance()
                    .setOnScheduleItemSelectedListener { scheduleDisplayItem ->
                        viewModel.insertScheduleItem(scheduleDisplayItem, position)
                    }
                    .show(childFragmentManager, "dialog-fragment-insert-schedule")
        }
    }

    override fun onItemMove(t: ScheduleItem, from: Int, to: Int) = Unit

    override fun onItemMoveFinished() {
        scheduleAdapter.reorderAfterMove()
                .forEach(viewModel::updateScheduleItem)
    }

    override fun onItemDismissed(t: ScheduleItem, position: Int) = Unit

    companion object {

        fun newInstance(): ScheduleFragment {
            val fragment = ScheduleFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
