package tipz.browservio.view;

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

import tipz.browservio.MainActivity;
import tipz.browservio.R;

public class MainActionBarRecycler {
    public static void initMainActionBarRecycler(Context context, MainActivity mMainActivity, RecyclerView actionBar) {
        final List<IconItem> iconItems = new ArrayList<>();

        iconItems.add(new IconItem(R.drawable.arrow_back_alt));
        iconItems.add(new IconItem(R.drawable.arrow_forward_alt));
        iconItems.add(new IconItem(R.drawable.refresh));
        iconItems.add(new IconItem(R.drawable.home));
        iconItems.add(new IconItem(R.drawable.smartphone));
        iconItems.add(new IconItem(R.drawable.new_tab));
        iconItems.add(new IconItem(R.drawable.delete));
        iconItems.add(new IconItem(R.drawable.share));
        iconItems.add(new IconItem(R.drawable.settings));
        iconItems.add(new IconItem(R.drawable.history));
        iconItems.add(new IconItem(R.drawable.favorites));
        iconItems.add(new IconItem(R.drawable.close));

        actionBar.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        actionBar.setAdapter(new ItemsAdapter(iconItems, mMainActivity));
    }

    public static class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
        private final List<IconItem> mItemList;
        private final MainActivity mMainActivity;

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final AppCompatImageView mImageView;

            public ViewHolder(View view) {
                super(view);
                mImageView = view.findViewById(R.id.imageView);
            }
        }

        public ItemsAdapter(List<IconItem> itemsList, MainActivity mainActivity) {
            mItemList = itemsList;
            mMainActivity = mainActivity;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_icon_item,parent,false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            IconItem item = mItemList.get(position);

            holder.mImageView.setImageResource(item.getImage());
            holder.mImageView.setOnClickListener(view -> mMainActivity.itemSelected(holder.mImageView, position));
        }

        @Override
        public int getItemCount() {
            return mItemList.size();
        }
    }

    static class IconItem {
        private final int image;

        public IconItem(int image) {
            this.image = image;
        }

        public int getImage() {
            return image;
        }
    }
}
