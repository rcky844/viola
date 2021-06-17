package tipz.browservio;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

import tipz.browservio.utils.BrowservioBasicUtil;
import tipz.browservio.utils.BrowservioSaverUtils;

public class SettingsActivity extends AppCompatActivity {

	private LinearLayout linear6;
	private LinearLayout linear5;
	private LinearLayout linear8;
	private ImageView imageview4;
	private TextView textview9;
	private TextView textview5;
	private CheckBox checkbox3;
	private CheckBox checkbox5;
	private ImageView imageview1;
	private CheckBox checkbox1;
	private CheckBox checkbox2;
	private CheckBox checkbox4;
	private ImageView imageview5;
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

	/**
	 * Initialize function
	 */
	private void initialize() {

		Toolbar _toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(_v -> onBackPressed());
		LinearLayout linear_general = findViewById(R.id.linear_general);
		linear6 = findViewById(R.id.linear6);
		LinearLayout linear_advenced = findViewById(R.id.linear_advenced);
		linear5 = findViewById(R.id.linear5);
		LinearLayout linear_about = findViewById(R.id.linear_about);
		linear8 = findViewById(R.id.linear8);
		imageview4 = findViewById(R.id.imageview4);
		LinearLayout linear1_search = findViewById(R.id.linear1_search);
		LinearLayout linear1_homepage = findViewById(R.id.linear1_homepage);
		LinearLayout linear11 = findViewById(R.id.linear11);
		LinearLayout linear_zoomkeys_b = findViewById(R.id.linear_zoomkeys_b);
		LinearLayout linearendp = findViewById(R.id.linearendp);
		LinearLayout linear1_b0 = findViewById(R.id.linear1_b0);
		findViewById(R.id.textview8);
		textview9 = findViewById(R.id.textview9);
		textview5 = findViewById(R.id.textview5);
		checkbox3 = findViewById(R.id.checkbox3);
		checkbox5 = findViewById(R.id.checkbox5);
		imageview1 = findViewById(R.id.imageview1);
		LinearLayout linear1_javascript = findViewById(R.id.linear1_javascript);
		LinearLayout linear1_overrideempt = findViewById(R.id.linear1_overrideempt);
		LinearLayout linear13 = findViewById(R.id.linear13);
		checkbox1 = findViewById(R.id.checkbox1);
		checkbox2 = findViewById(R.id.checkbox2);
		checkbox4 = findViewById(R.id.checkbox4);
		imageview5 = findViewById(R.id.imageview5);
		LinearLayout linear_version = findViewById(R.id.linear_version);
		LinearLayout linear_feed = findViewById(R.id.linear_feed);
		LinearLayout linear_source = findViewById(R.id.linear_source);
		TextView version_visiable = findViewById(R.id.version_visiable);
		textviewendp2 = findViewById(R.id.textviewendp2);
		browservio_saver = getSharedPreferences("browservio.cfg", Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(this);
		dialog1 = new AlertDialog.Builder(this);
		dialogendp = new AlertDialog.Builder(this);
		dabt = new AlertDialog.Builder(this);
		drst = new AlertDialog.Builder(this);
		diazoomrestart = new AlertDialog.Builder(this);

		// PackageManager for version info
		PackageManager manager = this.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		assert info != null;
		PackageInfo finalInfo = info;

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
			dialog1.setNegativeButton(android.R.string.cancel, null);
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
			dialog.setNegativeButton(android.R.string.cancel, null);
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
				dialogendp.setNegativeButton(android.R.string.cancel, null);
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
			drst.setNegativeButton(android.R.string.cancel, null);
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
				diazoomrestart.setNegativeButton(android.R.string.cancel, null);
				diazoomrestart.create().show();
			}
		});

		linear1_javascript.setOnClickListener(_view -> {
			BrowservioBasicUtil.updateChkbox(checkbox1);
			BrowservioSaverUtils.setPref(browservio_saver, "needReload", "1");
		});
		
		linear1_overrideempt.setOnClickListener(_view -> BrowservioBasicUtil.updateChkbox(checkbox2));
		
		linear13.setOnClickListener(_view -> BrowservioBasicUtil.updateChkbox(checkbox4));
		
		checkbox1.setOnCheckedChangeListener((_param1, _param2) -> BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, "isJavaScriptEnabled", _param2, false));
		
		checkbox2.setOnCheckedChangeListener((_param1, _param2) -> BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, "showFavicon", _param2, false));
		
		checkbox4.setOnCheckedChangeListener((_param1, _param2) -> BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, "showCustomError", _param2, false));

		linear_version.setOnClickListener(_view -> {
			dabt.setTitle(getResources().getString(R.string.version_info_title));
			dabt.setMessage(getResources().getString(R.string.version_info_message,
					finalInfo.versionName.concat(getResources().getString(R.string.versionName_p2)),
					getResources().getString(R.string.versionCodename),
					finalInfo.versionName.concat(getResources().getString(R.string.versionTechnical_p2)),
					finalInfo.versionName,
					String.valueOf(finalInfo.versionCode),
					getResources().getString(R.string.versionDate)));
			dabt.setPositiveButton(android.R.string.ok, null);
			dabt.create().show();
		});

		version_visiable.setText(getResources().getString(R.string.app_name).concat(" ").concat(finalInfo.versionName.concat(getResources().getString(R.string.versionName_p2))));
		
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
		writingScreen = false;
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
}
