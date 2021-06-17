package tipz.browservio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;

import tipz.browservio.utils.BrowservioBasicUtil;
import tipz.browservio.utils.BrowservioSaverUtils;

public class HistoryActivity extends AppCompatActivity {
	
	private final Timer _timer = new Timer();
	
	private ArrayList<String> history_list = new ArrayList<>();
	
	private ListView listview;
	
	private SharedPreferences browservio_saver;
	private AlertDialog.Builder del_hist;
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.fav);
		initialize();
		initializeLogic();
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
		browservio_saver = getSharedPreferences("browservio.cfg", Activity.MODE_PRIVATE);
		del_hist = new AlertDialog.Builder(this);
		
		listview.setOnItemClickListener((_param1, _param2, _param3, _param4) -> {
			BrowservioSaverUtils.setPref(browservio_saver, "needLoad", "1");
			BrowservioSaverUtils.setPref(browservio_saver, "needLoadUrl", (String) listview.getItemAtPosition(_param3));
			finish();
		});

		/*
		listview.setOnItemLongClickListener((_param1, _param2, _param3, _param4) -> {
			final int _position = _param3;
			del_hist.setTitle(getResources().getString(R.string.del_hist_title));
			del_hist.setPositiveButton(android.R.string.yes, (_dialog, _which) -> {
				history_list.remove(_position);
				BrowservioSaverUtils.setPref(bookmarks, "bookmark_".concat(String.valueOf((long)(_position))).concat("_show"), "0");
				((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
				BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.del_success));
				isEmptyCheck(bookmark_list, bookmarks);
			});
			del_fav.setNegativeButton(android.R.string.no, null);
			del_fav.create().show();
			return true;
		}); */
		
		_fab.setOnClickListener(_view -> {
			del_hist.setTitle(getResources().getString(R.string.del_fav2_title));
			del_hist.setMessage(getResources().getString(R.string.del_hist_title));
			del_hist.setPositiveButton(android.R.string.yes, (_dialog, _which) -> {
				BrowservioSaverUtils.setPref(browservio_saver, "history", "");
				BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.wiped_success));
				finish();
			});
			del_hist.setNegativeButton(android.R.string.no, null);
			del_hist.create().show();
		});
	}
	private void initializeLogic() {
		setTitle(getResources().getString(R.string.hist));
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStart() {
		super.onStart();
			history_list = new ArrayList<>(Arrays.asList(BrowservioSaverUtils.getPref(browservio_saver, "history").split("\n")));
			listview.setAdapter(new ArrayAdapter<>(getBaseContext(), R.layout.simple_list_item_1_daynight, history_list));
	}

	private void isEmptyCheck(ArrayList<String> list, SharedPreferences out) {
		// Placed here for old data migration
		if (list.isEmpty()) {
			out.edit().clear().apply();
			BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.fav_list_empty));
			finish();
		}
	}

	public void setDel_hist(AlertDialog.Builder del_hist) {
		this.del_hist = del_hist;
	}
}
