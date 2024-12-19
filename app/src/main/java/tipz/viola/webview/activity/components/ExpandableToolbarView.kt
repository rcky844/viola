// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import tipz.viola.R
import tipz.viola.databinding.TemplateIconDescriptionItemBinding
import tipz.viola.ext.dpToPx
import tipz.viola.webview.activity.BrowserActivity

class ExpandableToolbarView(
    context: Context, attrs: AttributeSet?
) : LinearLayoutCompat(context, attrs) {
    lateinit var activity: BrowserActivity
    private val recyclerView = RecyclerView(context)

    init {
        /* Set-up LinearLayoutCompat */
        background = ContextCompat.getDrawable(context, R.drawable.toolbar_expandable_background)
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        visibility = View.GONE

        /* Create hint indicator */
        val hint = ImageView(context)
        val hintParams = LayoutParams(context.dpToPx(64), context.dpToPx(16))
        hintParams.bottomMargin = context.dpToPx(4)
        hint.layoutParams = hintParams
        hint.setPadding(context.dpToPx(6))
        hint.setImageResource(R.drawable.toolbar_expandable_hint)
        addView(hint)

        /* Create RecyclerView */
        val recyclerViewParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        recyclerViewParams.setMargins(context.dpToPx(4))
        recyclerView.layoutParams = recyclerViewParams
        recyclerView.layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_AROUND
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        addView(recyclerView)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val params = layoutParams as ConstraintLayout.LayoutParams
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
            || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.height = resources.getDimension(R.dimen.toolbar_extendable_height).toInt()
            params.matchConstraintMaxWidth =
                resources.getDimension(R.dimen.toolbar_extendable_max_width).toInt()
        }
    }

    fun init() {
        /* Initialize RecyclerView */
        recyclerView.adapter = ToolbarItemsAdapter(this,
            toolsBarExpandableItemList, toolsBarExpandableDescriptionList)
    }

    fun expandToolBar() {
        val viewVisible: Boolean = visibility == View.VISIBLE
        val transition: Transition = Slide(Gravity.BOTTOM)
        transition.duration = resources.getInteger(R.integer.anim_expandable_speed) *
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    Settings.Global.getFloat(context.contentResolver,
                        Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
                else 1.0f).toLong()
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(this, transition)
        visibility = if (viewVisible) View.GONE else View.VISIBLE
    }

    class ToolbarItemsAdapter(
        private val expandableToolbarView: ExpandableToolbarView,
        private val itemsList: List<Int>,
        private val descriptionList: List<Int>
    ) : Adapter<ToolbarItemsAdapter.ViewHolder>() {
        private lateinit var binding: TemplateIconDescriptionItemBinding

        class ViewHolder(binding: TemplateIconDescriptionItemBinding)
            : RecyclerView.ViewHolder(binding.root) {
            val itemBox: LinearLayoutCompat = binding.root
            val imageView: AppCompatImageView = binding.imageView
            val textView: AppCompatTextView = binding.textView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            binding = TemplateIconDescriptionItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemBox.setOnClickListener {
                val closeToolBar = expandableToolbarView.activity
                    .itemSelected(holder.imageView, itemsList[position])
                if (closeToolBar) expandableToolbarView.expandToolBar()
            }
            holder.itemBox.setOnLongClickListener {
                expandableToolbarView.activity
                    .itemLongSelected(holder.imageView, itemsList[position])
                true
            }
            holder.imageView.setImageResource(itemsList[position])
            holder.textView.text =
                expandableToolbarView.activity.resources.getString(descriptionList[position])
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }
    }

    companion object {
        private val toolsBarExpandableItemList = listOf(
            R.drawable.new_tab,
            R.drawable.favorites,
            R.drawable.history,
            R.drawable.smartphone,
            R.drawable.favorites_add,
            R.drawable.download,
            R.drawable.fullscreen,
            R.drawable.app_shortcut,
            R.drawable.settings,
            R.drawable.code,
            R.drawable.print,
            R.drawable.close
        )

        private val toolsBarExpandableDescriptionList = listOf(
            R.string.toolbar_expandable_new_tab,
            R.string.toolbar_expandable_favorites,
            R.string.toolbar_expandable_history,
            R.string.toolbar_expandable_viewport,
            R.string.toolbar_expandable_favorites_add,
            R.string.toolbar_expandable_downloads,
            R.string.toolbar_expandable_fullscreen,
            R.string.toolbar_expandable_app_shortcut,
            R.string.toolbar_expandable_settings,
            R.string.toolbar_expandable_view_page_source,
            R.string.toolbar_expandable_print,
            R.string.toolbar_expandable_close
        )
    }
}