package tipz.browservio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
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

import tipz.browservio.sharedprefs.AllPrefs;
import tipz.browservio.utils.BrowservioBasicUtil;
import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;

public class HistoryActivity extends AppCompatActivity {

	private ArrayList<String> history_list = new ArrayList<>();
	
	private ListView listview;
	
	private SharedPreferences browservio_saver;
	private SharedPreferences bookmarks;
	private AlertDialog.Builder del_hist;

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
		browservio_saver = getSharedPreferences(AllPrefs.browservio_saver, Activity.MODE_PRIVATE);
		bookmarks = getSharedPreferences(AllPrefs.bookmarks, Activity.MODE_PRIVATE);
		del_hist = new AlertDialog.Builder(this);
		
		listview.setOnItemClickListener((_param1, _param2, _param3, _param4) -> {
			if (!popup) {
				BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.needLoad, "1");
				BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.needLoadUrl, (String) listview.getItemAtPosition(_param3));
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
					del_hist.setTitle(getResources().getString(R.string.del_hist_title));
					del_hist.setMessage(getResources().getString(R.string.del_hist_title));
					del_hist.setPositiveButton(android.R.string.yes, (_dialog, _which) -> {
						history_list.remove(_position);
						StringBuilder out = new StringBuilder();
						for (Object o : history_list) {
							out.append(o.toString());
							out.append(System.lineSeparator());
						}
						BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.history, out.toString().trim());
						((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
						BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.del_success));
						isEmptyCheck();
					});
					del_hist.setNegativeButton(android.R.string.no, null);
					del_hist.create().show();
					return true;
				} else if (item.getTitle().toString().equals(getResources().getString(android.R.string.copyUrl))) {
					((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", (String) listview.getItemAtPosition(_param3)));
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.copied_clipboard));
					return true;
				} else if (item.getTitle().toString().equals(getResources().getString(R.string.add_to_fav))) {
					BrowservioSaverUtils.setPref(bookmarks, AllPrefs.bookmarked_count, BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmarked_count).equals("") ? "0" : String.valueOf((long) (Double.parseDouble(BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmarked_count)) + 1)));
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
			del_hist.setTitle(getResources().getString(R.string.del_fav2_title));
			del_hist.setMessage(getResources().getString(R.string.del_hist_message));
			del_hist.setPositiveButton(android.R.string.yes, (_dialog, _which) -> {
				BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.history, "");
				BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.wiped_success));
				finish();
			});
			del_hist.setNegativeButton(android.R.string.no, null);
			del_hist.create().show();
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		history_list = new ArrayList<>(Arrays.asList(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.history).trim().split("\n")));
		listview.setAdapter(new ArrayAdapter<>(getBaseContext(), R.layout.simple_list_item_1_daynight, history_list));
		isEmptyCheck();
	}

	private void isEmptyCheck() {
		// Placed here for old data migration
		if (BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.history).trim().isEmpty()) {
			BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.hist_empty));
			finish();
		}
	}
}
