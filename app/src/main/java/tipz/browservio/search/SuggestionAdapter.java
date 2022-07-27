package tipz.browservio.search;

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
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tipz.browservio.R;

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
            convertView = mInflater.inflate(R.layout.recycler_list_item_1, parent, false);

        AppCompatTextView title = convertView.findViewById(android.R.id.text1);
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
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
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
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mItems.clear();
            if (results.values != null)
                mItems.addAll((List<String>) results.values);
            mQueryText = constraint != null
                    ? constraint.toString().toLowerCase(Locale.getDefault()).trim() : null;
            notifyDataSetChanged();
        }
    }

    private SuggestionProvider getProvider() {
        return new SuggestionProvider(mContext);
    }
}
