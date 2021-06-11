package tipz.browservio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import tipz.browservio.Utils.SketchwareUtil;

public class FavActivity extends AppCompatActivity {
	
	private final Timer _timer = new Timer();

	private double populate_count = 0;
	
	private final ArrayList<String> bookmark_list = new ArrayList<>();
	
	private ListView listview;
	
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
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		FloatingActionButton _fab = findViewById(R.id._fab);
		
		listview = findViewById(R.id.listview);
		bookmarks = getSharedPreferences("bookmarks.cfg", Activity.MODE_PRIVATE);
		del_fav = new AlertDialog.Builder(this);
		
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> _param1, View _param2, int _param3, long _param4) {
				getApplicationContext();
				((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", bookmark_list.get(_param3)));
				SketchwareUtil.showMessage(getApplicationContext(), "Copied to clipboard!");
			}
		});
		
		listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> _param1, View _param2, int _param3, long _param4) {
				final int _position = _param3;
				del_fav.setTitle("Do you want to delete this favourite?");
				del_fav.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						bookmark_list.remove(_position);
						bookmarks.edit().putString("bookmark_".concat(String.valueOf((long)(_position))).concat("_show"), "0").apply();
						((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
						SketchwareUtil.showMessage(getApplicationContext(), "Deleted successfully!");
					}
				});
				del_fav.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				del_fav.create().show();
				return true;
			}
		});
		
		_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				del_fav.setTitle("Delete all entries");
				del_fav.setMessage("You are about to delete all favourites entries, are you sure about this?");
				del_fav.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						bookmarks.edit().clear().apply();
						SketchwareUtil.showMessage(getApplicationContext(), "Wiped successfully!");
						finish();
					}
				});
				del_fav.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				del_fav.create().show();
			}
		});
	}
	private void initializeLogic() {
		setTitle("Favourites");
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
		prog.setTitle("Populating");
		prog.setMessage("Populating entries...");
		prog.setIndeterminate(true);
		prog.setCancelable(false);
		prog.show();
		populate = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!bookmarks.getString("bookmark_".concat(String.valueOf((long)(populate_count))).concat("_show"), "").equals("0")) {
							if (bookmarks.getString("bookmark_".concat(String.valueOf((long)(populate_count))), "").equals("")) {
								listview.setAdapter(new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, bookmark_list));
								populate.cancel();
								prog.dismiss();
							}
							else {
								bookmark_list.add(bookmarks.getString("bookmark_".concat(String.valueOf((long)(populate_count))), ""));
							}
						}
						populate_count++;
					}
				});
			}
		};
		_timer.scheduleAtFixedRate(populate, 0, 2);
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
