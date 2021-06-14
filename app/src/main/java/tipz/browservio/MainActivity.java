package tipz.browservio;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import tipz.browservio.Utils.BrowservioSaverUtils;
import tipz.browservio.Utils.BrowservioBasicUtil;
import tipz.browservio.Utils.UrlUtils;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {
	
	private FloatingActionButton _fab;
	private double desktop = 0;
	private double last_desktop = 0;
	private String page_before_error = "";
	private boolean defaulterror = true;

	private ImageView browse;
	private EditText urledit;
	private ProgressBar progmain;
	private WebView webview;
	private HorizontalScrollView hscroll_control;
	private LinearLayout linear_control;
	private LinearLayout linear_control_b2;
	private LinearLayout linear_control_b3;
	private LinearLayout linear_control_endp;
	private ImageView desktop_switch;
	private ImageView favicon;

	private SharedPreferences browservio_saver;
	private AlertDialog.Builder dialog;
	private AlertDialog.Builder favicondialog;
	private final Intent i = new Intent();
	private MediaPlayer mediaPlayer;
	private final ObjectAnimator baranim = new ObjectAnimator();
	private final ObjectAnimator barrrrrr = new ObjectAnimator();
	private AlertDialog.Builder dhist;
	private SharedPreferences bookmarks;
	private final int[] resID = { R.raw.win98_error };
	int from, to, times, songPosition, timesPosition=0;

	boolean bitmipUpdated_q = false;
	String checkedUrl;
	String UrlTitle;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize();
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
		}
		else {
			initializeLogic();
		}
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}

	private void initialize() {
		
		_fab = findViewById(R.id._fab);

		browse = findViewById(R.id.browse);
		urledit = findViewById(R.id.urledit);
		progmain = findViewById(R.id.progmain);
		webview = findViewById(R.id.webview);
		webview.getSettings().setSupportZoom(true);
		hscroll_control = findViewById(R.id.hscroll_control);
		linear_control = findViewById(R.id.linear_control);
		LinearLayout linear_control_b0 = findViewById(R.id.linear_control_b0);
		LinearLayout linear_control_b1 = findViewById(R.id.linear_control_b1);
		linear_control_b2 = findViewById(R.id.linear_control_b2);
		LinearLayout linear_control_b7 = findViewById(R.id.linear_control_b7);
		linear_control_b3 = findViewById(R.id.linear_control_b3);
		LinearLayout linear_control_b4 = findViewById(R.id.linear_control_b4);
		LinearLayout linear_control_b5 = findViewById(R.id.linear_control_b5);
		LinearLayout linear_control_b6 = findViewById(R.id.linear_control_b6);
		LinearLayout linear_control_b9 = findViewById(R.id.linear_control_b9);
		LinearLayout linear_control_b10 = findViewById(R.id.linear_control_b10);
		LinearLayout linear_control_b8 = findViewById(R.id.linear_control_b8);
		linear_control_endp = findViewById(R.id.linear_control_endp);
		desktop_switch = findViewById(R.id.desktop_switch);
		favicon = findViewById(R.id.favicon);
		browservio_saver = getSharedPreferences("browservio.cfg", Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(this);
		dhist = new AlertDialog.Builder(this);
		favicondialog = new AlertDialog.Builder(this);
		bookmarks = getSharedPreferences("bookmarks.cfg", Activity.MODE_PRIVATE);
		mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.win98_error);
		
		browse.setOnClickListener(_view -> _browservio_browse(urledit.getText().toString()));

		linear_control_b0.setOnClickListener(_view -> {
			// on forward being clicked, either go forward in history
			if (webview.canGoBack()) {
				// can go back
				webview.goBack();
			}
			else {
				// cannot go backwards
				BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.error_already_page, getResources().getString(R.string.first)));
			}
		});
		
		linear_control_b1.setOnClickListener(_view -> {
			// on forward being clicked, either go forward in history
			if (webview.canGoForward()) {
				// can go forward
				webview.goForward();
			}
			else {
				// cannot go forward
				BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.error_already_page, getResources().getString(R.string.last)));
			}
		});
		
		linear_control_b2.setOnClickListener(_view -> {
			if (page_before_error.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_no_error)))) {
				if (!webview.getUrl().equals("")) {
					webview.reload();
				}
			} else {
				webview.loadUrl(page_before_error);
				urledit.setText(page_before_error);
				page_before_error = getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_no_error));
			}
		});
		
		linear_control_b7.setOnClickListener(_view -> _browservio_browse(BrowservioSaverUtils.getPref(browservio_saver, "defaultHomePage")));
		
		linear_control_b3.setOnClickListener(_view -> {
			PopupMenu popup1 = new PopupMenu(MainActivity.this, linear_control_b3);
			Menu menu1 = popup1.getMenu();
			menu1.add(getResources().getString(R.string.linear_control_b3_desk));
			menu1.add(getResources().getString(R.string.linear_control_b3_mobi));
			menu1.add(getResources().getString(R.string.linear_control_b3_cus));
			popup1.setOnMenuItemClickListener(item -> {
				if (item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_desk))) {
					webview.getSettings().setUserAgentString(getResources().getString(R.string.webUserAgent, getResources().getString(R.string.webUserAgent_end)));
					last_desktop = desktop;
					desktop = 1;
					desktop_switch.setImageResource(R.drawable.ic_desktop_black);
					linear_control_b2.performClick();
				} else if (item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_mobi))) {
					webview.getSettings().setUserAgentString(Objects.requireNonNull(System.getProperty("http.agent")).concat(" ").concat(getResources().getString(R.string.webUserAgent_end)));
					last_desktop = desktop;
					desktop = 0;
					desktop_switch.setImageResource(R.drawable.ic_smartphone_black);
					linear_control_b2.performClick();
				} else if (item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_cus))) {
					dialog.setTitle(getResources().getString(R.string.ua));
					dialog.setMessage(getResources().getString(R.string.cus_ua_choose));
					final EditText custom_ua = new EditText(MainActivity.this);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
					custom_ua.setLayoutParams(lp);
					dialog.setView(custom_ua);
					dialog.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
						if (custom_ua.length() == 0) {
							webview.getSettings().setUserAgentString(Objects.requireNonNull(System.getProperty("http.agent")).concat(" ").concat(getResources().getString(R.string.webUserAgent_end)));
							linear_control_b2.performClick();
							desktop_switch.setImageResource(R.drawable.ic_smartphone_black);
							desktop = 0;
						} else {
							webview.getSettings().setUserAgentString(custom_ua.getText().toString());
							linear_control_b2.performClick();
						}
					});
					dialog.setNegativeButton(android.R.string.cancel, (_dialog, _which) -> {
						if (last_desktop == 0) {
							desktop = last_desktop;
							desktop_switch.setImageResource(R.drawable.ic_smartphone_black);
						} else {
							if (last_desktop == 1) {
								desktop = last_desktop;
								desktop_switch.setImageResource(R.drawable.ic_desktop_black);
							} else {
								if (last_desktop == 2) {
									desktop = last_desktop;
									desktop_switch.setImageResource(R.drawable.ic_edit_black);
								} else {
									throw new RuntimeException(getResources().getString(R.string.last_desktop_range_elog));
								}
							}
						}
					});
					dialog.setCancelable(false);
					dialog.create().show();
					last_desktop = desktop;
					desktop = 2;
					desktop_switch.setImageResource(R.drawable.ic_edit_black);
				}
				return false;
			});
			popup1.show();
		});
		
		linear_control_b4.setOnClickListener(_view -> {
			PopupMenu popup2 = new PopupMenu(MainActivity.this, linear_control_b3);
			Menu menu2 = popup2.getMenu();
			menu2.add(getResources().getString(R.string.clear, getResources().getString(R.string.cache)));
			menu2.add(getResources().getString(R.string.clear, getResources().getString(R.string.history)));
			menu2.add(getResources().getString(R.string.clear, getResources().getString(R.string.cookies)));
			menu2.add(getResources().getString(R.string.clear, getResources().getString(R.string.all)));
			popup2.setOnMenuItemClickListener(item -> {
				if (item.getTitle().toString().contains(getResources().getString(R.string.cache))) {
					webview.clearCache(true);
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cache)));
					linear_control_b2.performClick();
				} else if (item.getTitle().toString().contains(getResources().getString(R.string.history))) {
					webview.clearHistory();
					BrowservioSaverUtils.setPref(browservio_saver, "history", "");
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.history)));
					linear_control_b2.performClick();
				} else if (item.getTitle().toString().contains(getResources().getString(R.string.cookies))) {
					CookieManager.getInstance().removeAllCookies(null);
					CookieManager.getInstance().flush();
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cookies)));
					linear_control_b2.performClick();
				} else if (item.getTitle().toString().contains(getResources().getString(R.string.all))) {
					webview.clearCache(true);
					webview.clearHistory();
					BrowservioSaverUtils.setPref(browservio_saver, "history", "");
					CookieManager.getInstance().removeAllCookies(null);
								CookieManager.getInstance().flush();
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.all)));
					linear_control_b2.performClick();
				}
				return false;
			});
			popup2.show();
		});
		
		linear_control_b5.setOnClickListener(_view -> {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_TEXT, webview.getUrl());
			startActivity(Intent.createChooser(i, getResources().getString(R.string.linear_control_b5_title)));
		});
		
		linear_control_b6.setOnClickListener(_view -> {
			i.setClass(getApplicationContext(), SettingsActivity.class);
			startActivity(i);
		});
		
		linear_control_b9.setOnClickListener(_view -> {
			if (!BrowservioSaverUtils.getPref(browservio_saver, "history").equals("")) {
				dhist.setTitle(getResources().getString(R.string.history));
				dhist.setMessage(BrowservioSaverUtils.getPref(browservio_saver, "history"));
				dhist.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {

				});
				dhist.setNeutralButton(android.R.string.copy, (_dialog, _which) -> {
					getApplicationContext();
					((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", BrowservioSaverUtils.getPref(browservio_saver, "history")));
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.copied_clipboard));
				});
				dhist.setNegativeButton(getResources().getString(R.string.clear, ""), (_dialog, _which) -> {
					BrowservioSaverUtils.setPref(browservio_saver, "history", "");
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.history)));
				});
				dhist.create().show();
			}
			else {
				BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.hist_empty));
			}
		});
		
		linear_control_b10.setOnClickListener(_view -> {
			PopupMenu popup3 = new PopupMenu(MainActivity.this, linear_control_b3);
			Menu menu3 = popup3.getMenu();
			menu3.add(getResources().getString(R.string.add_dot));
			menu3.add(getResources().getString(R.string.favs));
			popup3.setOnMenuItemClickListener(item -> {
				if (item.getTitle().toString().equals(getResources().getString(R.string.add_dot))) {
					if (BrowservioSaverUtils.getPref(bookmarks, "bookmarked_count").equals("")) {
						BrowservioSaverUtils.setPref(bookmarks, "bookmarked_count", "0");
					} else {
						BrowservioSaverUtils.setPref(bookmarks, "bookmarked_count", String.valueOf((long) (Double.parseDouble(BrowservioSaverUtils.getPref(bookmarks, "bookmarked_count")) + 1)));
					}
					BrowservioSaverUtils.setPref(bookmarks, "bookmark_".concat(BrowservioSaverUtils.getPref(bookmarks, "bookmarked_count")), webview.getUrl());
					BrowservioSaverUtils.setPref(bookmarks, "bookmark_".concat(BrowservioSaverUtils.getPref(bookmarks, "bookmarked_count")).concat("_show"), "1");
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.saved_su));
				} else if (item.getTitle().toString().equals(getResources().getString(R.string.favs))) {
					if (bookmarks.getAll().size() == 0) {
						BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.fav_list_empty));
					} else {
						i.setClass(getApplicationContext(), FavActivity.class);
						startActivity(i);
					}

				}
				return false;
			});
			popup3.show();
		});
		
		linear_control_b8.setOnClickListener(_view -> finish());

		favicon.setOnClickListener(_view -> {
			favicondialog.setTitle(getResources().getString(R.string.favicondialog_title));
			favicondialog.setMessage(UrlTitle);
			favicondialog.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {

			});
			favicondialog.setNeutralButton(android.R.string.copy, (_dialog, _which) -> {
				getApplicationContext();
				((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", UrlTitle));
				BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.copied_clipboard));
			});
			/*favicondialog.setNegativeButton(getResources().getString(, ""), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface _dialog, int _which) {
				}
			});*/
			favicondialog.create().show();
		});
		
		_fab.setOnClickListener(_view -> {
			baranim.setTarget(_fab);
			barrrrrr.setTarget(hscroll_control);
			baranim.setPropertyName("rotation");
			barrrrrr.setPropertyName("alpha");
			baranim.setDuration(250);
			barrrrrr.setDuration(250);
			if (linear_control.getVisibility() == View.VISIBLE) {
				baranim.setFloatValues((float)(180), (float)(0));
				barrrrrr.setFloatValues((float)(1), (float)(0));
				linear_control.setVisibility(View.GONE);
			} else {
				linear_control.setVisibility(View.VISIBLE);
				baranim.setFloatValues((float)(0), (float)(180));
				barrrrrr.setFloatValues((float)(0), (float)(1));
			}
			baranim.start();
			barrrrrr.start();
		});
	}
	private void initializeLogic() {
		// Welcome to the Browservio (Shrek without Shrek browser)
		// This browser was originally designed with Sketchware
		// This project was started on Aug 13 2020
		// sur wen Sherk browser
		setTitle(getResources().getString(R.string.app_name));
		webview.setWebChromeClient(new CustomWebClient());
		// Keyboard press = browse
		urledit.setOnEditorActionListener((v, actionId, event) -> {
			  if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
					  _browservio_browse(urledit.getText().toString());
					  return true;
				  }
			  return false;
			});
		// Page stuff
		page_before_error = getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_no_error));
		// desktopMode init code
		webview.getSettings().setUserAgentString(Objects.requireNonNull(System.getProperty("http.agent")).concat(" ").concat(getResources().getString(R.string.webUserAgent_end)));
		desktop = 0;
		last_desktop = desktop;
		// Start downloadManager service
		_downloadManager(webview);
		// Set default fab stat
		linear_control.setVisibility(View.GONE);
		// Custom error page
		webview.setWebViewClient(new WebViewClient() {
			public void onPageStarted (WebView view, String url, Bitmap favicon) {
				if (!urledit.getText().toString().equals(url)) {
					urledit.setText(url);
				}
			}
			public void onPageFinished (WebView view, String url) {
				if (bitmipUpdated_q) {
					favicon.setImageResource(R.drawable.outline_public_24);
				}
				if (!urledit.getText().toString().equals(url)) {
					urledit.setText(url);
				}
				bitmipUpdated_q = false;
				CookieSyncManager.getInstance().sync();
			}
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				if (!defaulterror) {
					page_before_error = urledit.getText().toString();
					_errorpage();
				}
			}
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(URLUtil.isNetworkUrl(url)) {
					return false;
				}
				if (BrowservioBasicUtil.appInstalledOrNot(getApplicationContext(), url)) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(intent);
				} else {
					BrowservioBasicUtil.showMessage(getApplicationContext(), "Application is not installed.");
				}
				return true;
			}
		});
		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if (progress == 100) {
					progmain.setProgress(0);
				} else {
					progmain.setProgress(progress);
				}
			}
			public void onReceivedIcon(WebView view, Bitmap icon) {
				bitmipUpdated_q = true;
				favicon.setImageBitmap(icon);
			}
			public void onReceivedTitle (WebView view, String title) {
				UrlTitle = title;
			}
			public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
				callback.invoke(origin, true, false);
			}
		});
		if (!BrowservioSaverUtils.getPref(browservio_saver, "defaultHomePage").equals("")) {
			// Load default homepage.
			if (BrowservioSaverUtils.getPref(browservio_saver, "defaultHomePage").contains(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_no_error)))) {
				BrowservioSaverUtils.setPref(browservio_saver, "defaultHomePage", getResources().getString(R.string.url_default_homepage));
			}
			_browservio_browse(BrowservioSaverUtils.getPref(browservio_saver, "defaultHomePage"));
		}
		else {
			BrowservioSaverUtils.setPref(browservio_saver, "defaultHomePage", getResources().getString(R.string.url_default_homepage));
		}
		// zoom stuff - From SCMPNews
		webview.getSettings().setBuiltInZoomControls(true);

		// Share stuff
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		String scheme = intent.getScheme();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
				_browservio_browse(sharedText != null ? sharedText : "");
			}
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			if ("text/html".equals(type) || "text/plain".equals(type) || "application/xhtml+xml".equals(type) || "application/vnd.wap.xhtml+xml".equals(type) || "http".equals(scheme) || "https".equals(scheme) || "ftp".equals(scheme) || "file".equals(scheme)) {
				Uri uri = getIntent().getData();
				_browservio_browse(uri.toString());
			}
		}
	}
	
	public class CustomWebClient extends WebChromeClient {
		private View mCustomView;
		private WebChromeClient.CustomViewCallback mCustomViewCallback;

		// Initially mOriginalOrientation is set to Landscape
		private int mOriginalOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		private int mOriginalSystemUiVisibility;
		
		// Constructor for CustomWebClient
		public CustomWebClient() {}
		
		@Override
		public Bitmap getDefaultVideoPoster() {
            return BitmapFactory.decodeResource(MainActivity.this.getApplicationContext().getResources(), 2130837573); }
		
		public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback viewCallback) {
			if (this.mCustomView != null) {
				onHideCustomView();
				return; }
			this.mCustomView = paramView;
			this.mOriginalSystemUiVisibility = MainActivity.this.getWindow().getDecorView().getSystemUiVisibility();
			// When CustomView is shown screen orientation changes to mOriginalOrientation (Landscape).
			MainActivity.this.setRequestedOrientation(this.mOriginalOrientation);
			// After that mOriginalOrientation is set to portrait.
			this.mOriginalOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			this.mCustomViewCallback = viewCallback; ((FrameLayout)MainActivity.this.getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1)); MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(3846);
		}
		
		public void onHideCustomView() {
			((FrameLayout)MainActivity.this.getWindow().getDecorView()).removeView(this.mCustomView);
			this.mCustomView = null;
			MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
			// When CustomView is hidden, screen orientation is set to mOriginalOrientation (portrait).
			MainActivity.this.setRequestedOrientation(this.mOriginalOrientation);
			// After that mOriginalOrientation is set to landscape.
			this.mOriginalOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; this.mCustomViewCallback.onCustomViewHidden();
			this.mCustomViewCallback = null;
		}
	}

	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
	}
	
	@Override
	public void onBackPressed() {
		// onBackPressed to go back in history or finish activity
		if (webview.canGoBack()) {
			// Go back
			webview.goBack();
		}
		else {
			// Finish activity
			finish();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		_configChecker();
	}

	/**
	 * Download Manager
	 *
	 * Module to monitor downloads from a webview.
	 *
	 * @param webview to monitor
	 */
	private void _downloadManager (final WebView webview) {
		webview.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.allowScanningByMediaScanner();
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // Notify client once download is completed!
			final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
			DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
			dm.enqueue(request);
		});
	}

	/**
	 * History Saviour
	 *
	 * Module to save history into a SharedPref.
	 */
	private void _history_saviour() {
		if (BrowservioSaverUtils.getPref(browservio_saver, "history").equals("")) {
			BrowservioSaverUtils.setPref(browservio_saver, "history", BrowservioSaverUtils.getPref(browservio_saver, "history").concat(webview.getUrl()));
		} else {
			BrowservioSaverUtils.setPref(browservio_saver, "history", BrowservioSaverUtils.getPref(browservio_saver, "history").concat("\n").concat(webview.getUrl()));
		}
	}

	/**
	 * Config Checker
	 *
	 * Used to check if anything has been changed
	 * after resume of restart.
	 */
	private void _browservio_browse(String url) {
		checkedUrl = UrlUtils.UrlChecker(url, true, browservio_saver, "defaultSearch");
		if (page_before_error.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_no_error)))) {
			// Load URL
			if (url.startsWith(getResources().getString(R.string.url_prefix, ""))) {
				_URLindentify(url);
			} else {
				_URLindentify(checkedUrl);
				urledit.setText(checkedUrl);
				webview.loadUrl(checkedUrl);
			}
		} else {
			_URLindentify(page_before_error);
			urledit.setText(page_before_error);
			webview.loadUrl(page_before_error);
			page_before_error = getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_no_error));
		}
		_history_saviour();
	}

	/**
	 * Config Checker
	 *
	 * Used to check if anything has been changed
	 * after resume of restart.
	 */
	private void _configChecker() {
		// Restart code
		if (BrowservioSaverUtils.getPref(browservio_saver, "needRestart").equals("1")) {
			BrowservioSaverUtils.setPref(browservio_saver, "needRestart", "0");
			restart_app();
		}

		// Dark mode
		switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
			case Configuration.UI_MODE_NIGHT_YES:
				// Darken web content in WebView
				if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
					WebSettingsCompat.setForceDark(webview.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
				}
				break;
			case Configuration.UI_MODE_NIGHT_UNDEFINED:
			case Configuration.UI_MODE_NIGHT_NO:
				// Darken web content in WebView
				if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
					WebSettingsCompat.setForceDark(webview.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
				}
				break;
		}

		// PackageManager for version info
		PackageManager manager = this.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		// Get version info for dialog
        assert info != null;
		BrowservioSaverUtils.setPref(browservio_saver, "versionFamily", info.versionName);
		BrowservioSaverUtils.setPref(browservio_saver, "versionName", info.versionName.concat(getResources().getString(R.string.versionName_p2)));
		BrowservioSaverUtils.setPref(browservio_saver, "versionTechnical", info.versionName.concat(getResources().getString(R.string.versionTechnical_p2)));
		BrowservioSaverUtils.setPref(browservio_saver, "versionCodename", getResources().getString(R.string.versionCodename));
		BrowservioSaverUtils.setPref(browservio_saver, "versionCode", String.valueOf(info.versionCode));
		BrowservioSaverUtils.setPref(browservio_saver, "versionDate", getResources().getString(R.string.versionDate));
		if (BrowservioSaverUtils.getPref(browservio_saver, "isFirstLaunch").equals("1")) {
			final ProgressDialog prog = new ProgressDialog(MainActivity.this);
			prog.setMax(100);
			prog.setMessage(getResources().getString(R.string.dialog_resetting_message));
			prog.setIndeterminate(true);
			prog.setCancelable(false);
			prog.show();
			CookieManager.getInstance().removeAllCookies(null);
			CookieManager.getInstance().flush();
			BrowservioSaverUtils.setPref(browservio_saver, "history", "");
			webview.clearCache(true);
			webview.clearHistory();
			restart_app();
			BrowservioBasicUtil.showMessage(getApplicationContext(), "Reset successfully!");
		}
		if (BrowservioSaverUtils.getPref(browservio_saver, "defaultHomePage").equals("")) {
			Intent i = getIntent();
			finish();
			startActivity(i);
		}
		if ((BrowservioSaverUtils.getPref(browservio_saver, "isFirstLaunch").equals("") || BrowservioSaverUtils.getPref(browservio_saver, "isFirstLaunch").equals("1"))) {
			boolean isEqualToOneFirstLaunch = BrowservioSaverUtils.getPref(browservio_saver, "isFirstLaunch").equals("1");
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "isJavaScriptEnabled", "1", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "defaultHomePage", getResources().getString(R.string.url_default_homepage, ""), isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "defaultSearch", getResources().getString(R.string.url_default_homepage, getResources().getString(R.string.url_default_search_subfix)), isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "endpPadding", "500", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "showFavicon", "1", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "showBrowseBtn", "0", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "showZoomKeys", "0", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "showCustomError", "1", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "isFirstLaunch", "0", isEqualToOneFirstLaunch);
		}
		// Settings check
		if (BrowservioSaverUtils.getPref(browservio_saver, "isJavaScriptEnabled").equals("1")) {
			webview.getSettings().setJavaScriptEnabled(true);
			webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		}
		else {
			webview.getSettings().setJavaScriptEnabled(false);
			webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		}

		if (BrowservioSaverUtils.getPref(browservio_saver, "needReload").equals("1")) {
			linear_control_b2.performClick();
			BrowservioSaverUtils.setPref(browservio_saver, "needReload", "0");
		}

		if (BrowservioSaverUtils.getPref(browservio_saver, "showBrowseBtn").equals("1")) {
			browse.setVisibility(View.VISIBLE);
		}
		else {
			browse.setVisibility(View.GONE);
		}
		if (BrowservioSaverUtils.getPref(browservio_saver, "showFavicon").equals("1")) {
			favicon.setVisibility(View.VISIBLE);
		}
		else {
			favicon.setVisibility(View.GONE);
		}
		// Code to modify the action bar end padding
		linear_control_endp.getLayoutParams().width = Integer.parseInt(BrowservioSaverUtils.getPref(browservio_saver, "endpPadding"));
		linear_control_endp.requestLayout();

		defaulterror = !BrowservioSaverUtils.getPref(browservio_saver, "showCustomError").equals("1");
		webview.getSettings().setDisplayZoomControls(BrowservioSaverUtils.getPref(browservio_saver, "showZoomKeys").equals("1"));
		BrowservioSaverUtils.setPref(browservio_saver, "lastVersionCode", BrowservioSaverUtils.getPref(browservio_saver, "versionCode"));

		// Need load
		if (BrowservioSaverUtils.getPref(browservio_saver, "needLoad").equals("1")) {
			_browservio_browse(BrowservioSaverUtils.getPref(browservio_saver, "needLoadUrl"));
			BrowservioSaverUtils.setPref(browservio_saver, "needLoad", "0");
		}

		// HTML5 API flags
		webview.getSettings().setAppCacheEnabled(true);
		webview.getSettings().setDatabaseEnabled(true);
		webview.getSettings().setDomStorageEnabled(true);

		// Location
		webview.getSettings().setGeolocationDatabasePath(getApplicationContext().getFilesDir().getPath());
	}

	private void restart_app() {
		Intent i = getIntent();
		finish();
		startActivity(i);
	}

	private void playSong(int position) {
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.release();
			}
		}
		mediaPlayer = new MediaPlayer();
		mediaPlayer = MediaPlayer.create(getApplicationContext(), resID[position]);
		mediaPlayer.start();
		mediaPlayer.setOnCompletionListener(mp -> {

			if (timesPosition<times){
				if (songPosition <= to){
					songPosition = songPosition + 1;
				} else {
					songPosition = from;
					songPosition = timesPosition + timesPosition + 1;
				}
				playSong(songPosition);
			}
		});
	}

	private void _errorpage () {
		webview.loadUrl(getResources().getString(R.string.url_error_real));
		//Setup media player (rewrote 200815-1307)
		playSong(songPosition);
		urledit.setText(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_error)));
	}

	private void _URLindentify(String url) {
		if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_no_error)))) {
			throw new RuntimeException(getResources().getString(R.string.no_error_elog));
		}
		if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_error))) || urledit.getText().toString().equals(getResources().getString(R.string.url_error_real))) {
			_errorpage();
		}
	}
}
