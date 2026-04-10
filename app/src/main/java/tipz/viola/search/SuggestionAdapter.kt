// Copyright (c) 2020-2025 The LineageOS Project
// Copyright (c) 2022-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.search

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.Space
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import tipz.viola.R
import tipz.viola.databinding.TemplateTextSuggestionsBinding
import tipz.viola.ext.copyClipboard
import tipz.viola.ext.getOnSurfaceColor
import tipz.viola.ext.setStartAligned
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.UrlUtils
import tipz.viola.webview.VWebViewActivity
import java.util.Locale

class SuggestionAdapter(private val context: VWebViewActivity) : BaseAdapter(), Filterable {
    private var items = listOf<SuggestItem>()
    private val filter = ItemFilter()
    private var queryText: String? = null

    var enableFiltering = true
        set(value) {
            field = value
            queryText = ""
            items = listOf()
        }

    override fun getCount() = items.size

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = 0L

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: TemplateTextSuggestionsBinding
        var itemView = convertView

        if (convertView == null || convertView !is ConstraintLayout) {
            binding = TemplateTextSuggestionsBinding.inflate(
                LayoutInflater.from(context), parent, false)
            itemView = binding.root
        } else {
            binding = TemplateTextSuggestionsBinding.bind(itemView)
        }

        val suggestion = items.getOrNull(position) ?: return Space(context)
        queryText?.also { query ->
            binding.run {
                title.text = getSpannable(query, suggestion.title)
                url.isVisible = !suggestion.url.isNullOrEmpty()
                url.text = getSpannable(query, suggestion.url ?: "")
                if (suggestion.icon == null) {
                    icon.setImageResource(R.drawable.search)
                    icon.setColorFilter(context.getOnSurfaceColor())
                } else {
                    icon.setImageBitmap(suggestion.icon)
                    icon.clearColorFilter()
                }

                autocompleteButton.isVisible = !url.isVisible
                autocompleteButton.setOnClickListener {
                    context.onUrlUpdated(suggestion.title, suggestion.title.length)
                }
            }
        }

        return itemView
    }

    private fun getSpannable(query: String, text: String): SpannableStringBuilder {
        val spannable = SpannableStringBuilder(text)
        val lcSuggestion = text.lowercase(Locale.getDefault())
        var queryTextPos = lcSuggestion.indexOf(query)
        while (queryTextPos >= 0) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                queryTextPos,
                queryTextPos + query.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            queryTextPos = lcSuggestion.indexOf(query, queryTextPos + query.length)
        }
        return spannable
    }

    override fun getFilter(): Filter {
        return filter
    }

    private inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()

            constraint?.takeUnless { it.isBlank() }?.let {
                val provider: SuggestionProvider = provider
                val query = constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                if (!enableFiltering || UrlUtils.isUriSupported(query)
                    || !context.settingsPreference.getIntBool(SettingsKeys.useSearchSuggestions))
                    return cancelFiltering()

                val results = provider.fetchResults(query)
                filterResults.count = items.size
                filterResults.values = items
                queryText = query
                items = results
            }
            return filterResults
        }

        fun cancelFiltering(): FilterResults {
            queryText = ""
            items = listOf()
            return FilterResults().apply {
                count = 0
                values = null
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            notifyDataSetChanged()
        }
    }

    private val provider: SuggestionProvider
        get() = SuggestionProvider(context) { error, throwable ->
            MainScope().launch {
                Snackbar.make(
                    context.webviewContainer,
                    error, Snackbar.LENGTH_SHORT
                ).run {
                    setBehavior(BaseTransientBottomBar.Behavior().apply {
                        setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY)
                    })
                    setAction(R.string.customactivityoncrash_error_activity_error_details_title) {
                        context.copyClipboard(throwable.message)
                    }
                    setStartAligned()
                    show()
                }
            }
        }
}