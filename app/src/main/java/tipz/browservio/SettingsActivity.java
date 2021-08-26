package tipz.browservio;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import tipz.browservio.sharedprefs.AllPrefs;
import tipz.browservio.utils.BrowservioBasicUtil;
import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;

public class SettingsActivity extends AppCompatActivity {

	private LinearLayoutCompat linear6;
	private LinearLayoutCompat linear5;
	private LinearLayoutCompat linear8;
	private AppCompatImageView imageview4;
	private AppCompatTextView textview9;
	private AppCompatTextView textview5;
	private AppCompatCheckBox checkbox3;
	private AppCompatCheckBox checkbox5;
	private AppCompatImageView imageview1;
	private AppCompatCheckBox checkbox1;
	private AppCompatCheckBox checkbox2;
	private AppCompatCheckBox checkbox4;
	private AppCompatImageView imageview5;

	private SharedPreferences browservio_saver;
	private AlertDialog.Builder dialog;
	private final ObjectAnimator stackanim = new ObjectAnimator();
	private AlertDialog.Builder dialog1;
	private AlertDialog.Builder dabt;
	private AlertDialog.Builder drst;
	private AlertDialog.Builder diazoomrestart;
	private final ObjectAnimator Sherlockanimation = new ObjectAnimator();

	boolean writingScreen = true;
	long downloadID;
	final File apkFile = new File(Environment.getExternalStorageDirectory().toString().concat("/").concat(Environment.DIRECTORY_DOWNLOADS).concat("/browservio-update.apk"));

