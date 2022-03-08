package tipz.browservio.fav;

import static tipz.browservio.fav.FavApi.bookmarks;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tipz.browservio.R;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class FavActivity extends AppCompatActivity {
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
                    CommonUtils.showMessage(getApplicationContext(), getResources().getString(R.string.wiped_success));
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
        final List<String> bookmark_list = new ArrayList<>();
        boolean loopComplete = false;
        while (!loopComplete) {
            String shouldShow = SettingsUtils.getPref(bookmarks(FavActivity.this), SettingsKeys.bookmarked.concat(Integer.toString(populate_count)).concat(SettingsKeys.bookmarked_count_show));
            if (!shouldShow.equals("0")) {
                if (shouldShow.isEmpty()) {
                    loopComplete = true;
                    isEmptyCheck(bookmark_list, bookmarks(FavActivity.this));
                    PopulationProg.setVisibility(View.GONE);
                } else {
                    String bookmarkTitle = SettingsKeys.bookmarked.concat(Integer.toString(populate_count)).concat(SettingsKeys.bookmarked_count_title);
                    bookmark_list.add(SettingsUtils.getPref(bookmarks(FavActivity.this), bookmarkTitle).isEmpty() ?
                            SettingsUtils.getPref(bookmarks(FavActivity.this), SettingsKeys.bookmarked.concat(Integer.toString(populate_count))) :
                            SettingsUtils.getPref(bookmarks(FavActivity.this), bookmarkTitle));
                }
            }
            populate_count++;
        }

        new FavRecycler(this, this, findViewById(R.id.recyclerView), bookmark_list);
    }

    void isEmptyCheck(List<String> list, SharedPreferences out) {
        // Placed here for old data migration
        if (list.isEmpty()) {
            out.edit().clear().apply();
            CommonUtils.showMessage(getApplicationContext(), getResources().getString(R.string.fav_list_empty));
            finish();
        }
    }
}
