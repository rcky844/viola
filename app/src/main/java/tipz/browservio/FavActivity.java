package tipz.browservio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import tipz.browservio.Utils.BrowservioSaverUtils;
import tipz.browservio.Utils.SketchwareUtil;

public class FavActivity extends AppCompatActivity {
	
	private final Timer _timer = new Timer();

	private double populate_count = 0;
	
	private final ArrayList<String> bookmark_list = new ArrayList<>();
	
	private ListView listview;
	
	private SharedPreferences browservio_saver;
	private SharedPreferences bookmarks;
	private TimerTask populate;
	private AlertDialog.Builder del_fav;
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.fav);
		initialize();
		initializeLogic();
	}
	
	private void initialize() {

		Toolbar _toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(_v -> onBackPressed());
		FloatingActionButton _fab = findViewById(R.id._fab);
		
		listview = findViewById(R.id.listview);
		browservio_saver = getSharedPreferences("browservio.cfg", Activity.MODE_PRIVATE);
		bookmarks = getSharedPreferences("bookmarks.cfg", Activity.MODE_PRIVATE);
		del_fav = new AlertDialog.Builder(this);
		
		listview.setOnItemClickListener((_param1, _param2, _param3, _param4) -> {
			BrowservioSaverUtils.setPref(browservio_saver, "needLoad", "1");
			BrowservioSaverUtils.setPref(browservio_saver, "needLoadUrl", BrowservioSaverUtils.getPref(bookmarks, "bookmark_".concat(String.valueOf((long)(_param3)))));
			finish();
		});
		
		listview.setOnItemLongClickListener((_param1, _param2, _param3, _param4) -> {
			final int _position = _param3;
			del_fav.setTitle(getResources().getString(R.string.del_fav_title));
			del_fav.setPositiveButton(android.R.string.yes, (_dialog, _which) -> {
				bookmark_list.remove(_position);
				BrowservioSaverUtils.setPref(bookmarks, "bookmark_".concat(String.valueOf((long)(_position))).concat("_show"), "0");
				((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
				SketchwareUtil.showMessage(getApplicationContext(), getResources().getString(R.string.del_success));
				isEmptyCheck(bookmark_list, bookmarks);
			});
			del_fav.setNegativeButton(android.R.string.no, (_dialog, _which) -> {

			});
			del_fav.create().show();
			return true;
		});
		
		_fab.setOnClickListener(_view -> {
			del_fav.setTitle(getResources().getString(R.string.del_fav2_title));
			del_fav.setMessage(getResources().getString(R.string.del_fav2_message));
			del_fav.setPositiveButton(android.R.string.yes, (_dialog, _which) -> {
				bookmarks.edit().clear().apply();
				SketchwareUtil.showMessage(getApplicationContext(), getResources().getString(R.string.wiped_success));
				finish();
			});
			del_fav.setNegativeButton(android.R.string.no, (_dialog, _which) -> {

			});
			del_fav.create().show();
		});
	}
	private void initializeLogic() {
		setTitle(getResources().getString(R.string.fav));
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
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
					if (!BrowservioSaverUtils.getPref(bookmarks, "bookmark_".concat(String.valueOf((long)(populate_count))).concat("_show")).equals("0")) {
						if (BrowservioSaverUtils.getPref(bookmarks, "bookmark_".concat(String.valueOf((long)(populate_count)))).equals("")) {
							listview.setAdapter(new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, bookmark_list));
							populate.cancel();
							prog.dismiss();
							isEmptyCheck(bookmark_list, bookmarks); // Place here for old data migration
						}
						else {
							bookmark_list.add(BrowservioSaverUtils.getPref(bookmarks, "bookmark_".concat(String.valueOf((long)(populate_count)))));
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
			SketchwareUtil.showMessage(getApplicationContext(), getResources().getString(R.string.fav_list_empty));
			finish();
		}
	}

	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int[] _location = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int[] _location = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input){
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels(){
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels(){
		return getResources().getDisplayMetrics().heightPixels;
	}
	
}