	final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			if (downloadID == id) {
				Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
				Uri photoURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", apkFile);
				installIntent.setData(photoURI);
				installIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
				installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				startActivity(installIntent);
			}
		}
	};

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.settings);
		initialize();
		initializeLogic();
		registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(onDownloadComplete);
	}

	private boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
		LinearLayoutCompat linear_general = findViewById(R.id.linear_general);
		linear6 = findViewById(R.id.linear6);
		LinearLayoutCompat linear_advenced = findViewById(R.id.linear_advenced);
		linear5 = findViewById(R.id.linear5);
		LinearLayoutCompat linear_about = findViewById(R.id.linear_about);
		linear8 = findViewById(R.id.linear8);
		imageview4 = findViewById(R.id.imageview4);
		LinearLayoutCompat linear1_search = findViewById(R.id.linear1_search);
		LinearLayoutCompat linear1_homepage = findViewById(R.id.linear1_homepage);
		LinearLayoutCompat linear11 = findViewById(R.id.linear11);
		LinearLayoutCompat linear_zoomkeys_b = findViewById(R.id.linear_zoomkeys_b);
		LinearLayoutCompat linear1_b0 = findViewById(R.id.linear1_b0);
		findViewById(R.id.textview8);
		textview9 = findViewById(R.id.textview9);
		textview5 = findViewById(R.id.textview5);
		checkbox3 = findViewById(R.id.checkbox3);
		checkbox5 = findViewById(R.id.checkbox5);
		imageview1 = findViewById(R.id.imageview1);
		LinearLayoutCompat linear1_javascript = findViewById(R.id.linear1_javascript);
		LinearLayoutCompat linear1_overrideempt = findViewById(R.id.linear1_overrideempt);
		LinearLayoutCompat linear13 = findViewById(R.id.linear13);
		checkbox1 = findViewById(R.id.checkbox1);
		checkbox2 = findViewById(R.id.checkbox2);
		checkbox4 = findViewById(R.id.checkbox4);
		imageview5 = findViewById(R.id.imageview5);
		LinearLayoutCompat linear_version = findViewById(R.id.linear_version);
		LinearLayoutCompat linear_feed = findViewById(R.id.linear_feed);
		LinearLayoutCompat linear_source = findViewById(R.id.linear_source);
		AppCompatTextView version_visiable = findViewById(R.id.version_visiable);
		browservio_saver = getSharedPreferences(AllPrefs.browservio_saver, Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(this);
		dialog1 = new AlertDialog.Builder(this);
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
				stackanim.setFloatValues(0, 180);
				Sherlockanimation.setFloatValues(1, 0);
				linear6.setVisibility(View.GONE);
			} else {
				linear6.setVisibility(View.VISIBLE);
				stackanim.setFloatValues(180, 0);
				Sherlockanimation.setFloatValues(0, 1);
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
				stackanim.setFloatValues(0, 180);
				Sherlockanimation.setFloatValues(1, 0);
				linear5.setVisibility(View.GONE);
			} else {
				linear5.setVisibility(View.VISIBLE);
				stackanim.setFloatValues(180, 0);
				Sherlockanimation.setFloatValues(0, 1);
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
				stackanim.setFloatValues(0, 180);
				Sherlockanimation.setFloatValues(1, 0);
				linear8.setVisibility(View.GONE);
			} else {
				linear8.setVisibility(View.VISIBLE);
				stackanim.setFloatValues(180, 0);
				Sherlockanimation.setFloatValues(0, 1);
			}
			stackanim.start();
			Sherlockanimation.start();
		});
		
		linear1_search.setOnClickListener(_view -> {
			dialog1.setTitle(getResources().getString(R.string.search_engine));
			dialog1.setMessage(getResources().getString(R.string.search_engine_current, BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.defaultSearch)));
			final AppCompatEditText custom_se = new AppCompatEditText(SettingsActivity.this); LinearLayoutCompat.LayoutParams lp2 = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.MATCH_PARENT);
			custom_se.setLayoutParams(lp2); dialog1.setView(custom_se);
			dialog1.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
				if (!Objects.requireNonNull(custom_se.getText()).toString().equals("") && custom_se.getText().toString().contains("{term}"))
				{
					BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.defaultSearch, custom_se.getText().toString());
					textview5.setText(getResources().getString(R.string.search_engine_current, BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.defaultSearch)));
				}
			});
			dialog1.setNegativeButton(android.R.string.cancel, null);
			dialog1.create().show();
		});
		
		linear1_homepage.setOnClickListener(_view -> {
			dialog.setTitle(getResources().getString(R.string.homepage));
			dialog.setMessage(getResources().getString(R.string.homepage_current, BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.defaultHomePage)));
			final AppCompatEditText custom_hp = new AppCompatEditText(SettingsActivity.this); LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.MATCH_PARENT);
			custom_hp.setLayoutParams(lp); dialog.setView(custom_hp);
			dialog.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
				if (!Objects.requireNonNull(custom_hp.getText()).toString().equals(""))
				{
					BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.defaultHomePage, custom_hp.getText().toString());
					textview5.setText(getResources().getString(R.string.homepage_current, BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.defaultHomePage)));
				}
			});
			dialog.setNegativeButton(android.R.string.cancel, null);
			dialog.create().show();
		});

		onClickChangeChkBox(linear11, checkbox3);
		onClickChangeChkBox(linear_zoomkeys_b, checkbox5);
		onClickChangeChkBox(linear1_overrideempt, checkbox2);
		onClickChangeChkBox(linear13, checkbox4);

		linear1_b0.setOnClickListener(_view -> {
			drst.setTitle(getResources().getString(R.string.are_you_sure_q));
			drst.setMessage(getResources().getString(R.string.dialog_set_reset_message));
			drst.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
				BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.isFirstLaunch, "1");
				finish();
			});
			drst.setNegativeButton(android.R.string.cancel, null);
			drst.create().show();
		});
		
		checkbox3.setOnCheckedChangeListener((_param1, _param2) -> BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, AllPrefs.showBrowseBtn, _param2, false));
		
		checkbox5.setOnCheckedChangeListener((_param1, _param2) -> {
			BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, AllPrefs.showZoomKeys, _param2, false);
			if (!writingScreen) {
				diazoomrestart.setTitle(getResources().getString(R.string.restart_app_q));
				diazoomrestart.setMessage(getResources().getString(R.string.restart_app_qmsg));
				diazoomrestart.setPositiveButton(getResources().getString(R.string.restart_app_now), (_dialog, _which) -> {
					BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.needRestart, "1");
					finish();
				});
				diazoomrestart.setNegativeButton(android.R.string.cancel, null);
				diazoomrestart.create().show();
			}
		});

		linear1_javascript.setOnClickListener(_view -> {
			BrowservioBasicUtil.updateChkbox(checkbox1);
			BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.needReload, "1");
		});
		
		checkbox1.setOnCheckedChangeListener((_param1, _param2) -> BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, AllPrefs.isJavaScriptEnabled, _param2, false));
		
		checkbox2.setOnCheckedChangeListener((_param1, _param2) -> BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, AllPrefs.showFavicon, _param2, false));
		
		checkbox4.setOnCheckedChangeListener((_param1, _param2) -> BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver, AllPrefs.showCustomError, _param2, false));

		linear_version.setOnClickListener(_view -> {
			View dialogView = this.getLayoutInflater().inflate(R.layout.about_dialog, null);
			AppCompatTextView dialog_text = dialogView.findViewById(R.id.dialog_text);
			AppCompatButton update_btn = dialogView.findViewById(R.id.update_btn);
			dialog_text.setText(getResources().getString(R.string.version_info_message,
					BuildConfig.VERSION_NAME.concat(BuildConfig.VERSION_NAME_EXTRA),
					BuildConfig.VERSION_CODENAME,
					BuildConfig.VERSION_NAME.concat(BuildConfig.VERSION_TECHNICAL_EXTRA),
					BuildConfig.VERSION_NAME,
					String.valueOf(BuildConfig.VERSION_CODE),
					BuildConfig.VERSION_BUILD_DATE,
					BuildConfig.BUILD_TYPE));
			update_btn.setOnClickListener(_update_btn -> {
				if (!isNetworkAvailable(getApplicationContext())) {
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.network_unavailable_toast));
				} else {
					new Thread() {
						@Override
						public void run() {
							String path = "https://gitlab.com/TipzTeam/browservio/-/raw/master/update_files/latest.cfg";
							URL u;
							try {
								u = new URL(path);
								HttpURLConnection c = (HttpURLConnection) u.openConnection();
								c.setRequestMethod("GET");
								c.connect();
								final ByteArrayOutputStream bo = new ByteArrayOutputStream();
								byte[] buffer = new byte[1024];
								int inputStreamTest = c.getInputStream().read(buffer);
								if (inputStreamTest >= 5) {
									bo.write(buffer);

									runOnUiThread(() -> {
										int position = 0;
										boolean isLatest = false;
										String[] array = bo.toString().split(System.lineSeparator());
										for (String obj : array) {
											if (position == 0) {
												if (Integer.parseInt(obj) <= BuildConfig.VERSION_CODE) {
													isLatest = true;
													BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.version_latest_toast));
												}
											}
											if (position == 1 && !isLatest) {
												BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.new_update_detect_toast));
												if (apkFile.delete() || !apkFile.exists()) {
													DownloadManager.Request request = new DownloadManager.Request(Uri.parse(obj));
													request.allowScanningByMediaScanner();
													request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
													request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "browservio-update.apk");
													DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
													downloadID = dm.enqueue(request);
												} else {
													BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.update_down_failed_toast));
												}
											}
											position += 1;
										}
										try {
											bo.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
									});
								} else {
									BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.update_down_failed_toast));
								}
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
					}.start();
				}
			});
			dabt.setView(dialogView);
			dabt.setPositiveButton(android.R.string.ok, null);
			dabt.create().show();
		});

		version_visiable.setText(getResources().getString(R.string.app_name).concat(" ").concat(BuildConfig.VERSION_NAME.concat(BuildConfig.VERSION_NAME_EXTRA)));

		linear_feed.setOnClickListener(_view -> needLoad(getResources().getString(R.string.url_source_code,
				getResources().getString(R.string.url_bug_report_subfix))));
		
		linear_source.setOnClickListener(_view -> needLoad(getResources().getString(R.string.url_source_code,
				"")));
	}

	private void needLoad(String Url) {
		BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.needLoad, "1");
		BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.needLoadUrl, Url);
		finish();
	}

	private void initializeLogic() {
		checkIfPrefIntIsTrue("isJavaScriptEnabled", checkbox1);
		checkIfPrefIntIsTrue("showFavicon", checkbox2);
		checkIfPrefIntIsTrue("showBrowseBtn", checkbox3);
		checkIfPrefIntIsTrue("showCustomError", checkbox4);
		checkIfPrefIntIsTrue("showZoomKeys", checkbox5);
		textview5.setText(getResources().getString(R.string.homepage_current, BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.defaultHomePage)));
		textview9.setText(getResources().getString(R.string.search_engine_current, BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.defaultSearch)));
		writingScreen = false;
	}

	private void checkIfPrefIntIsTrue(String tag, AppCompatCheckBox checkBox) {
		checkBox.setChecked(BrowservioSaverUtils.getPref(browservio_saver, tag).equals("1"));
	}

	public void onClickChangeChkBox(View view, AppCompatCheckBox chkbox) {
		view.setOnClickListener(_view -> BrowservioBasicUtil.updateChkbox(chkbox));
	}
}
