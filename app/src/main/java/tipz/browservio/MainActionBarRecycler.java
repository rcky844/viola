package tipz.browservio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActionBarRecycler {
    public static void initMainActionBarRecycler(Context context, MainActivity mMainActivity, RecyclerView actionBar) {
        final List<Integer> iconItems = new ArrayList<>();

        iconItems.add(R.drawable.arrow_back_alt);
        iconItems.add(R.drawable.arrow_forward_alt);
        iconItems.add(R.drawable.refresh);
        iconItems.add(R.drawable.home);
        iconItems.add(R.drawable.smartphone);
        iconItems.add(R.drawable.new_tab);
        iconItems.add(R.drawable.delete);
        iconItems.add(R.drawable.share);
        iconItems.add(R.drawable.settings);
        iconItems.add(R.drawable.history);
        iconItems.add(R.drawable.favorites);
        iconItems.add(R.drawable.close);

        actionBar.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        actionBar.setAdapter(new ItemsAdapter(iconItems, mMainActivity));
    }

    public static class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
        private final List<Integer> mItemList;
        private final MainActivity mMainActivity;

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final AppCompatImageView mImageView;

            public ViewHolder(View view) {
                super(view);
                mImageView = view.findViewById(R.id.imageView);
            }
        }

        public ItemsAdapter(List<Integer> itemsList, MainActivity mainActivity) {
            mItemList = itemsList;
            mMainActivity = mainActivity;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_icon_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.mImageView.setImageResource(mItemList.get(position));
            holder.mImageView.setOnClickListener(view -> mMainActivity.itemSelected(holder.mImageView, position));
        }

        @Override
        public int getItemCount() {
            return mItemList.size();
        }
    }
}
