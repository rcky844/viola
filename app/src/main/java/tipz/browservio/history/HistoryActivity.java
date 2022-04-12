package tipz.browservio.history;

import static tipz.browservio.fav.FavApi.bookmarks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import tipz.browservio.R;
import tipz.browservio.broha.Broha;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class HistoryActivity extends AppCompatActivity {
    private static List<Broha> listData;

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
                    HistoryUtils.clear(this);
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
        listData = HistoryApi.historyBroha(this).getAll();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        layoutManager.setStackFromEnd(true);
        historyList.setLayoutManager(layoutManager);
        historyList.setAdapter(new ItemsAdapter(this));
    }

    void isEmptyCheck() {
        if (HistoryUtils.isEmptyCheck(this)) {
            CommonUtils.showMessage(this, getResources().getString(R.string.hist_empty));
            finish();
        }
    }

    public static class ItemsAdapter extends RecyclerView.Adapter<HistoryActivity.ItemsAdapter.ViewHolder> {
        private final HistoryActivity mHistoryActivity;

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final ConstraintLayout back;
            private final AppCompatImageView icon;
            private final AppCompatTextView title;
            private final AppCompatTextView url;
            private final AppCompatTextView time;

            public ViewHolder(View view) {
                super(view);
                back = view.findViewById(R.id.bg);
                icon = view.findViewById(R.id.icon);
                title = view.findViewById(R.id.title);
                url = view.findViewById(R.id.url);
                time = view.findViewById(R.id.time);
            }
        }

        public ItemsAdapter(HistoryActivity historyActivity) {
            mHistoryActivity = historyActivity;
        }

        @NonNull
        @Override
        public HistoryActivity.ItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_broha_history, parent, false);

            return new HistoryActivity.ItemsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryActivity.ItemsAdapter.ViewHolder holder, int position) {
            holder.icon.setImageResource(R.drawable.default_favicon);
            holder.title.setText(listData.get(position).getUrl());
            holder.url.setText(Uri.parse(listData.get(position).getUrl()).getHost());
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(listData.get(position).getTimestamp() * 1000L);
            holder.time.setText(String.valueOf(date.get(Calendar.HOUR_OF_DAY)).concat(":")
                    .concat(String.valueOf(date.get(Calendar.MINUTE))));

            holder.back.setOnClickListener(view -> {
                Intent needLoad = new Intent();
                needLoad.putExtra("needLoadUrl", listData.get(position).getUrl());
                mHistoryActivity.setResult(0, needLoad);
                mHistoryActivity.finish();
            });

            holder.back.setOnLongClickListener(view -> {
                PopupMenu popup1 = new PopupMenu(mHistoryActivity, view);
                Menu menu1 = popup1.getMenu();
                menu1.add(mHistoryActivity.getResources().getString(R.string.del_hist));
                menu1.add(mHistoryActivity.getResources().getString(android.R.string.copyUrl));
                menu1.add(mHistoryActivity.getResources().getString(R.string.add_to_fav));
                popup1.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().toString().equals(mHistoryActivity.getResources().getString(R.string.del_hist))) {
                        HistoryUtils.deleteById(mHistoryActivity, listData.get(position).getId());
                        listData.remove(position);
                        notifyItemRangeRemoved(position, 1);
                        mHistoryActivity.isEmptyCheck();
                        return true;
                    } else if (item.getTitle().toString().equals(mHistoryActivity.getResources().getString(android.R.string.copyUrl))) {
                        CommonUtils.copyClipboard(mHistoryActivity, listData.get(position).getUrl());
                        return true;
                    } else if (item.getTitle().toString().equals(mHistoryActivity.getResources().getString(R.string.add_to_fav))) {
                        SettingsUtils.setPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count, SettingsUtils.getPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count).isEmpty() ? "0" : String.valueOf((long) (Double.parseDouble(SettingsUtils.getPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count)) + 1)));
                        SettingsUtils.setPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked.concat(SettingsUtils.getPref(bookmarks(mHistoryActivity), SettingsKeys.bookmarked_count)), listData.get(position).getUrl());
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
