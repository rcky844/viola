package tipz.browservio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.net.*;
import android.text.*;
import android.util.*;
import android.webkit.*;
import android.animation.*;
import android.view.animation.*;
import java.util.*;
import java.text.*;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.app.Activity;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.CompoundButton;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {
	
	
	private Toolbar _toolbar;
	private boolean linear_general_open = false;
	private boolean linear_advenced_open = false;
	
	private ScrollView vscroll_back;
	private LinearLayout linear_back;
	private LinearLayout linear_general;
	private LinearLayout linear6;
	private LinearLayout linear_advenced;
	private LinearLayout linear5;
	private TextView textview_cool2;
	private ImageView imageview4;
	private LinearLayout linear1_homepage;
	private LinearLayout linear1_searches;
	private LinearLayout linear1_homepages;
	private TextView textview4;
	private TextView textview5;
	private LinearLayout linear1_search;
	private TextView textview8;
	private TextView textview9;
	private TextView textview_cool1;
	private ImageView imageview1;
	private LinearLayout linear1_javascript;
	private LinearLayout linear1_overrideempt;
	private LinearLayout linear1_javascripts;
	private CheckBox checkbox1;
	private TextView textview1;
	private TextView textview2;
	private LinearLayout linear1_overrideemp;
	private CheckBox checkbox2;
	private TextView textview12;
	private TextView textview13;
	
	private SharedPreferences browservio_saver;
	private AlertDialog.Builder dialog;
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.settings);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		
		_toolbar = (Toolbar) findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		vscroll_back = (ScrollView) findViewById(R.id.vscroll_back);
		linear_back = (LinearLayout) findViewById(R.id.linear_back);
		linear_general = (LinearLayout) findViewById(R.id.linear_general);
		linear6 = (LinearLayout) findViewById(R.id.linear6);
		linear_advenced = (LinearLayout) findViewById(R.id.linear_advenced);
		linear5 = (LinearLayout) findViewById(R.id.linear5);
		textview_cool2 = (TextView) findViewById(R.id.textview_cool2);
		imageview4 = (ImageView) findViewById(R.id.imageview4);
		linear1_homepage = (LinearLayout) findViewById(R.id.linear1_homepage);
		linear1_searches = (LinearLayout) findViewById(R.id.linear1_searches);
		linear1_homepages = (LinearLayout) findViewById(R.id.linear1_homepages);
		textview4 = (TextView) findViewById(R.id.textview4);
		textview5 = (TextView) findViewById(R.id.textview5);
		linear1_search = (LinearLayout) findViewById(R.id.linear1_search);
		textview8 = (TextView) findViewById(R.id.textview8);
		textview9 = (TextView) findViewById(R.id.textview9);
		textview_cool1 = (TextView) findViewById(R.id.textview_cool1);
		imageview1 = (ImageView) findViewById(R.id.imageview1);
		linear1_javascript = (LinearLayout) findViewById(R.id.linear1_javascript);
		linear1_overrideempt = (LinearLayout) findViewById(R.id.linear1_overrideempt);
		linear1_javascripts = (LinearLayout) findViewById(R.id.linear1_javascripts);
		checkbox1 = (CheckBox) findViewById(R.id.checkbox1);
		textview1 = (TextView) findViewById(R.id.textview1);
		textview2 = (TextView) findViewById(R.id.textview2);
		linear1_overrideemp = (LinearLayout) findViewById(R.id.linear1_overrideemp);
		checkbox2 = (CheckBox) findViewById(R.id.checkbox2);
		textview12 = (TextView) findViewById(R.id.textview12);
		textview13 = (TextView) findViewById(R.id.textview13);
		browservio_saver = getSharedPreferences("browservio.cfg", Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(this);
		
		linear_general.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_rippleAnimator("grey", textview_cool2);
				if (linear_general_open) {
					imageview4.setImageResource(R.drawable.ic_arrow_up_white);
					linear6.setVisibility(View.GONE);
					linear_general_open = false;
				}
				else {
					imageview4.setImageResource(R.drawable.ic_arrow_down_white);
					linear6.setVisibility(View.VISIBLE);
					linear_general_open = true;
				}
			}
		});
		
		linear_advenced.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_rippleAnimator("grey", textview_cool1);
				if (linear_advenced_open) {
					imageview1.setImageResource(R.drawable.ic_arrow_up_white);
					linear5.setVisibility(View.GONE);
					linear_advenced_open = false;
				}
				else {
					imageview1.setImageResource(R.drawable.ic_arrow_down_white);
					linear5.setVisibility(View.VISIBLE);
					linear_advenced_open = true;
				}
			}
		});
		
		linear1_homepages.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_rippleAnimator("grey", linear1_homepages);
				dialog.setTitle("Homepage");
				dialog.setMessage("Current homepage: ".concat(browservio_saver.getString("defaultHomePage", "")));
				final EditText custom_hp = new EditText(SettingsActivity.this); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT); 
				custom_hp.setLayoutParams(lp); dialog.setView(custom_hp);
				dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						browservio_saver.edit().putString("defaultHomePage", custom_hp.getText().toString()).commit();
						textview5.setText("Current homepage: ".concat(browservio_saver.getString("defaultHomePage", "")));
					}
				});
				dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				dialog.setCancelable(false);
				dialog.create().show();
			}
		});
		
		linear1_search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_rippleAnimator("grey", linear1_search);
				dialog.setTitle("Search engine");
				dialog.setMessage("Current search engine: ".concat(browservio_saver.getString("defaultSearch", "")));
				final EditText custom_se = new EditText(SettingsActivity.this); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT); 
				custom_se.setLayoutParams(lp); dialog.setView(custom_se);
				dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						browservio_saver.edit().putString("defaultSearch", custom_se.getText().toString()).commit();
						textview5.setText("Current homepage: ".concat(browservio_saver.getString("defaultSearch", "")));
					}
				});
				dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				dialog.setCancelable(false);
				dialog.create().show();
			}
		});
		
		linear1_overrideempt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_rippleAnimator("grey", linear1_overrideempt);
				if (checkbox2.isChecked()) {
					checkbox2.setChecked(false);
				}
				else {
					checkbox2.setChecked(true);
				}
			}
		});
		
		linear1_javascripts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_rippleAnimator("grey", linear1_javascripts);
				if (checkbox1.isChecked()) {
					checkbox1.setChecked(false);
				}
				else {
					checkbox1.setChecked(true);
				}
			}
		});
		
		checkbox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2)  {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					browservio_saver.edit().putString("isJavaScriptEnabled", "1").commit();
				}
				else {
					browservio_saver.edit().putString("isJavaScriptEnabled", "0").commit();
				}
			}
		});
		
		checkbox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2)  {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					browservio_saver.edit().putString("overrideEmptyError", "1").commit();
				}
				else {
					browservio_saver.edit().putString("overrideEmptyError", "0").commit();
				}
			}
		});
	}
	private void initializeLogic() {
		setTitle("Settings");
		if (browservio_saver.getString("isJavaScriptEnabled", "").equals("1")) {
			checkbox1.setChecked(true);
		}
		if (browservio_saver.getString("overrideEmptyError", "").equals("1")) {
			checkbox2.setChecked(true);
		}
		linear_general_open = true;
		linear_advenced_open = true;
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
		
		switch (_requestCode) {
			
			default:
			break;
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		textview5.setText("Current homepage: ".concat(browservio_saver.getString("defaultHomePage", "")));
		textview9.setText("Current search engine: ".concat(browservio_saver.getString("defaultSearch", "")));
	}
	private void _rippleAnimator (final String _color, final View _view) {
		android.content.res.ColorStateList clr = new android.content.res.ColorStateList(new int[][]{new int[]{}},new int[]{Color.parseColor(_color)});
		android.graphics.drawable.RippleDrawable ripdr = new android.graphics.drawable.RippleDrawable(clr, null, null);
		_view.setBackground(ripdr);
	}
	
	
	private void _BorderLinear (final View _view, final String _color1, final double _border, final String _color2, final double _round) {
		android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
		gd.setColor(Color.parseColor(_color1));
		gd.setCornerRadius((int) _round);
		gd.setStroke((int) _border, Color.parseColor(_color2));
		_view.setBackground(gd);
	}
	
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
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
		ArrayList<Double> _result = new ArrayList<Double>();
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
