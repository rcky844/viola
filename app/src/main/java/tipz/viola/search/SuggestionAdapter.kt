// Copyright (c) 2020-2023 The LineageOS Project
// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.search

import android.content.Context
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
import tipz.viola.databinding.TemplateTextSuggestionsBinding
import tipz.viola.webview.VWebViewActivity
import java.util.Locale

class SuggestionAdapter(private val mContext: Context)
    : BaseAdapter(), Filterable {
    private var items = listOf<String>()
    private val filter = ItemFilter()
    private var queryText: String? = null

    override fun getCount() = items.size

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = 0L

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: TemplateTextSuggestionsBinding
        var itemView = convertView

        if (convertView == null) {
            binding = TemplateTextSuggestionsBinding.inflate(
                LayoutInflater.from(mContext), parent, false)
            itemView = binding.root
        } else {
            binding = TemplateTextSuggestionsBinding.bind(itemView!!)
        }

        val title = binding.text1
        val copyToSearchBarButton = binding.copyToSearchBarButton
        val suggestion = items[position]

        if (queryText != null) {
            val spannable = SpannableStringBuilder(suggestion)
            val lcSuggestion = suggestion.lowercase(Locale.getDefault())
            var queryTextPos = lcSuggestion.indexOf(queryText!!)
            while (queryTextPos >= 0) {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    queryTextPos, queryTextPos + queryText!!.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                queryTextPos = lcSuggestion.indexOf(queryText!!,
                    queryTextPos + queryText!!.length)
            }
            title.text = spannable
        } else {
            title.text = suggestion
        }
        copyToSearchBarButton.setOnClickListener {
            (mContext as VWebViewActivity).onUrlUpdated(suggestion, suggestion.length)
        }
        itemView.setOnLongClickListener {
            (mContext as VWebViewActivity).onUrlUpdated(suggestion, suggestion.length)
            true
        }
        return itemView
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
                val results = provider.fetchResults(query)
                filterResults.count = items.size
                filterResults.values = items
                queryText = query
                items = results
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            notifyDataSetChanged()
        }
    }

    private val provider: SuggestionProvider
        get() = SuggestionProvider(mContext)
}