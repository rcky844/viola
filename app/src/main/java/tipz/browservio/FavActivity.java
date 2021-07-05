package tipz.browservio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import tipz.browservio.sharedprefs.AllPrefs;
import tipz.browservio.utils.BrowservioBasicUtil;
import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;

public class FavActivity extends AppCompatActivity {
	
	private final Timer _timer = new Timer();

	private double populate_count = 0;
	
	private final ArrayList<String> bookmark_list = new ArrayList<>();
	
	private ListView listview;
	
	private SharedPreferences browservio_saver;
	private SharedPreferences bookmarks;
	private TimerTask populate;
	private AlertDialog.Builder del_fav;

	private Boolean popup = false;
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.fav);
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
		
		listview = findViewById(R.id.listview);
		browservio_saver = getSharedPreferences(AllPrefs.browservio_saver, Activity.MODE_PRIVATE);
		bookmarks = getSharedPreferences(AllPrefs.bookmarks, Activity.MODE_PRIVATE);
		del_fav = new AlertDialog.Builder(this);
		
		listview.setOnItemClickListener((_param1, _param2, _param3, _param4) -> {
			if (!popup) {
				BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.needLoad, "1");
				BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.needLoadUrl, BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmark.concat(String.valueOf(_param3))));
				finish();
			} else {
				popup = false;
			}
		});
		
		listview.setOnItemLongClickListener((_param1, _param2, _param3, _param4) -> {
			popup = true;
			PopupMenu popup1 = new PopupMenu(FavActivity.this, _param2);
			Menu menu1 = popup1.getMenu();
			menu1.add(getResources().getString(R.string.del_fav));
			menu1.add(getResources().getString(android.R.string.copyUrl));
			popup1.setOnMenuItemClickListener(item -> {
				if (item.getTitle().toString().equals(getResources().getString(R.string.del_fav))) {
					final int _position = _param3;
					del_fav.setTitle(getResources().getString(R.string.del_fav_title));
					del_fav.setMessage(getResources().getString(R.string.del_fav_title));
					del_fav.setPositiveButton(android.R.string.yes, (_dialog, _which) -> {
						bookmark_list.remove(_position);
						BrowservioSaverUtils.setPref(bookmarks, AllPrefs.bookmark.concat(String.valueOf((long)(_position))).concat(AllPrefs.bookmarked_count_show), "0");
						((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
						BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.del_success));
						isEmptyCheck(bookmark_list, bookmarks);
					});
					del_fav.setNegativeButton(android.R.string.no, null);
					del_fav.create().show();
					return true;
				} else if (item.getTitle().toString().equals(getResources().getString(android.R.string.copyUrl))) {
					((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmark.concat(String.valueOf(_param3)))));
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.copied_clipboard));
					return true;
				}
				return false;
			});
			popup1.show();
			return false;
		});
		
		_fab.setOnClickListener(_view -> {
			del_fav.setTitle(getResources().getString(R.string.del_fav2_title));
			del_fav.setMessage(getResources().getString(R.string.del_fav2_message));
			del_fav.setPositiveButton(android.R.string.yes, (_dialog, _which) -> {
				bookmarks.edit().clear().apply();
				BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.wiped_success));
				finish();
			});
			del_fav.setNegativeButton(android.R.string.no, null);
			del_fav.create().show();
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		populate_count = 0;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		populate_count = 0;
		final ProgressDialog prog = new ProgressDialog(FavActivity.this);
		prog.setMax(100);
		prog.setMessage(getResources().getString(R.string.populating_dialog_message));
		prog.setIndeterminate(true);
		prog.setCancelable(false);
		prog.show();
		populate = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(() -> {
					if (!BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmark.concat(String.valueOf((long)(populate_count))).concat(AllPrefs.bookmarked_count_show)).equals("0")) {
						if (BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmark.concat(String.valueOf((long)(populate_count)))).equals("")) {
							listview.setAdapter(new ArrayAdapter<>(getBaseContext(), R.layout.simple_list_item_1_daynight, bookmark_list));
							populate.cancel();
							prog.dismiss();
							isEmptyCheck(bookmark_list, bookmarks); // Place here for old data migration
						}
						else {
							if (BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmark.concat(String.valueOf((long)(populate_count))).concat(AllPrefs.bookmarked_count_title)).equals("")) {
								bookmark_list.add(getResources().getString(android.R.string.untitled));
							} else {
								bookmark_list.add(BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmark.concat(String.valueOf((long)(populate_count))).concat(AllPrefs.bookmarked_count_title)));
							}
						}
					}
					populate_count++;
				});
			}
		};
		_timer.scheduleAtFixedRate(populate, 0, 2);
	}

	private void isEmptyCheck(ArrayList<String> list, SharedPreferences out) {
		// Placed here for old data migration
		if (list.isEmpty()) {
			out.edit().clear().apply();
			BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.fav_list_empty));
			finish();
		}
	}
}
