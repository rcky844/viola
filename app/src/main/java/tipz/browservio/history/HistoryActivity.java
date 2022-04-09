package tipz.browservio.history;

import static tipz.browservio.fav.FavApi.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import tipz.browservio.R;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class HistoryActivity extends AppCompatActivity {
    private static List<String> listData;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.recycler_list_item_activity);
        initialize();
        setTitle(getResources().getString(R.string.hist));
    }

    /**
     * Initialize function
     */
    private void initialize() {

        Toolbar _toolbar = findViewById(R.id._toolbar);
        setSupportActionBar(_toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        _toolbar.setNavigationOnClickListener(_v -> onBackPressed());
        FloatingActionButton _fab = findViewById(R.id._fab);
        _fab.setContentDescription(getResources().getString(R.string.del_hist_fab_desp));

        _fab.setOnClickListener(_view -> new MaterialAlertDialogBuilder(this)
                .setTitle(getResources().getString(R.string.del_fav2_title))
                .setMessage(getResources().getString(R.string.del_hist_message))
                .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                    HistoryReader.clear(this);
                    CommonUtils.showMessage(this, getResources().getString(R.string.wiped_success));
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show());
    }

    @Override
    public void onStart() {
        super.onStart();
        isEmptyCheck();


        RecyclerView historyList = findViewById(R.id.recyclerView);
        listData = new ArrayList<>(Arrays.asList(HistoryReader.history_data(this).trim().split("\n")));

        historyList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        historyList.setAdapter(new ItemsAdapter(this));
    }

    void isEmptyCheck() {
        if (HistoryReader.isEmptyCheck(this)) {
            CommonUtils.showMessage(this, getResources().getString(R.string.hist_empty));
            finish();
        }
    }

    public static class ItemsAdapter extends RecyclerView.Adapter<HistoryActivity.ItemsAdapter.ViewHolder> {
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
        public HistoryActivity.ItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_item_1, parent, false);

            return new HistoryActivity.ItemsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryActivity.ItemsAdapter.ViewHolder holder, int position) {
            holder.mTextView.setText(listData.get(position));

            holder.mTextView.setOnClickListener(view -> {
                Intent needLoad = new Intent();
                needLoad.putExtra("needLoadUrl", listData.get(position));
                mHistoryActivity.setResult(0, needLoad);
                mHistoryActivity.finish();
            });

            holder.mTextView.setOnLongClickListener(view -> {
                PopupMenu popup1 = new PopupMenu(mHistoryActivity, view);
                Menu menu1 = popup1.getMenu();
                menu1.add(mHistoryActivity.getResources().getString(R.string.del_hist));
                menu1.add(mHistoryActivity.getResources().getString(android.R.string.copyUrl));
                menu1.add(mHistoryActivity.getResources().getString(R.string.add_to_fav));
                popup1.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().toString().equals(mHistoryActivity.getResources().getString(R.string.del_hist))) {
                        listData.remove(position);
                        StringBuilder out = new StringBuilder();
                        for (Object o : listData) {
                            out.append(o.toString());
                            out.append(CommonUtils.LINE_SEPARATOR());
                        }
                        HistoryReader.write(mHistoryActivity, out.toString().trim());
                        notifyItemRangeRemoved(position, 1);
                        mHistoryActivity.isEmptyCheck();
                        return true;
                    } else if (item.getTitle().toString().equals(mHistoryActivity.getResources().getString(android.R.string.copyUrl))) {
                        CommonUtils.copyClipboard(mHistoryActivity, listData.get(position));
                        return true;
                    } else if (item.getTitle().toString().equals(mHistoryActivity.getResources().getString(R.string.add_to_fav))) {
                        SettingsUtils.setPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count, SettingsUtils.getPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count).isEmpty() ? "0" : String.valueOf((long) (Double.parseDouble(SettingsUtils.getPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count)) + 1)));
                        SettingsUtils.setPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked.concat(SettingsUtils.getPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count)), listData.get(position));
                        SettingsUtils.setPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked.concat(SettingsUtils.getPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count)).concat(SettingsKeys.bookmarked_show), "1");
                        CommonUtils.showMessage(mHistoryActivity, mHistoryActivity.getResources().getString(R.string.saved_su));
                        return true;
                    }
                    return false;
                });
                popup1.show();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return listData.size();
        }
    }
}
