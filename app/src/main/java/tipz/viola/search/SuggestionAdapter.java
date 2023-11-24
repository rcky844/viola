/*
 * Copyright (C) 2022-2023 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.search;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tipz.viola.R;
import tipz.viola.webview.VioWebViewActivity;

/*
    "Inspired" by LineageOS' Jelly
 */

public class SuggestionAdapter extends ArrayAdapter<String> {
    private final ArrayList<String> mItems = new ArrayList<>();
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final ItemFilter mFilter;
    private String mQueryText;

    public SuggestionAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mFilter = new ItemFilter();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public String getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.list_item_suggestions, parent, false);

        AppCompatTextView title = convertView.findViewById(android.R.id.text1);
        AppCompatImageView copy_to_search_bar_button = convertView.findViewById(R.id.copy_to_search_bar_button);
        String suggestion = mItems.get(position);

        if (mQueryText != null) {
            SpannableStringBuilder spannable = new SpannableStringBuilder(suggestion);
            String lcSuggestion = suggestion.toLowerCase(Locale.getDefault());
            int queryTextPos = lcSuggestion.indexOf(mQueryText);
            while (queryTextPos >= 0) {
                spannable.setSpan(new StyleSpan(Typeface.BOLD),
                        queryTextPos, queryTextPos + mQueryText.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                queryTextPos = lcSuggestion.indexOf(mQueryText, queryTextPos + mQueryText.length());
            }
            title.setText(spannable);
        } else {
            title.setText(suggestion);
        }

        copy_to_search_bar_button.setOnClickListener(view -> ((VioWebViewActivity) mContext).onUrlUpdated(suggestion, suggestion.length()));
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(@Nullable CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0)
                return results;

            SuggestionProvider provider = getProvider();
            String query = constraint.toString().toLowerCase(Locale.getDefault()).trim();

            List<String> items = provider.fetchResults(query);
            results.count = items.size();
            results.values = items;

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(@Nullable CharSequence constraint, FilterResults results) {
            mItems.clear();
            if (results.values != null) {
                mItems.addAll((List<String>) results.values);
                mQueryText = constraint != null
                        ? constraint.toString().toLowerCase(Locale.getDefault()).trim() : null;
            }
            notifyDataSetChanged();
        }
    }

    private SuggestionProvider getProvider() {
        return new SuggestionProvider(mContext);
    }
}
