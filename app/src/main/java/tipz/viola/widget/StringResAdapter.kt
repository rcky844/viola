package tipz.viola.widget

import android.content.Context
import android.widget.ArrayAdapter
import androidx.annotation.StringRes
import tipz.viola.R

open class StringResAdapter(private val mContext: Context) :
    ArrayAdapter<String>(mContext, R.layout.template_item_text_single) {

    private val mResIdObjects: MutableList<Int> = ArrayList()

    open fun add(@StringRes resId: Int) {
        mResIdObjects.add(resId)
        super.add(mContext.resources.getString(resId))
    }

    open fun addAll(@StringRes vararg resId: Int) = resId.forEach { add(it) }

    open fun insert(@StringRes resId: Int, index: Int) {
        mResIdObjects.add(resId, index)
        super.insert(mContext.resources.getString(resId), index)
    }

    open fun remove(@StringRes resId: Int) {
        mResIdObjects.remove(resId)
        super.remove(mContext.resources.getString(resId))
    }

    override fun clear() {
        mResIdObjects.clear()
        super.clear()
    }

    open fun getItemResId(position: Int): Int = mResIdObjects[position]
}