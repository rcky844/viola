package tipz.browservio.history;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tipz.browservio.R;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class HistoryRecycler {
    private static List<String> listData;
    private static Boolean popup = false;

    private static MaterialAlertDialogBuilder deleteHistory;

    public HistoryRecycler(Context context, HistoryActivity mHistoryActivity, RecyclerView historyList) {
        listData = new ArrayList<>(Arrays.asList(HistoryReader.history_data(context).trim().split("\n")));

        historyList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        historyList.setAdapter(new HistoryRecycler.ItemsAdapter(mHistoryActivity));

        deleteHistory = new MaterialAlertDialogBuilder(context);
    }

    public static class ItemsAdapter extends RecyclerView.Adapter<HistoryRecycler.ItemsAdapter.ViewHolder> {
        private final HistoryActivity mHistoryActivity;

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final AppCompatTextView mTextView;

            public ViewHolder(View view) {
                super(view);
                mTextView = view.findViewById(android.R.id.text1);
            }
        }

        public ItemsAdapter(HistoryActivity historyActivity) {
            mHistoryActivity = historyActivity;
        }

        @NonNull
        @Override
        public HistoryRecycler.ItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_item_1,parent,false);

            return new HistoryRecycler.ItemsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryRecycler.ItemsAdapter.ViewHolder holder, int position) {
            holder.mTextView.setText(listData.get(position));

            holder.mTextView.setOnClickListener(view -> {
                if (!popup) {
                    Intent needLoad = new Intent();
                    needLoad.putExtra("needLoadUrl", listData.get(position));
                    mHistoryActivity.setResult(0, needLoad);
                    mHistoryActivity.finish();
                } else {
                    popup = false;
                }
            });

            holder.mTextView.setOnLongClickListener(view -> {
                popup = true;
                PopupMenu popup1 = new PopupMenu(mHistoryActivity, view);
                Menu menu1 = popup1.getMenu();
                menu1.add(mHistoryActivity.getResources().getString(R.string.del_hist));
                menu1.add(mHistoryActivity.getResources().getString(android.R.string.copyUrl));
                menu1.add(mHistoryActivity.getResources().getString(R.string.add_to_fav));
                popup1.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().toString().equals(mHistoryActivity.getResources().getString(R.string.del_hist))) {
                        deleteHistory.setTitle(mHistoryActivity.getResources().getString(R.string.del_hist))
                                .setMessage(mHistoryActivity.getResources().getString(R.string.del_hist_title))
                                .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                                    listData.remove(position);
                                    StringBuilder out = new StringBuilder();
                                    for (Object o : listData) {
                                        out.append(o.toString());
                                        out.append(CommonUtils.LINE_SEPARATOR());
                                    }
                                    HistoryReader.write(mHistoryActivity, out.toString().trim());
                                    notifyItemRangeRemoved(position, 1);
                                    CommonUtils.showMessage(mHistoryActivity.getApplicationContext(), mHistoryActivity.getResources().getString(R.string.del_success));
                                    mHistoryActivity.isEmptyCheck();
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .create().show();
                        return true;
                    } else if (item.getTitle().toString().equals(mHistoryActivity.getResources().getString(android.R.string.copyUrl))) {
                        CommonUtils.copyClipboard(mHistoryActivity, listData.get(position));
                        return true;
                    } else if (item.getTitle().toString().equals(mHistoryActivity.getResources().getString(R.string.add_to_fav))) {
                        SettingsUtils.setPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count, SettingsUtils.getPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count).isEmpty() ? "0" : String.valueOf((long) (Double.parseDouble(SettingsUtils.getPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count)) + 1)));
                        SettingsUtils.setPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked.concat(SettingsUtils.getPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count)), listData.get(position));
                        SettingsUtils.setPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked.concat(SettingsUtils.getPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count)).concat(SettingsKeys.bookmarked_count_show), "1");
                        CommonUtils.showMessage(mHistoryActivity.getApplicationContext(), mHistoryActivity.getResources().getString(R.string.saved_su));
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
