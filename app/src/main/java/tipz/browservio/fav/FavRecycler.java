package tipz.browservio.fav;

import static tipz.browservio.fav.FavApi.bookmarks;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import tipz.browservio.R;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class FavRecycler {
    private static List<String> listData;
    private static Boolean popup = false;

    private static MaterialAlertDialogBuilder delFav;

    public FavRecycler(Context context, FavActivity mFavActivity, RecyclerView favList, List<String> mListData) {
        listData = mListData;

        favList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        favList.setAdapter(new FavRecycler.ItemsAdapter(mFavActivity));

        delFav = new MaterialAlertDialogBuilder(context);
    }

    public static class ItemsAdapter extends RecyclerView.Adapter<FavRecycler.ItemsAdapter.ViewHolder> {
        private final FavActivity mFavActivity;

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final AppCompatTextView mTextView;

            public ViewHolder(View view) {
                super(view);
                mTextView = view.findViewById(android.R.id.text1);
            }
        }

        public ItemsAdapter(FavActivity favActivity) {
            mFavActivity = favActivity;
        }

        @NonNull
        @Override
        public FavRecycler.ItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_item_1,parent,false);

            return new FavRecycler.ItemsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FavRecycler.ItemsAdapter.ViewHolder holder, int position) {
            holder.mTextView.setText(listData.get(position));

            holder.mTextView.setOnClickListener(view -> {
                if (!popup) {
                    Intent needLoad = new Intent();
                    needLoad.putExtra("needLoadUrl", SettingsUtils.getPref(bookmarks(mFavActivity), SettingsKeys.bookmarked.concat(Integer.toString(position))));
                    mFavActivity.setResult(0, needLoad);
                    mFavActivity.finish();
                } else {
                    popup = false;
                }
            });

            holder.mTextView.setOnLongClickListener(view -> {
                popup = true;
                PopupMenu popup1 = new PopupMenu(mFavActivity, view);
                Menu menu1 = popup1.getMenu();
                menu1.add(mFavActivity.getResources().getString(R.string.del_fav));
                menu1.add(mFavActivity.getResources().getString(android.R.string.copyUrl));
                popup1.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().toString().equals(mFavActivity.getResources().getString(R.string.del_fav))) {
                        delFav.setTitle(mFavActivity.getResources().getString(R.string.del_fav_title))
                                .setMessage(mFavActivity.getResources().getString(R.string.del_fav_title))
                                .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                                    listData.remove(position);
                                    SettingsUtils.setPref(bookmarks(mFavActivity), SettingsKeys.bookmarked.concat(String.valueOf(position)).concat(SettingsKeys.bookmarked_show), "0");
                                    notifyItemRangeRemoved(position, 1);
                                    CommonUtils.showMessage(mFavActivity, mFavActivity.getResources().getString(R.string.del_success));
                                    mFavActivity.isEmptyCheck(listData, bookmarks(mFavActivity));
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .create().show();
                        return true;
                    } else if (item.getTitle().toString().equals(mFavActivity.getResources().getString(android.R.string.copyUrl))) {
                        CommonUtils.copyClipboard(mFavActivity, SettingsUtils.getPref(bookmarks(mFavActivity), SettingsKeys.bookmarked.concat(String.valueOf(position))));
                        return true;
                    }
                    return false;
                });
                popup1.show();
                return false;
            });
        }

        @Override
        public int getItemCount() {
            return listData.size();
        }
    }
}
