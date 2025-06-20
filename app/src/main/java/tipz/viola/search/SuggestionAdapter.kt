// Copyright (c) 2020-2023 The LineageOS Project
// Copyright (c) 2022-2025 Tipz Team
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import tipz.viola.databinding.TemplateTextSuggestionsBinding
import tipz.viola.settings.SettingsKeys
import tipz.viola.webview.VWebViewActivity
import java.util.Locale

class SuggestionAdapter(private val context: VWebViewActivity) : BaseAdapter(), Filterable {
    private var items = listOf<String>()
    private val filter = ItemFilter()
    private var queryText: String? = null

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
            binding = TemplateTextSuggestionsBinding.bind(itemView!!)
        }

        val title = binding.text1
        val copyToSearchBarButton = binding.copyToSearchBarButton
        val suggestion = items.getOrNull(position) ?: return Space(context)

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
            context.onUrlUpdated(suggestion, suggestion.length)
        }
        return itemView
    }

    override fun getFilter(): Filter {
        return filter
    }

    private inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (!context.settingsPreference.getIntBool(SettingsKeys.useSearchSuggestions)) {
                queryText = ""
                items = listOf()
                return FilterResults().apply {
                    count = 0
                    values = null
                }
            }

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
        get() = SuggestionProvider { error, throwable ->
            MainScope().launch {
                MaterialAlertDialogBuilder(context)
                    .setTitle(error)
                    .setMessage(throwable.message)
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show()
            }
        }
}