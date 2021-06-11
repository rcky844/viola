package tipz.browservio;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class SettingsActivity extends AppCompatActivity {

	private LinearLayout linear_general;
	private LinearLayout linear6;
	private LinearLayout linear_advenced;
	private LinearLayout linear5;
	private LinearLayout linear_about;
	private LinearLayout linear8;
	private TextView textview_cool1;
	private ImageView imageview4;
	private LinearLayout linear1_search;
	private LinearLayout linear1_homepage;
	private LinearLayout linear11;
	private LinearLayout linear_zoomkeys_b;
	private LinearLayout linear1_b0;
	private TextView textview8;
	private TextView textview9;
	private TextView textview4;
	private TextView textview5;
	private LinearLayout linear12;
	private CheckBox checkbox3;
	private TextView textview31;
	private TextView textview32;
	private LinearLayout linear_zoomkeys_a;
	private CheckBox checkbox5;
	private TextView textview37;
	private TextView textview38;
	private TextView textview15;
	private TextView textview16;
	private TextView textview_cool2;
	private ImageView imageview1;
	private LinearLayout linear1_javascript;
	private LinearLayout linear1_overrideempt;
	private LinearLayout linear13;
	private LinearLayout linear1_b1;
	private CheckBox checkbox1;
	private TextView textview29;
	private TextView textview30;
	private LinearLayout linear1_overrideemp;
	private CheckBox checkbox2;
	private TextView textview12;
	private TextView textview13;
	private LinearLayout linear14;
	private CheckBox checkbox4;
	private TextView textview33;
	private TextView textview34;
	private TextView textview_cool3;
	private ImageView imageview5;
	private LinearLayout linear_version;
	private LinearLayout linear_feed;
	private LinearLayout linear_source;
	private TextView textview23;
	private TextView version_visiable;
	private TextView textview25;
	private TextView textview26;
	private TextView textview35;
	private TextView textview36;
	
	private SharedPreferences browservio_saver;
	private AlertDialog.Builder dialog;
	private final ObjectAnimator stackanim = new ObjectAnimator();
	private AlertDialog.Builder dialog1;
	private AlertDialog.Builder dabt;
	private AlertDialog.Builder drst;
	private final Intent telegrambot = new Intent();
	private final Intent re = new Intent();
	private AlertDialog.Builder d2;
	private final ObjectAnimator Sherlockanimation = new ObjectAnimator();
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.settings);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {

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
		linear_general = findViewById(R.id.linear_general);
		linear6 = findViewById(R.id.linear6);
		linear_advenced = findViewById(R.id.linear_advenced);
		linear5 = findViewById(R.id.linear5);
		linear_about = findViewById(R.id.linear_about);
		linear8 = findViewById(R.id.linear8);
		textview_cool1 = findViewById(R.id.textview_cool1);
		imageview4 = findViewById(R.id.imageview4);
		linear1_search = findViewById(R.id.linear1_search);
		linear1_homepage = findViewById(R.id.linear1_homepage);
		linear11 = findViewById(R.id.linear11);
		linear_zoomkeys_b = findViewById(R.id.linear_zoomkeys_b);
		linear1_b0 = findViewById(R.id.linear1_b0);
		textview8 = findViewById(R.id.textview8);
		textview9 = findViewById(R.id.textview9);
		textview4 = findViewById(R.id.textview4);
		textview5 = findViewById(R.id.textview5);
		linear12 = findViewById(R.id.linear12);
		checkbox3 = findViewById(R.id.checkbox3);
		textview31 = findViewById(R.id.textview31);
		textview32 = findViewById(R.id.textview32);
		linear_zoomkeys_a = findViewById(R.id.linear_zoomkeys_a);
		checkbox5 = findViewById(R.id.checkbox5);
		textview37 = findViewById(R.id.textview37);
		textview38 = findViewById(R.id.textview38);
		textview15 = findViewById(R.id.textview15);
		textview16 = findViewById(R.id.textview16);
		textview_cool2 = findViewById(R.id.textview_cool2);
		imageview1 = findViewById(R.id.imageview1);
		linear1_javascript = findViewById(R.id.linear1_javascript);
		linear1_overrideempt = findViewById(R.id.linear1_overrideempt);
		linear13 = findViewById(R.id.linear13);
		linear1_b1 = findViewById(R.id.linear1_b1);
		checkbox1 = findViewById(R.id.checkbox1);
		textview29 = findViewById(R.id.textview29);
		textview30 = findViewById(R.id.textview30);
		linear1_overrideemp = findViewById(R.id.linear1_overrideemp);
		checkbox2 = findViewById(R.id.checkbox2);
		textview12 = findViewById(R.id.textview12);
		textview13 = findViewById(R.id.textview13);
		linear14 = findViewById(R.id.linear14);
		checkbox4 = findViewById(R.id.checkbox4);
		textview33 = findViewById(R.id.textview33);
		textview34 = findViewById(R.id.textview34);
		textview_cool3 = findViewById(R.id.textview_cool3);
		imageview5 = findViewById(R.id.imageview5);
		linear_version = findViewById(R.id.linear_version);
		linear_feed = findViewById(R.id.linear_feed);
		linear_source = findViewById(R.id.linear_source);
		textview23 = findViewById(R.id.textview23);
		version_visiable = findViewById(R.id.version_visiable);
		textview25 = findViewById(R.id.textview25);
		textview26 = findViewById(R.id.textview26);
		textview35 = findViewById(R.id.textview35);
		textview36 = findViewById(R.id.textview36);
		browservio_saver = getSharedPreferences("browservio.cfg", Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(this);
		dialog1 = new AlertDialog.Builder(this);
		dabt = new AlertDialog.Builder(this);
		drst = new AlertDialog.Builder(this);
		d2 = new AlertDialog.Builder(this);
		
		linear_general.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				stackanim.setTarget(imageview4);
				Sherlockanimation.setTarget(linear6);
				stackanim.setPropertyName("rotation");
				Sherlockanimation.setPropertyName("alpha");
				stackanim.setDuration(250);
				Sherlockanimation.setDuration(250);
				if (linear6.getVisibility() == View.VISIBLE) {
					stackanim.setFloatValues((float)(0), (float)(180));
					Sherlockanimation.setFloatValues((float)(1), (float)(0));
					linear6.setVisibility(View.GONE);
				} else {
					linear6.setVisibility(View.VISIBLE);
					stackanim.setFloatValues((float)(180), (float)(0));
					Sherlockanimation.setFloatValues((float)(0), (float)(1));
				}
				stackanim.start();
				Sherlockanimation.start();
			}
		});
		
		linear_advenced.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				stackanim.setTarget(imageview1);
				Sherlockanimation.setTarget(linear5);
				stackanim.setPropertyName("rotation");
				Sherlockanimation.setPropertyName("alpha");
				stackanim.setDuration(250);
				Sherlockanimation.setDuration(250);
				if (linear5.getVisibility() == View.VISIBLE) {
					stackanim.setFloatValues((float)(0), (float)(180));
					Sherlockanimation.setFloatValues((float)(1), (float)(0));
					linear5.setVisibility(View.GONE);
				} else {
					linear5.setVisibility(View.VISIBLE);
					stackanim.setFloatValues((float)(180), (float)(0));
					Sherlockanimation.setFloatValues((float)(0), (float)(1));
				}
				stackanim.start();
				Sherlockanimation.start();
			}
		});
		
		linear_about.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				stackanim.setTarget(imageview5);
				Sherlockanimation.setTarget(linear8);
				stackanim.setPropertyName("rotation");
				Sherlockanimation.setPropertyName("alpha");
				stackanim.setDuration(250);
				Sherlockanimation.setDuration(250);
				if (linear8.getVisibility() == View.VISIBLE) {
					stackanim.setFloatValues((float)(0), (float)(180));
					Sherlockanimation.setFloatValues((float)(1), (float)(0));
					linear8.setVisibility(View.GONE);
				} else {
					linear8.setVisibility(View.VISIBLE);
					stackanim.setFloatValues((float)(180), (float)(0));
					Sherlockanimation.setFloatValues((float)(0), (float)(1));
				}
				stackanim.start();
				Sherlockanimation.start();
			}
		});
		
		linear1_search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				dialog1.setTitle("Search engine");
				dialog1.setMessage("Current search engine: ".concat(browservio_saver.getString("defaultSearch", "")));
				final EditText custom_se = new EditText(SettingsActivity.this); LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT); 
				custom_se.setLayoutParams(lp2); dialog1.setView(custom_se);
				dialog1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						if (!custom_se.getText().toString().equals(""))
						{
							browservio_saver.edit().putString("defaultSearch", custom_se.getText().toString()).apply();
							textview5.setText("Current homepage: ".concat(browservio_saver.getString("defaultSearch", "")));
						}
					}
				});
				dialog1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				dialog1.setCancelable(false);
				dialog1.create().show();
			}
		});
		
		linear1_homepage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				dialog.setTitle("Homepage");
				dialog.setMessage("Current homepage: ".concat(browservio_saver.getString("defaultHomePage", "")));
				final EditText custom_hp = new EditText(SettingsActivity.this); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT); 
				custom_hp.setLayoutParams(lp); dialog.setView(custom_hp);
				dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						if (!custom_hp.getText().toString().equals(""))
						{
							browservio_saver.edit().putString("defaultHomePage", custom_hp.getText().toString()).apply();
							textview5.setText("Current homepage: ".concat(browservio_saver.getString("defaultHomePage", "")));
						}
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
		
		linear11.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_updateChkbox(checkbox3);
			}
		});
		
		linear_zoomkeys_b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_updateChkbox(checkbox5);
			}
		});
		
		linear1_b0.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				drst.setTitle("Are you sure?");
				drst.setMessage("You are currently trying to reset Browservio.\n\nNote this option will do the following things:\n• Reset all your settings\n• Clear browser cache and history\n\nARE YOU SURE ABOUT THIS?");
				drst.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						browservio_saver.edit().putString("isFirstLaunch", "1").apply();
						finish();
					}
				});
				drst.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				drst.create().show();
			}
		});
		
		checkbox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2)  {
				if (_param2) {
					browservio_saver.edit().putString("showBrowseBtn", "1").apply();
				}
				else {
					browservio_saver.edit().putString("showBrowseBtn", "0").apply();
				}
			}
		});
		
		checkbox5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2)  {
				if (_param2) {
					browservio_saver.edit().putString("showZoomKeys", "1").apply();
				}
				else {
					browservio_saver.edit().putString("showZoomKeys", "0").apply();
				}
			}
		});
		
		linear1_javascript.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_updateChkbox(checkbox1);
			}
		});
		
		linear1_overrideempt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_updateChkbox(checkbox2);
			}
		});
		
		linear13.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				_updateChkbox(checkbox4);
			}
		});
		
		checkbox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2)  {
				if (_param2) {
					browservio_saver.edit().putString("isJavaScriptEnabled", "1").apply();
				}
				else {
					browservio_saver.edit().putString("isJavaScriptEnabled", "0").apply();
				}
			}
		});
		
		checkbox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2)  {
				if (_param2) {
					browservio_saver.edit().putString("overrideEmptyError", "1").apply();
				}
				else {
					browservio_saver.edit().putString("overrideEmptyError", "0").apply();
				}
			}
		});
		
		checkbox4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2)  {
				if (_param2) {
					browservio_saver.edit().putString("showCustomError", "1").apply();
				}
				else {
					browservio_saver.edit().putString("showCustomError", "0").apply();
				}
			}
		});
		
		linear_version.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				dabt.setTitle("Version information");
				dabt.setMessage("Version name: ".concat(browservio_saver.getString("versionName", "").concat("\n")).concat("Version codename: ".concat(browservio_saver.getString("versionCodename", "").concat("\n")).concat("Version technical name: ".concat(browservio_saver.getString("versionTechnical", "").concat("\n")).concat("Version family: ".concat(browservio_saver.getString("versionFamily", "")).concat("\n").concat("Version code: ".concat(browservio_saver.getString("versionCode", "").concat("\n")).concat("Version build date: ".concat(browservio_saver.getString("versionDate", "")).concat("\n")))))).concat("Config version: ".concat(browservio_saver.getString("configVersion", "")).concat("\n")));
				dabt.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				dabt.create().show();
			}
		});
		
		linear_feed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				telegrambot.setAction(Intent.ACTION_VIEW);
				telegrambot.setData(Uri.parse("https://gitlab.com/TipzTeam/browservio/-/issues"));
				startActivity(telegrambot);
			}
		});
		
		linear_source.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				telegrambot.setAction(Intent.ACTION_VIEW);
				telegrambot.setData(Uri.parse("https://gitlab.com/TipzTeam/browservio"));
				startActivity(telegrambot);
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
		if (browservio_saver.getString("showBrowseBtn", "").equals("1")) {
			checkbox3.setChecked(true);
		}
		if (browservio_saver.getString("showZoomKeys", "").equals("1")) {
			checkbox5.setChecked(true);
		}
		if (browservio_saver.getString("showCustomError", "").equals("1")) {
			checkbox4.setChecked(true);
		}
		textview5.setText("Current homepage: ".concat(browservio_saver.getString("defaultHomePage", "")));
		textview9.setText("Current search engine: ".concat(browservio_saver.getString("defaultSearch", "")));
		version_visiable.setText("Browservio ".concat(browservio_saver.getString("versionName", "")));
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		_setAllRipple(linear_general, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear_advenced, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear_about, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear_version, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear_feed, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear1_search, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear1_homepage, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear11, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear1_javascript, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear1_overrideempt, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear1_b0, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear13, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear_source, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
		_setAllRipple(linear_zoomkeys_b, "#ffffff", "#3DDC84", 6, 2, "#3DDC84", "#3DDC84");
	}
	private void _setRipple (final View _view) {
		TypedValue typedValue = new TypedValue();
		
		getApplicationContext().getTheme().resolveAttribute(16843868, typedValue, true);
		
		_view.setBackgroundResource(typedValue.resourceId);
		
		_view.setClickable(true);
	}
	
	
	private void _rippleRoundStroke (final View _view, final String _focus, final String _pressed, final double _round, final double _stroke, final String _strokeclr) {
		android.graphics.drawable.GradientDrawable GG = new android.graphics.drawable.GradientDrawable();
		GG.setColor(Color.parseColor(_focus));
		GG.setCornerRadius((float)_round);
		GG.setStroke((int) _stroke,
		Color.parseColor("#" + _strokeclr.replace("#", "")));
		android.graphics.drawable.RippleDrawable RE = new android.graphics.drawable.RippleDrawable(new android.content.res.ColorStateList(new int[][]{new int[]{}}, new int[]{ Color.parseColor(_pressed)}), GG, null);
		_view.setBackground(RE);
	}
	
	
	private void _ripple (final View _view, final String _c) {
		_view.setBackground(Drawables.getSelectableDrawableFor(Color.parseColor(_c)));
		_view.setClickable(true);
		
	}
	
	public static class Drawables {
		    public static android.graphics.drawable.Drawable getSelectableDrawableFor(int color) {
				android.content.res.ColorStateList pressedColor = android.content.res.ColorStateList.valueOf(color);
				android.graphics.drawable.ColorDrawable defaultColor = new android.graphics.drawable.ColorDrawable(Color.parseColor("#00ffffff"));

				android.graphics.drawable.Drawable rippleColor = getRippleColor(color);
				return new android.graphics.drawable.RippleDrawable(
					pressedColor,
					defaultColor,
					rippleColor
				);
			}
		
		    private static android.graphics.drawable.Drawable getRippleColor(int color) {
			        float[] outerRadii = new float[8];
			        Arrays.fill(outerRadii, 0);
			        android.graphics.drawable.shapes.RoundRectShape r = new android.graphics.drawable.shapes.RoundRectShape(outerRadii, null, null);
			        
			android.graphics.drawable.ShapeDrawable shapeDrawable = new 
			android.graphics.drawable.ShapeDrawable(r);
			        shapeDrawable.getPaint().setColor(color);
			        return shapeDrawable;
			    }
	}


	private void _updateChkbox (final CheckBox _chk) {
		_chk.setChecked(!_chk.isChecked());
	}
	
	
	private void _setAllRipple (final View _view, final String _focus, final String _press, final double _round, final double _stroke, final String _colorStroke, final String _color) {
		_setRipple(_view);
		_rippleRoundStroke(_view, _focus, _press, _round, _stroke, _colorStroke);
		_ripple(_view, _color);
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
