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
    lateinit var binding: TemplateTextSuggestionsBinding
    private val mItems = ArrayList<String>()
    private val mFilter: ItemFilter
    private var mQueryText: String? = null

    init {
        mFilter = ItemFilter()
    }

    override fun getCount(): Int {
        return mItems.size
    }

    override fun getItem(position: Int): String {
        return mItems[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mConvertView = convertView
        if (mConvertView == null) {
            binding = TemplateTextSuggestionsBinding.inflate(
                LayoutInflater.from(mContext), parent, false)
            mConvertView = binding.root
        }

        val title = binding.text1
        val copyToSearchBarButton = binding.copyToSearchBarButton
        val suggestion = mItems[position]

        if (mQueryText != null) {
            val spannable = SpannableStringBuilder(suggestion)
            val lcSuggestion = suggestion.lowercase(Locale.getDefault())
            var queryTextPos = lcSuggestion.indexOf(mQueryText!!)
            while (queryTextPos >= 0) {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    queryTextPos, queryTextPos + mQueryText!!.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                queryTextPos = lcSuggestion.indexOf(mQueryText!!,
                    queryTextPos + mQueryText!!.length)
            }
            title.text = spannable
        } else {
            title.text = suggestion
        }
        copyToSearchBarButton.setOnClickListener {
            (mContext as VWebViewActivity).onUrlUpdated(suggestion, suggestion.length)
        }
        return mConvertView
    }

    override fun getFilter(): Filter {
        return mFilter
    }

    private inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            if (constraint.isNullOrEmpty()) return results
            val provider: SuggestionProvider = provider
            val query = constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
            val items = provider.fetchResults(query)
            results.count = items.size
            results.values = items
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            mItems.clear()
            if (results.values != null) {
                mItems.addAll((results.values as List<String>))
                mQueryText = constraint?.toString()?.lowercase(Locale.getDefault())
                    ?.trim { it <= ' ' }
            }
            notifyDataSetChanged()
        }
    }

    private val provider: SuggestionProvider
        get() = SuggestionProvider(mContext)
}