package tipz.browservio.history;

import android.annotation.SuppressLint;
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

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import tipz.browservio.R;
import tipz.browservio.broha.Broha;
import tipz.browservio.fav.FavUtils;
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
        private final WeakReference<HistoryActivity> mHistoryActivity;

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
            mHistoryActivity = new WeakReference<>(historyActivity);
        }

        @NonNull
        @Override
        public HistoryActivity.ItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_broha, parent, false);

            return new HistoryActivity.ItemsAdapter.ViewHolder(view);
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        public void onBindViewHolder(@NonNull HistoryActivity.ItemsAdapter.ViewHolder holder, int position) {
            final HistoryActivity historyActivity = mHistoryActivity.get();
            Broha data = listData.get(position);
            String title = data.getTitle();
            String url = data.getUrl();

            holder.icon.setImageResource(R.drawable.default_favicon);
            holder.title.setText(title == null ? url : title);
            holder.url.setText(Uri.parse(url).getHost());
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(data.getTimestamp() * 1000L);
            holder.time.setText(new SimpleDateFormat("dd/MM\nHH:ss").format(date.getTime()));

            holder.back.setOnClickListener(view -> {
                Intent needLoad = new Intent();
                needLoad.putExtra("needLoadUrl", url);
                historyActivity.setResult(0, needLoad);
                historyActivity.finish();
            });

            holder.back.setOnLongClickListener(view -> {
                PopupMenu popup1 = new PopupMenu(historyActivity, view);
                Menu menu1 = popup1.getMenu();
                menu1.add(historyActivity.getResources().getString(R.string.delete));
                menu1.add(historyActivity.getResources().getString(android.R.string.copyUrl));
                menu1.add(historyActivity.getResources().getString(R.string.add_to_fav));
                popup1.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().toString().equals(historyActivity.getResources().getString(R.string.delete))) {
                        HistoryUtils.deleteById(historyActivity, data.getId());
                        listData.remove(position);
                        notifyItemRangeRemoved(position, 1);
                        historyActivity.isEmptyCheck();
                        return true;
                    } else if (item.getTitle().toString().equals(historyActivity.getResources().getString(android.R.string.copyUrl))) {
                        CommonUtils.copyClipboard(historyActivity, url);
                        return true;
                    } else if (item.getTitle().toString().equals(historyActivity.getResources().getString(R.string.add_to_fav))) {
                        FavUtils.appendData(historyActivity, title, url);
                        CommonUtils.showMessage(historyActivity, historyActivity.getResources().getString(R.string.saved_su));
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
