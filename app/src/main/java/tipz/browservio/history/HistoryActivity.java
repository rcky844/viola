package tipz.browservio.history;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import tipz.browservio.R;
import tipz.browservio.utils.CommonUtils;

public class HistoryActivity extends AppCompatActivity {

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
                    CommonUtils.showMessage(getApplicationContext(), getResources().getString(R.string.wiped_success));
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show());
    }

    @Override
    public void onStart() {
        super.onStart();
        isEmptyCheck();

        new HistoryRecycler(this, this, findViewById(R.id.recyclerView));
    }

    void isEmptyCheck() {
        if (HistoryReader.isEmptyCheck(this)) {
            CommonUtils.showMessage(getApplicationContext(), getResources().getString(R.string.hist_empty));
            finish();
        }
    }
}
