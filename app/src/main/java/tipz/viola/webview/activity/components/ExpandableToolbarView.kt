// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.content.res.Configuration
import android.os.Build
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
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
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
        ContextCompat.getDrawable(context, R.drawable.toolbar_expandable_background).let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                background = it
            } else {
                @Suppress("DEPRECATION") setBackgroundDrawable(it)
            }
        }
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
        recyclerView.adapter = ToolbarItemsAdapter(this, toolsBarExpandableItemList)
    }

    fun expandToolBar() {
        val viewVisible: Boolean = visibility == View.VISIBLE
        val transitionSet = TransitionSet()
            .addTransition(Slide()
                .addTarget(this)
                .setDuration(resources.getInteger(R.integer.anim_toolbar_expand_slide_speed).toLong())
            )
            .addTransition(Fade()
                .addTarget(this)
                .setDuration(resources.getInteger(R.integer.anim_toolbar_expand_fade_speed).toLong())
            )
        TransitionManager.beginDelayedTransition(this, transitionSet)
        visibility = if (viewVisible) View.GONE else View.VISIBLE
    }

    class ToolbarItemsAdapter(
        private val expandableToolbarView: ExpandableToolbarView,
        private val itemsList: ArrayList<Array<Int>>,
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
                    .itemSelected(holder.imageView, itemsList[position][0])
                if (closeToolBar) expandableToolbarView.expandToolBar()
            }
            holder.itemBox.setOnLongClickListener {
                expandableToolbarView.activity
                    .itemLongSelected(holder.imageView, itemsList[position][0])
                true
            }
            holder.imageView.setImageResource(itemsList[position][0])
            holder.textView.text =
                expandableToolbarView.activity.resources.getString(itemsList[position][1])
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }
    }

    companion object {
        private val toolsBarExpandableItemList: ArrayList<Array<Int>> =
            arrayListOf(
                arrayOf(R.drawable.new_tab, R.string.toolbar_expandable_new_tab),
                arrayOf(R.drawable.favorites, R.string.toolbar_expandable_favorites),
                arrayOf(R.drawable.history, R.string.toolbar_expandable_history),
                arrayOf(R.drawable.smartphone, R.string.toolbar_expandable_viewport),
                arrayOf(R.drawable.favorites_add, R.string.toolbar_expandable_favorites_add),
                arrayOf(R.drawable.download, R.string.toolbar_expandable_downloads),
                arrayOf(R.drawable.fullscreen, R.string.toolbar_expandable_fullscreen),
                arrayOf(R.drawable.app_shortcut, R.string.toolbar_expandable_app_shortcut),
                arrayOf(R.drawable.settings, R.string.toolbar_expandable_settings),
                arrayOf(R.drawable.code, R.string.toolbar_expandable_view_page_source),
                arrayOf(R.drawable.print, R.string.toolbar_expandable_print),
                arrayOf(R.drawable.close, R.string.toolbar_expandable_close)
            )
    }
}