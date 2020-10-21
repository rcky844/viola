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
import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.CompoundButton;

public class SettingsActivity extends AppCompatActivity {
	
	
	private Toolbar _toolbar;
	private boolean linear_general_open = false;
	private boolean linear_advenced_open = false;
	private boolean linear_about_open = false;
	
	private ScrollView vscroll_back;
	private LinearLayout linear_back;
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
	private TextView textview15;
	private TextView textview16;
	private LinearLayout linear_zoomkeys_a;
	private CheckBox checkbox5;
	private TextView textview37;
	private TextView textview38;
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
	private ObjectAnimator stackanim = new ObjectAnimator();
	private AlertDialog.Builder dialog1;
	private AlertDialog.Builder dabt;
	private AlertDialog.Builder drst;
	private Intent telegrambot = new Intent();
	private Intent re = new Intent();
	private AlertDialog.Builder d2;
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
		linear_about = (LinearLayout) findViewById(R.id.linear_about);
		linear8 = (LinearLayout) findViewById(R.id.linear8);
		textview_cool1 = (TextView) findViewById(R.id.textview_cool1);
		imageview4 = (ImageView) findViewById(R.id.imageview4);
		linear1_search = (LinearLayout) findViewById(R.id.linear1_search);
		linear1_homepage = (LinearLayout) findViewById(R.id.linear1_homepage);
		linear11 = (LinearLayout) findViewById(R.id.linear11);
		linear_zoomkeys_b = (LinearLayout) findViewById(R.id.linear_zoomkeys_b);
		linear1_b0 = (LinearLayout) findViewById(R.id.linear1_b0);
		textview8 = (TextView) findViewById(R.id.textview8);
		textview9 = (TextView) findViewById(R.id.textview9);
		textview4 = (TextView) findViewById(R.id.textview4);
		textview5 = (TextView) findViewById(R.id.textview5);
		linear12 = (LinearLayout) findViewById(R.id.linear12);
		checkbox3 = (CheckBox) findViewById(R.id.checkbox3);
		textview31 = (TextView) findViewById(R.id.textview31);
		textview32 = (TextView) findViewById(R.id.textview32);
		textview15 = (TextView) findViewById(R.id.textview15);
		textview16 = (TextView) findViewById(R.id.textview16);
		linear_zoomkeys_a = (LinearLayout) findViewById(R.id.linear_zoomkeys_a);
		checkbox5 = (CheckBox) findViewById(R.id.checkbox5);
		textview37 = (TextView) findViewById(R.id.textview37);
		textview38 = (TextView) findViewById(R.id.textview38);
		textview_cool2 = (TextView) findViewById(R.id.textview_cool2);
		imageview1 = (ImageView) findViewById(R.id.imageview1);
		linear1_javascript = (LinearLayout) findViewById(R.id.linear1_javascript);
		linear1_overrideempt = (LinearLayout) findViewById(R.id.linear1_overrideempt);
		linear13 = (LinearLayout) findViewById(R.id.linear13);
		linear1_b1 = (LinearLayout) findViewById(R.id.linear1_b1);
		checkbox1 = (CheckBox) findViewById(R.id.checkbox1);
		textview29 = (TextView) findViewById(R.id.textview29);
		textview30 = (TextView) findViewById(R.id.textview30);
		linear1_overrideemp = (LinearLayout) findViewById(R.id.linear1_overrideemp);
		checkbox2 = (CheckBox) findViewById(R.id.checkbox2);
		textview12 = (TextView) findViewById(R.id.textview12);
		textview13 = (TextView) findViewById(R.id.textview13);
		linear14 = (LinearLayout) findViewById(R.id.linear14);
		checkbox4 = (CheckBox) findViewById(R.id.checkbox4);
		textview33 = (TextView) findViewById(R.id.textview33);
		textview34 = (TextView) findViewById(R.id.textview34);
		textview_cool3 = (TextView) findViewById(R.id.textview_cool3);
		imageview5 = (ImageView) findViewById(R.id.imageview5);
		linear_version = (LinearLayout) findViewById(R.id.linear_version);
		linear_feed = (LinearLayout) findViewById(R.id.linear_feed);
		linear_source = (LinearLayout) findViewById(R.id.linear_source);
		textview23 = (TextView) findViewById(R.id.textview23);
		version_visiable = (TextView) findViewById(R.id.version_visiable);
		textview25 = (TextView) findViewById(R.id.textview25);
		textview26 = (TextView) findViewById(R.id.textview26);
		textview35 = (TextView) findViewById(R.id.textview35);
		textview36 = (TextView) findViewById(R.id.textview36);
		browservio_saver = getSharedPreferences("browservio.cfg", Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(this);
		dialog1 = new AlertDialog.Builder(this);
		dabt = new AlertDialog.Builder(this);
		drst = new AlertDialog.Builder(this);
		d2 = new AlertDialog.Builder(this);
		
		linear_general.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (linear_general_open) {
					linear6.setVisibility(View.GONE);
					linear_general_open = false;
					stackanim.setTarget(imageview4);
					stackanim.setPropertyName("rotation");
					stackanim.setFloatValues((float)(0), (float)(180));
					stackanim.setDuration((int)(250));
					stackanim.start();
				}
				else {
					linear6.setVisibility(View.VISIBLE);
					linear_general_open = true;
					stackanim.setTarget(imageview4);
					stackanim.setPropertyName("rotation");
					stackanim.setFloatValues((float)(180), (float)(0));
					stackanim.setDuration((int)(250));
					stackanim.start();
				}
			}
		});
		
		linear_advenced.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (linear_advenced_open) {
					linear5.setVisibility(View.GONE);
					linear_advenced_open = false;
					stackanim.setTarget(imageview1);
					stackanim.setPropertyName("rotation");
					stackanim.setFloatValues((float)(0), (float)(180));
					stackanim.setDuration((int)(250));
					stackanim.start();
				}
				else {
					linear5.setVisibility(View.VISIBLE);
					linear_advenced_open = true;
					stackanim.setTarget(imageview1);
					stackanim.setPropertyName("rotation");
					stackanim.setFloatValues((float)(180), (float)(0));
					stackanim.setDuration((int)(250));
					stackanim.start();
				}
			}
		});
		
		linear_about.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (linear_about_open) {
					linear8.setVisibility(View.GONE);
					linear_about_open = false;
					stackanim.setTarget(imageview5);
					stackanim.setPropertyName("rotation");
					stackanim.setFloatValues((float)(0), (float)(180));
					stackanim.setDuration((int)(250));
					stackanim.start();
				}
				else {
					linear8.setVisibility(View.VISIBLE);
					linear_about_open = true;
					stackanim.setTarget(imageview5);
					stackanim.setPropertyName("rotation");
					stackanim.setFloatValues((float)(180), (float)(0));
					stackanim.setDuration((int)(250));
					stackanim.start();
				}
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
						if (custom_se.getText().toString().equals(""))
						{
						} else {
							browservio_saver.edit().putString("defaultSearch", custom_se.getText().toString()).commit();
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
						if (custom_hp.getText().toString().equals(""))
						{
						} else {
							browservio_saver.edit().putString("defaultHomePage", custom_hp.getText().toString()).commit();
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
						browservio_saver.edit().putString("isFirstLaunch", "1").commit();
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
				final boolean _isChecked = _param2;
				if (_isChecked) {
					browservio_saver.edit().putString("showBrowseBtn", "1").commit();
				}
				else {
					browservio_saver.edit().putString("showBrowseBtn", "0").commit();
				}
			}
		});
		
		checkbox5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2)  {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					browservio_saver.edit().putString("showZoomKeys", "1").commit();
				}
				else {
					browservio_saver.edit().putString("showZoomKeys", "0").commit();
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
		
		checkbox4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _param1, boolean _param2)  {
				final boolean _isChecked = _param2;
				if (_isChecked) {
					browservio_saver.edit().putString("showCustomError", "1").commit();
				}
				else {
					browservio_saver.edit().putString("showCustomError", "0").commit();
				}
			}
		});
		
		linear_version.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				dabt.setTitle("Version information");
				dabt.setMessage("Version name: ".concat(browservio_saver.getString("versionName", "").concat("\n")).concat("Version codename: ".concat(browservio_saver.getString("versionCodename", "").concat("\n")).concat("Version technical name: ".concat(browservio_saver.getString("versionTechnical", "").concat("\n")).concat("Version family: ".concat(browservio_saver.getString("versionFamily", "")).concat("\n").concat("Version code: ".concat(browservio_saver.getString("versionCode", "").concat("\n")).concat("Version build date: ".concat(browservio_saver.getString("versionDate", "")).concat("\n")))))));
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
				telegrambot.setData(Uri.parse("https://github.com/Browservio/browservio/issues"));
				startActivity(telegrambot);
			}
		});
		
		linear_source.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				telegrambot.setAction(Intent.ACTION_VIEW);
				telegrambot.setData(Uri.parse("https://github.com/browservio/browservio"));
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
		linear_general_open = true;
		linear_advenced_open = true;
		linear_about_open = true;
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
			        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				            android.graphics.drawable.StateListDrawable stateListDrawable = new android.graphics.drawable.StateListDrawable();
				            stateListDrawable.addState(
				                new int[]{android.R.attr.state_pressed},
				                new android.graphics.drawable.ColorDrawable(Color.parseColor("#ffffff"))
				            );
				            stateListDrawable.addState(
				                new int[]{android.R.attr.state_focused},
				                new android.graphics.drawable.ColorDrawable(Color.parseColor("#00ffffff"))
				            );
				            stateListDrawable.addState(
				                new int[]{},
				                new android.graphics.drawable.ColorDrawable(Color.parseColor("#00ffffff"))
				            );
				            return stateListDrawable;
				        } else {
				            android.content.res.ColorStateList pressedColor = android.content.res.ColorStateList.valueOf(color);
				            android.graphics.drawable.ColorDrawable defaultColor = new android.graphics.drawable.ColorDrawable(Color.parseColor("#00ffffff"));
				            
				android.graphics.drawable.Drawable rippleColor = getRippleColor(color);
				            return new android.graphics.drawable.RippleDrawable(
				                pressedColor,
				                defaultColor,
				                rippleColor
				            );
				        }
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
		 
		    private static int lightenOrDarken(int color, double fraction) {
			        if (canLighten(color, fraction)) {
				            return lighten(color, fraction);
				        } else {
				            return darken(color, fraction);
				        }
			    }
		 
		    private static int lighten(int color, double fraction) {
			        int red = Color.red(color);
			        int green = Color.green(color);
			        int blue = Color.blue(color);
			        red = lightenColor(red, fraction);
			        green = lightenColor(green, fraction);
			        blue = lightenColor(blue, fraction);
			        int alpha = Color.alpha(color);
			        return Color.argb(alpha, red, green, blue);
			    }
		 
		    private static int darken(int color, double fraction) {
			        int red = Color.red(color);
			        int green = Color.green(color);
			        int blue = Color.blue(color);
			        red = darkenColor(red, fraction);
			        green = darkenColor(green, fraction);
			        blue = darkenColor(blue, fraction);
			        int alpha = Color.alpha(color);
			 
			        return Color.argb(alpha, red, green, blue);
			    }
		 
		    private static boolean canLighten(int color, double fraction) {
			        int red = Color.red(color);
			        int green = Color.green(color);
			        int blue = Color.blue(color);
			        return canLightenComponent(red, fraction)
			            && canLightenComponent(green, fraction)
			            && canLightenComponent(blue, fraction);
			    }
		 
		    private static boolean canLightenComponent(int colorComponent, double fraction) {
			        int red = Color.red(colorComponent);
			        int green = Color.green(colorComponent);
			        int blue = Color.blue(colorComponent);
			        return red + (red * fraction) < 255
			            && green + (green * fraction) < 255
			            && blue + (blue * fraction) < 255;
			    }
		 
		    private static int darkenColor(int color, double fraction) {
			        return (int) Math.max(color - (color * fraction), 0);
			    }
		 
		    private static int lightenColor(int color, double fraction) {
			        return (int) Math.min(color + (color * fraction), 255);
			    }
	}
	public static class CircleDrawables {
		    public static android.graphics.drawable.Drawable getSelectableDrawableFor(int color) {
			        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				            android.graphics.drawable.StateListDrawable stateListDrawable = new android.graphics.drawable.StateListDrawable();
				            stateListDrawable.addState(
				                new int[]{android.R.attr.state_pressed},
				                new android.graphics.drawable.ColorDrawable(Color.parseColor("#ffffff"))
				            );
				            stateListDrawable.addState(
				                new int[]{android.R.attr.state_focused},
				                new android.graphics.drawable.ColorDrawable(Color.parseColor("#00ffffff"))
				            );
				            stateListDrawable.addState(
				                new int[]{},
				                new android.graphics.drawable.ColorDrawable(Color.parseColor("#00ffffff"))
				            );
				            return stateListDrawable;
				        } else {
				            android.content.res.ColorStateList pressedColor = android.content.res.ColorStateList.valueOf(color);
				            android.graphics.drawable.ColorDrawable defaultColor = new android.graphics.drawable.ColorDrawable(Color.parseColor("#00ffffff"));
				            
				android.graphics.drawable.Drawable rippleColor = getRippleColor(color);
				            return new android.graphics.drawable.RippleDrawable(
				                pressedColor,
				                defaultColor,
				                rippleColor
				            );
				        }
			    }
		
		    private static android.graphics.drawable.Drawable getRippleColor(int color) {
			        float[] outerRadii = new float[180];
			        Arrays.fill(outerRadii, 80);
			        android.graphics.drawable.shapes.RoundRectShape r = new android.graphics.drawable.shapes.RoundRectShape(outerRadii, null, null);
			        
			android.graphics.drawable.ShapeDrawable shapeDrawable = new 
			android.graphics.drawable.ShapeDrawable(r);
			        shapeDrawable.getPaint().setColor(color);
			        return shapeDrawable;
			    }
		 
		    private static int lightenOrDarken(int color, double fraction) {
			        if (canLighten(color, fraction)) {
				            return lighten(color, fraction);
				        } else {
				            return darken(color, fraction);
				        }
			    }
		 
		    private static int lighten(int color, double fraction) {
			        int red = Color.red(color);
			        int green = Color.green(color);
			        int blue = Color.blue(color);
			        red = lightenColor(red, fraction);
			        green = lightenColor(green, fraction);
			        blue = lightenColor(blue, fraction);
			        int alpha = Color.alpha(color);
			        return Color.argb(alpha, red, green, blue);
			    }
		 
		    private static int darken(int color, double fraction) {
			        int red = Color.red(color);
			        int green = Color.green(color);
			        int blue = Color.blue(color);
			        red = darkenColor(red, fraction);
			        green = darkenColor(green, fraction);
			        blue = darkenColor(blue, fraction);
			        int alpha = Color.alpha(color);
			 
			        return Color.argb(alpha, red, green, blue);
			    }
		 
		    private static boolean canLighten(int color, double fraction) {
			        int red = Color.red(color);
			        int green = Color.green(color);
			        int blue = Color.blue(color);
			        return canLightenComponent(red, fraction)
			            && canLightenComponent(green, fraction)
			            && canLightenComponent(blue, fraction);
			    }
		 
		    private static boolean canLightenComponent(int colorComponent, double fraction) {
			        int red = Color.red(colorComponent);
			        int green = Color.green(colorComponent);
			        int blue = Color.blue(colorComponent);
			        return red + (red * fraction) < 255
			            && green + (green * fraction) < 255
			            && blue + (blue * fraction) < 255;
			    }
		 
		    private static int darkenColor(int color, double fraction) {
			        return (int) Math.max(color - (color * fraction), 0);
			    }
		 
		    private static int lightenColor(int color, double fraction) {
			        return (int) Math.min(color + (color * fraction), 255);
		}
	}
	
	public void drawableclass() {
	}
	
	
	private void _updateChkbox (final CheckBox _chk) {
		if (_chk.isChecked()) {
			_chk.setChecked(false);
		}
		else {
			_chk.setChecked(true);
		}
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
