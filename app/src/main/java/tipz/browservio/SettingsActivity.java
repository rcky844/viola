package tipz.browservio;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Arrays;
import java.util.Objects;

import tipz.browservio.Utils.BrowservioSaverUtils;
import tipz.browservio.Utils.BrowservioBasicUtil;

public class SettingsActivity extends AppCompatActivity {

	private LinearLayout linear_general;
	private LinearLayout linear6;
	private LinearLayout linear_advenced;
	private LinearLayout linear5;
	private LinearLayout linear_about;
	private LinearLayout linear8;
	private ImageView imageview4;
	private LinearLayout linear1_search;
	private LinearLayout linear1_homepage;
	private LinearLayout linear11;
	private LinearLayout linear_zoomkeys_b;
	private LinearLayout linearendp;
	private LinearLayout linear1_b0;
	private TextView textview9;
	private TextView textview5;
	private CheckBox checkbox3;
	private CheckBox checkbox5;
	private ImageView imageview1;
	private LinearLayout linear1_javascript;
	private LinearLayout linear1_overrideempt;
	private LinearLayout linear13;
	private CheckBox checkbox1;
	private CheckBox checkbox2;
	private CheckBox checkbox4;
	private ImageView imageview5;
	private LinearLayout linear_version;
	private LinearLayout linear_feed;
	private LinearLayout linear_source;
	private TextView version_visiable;
	private TextView textviewendp2;

	private SharedPreferences browservio_saver;
	private AlertDialog.Builder dialog;
	private final ObjectAnimator stackanim = new ObjectAnimator();
	private AlertDialog.Builder dialog1;
	private AlertDialog.Builder dialogendp;
	private AlertDialog.Builder dabt;
	private AlertDialog.Builder drst;
	private AlertDialog.Builder diazoomrestart;
	private final ObjectAnimator Sherlockanimation = new ObjectAnimator();

