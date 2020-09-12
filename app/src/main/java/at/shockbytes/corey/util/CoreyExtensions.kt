package at.shockbytes.corey.util

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.widget.AbsListView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.shockbytes.core.ShockbytesInjector
import at.shockbytes.core.ui.fragment.BaseFragment
import at.shockbytes.core.viewmodel.BaseViewModel
import at.shockbytes.corey.R
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

fun Fragment.isPortrait(): Boolean {
    return this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}

fun Drawable.toBitmap(): Bitmap {

    if (this is BitmapDrawable) {
        return this.bitmap
    }

    val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)

    return bitmap
}

inline fun <reified T : BaseViewModel> FragmentActivity.viewModelOfActivity(vmFactory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, vmFactory)[T::class.java]
}

inline fun <reified T : BaseViewModel> Fragment.viewModelOf(vmFactory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, vmFactory)[T::class.java]
}

@SuppressLint("PrivateResource")
fun <T : ShockbytesInjector> FragmentManager.showBaseFragment(fragment: BaseFragment<T>) {

    beginTransaction()
            .setCustomAnimations(
                    R.anim.abc_fade_in,
                    R.anim.abc_fade_out,
                    R.anim.abc_fade_in,
                    R.anim.abc_fade_out
            )
            .addToBackStack(fragment.javaClass.name)
            .add(android.R.id.content, fragment)
            .commit()
}

fun RecyclerView.observePositionChanges(subscribeOn: Scheduler): Observable<Int> {

    return Observable
            .create<Int> { source ->
                addOnScrollListener(object : RecyclerView.OnScrollListener() {

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        val position = (layoutManager as LinearLayoutManager)
                                .findFirstCompletelyVisibleItemPosition()
                        source.onNext(position)
                    }
                })
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(subscribeOn)
            .distinctUntilChanged()
}