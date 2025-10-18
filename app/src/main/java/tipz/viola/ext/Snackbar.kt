package tipz.viola.ext

import android.view.Gravity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar

fun Snackbar.setStartAligned() {
    val params = view.layoutParams as (CoordinatorLayout.LayoutParams)
    params.gravity = Gravity.BOTTOM or Gravity.START
    view.layoutParams = params
}