	boolean writingScreen = true;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.settings);
		initialize();
		initializeLogic();
	}
	
	private void initialize() {

		Toolbar _toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(_v -> onBackPressed());
		linear_general = findViewById(R.id.linear_general);
		linear6 = findViewById(R.id.linear6);
		linear_advenced = findViewById(R.id.linear_advenced);
		linear5 = findViewById(R.id.linear5);
		linear_about = findViewById(R.id.linear_about);
		linear8 = findViewById(R.id.linear8);
		imageview4 = findViewById(R.id.imageview4);
		linear1_search = findViewById(R.id.linear1_search);
		linear1_homepage = findViewById(R.id.linear1_homepage);
		linear11 = findViewById(R.id.linear11);
		linear_zoomkeys_b = findViewById(R.id.linear_zoomkeys_b);
		linearendp = findViewById(R.id.linearendp);
		linear1_b0 = findViewById(R.id.linear1_b0);
		findViewById(R.id.textview8);
		textview9 = findViewById(R.id.textview9);
		textview5 = findViewById(R.id.textview5);
		checkbox3 = findViewById(R.id.checkbox3);
		checkbox5 = findViewById(R.id.checkbox5);
		imageview1 = findViewById(R.id.imageview1);
		linear1_javascript = findViewById(R.id.linear1_javascript);
		linear1_overrideempt = findViewById(R.id.linear1_overrideempt);
		linear13 = findViewById(R.id.linear13);
		checkbox1 = findViewById(R.id.checkbox1);
		checkbox2 = findViewById(R.id.checkbox2);
		checkbox4 = findViewById(R.id.checkbox4);
		imageview5 = findViewById(R.id.imageview5);
		linear_version = findViewById(R.id.linear_version);
		linear_feed = findViewById(R.id.linear_feed);
		linear_source = findViewById(R.id.linear_source);
		version_visiable = findViewById(R.id.version_visiable);
		textviewendp2 = findViewById(R.id.textviewendp2);
		browservio_saver = getSharedPreferences("browservio.cfg", Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(this);
		dialog1 = new AlertDialog.Builder(this);
		dialogendp = new AlertDialog.Builder(this);
		dabt = new AlertDialog.Builder(this);
		drst = new AlertDialog.Builder(this);
		diazoomrestart = new AlertDialog.Builder(this);

		linear_general.setOnClickListener(_view -> {
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
		});
		
		linear_advenced.setOnClickListener(_view -> {
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
		});
		
		linear_about.setOnClickListener(_view -> {
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
		});
		
		linear1_search.setOnClickListener(_view -> {
			dialog1.setTitle(getResources().getString(R.string.search_engine));
			dialog1.setMessage(getResources().getString(R.string.search_engine_current, BrowservioSaverUtils.getPref(browservio_saver, "defaultSearch")));
			final EditText custom_se = new EditText(SettingsActivity.this); LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
			custom_se.setLayoutParams(lp2); dialog1.setView(custom_se);
			dialog1.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
				if (!custom_se.getText().toString().equals("") && custom_se.getText().toString().contains("{term}"))
				{
					BrowservioSaverUtils.setPref(browservio_saver, "defaultSearch", custom_se.getText().toString());
					textview5.setText(getResources().getString(R.string.search_engine_current, BrowservioSaverUtils.getPref(browservio_saver, "defaultSearch")));
				}
			});
			dialog1.setNegativeButton(android.R.string.cancel, (_dialog, _which) -> {

			});
			dialog1.setCancelable(false);
			dialog1.create().show();
		});
		
		linear1_homepage.setOnClickListener(_view -> {
			dialog.setTitle(getResources().getString(R.string.homepage));
			dialog.setMessage(getResources().getString(R.string.homepage_current, BrowservioSaverUtils.getPref(browservio_saver, "defaultHomePage")));
			final EditText custom_hp = new EditText(SettingsActivity.this); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
			custom_hp.setLayoutParams(lp); dialog.setView(custom_hp);
			dialog.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
				if (!custom_hp.getText().toString().equals(""))
				{
					BrowservioSaverUtils.setPref(browservio_saver, "defaultHomePage", custom_hp.getText().toString());
					textview5.setText(getResources().getString(R.string.homepage_current, BrowservioSaverUtils.getPref(browservio_saver, "defaultHomePage")));
				}
			});
			dialog.setNegativeButton(android.R.string.cancel, (_dialog, _which) -> {

			});
			dialog.setCancelable(false);
			dialog.create().show();
		});
		
		linear11.setOnClickListener(_view -> BrowservioBasicUtil.updateChkbox(checkbox3));

		linear_zoomkeys_b.setOnClickListener(_view -> BrowservioBasicUtil.updateChkbox(checkbox5));

		linearendp.setOnClickListener(_view -> {
				dialogendp.setTitle(getResources().getString(R.string.action_bar_endp));
				dialogendp.setMessage(BrowservioSaverUtils.getPref(browservio_saver, "endpPadding").concat("dp"));
				final EditText custom_hp2 = new EditText(SettingsActivity.this);
				LinearLayout.LayoutParams lpdnep = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				custom_hp2.setLayoutParams(lpdnep);
				dialogendp.setView(custom_hp2);
				dialogendp.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
					if (!custom_hp2.getText().toString().equals("")) {
						BrowservioSaverUtils.setPref(browservio_saver, "endpPadding", custom_hp2.getText().toString());
						textview5.setText(BrowservioSaverUtils.getPref(browservio_saver, "endpPadding").concat("dp"));
					}
				});
				dialogendp.setNegativeButton(android.R.string.cancel, (_dialog, _which) -> {

				});
				dialogendp.setCancelable(false);
				dialogendp.create().show();
		});

		linear1_b0.setOnClickListener(_view -> {
			drst.setTitle(getResources().getString(R.string.are_you_sure_q));
			drst.setMessage(getResources().getString(R.string.dialog_set_reset_message));
			drst.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
				BrowservioSaverUtils.setPref(browservio_saver, "isFirstLaunch", "1");
				finish();
			});
			drst.setNegativeButton(android.R.string.cancel, (_dialog, _which) -> {

			});
			drst.create().show();
		});
		
		checkbox3.setOnCheckedChangeListener((_param1, _param2) -> BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, "showBrowseBtn", _param2, false));
		
		checkbox5.setOnCheckedChangeListener((_param1, _param2) -> {
			BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, "showZoomKeys", _param2, false);
			if (!writingScreen) {
				diazoomrestart.setTitle(getResources().getString(R.string.restart_app_q));
				diazoomrestart.setMessage(getResources().getString(R.string.restart_app_qmsg));
				diazoomrestart.setPositiveButton(getResources().getString(R.string.restart_app_now), (_dialog, _which) -> {
					BrowservioSaverUtils.setPref(browservio_saver, "needRestart", "1");
					finish();
				});
				diazoomrestart.setNegativeButton(android.R.string.cancel, (_dialog, _which) -> {

				});
				diazoomrestart.create().show();
			}
		});
		
		linear1_javascript.setOnClickListener(_view -> BrowservioBasicUtil.updateChkbox(checkbox1));
		
		linear1_overrideempt.setOnClickListener(_view -> BrowservioBasicUtil.updateChkbox(checkbox2));
		
		linear13.setOnClickListener(_view -> BrowservioBasicUtil.updateChkbox(checkbox4));
		
		checkbox1.setOnCheckedChangeListener((_param1, _param2) -> BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, "isJavaScriptEnabled", _param2, false));
		
		checkbox2.setOnCheckedChangeListener((_param1, _param2) -> BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, "showFavicon", _param2, false));
		
		checkbox4.setOnCheckedChangeListener((_param1, _param2) -> BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, "showCustomError", _param2, false));
		
		linear_version.setOnClickListener(_view -> {
			dabt.setTitle(getResources().getString(R.string.version_info_title));
			dabt.setMessage(getResources().getString(R.string.version_info_message, BrowservioSaverUtils.getPref(browservio_saver, "versionName"), BrowservioSaverUtils.getPref(browservio_saver, "versionCodename"), BrowservioSaverUtils.getPref(browservio_saver, "versionTechnical"), BrowservioSaverUtils.getPref(browservio_saver, "versionFamily"), BrowservioSaverUtils.getPref(browservio_saver, "versionCode"), BrowservioSaverUtils.getPref(browservio_saver, "versionDate")));
			dabt.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {

			});
			dabt.create().show();
		});
		
		linear_feed.setOnClickListener(_view -> {
			BrowservioSaverUtils.setPref(browservio_saver, "needLoad", "1");
			BrowservioSaverUtils.setPref(browservio_saver, "needLoadUrl", getResources().getString(R.string.url_bug_report));
			finish();
		});
		
		linear_source.setOnClickListener(_view -> {
			BrowservioSaverUtils.setPref(browservio_saver, "needLoad", "1");
			BrowservioSaverUtils.setPref(browservio_saver, "needLoadUrl", getResources().getString(R.string.url_source_code));
			finish();
		});
	}
	private void initializeLogic() {
		setTitle(getResources().getString(R.string.settings));
		if (BrowservioSaverUtils.getPref(browservio_saver, "isJavaScriptEnabled").equals("1")) {
			checkbox1.setChecked(true);
		}
		if (BrowservioSaverUtils.getPref(browservio_saver, "showFavicon").equals("1")) {
			checkbox2.setChecked(true);
		}
		if (BrowservioSaverUtils.getPref(browservio_saver, "showBrowseBtn").equals("1")) {
			checkbox3.setChecked(true);
		}
		if (BrowservioSaverUtils.getPref(browservio_saver, "showZoomKeys").equals("1")) {
			checkbox5.setChecked(true);
		}
		if (BrowservioSaverUtils.getPref(browservio_saver, "showCustomError").equals("1")) {
			checkbox4.setChecked(true);
		}
		textview5.setText(getResources().getString(R.string.homepage_current, BrowservioSaverUtils.getPref(browservio_saver, "defaultHomePage")));
		textview9.setText(getResources().getString(R.string.search_engine_current, BrowservioSaverUtils.getPref(browservio_saver, "defaultSearch")));
		textviewendp2.setText(BrowservioSaverUtils.getPref(browservio_saver, "endpPadding").concat("dp"));
		version_visiable.setText(getResources().getString(R.string.app_name).concat(" ").concat(BrowservioSaverUtils.getPref(browservio_saver, "versionName")));
		writingScreen = false;
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		_setAllRipple(linear_general, "#ffffff", 6, 2);
		_setAllRipple(linear_advenced, "#ffffff", 6, 2);
		_setAllRipple(linear_about, "#ffffff", 6, 2);
		_setAllRipple(linear_version, "#ffffff", 6, 2);
		_setAllRipple(linear_feed, "#ffffff", 6, 2);
		_setAllRipple(linear1_search, "#ffffff", 6, 2);
		_setAllRipple(linear1_homepage, "#ffffff", 6, 2);
		_setAllRipple(linear11, "#ffffff", 6, 2);
		_setAllRipple(linear1_javascript, "#ffffff", 6, 2);
		_setAllRipple(linear1_overrideempt, "#ffffff", 6, 2);
		_setAllRipple(linear1_b0, "#ffffff", 6, 2);
		_setAllRipple(linear13, "#ffffff", 6, 2);
		_setAllRipple(linear_source, "#ffffff", 6, 2);
		_setAllRipple(linear_zoomkeys_b, "#ffffff", 6, 2);
		_setAllRipple(linearendp, "#ffffff", 6, 2);
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
	
	private void _setAllRipple(final View _view, final String _focus, final double _round, final double _stroke) {
		String colorControlHighlight = String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorControlHighlight)));
		_setRipple(_view);
		_rippleRoundStroke(_view, _focus, colorControlHighlight, _round, _stroke, colorControlHighlight);
		_ripple(_view, colorControlHighlight);
	}
}
