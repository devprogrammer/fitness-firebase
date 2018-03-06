package at.shockbytes.corey.ui.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.Toast
import at.shockbytes.corey.R
import at.shockbytes.corey.adapter.DaysScheduleAdapter
import at.shockbytes.corey.adapter.ScheduleAdapter
import at.shockbytes.corey.dagger.AppComponent
import at.shockbytes.corey.storage.live.LiveScheduleUpdateListener
import at.shockbytes.corey.ui.fragment.dialog.InsertScheduleDialogFragment
import at.shockbytes.corey.schedule.ScheduleItem
import at.shockbytes.corey.schedule.ScheduleManager
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseItemTouchHelper
import at.shockbytes.util.view.EqualSpaceItemDecoration
import kotterknife.bindView
import javax.inject.Inject

/**
 * @author Martin Macheiner
 * Date: 26.10.2015.
 */
class ScheduleFragment : BaseFragment(), LiveScheduleUpdateListener,
        ScheduleAdapter.OnItemMoveListener, ScheduleAdapter.OnItemClickListener {

    @Inject
    protected lateinit var scheduleManager: ScheduleManager

    private lateinit var touchHelper: ItemTouchHelper
    private lateinit var adapter: ScheduleAdapter

    private val recyclerView: RecyclerView by bindView(R.id.fragment_schedule_rv)
    private val recyclerViewDays: RecyclerView by bindView(R.id.fragment_schedule_rv_days)

    private val colsForOrientation: Int
        get() = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            1 else 7

    override val layoutId = R.layout.fragment_schedule

    override fun onStop() {
        super.onStop()
        scheduleManager.unregisterLiveForScheduleUpdates()
    }

    override fun onScheduleItemAdded(item: ScheduleItem) {
        adapter.insertEntity(item)
    }

    override fun onScheduleItemDeleted(item: ScheduleItem) {
        adapter.resetEntity(item)
    }

    override fun onScheduleItemChanged(item: ScheduleItem) {
        adapter.updateEntity(item)
    }

    override fun onItemMove(item: ScheduleItem, from: Int, to: Int) {
    }

    override fun onItemMoveFinished() {
        adapter.scheduleData.forEach { scheduleManager.updateScheduleItem(it) }
    }

    override fun onItemDismissed(item: ScheduleItem, position: Int) {
        if (!item.isEmpty) {
            scheduleManager.deleteScheduleItem(item)
        }
    }

    override fun onItemClick(item: ScheduleItem, v: View, position: Int) {

        if (item.isEmpty) {
            InsertScheduleDialogFragment.newInstance(position)
                    .setOnScheduleItemSelectedListener { i, d ->
                        scheduleManager.insertScheduleItem(ScheduleItem(i, d))
                    }
                    .show(fragmentManager, "dialogfragment-insert-schedule")
        }
    }

    override fun setupViews() {

        recyclerView.layoutManager = GridLayoutManager(context, colsForOrientation)
        recyclerViewDays.layoutManager = GridLayoutManager(context, colsForOrientation)
        recyclerViewDays.adapter = DaysScheduleAdapter(context!!, resources.getStringArray(R.array.days).toList())
        recyclerViewDays.addItemDecoration(EqualSpaceItemDecoration(AppUtils.convertDpInPixel(4, context!!)))

        adapter = ScheduleAdapter(context, listOf())
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.addItemDecoration(EqualSpaceItemDecoration(AppUtils.convertDpInPixel(4, context!!)))
        val callback = BaseItemTouchHelper(adapter, true, BaseItemTouchHelper.DragAccess.VERTICAL)
        adapter.setOnItemMoveListener(this)
        adapter.setOnItemClickListener(this)

        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)

        scheduleManager.schedule.subscribe({ scheduleItems ->
            adapter.data = scheduleItems
        }, { throwable ->
            throwable.printStackTrace()
            Toast.makeText(context, throwable.toString(), Toast.LENGTH_LONG).show()
        })

        scheduleManager.registerLiveForScheduleUpdates(this)
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    companion object {

        fun newInstance(): ScheduleFragment {
            val fragment = ScheduleFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}
