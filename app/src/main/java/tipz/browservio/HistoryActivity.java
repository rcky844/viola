package tipz.browservio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static tipz.browservio.history.HistoryApi.historyPref;
import tipz.browservio.history.HistoryReader;
import tipz.browservio.sharedprefs.AllPrefs;
import tipz.browservio.utils.BrowservioBasicUtil;
import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<String> history_list = new ArrayList<>();

    private ListView listview;

    private SharedPreferences bookmarks;
    private AlertDialog.Builder deleteHistory;

    private Boolean popup = false;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.fav);
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

        listview = findViewById(R.id.listview);
        bookmarks = getSharedPreferences(AllPrefs.bookmarks, Activity.MODE_PRIVATE);
        deleteHistory = new AlertDialog.Builder(this);

        listview.setOnItemClickListener((_param1, _param2, _param3, _param4) -> {
            if (!popup) {
                Intent needLoad = new Intent();
                needLoad.putExtra("needLoadUrl", _param3);
                setResult(0, needLoad);
                finish();
            } else {
                popup = false;
            }
        });

        listview.setOnItemLongClickListener((_param1, _param2, _param3, _param4) -> {
            popup = true;
            PopupMenu popup1 = new PopupMenu(HistoryActivity.this, _param2);
            Menu menu1 = popup1.getMenu();
            menu1.add(getResources().getString(R.string.del_hist));
            menu1.add(getResources().getString(android.R.string.copyUrl));
            menu1.add(getResources().getString(R.string.add_to_fav));
            popup1.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equals(getResources().getString(R.string.del_hist))) {
                    final int _position = _param3;
                    deleteHistory.setTitle(getResources().getString(R.string.del_hist_title));
                    deleteHistory.setMessage(getResources().getString(R.string.del_hist_title));
                    deleteHistory.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                        history_list.remove(_position);
                        StringBuilder out = new StringBuilder();
                        for (Object o : history_list) {
                            out.append(o.toString());
                            out.append(BrowservioBasicUtil.LINE_SEPARATOR());
                        }
                        HistoryReader.write(historyPref(this), out.toString().trim());
                        ((BaseAdapter) listview.getAdapter()).notifyDataSetChanged();
                        BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.del_success));
                        isEmptyCheck();
                    });
                    deleteHistory.setNegativeButton(android.R.string.cancel, null);
                    deleteHistory.create().show();
                    return true;
                } else if (item.getTitle().toString().equals(getResources().getString(android.R.string.copyUrl))) {
                    ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", (String) listview.getItemAtPosition(_param3)));
                    BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.copied_clipboard));
                    return true;
                } else if (item.getTitle().toString().equals(getResources().getString(R.string.add_to_fav))) {
                    BrowservioSaverUtils.setPref(bookmarks, AllPrefs.bookmarked_count, BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmarked_count).isEmpty() ? "0" : String.valueOf((long) (Double.parseDouble(BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmarked_count)) + 1)));
                    BrowservioSaverUtils.setPref(bookmarks, AllPrefs.bookmarked.concat(BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmarked_count)), (String) listview.getItemAtPosition(_param3));
                    BrowservioSaverUtils.setPref(bookmarks, AllPrefs.bookmarked.concat(BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmarked_count)).concat(AllPrefs.bookmarked_count_show), "1");
                    BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.saved_su));
                    return true;
                }
                return false;
            });
            popup1.show();
            return false;
        });

        _fab.setOnClickListener(_view -> {
            deleteHistory.setTitle(getResources().getString(R.string.del_fav2_title));
            deleteHistory.setMessage(getResources().getString(R.string.del_hist_message));
            deleteHistory.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                HistoryReader.clear(historyPref(this));
                BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.wiped_success));
                finish();
            });
            deleteHistory.setNegativeButton(android.R.string.cancel, null);
            deleteHistory.create().show();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        history_list = new ArrayList<>(Arrays.asList(HistoryReader.history_data(historyPref(this)).trim().split("\n")));
        listview.setAdapter(new ArrayAdapter<>(getBaseContext(), R.layout.simple_list_item_1_daynight, history_list));
        isEmptyCheck();
    }

    private void isEmptyCheck() {
        if (HistoryReader.isEmptyCheck(historyPref(this))) {
            BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.hist_empty));
            finish();
        }
    }
}
