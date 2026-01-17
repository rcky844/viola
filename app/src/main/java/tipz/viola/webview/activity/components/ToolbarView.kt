// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.divider.MaterialDivider
import tipz.viola.R
import tipz.viola.databinding.TemplateIconItemBinding
import tipz.viola.webview.activity.BrowserActivity

class ToolbarView(
    context: Context, attrs: AttributeSet?
) : LinearLayoutCompat(context, attrs) {
    lateinit var activity: BrowserActivity
    private val recyclerView = RecyclerView(context, attrs)

    init {
        /* Set-up LinearLayoutCompat */
        orientation = VERTICAL
        isSoundEffectsEnabled = false

        /* Create divider */
        if (tag != "hideDivider") {
            addView(MaterialDivider(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            })
        }

        /* Create RecyclerView */
        val recyclerViewParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        recyclerView.layoutParams = recyclerViewParams
        recyclerView.layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.SPACE_AROUND
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER)
        addView(recyclerView)
    }

    fun init() {
        /* Initialize RecyclerView */
        recyclerView.adapter = ItemsAdapter(this, toolsBarItemList)
    }

    class ItemsAdapter(
        private val toolbarView: ToolbarView,
        private val itemsList: List<Int>,
    ) :
        RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
        private lateinit var binding: TemplateIconItemBinding

        class ViewHolder(binding: TemplateIconItemBinding) : RecyclerView.ViewHolder(binding.root) {
            val imageView: AppCompatImageView = binding.imageView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            binding = TemplateIconItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.imageView.setImageResource(itemsList[position])
            holder.imageView.setOnClickListener {
                toolbarView.performClick()
                toolbarView.activity.itemSelected(holder.imageView, itemsList[position])
            }
            holder.imageView.setOnLongClickListener {
                toolbarView.performLongClick()
                toolbarView.activity.itemLongSelected(holder.imageView, itemsList[position])
                true
            }
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }
    }

    companion object {
        private val toolsBarItemList = listOf(
            R.drawable.arrow_back_alt,
            R.drawable.arrow_forward_alt,
            R.drawable.home,
            R.drawable.share,
            R.drawable.view_stream
        )

        // TODO: Add support for reverting to legacy layout
        private val legacyToolsBarItemList = listOf(
            R.drawable.arrow_back_alt,
            R.drawable.arrow_forward_alt,
            R.drawable.refresh,
            R.drawable.home,
            R.drawable.smartphone,
            R.drawable.new_tab,
            R.drawable.share,
            R.drawable.app_shortcut,
            R.drawable.settings,
            R.drawable.history,
            R.drawable.favorites,
            R.drawable.download,
            R.drawable.close
        )
    }
}