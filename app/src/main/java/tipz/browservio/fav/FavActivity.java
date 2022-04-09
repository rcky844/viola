package tipz.browservio.fav;

import static tipz.browservio.fav.FavApi.bookmarks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;

import tipz.browservio.R;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class FavActivity extends AppCompatActivity {
    private static List<String> listData;
    private ProgressBar PopulationProg;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.recycler_list_item_activity);
        initialize();
        setTitle(getResources().getString(R.string.fav));
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

        PopulationProg = findViewById(R.id.PopulationProg);

        _fab.setOnClickListener(_view -> new MaterialAlertDialogBuilder(this)
                .setTitle(getResources().getString(R.string.del_fav2_title))
                .setMessage(getResources().getString(R.string.del_fav2_message))
                .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                    bookmarks(FavActivity.this).edit().clear().apply();
                    CommonUtils.showMessage(this, getResources().getString(R.string.wiped_success));
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show());
    }

    @Override
    public void onStart() {
        super.onStart();
        PopulationProg.setVisibility(View.VISIBLE);
        int populate_count = 0;
        boolean loopComplete = false;
        while (!loopComplete) {
            String shouldShow = SettingsUtils.getPref(bookmarks(FavActivity.this), SettingsKeys.bookmarked.concat(Integer.toString(populate_count)).concat(SettingsKeys.bookmarked_show));
            if (!shouldShow.equals("0")) {
                if (shouldShow.isEmpty()) {
                    loopComplete = true;
                    isEmptyCheck(listData, bookmarks(FavActivity.this));
                    PopulationProg.setVisibility(View.GONE);
                } else {
                    String bookmarkTitle = SettingsKeys.bookmarked.concat(Integer.toString(populate_count)).concat(SettingsKeys.bookmarked_title);
                    listData.add(SettingsUtils.getPref(bookmarks(FavActivity.this), bookmarkTitle).isEmpty() ?
                            SettingsUtils.getPref(bookmarks(FavActivity.this), SettingsKeys.bookmarked.concat(Integer.toString(populate_count))) :
                            SettingsUtils.getPref(bookmarks(FavActivity.this), bookmarkTitle));
                }
            }
            populate_count++;
        }

        RecyclerView favList = findViewById(R.id.recyclerView);

        favList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        favList.setAdapter(new ItemsAdapter(this));
    }

    void isEmptyCheck(List<String> list, SharedPreferences out) {
        // Placed here for old data migration
        if (list.isEmpty()) {
            out.edit().clear().apply();
            CommonUtils.showMessage(this, getResources().getString(R.string.fav_list_empty));
            finish();
        }
    }

    public static class ItemsAdapter extends RecyclerView.Adapter<FavActivity.ItemsAdapter.ViewHolder> {
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
        public FavActivity.ItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_item_1, parent, false);

            return new FavActivity.ItemsAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FavActivity.ItemsAdapter.ViewHolder holder, int position) {
            holder.mTextView.setText(listData.get(position));

            holder.mTextView.setOnClickListener(view -> {
                Intent needLoad = new Intent();
                needLoad.putExtra("needLoadUrl", SettingsUtils.getPref(bookmarks(mFavActivity), SettingsKeys.bookmarked.concat(Integer.toString(position))));
                mFavActivity.setResult(0, needLoad);
                mFavActivity.finish();
            });

            holder.mTextView.setOnLongClickListener(view -> {
                PopupMenu popup1 = new PopupMenu(mFavActivity, view);
                Menu menu1 = popup1.getMenu();
                menu1.add(mFavActivity.getResources().getString(R.string.del_fav));
                menu1.add(mFavActivity.getResources().getString(android.R.string.copyUrl));
                popup1.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().toString().equals(mFavActivity.getResources().getString(R.string.del_fav))) {
                        listData.remove(position);
                        SettingsUtils.setPref(bookmarks(mFavActivity), SettingsKeys.bookmarked.concat(String.valueOf(position)).concat(SettingsKeys.bookmarked_show), "0");
                        notifyItemRangeRemoved(position, 1);
                        mFavActivity.isEmptyCheck(listData, bookmarks(mFavActivity));
                        return true;
                    } else if (item.getTitle().toString().equals(mFavActivity.getResources().getString(android.R.string.copyUrl))) {
                        CommonUtils.copyClipboard(mFavActivity, SettingsUtils.getPref(bookmarks(mFavActivity), SettingsKeys.bookmarked.concat(String.valueOf(position))));
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
