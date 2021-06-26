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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.Menu;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewFeature;

import java.util.Objects;

import tipz.browservio.utils.BrowservioBasicUtil;
import tipz.browservio.utils.BrowservioSaverUtils;
import tipz.browservio.utils.UrlUtils;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {

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
	private ImageView desktop_switch;
	private ImageView favicon;

	private SharedPreferences browservio_saver;
	private AlertDialog.Builder dialog;
	private final Intent i = new Intent();
	private MediaPlayer mediaPlayer;
	private final ObjectAnimator fabanim = new ObjectAnimator();
	private final ObjectAnimator baranim = new ObjectAnimator();
	private SharedPreferences bookmarks;

	boolean bitmipUpdated_q = false;
	String checkedUrl;
	String UrlTitle;

	/**
	 * An array used for intent filtering
	 */
	private static final String[] TypeSchemeMatch = {
			"text/html", "text/plain", "application/xhtml+xml", "application/vnd.wap.xhtml+xml",
			"http", "https", "ftp", "file"};

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

	private void setDesktopMode(Boolean enableDesktop, String ua, Integer image) {
		webview.getSettings().setUserAgentString(ua);
		webview.getSettings().setLoadWithOverviewMode(enableDesktop);
		webview.getSettings().setUseWideViewPort(enableDesktop);
		webview.setScrollBarStyle(enableDesktop ? WebView.SCROLLBARS_OUTSIDE_OVERLAY : View.SCROLLBARS_INSIDE_OVERLAY);
		desktop_switch.setImageResource(image);
		linear_control_b2.performClick();
	}

	private void deskModeSet(double mode) {
		if (mode == 0) {
			setDesktopMode(false,
					Objects.requireNonNull(System.getProperty("http.agent")).concat(" ").concat(getResources().getString(R.string.webUserAgent_end)),
					R.drawable.outline_smartphone_24);
		} else if (mode == 1) {
			setDesktopMode(true,
					getResources().getString(R.string.webUserAgent, getResources().getString(R.string.webUserAgent_end)),
					R.drawable.outline_desktop_windows_24);
		}

	}

	/**
	 * Initialize function
	 */
	private void initialize() {

		ImageView fab = findViewById(R.id.fab);
		browse = findViewById(R.id.browse);
		urledit = findViewById(R.id.urledit);
		progmain = findViewById(R.id.progmain);
		webview = findViewById(R.id.webview);
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
		desktop_switch = findViewById(R.id.desktop_switch);
		favicon = findViewById(R.id.favicon);
		browservio_saver = getSharedPreferences("browservio.cfg", Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(this);
		bookmarks = getSharedPreferences("bookmarks.cfg", Activity.MODE_PRIVATE);
		
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
					_URLindentify(webview.getUrl());
					webview.reload();
				}
			} else {
				_URLindentify(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_error)));
				webview.loadUrl(page_before_error);
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
					deskModeSet(1);
				} else if (item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_mobi))) {
					deskModeSet(0);
				} else if (item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_cus))) {
					dialog.setTitle(getResources().getString(R.string.ua));
					dialog.setMessage(getResources().getString(R.string.cus_ua_choose));
					final EditText custom_ua = new EditText(MainActivity.this);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
					custom_ua.setLayoutParams(lp);
					dialog.setView(custom_ua);
					dialog.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
						if (custom_ua.length() == 0) {
							deskModeSet(0);
						} else {
							webview.getSettings().setUserAgentString(custom_ua.getText().toString());
							linear_control_b2.performClick();
						}
					});
					dialog.setNegativeButton(android.R.string.cancel, (_dialog, _which) -> deskModeSet(0));
					dialog.setOnDismissListener((_dialog) -> deskModeSet(0));
					dialog.create().show();
					desktop_switch.setImageResource(R.drawable.outline_mode_edit_24);
				}
				return false;
			});
			popup1.show();
		});
		
		linear_control_b4.setOnClickListener(_view -> {
			PopupMenu popup2 = new PopupMenu(MainActivity.this, linear_control_b4);
			Menu menu2 = popup2.getMenu();
			menu2.add(getResources().getString(R.string.clear, getResources().getString(R.string.cache)));
			menu2.add(getResources().getString(R.string.clear, getResources().getString(R.string.history)));
			menu2.add(getResources().getString(R.string.clear, getResources().getString(R.string.cookies)));
			menu2.add(getResources().getString(R.string.clear, getResources().getString(R.string.all)));
			popup2.setOnMenuItemClickListener(item -> {
				if (item.getTitle().toString().contains(getResources().getString(R.string.cache))) {
					mainClearCache();
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cache)));
					linear_control_b2.performClick();
				} else if (item.getTitle().toString().contains(getResources().getString(R.string.history))) {
					mainClearHistory();
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.history)));
					linear_control_b2.performClick();
				} else if (item.getTitle().toString().contains(getResources().getString(R.string.cookies))) {
					mainClearCookies();
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cookies)));
					linear_control_b2.performClick();
				} else if (item.getTitle().toString().contains(getResources().getString(R.string.all))) {
					mainClearCache();
					mainClearHistory();
					mainClearCookies();
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
			i.setClass(getApplicationContext(), HistoryActivity.class);
			startActivity(i);
		});
		
		linear_control_b10.setOnClickListener(_view -> {
			PopupMenu popup3 = new PopupMenu(MainActivity.this, linear_control_b10);
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
					BrowservioSaverUtils.setPref(bookmarks, "bookmark_".concat(BrowservioSaverUtils.getPref(bookmarks, "bookmarked_count")).concat("_title"), UrlTitle);
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
			PopupMenu popup4 = new PopupMenu(MainActivity.this, favicon);
			Menu menu4 = popup4.getMenu();
			menu4.add(UrlTitle).setEnabled(false);
			menu4.add(getResources().getString(android.R.string.copy).concat(" ").concat(getResources().getString(R.string.favicondialog_title)));
			popup4.setOnMenuItemClickListener(item -> {
				if (item.getTitle().toString().equals(getResources().getString(android.R.string.copy))) {
					((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", UrlTitle));
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.copied_clipboard));
					return true;
				}
				return false;
			});
			popup4.show();
		});
		
		fab.setOnClickListener(_view -> {
			fabanim.setTarget(fab);
			baranim.setTarget(hscroll_control);
			fabanim.setPropertyName("rotation");
			baranim.setPropertyName("alpha");
			fabanim.setDuration(250);
			baranim.setDuration(250);
			if (linear_control.getVisibility() == View.VISIBLE) {
				fabanim.setFloatValues((float)(0), (float)(180));
				baranim.setFloatValues((float)(1), (float)(0));
				linear_control.setVisibility(View.GONE);
			} else {
				linear_control.setVisibility(View.VISIBLE);
				fabanim.setFloatValues((float)(180), (float)(0));
				baranim.setFloatValues((float)(0), (float)(1));
			}
			baranim.start();
			fabanim.start();
		});
	}

	private void mainClearCache() {
		webview.clearCache(true);
	}

	private void mainClearHistory() {
		webview.clearHistory();
		BrowservioSaverUtils.setPref(browservio_saver, "history", "");
	}

	private void mainClearCookies() {
		CookieManager.getInstance().removeAllCookies(null);
		CookieManager.getInstance().flush();
	}

	private void initializeLogic() {
		/*
		 * Welcome to the Browservio (The Shrek Browser without Shrek)
		 * This browser was originally designed with Sketchware
		 * This project was started on Aug 13 2020
		 *
		 * sur wen real Sherk browser plssssssssssssssssssssssssssssss
		 */
		webview.setWebViewClient(new WebClient());
		webview.setWebChromeClient(new ChromeWebClient());

		/* Code for detecting return key presses */
		urledit.setOnEditorActionListener((v, actionId, event) -> {
			  if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
					  _browservio_browse(urledit.getText().toString());
					  return true;
				  }
			  return false;
			});

		/* Page reloading stuff */
		page_before_error = getResources().getString(R.string.url_prefix,
				getResources().getString(R.string.url_subfix_no_error));

		/* User agent init code */
		webview.getSettings().setUserAgentString(
				Objects.requireNonNull(System.getProperty("http.agent"))
				.concat(" ")
				.concat(getResources().getString(R.string.webUserAgent_end)));

		_downloadManager(webview); /* Start the download manager service */
		_browservio_browse(BrowservioSaverUtils.getPref(browservio_saver, "defaultHomePage")); /* Load default webpage */

		/* zoom related stuff - From SCMPNews project */
		webview.getSettings().setSupportZoom(true);
		webview.getSettings().setBuiltInZoomControls(true);

		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); /* Setting the style of the scroll bar */

		/*
		 * Getting information from intents, either from
		 * sharing menu or default browser launch.
		 */
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		String scheme = intent.getScheme();

		if (Intent.ACTION_SEND.equals(action) /* From share menu */
			|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) { /* NFC sharing */
			if (type != null) {
				if ("text/plain".equals(type)) {
					String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
					_browservio_browse(sharedText != null ? sharedText : "");
				}
			}
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) { /* From default browser */
			for (String match : TypeSchemeMatch) {
				if (match.equals(type) || match.equals(scheme)) {
					Uri uri = getIntent().getData();
					_browservio_browse(uri.toString());
				}
			}
		}
	}

	/**
	 * WebViewClient
	 */
	public class WebClient extends WebViewClientCompat {
		private void UrlSet(String url) {
			if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_error))) || url.equals(getResources().getString(R.string.url_error_real))) {
				urledit.setText(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_error)));
			} else if (!urledit.getText().toString().equals(url)) {
				urledit.setText(url);
				_history_saviour();
			}
		}
		public void onPageStarted (WebView view, String url, Bitmap icon) {
			favicon.setImageResource(R.drawable.outline_public_24); // Set favicon as default before getting real favicon
			UrlSet(url);
		}
		public void onPageFinished (WebView view, String url) {
			if (bitmipUpdated_q) {
				favicon.setImageResource(R.drawable.outline_public_24);
			}
			UrlSet(url);
			bitmipUpdated_q = false;
			CookieManager.getInstance().flush();
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
				BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.app_not_installed));
			}
			return true;
		}
	}

	/**
	 * WebChromeClient
	 */
	public class ChromeWebClient extends WebChromeClient {
		private View mCustomView;
		private WebChromeClient.CustomViewCallback mCustomViewCallback;

		// Initially mOriginalOrientation is set to Landscape
		private int mOriginalOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		private int mOriginalSystemUiVisibility;

		// Constructor for ChromeWebClient
		public ChromeWebClient() {}
		
		@Override
		public Bitmap getDefaultVideoPoster() {
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
		}
		
		public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback viewCallback) {
			if (mCustomView != null) {
				onHideCustomView();
				return;
			}
			mCustomView = paramView;
			mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
			// When CustomView is shown screen orientation changes to mOriginalOrientation (Landscape).
			setRequestedOrientation(mOriginalOrientation);
			// After that mOriginalOrientation is set to portrait.
			mOriginalOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			mCustomViewCallback = viewCallback;
			((FrameLayout) getWindow().getDecorView()).addView(mCustomView, new FrameLayout.LayoutParams(-1, -1));
			getWindow().getDecorView().setSystemUiVisibility(3846);
		}
		
		public void onHideCustomView() {
			((FrameLayout) getWindow().getDecorView()).removeView(mCustomView);
			mCustomView = null;
			getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
			// When CustomView is hidden, screen orientation is set to mOriginalOrientation (portrait).
			setRequestedOrientation(mOriginalOrientation);
			// After that mOriginalOrientation is set to landscape.
			mOriginalOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			mCustomViewCallback.onCustomViewHidden();
			mCustomViewCallback = null;
		}

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
			if (BrowservioSaverUtils.getPref(browservio_saver, "needLoadUrlIsApk").equals("1")) {
				mimetype = "application/vnd.android.package-archive";
				BrowservioSaverUtils.setPref(browservio_saver, "needLoadUrlIsApk", "0");
			}
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
			BrowservioSaverUtils.setPref(browservio_saver, "history", BrowservioSaverUtils.getPref(browservio_saver, "history").concat(System.lineSeparator()).concat(webview.getUrl()));
		}
	}

	/**
	 * Browservio Browse URL checker & loader
	 *
	 * @param url is for strings of URL to check and load
	 */
	private void _browservio_browse(String url) {
		checkedUrl = UrlUtils.UrlChecker(url, true, BrowservioSaverUtils.getPref(browservio_saver, "defaultSearch"));
		if (page_before_error.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_no_error)))) {
			// Load URL
			if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_error))) || url.equals(getResources().getString(R.string.url_error_real))) {
				_URLindentify(url);
			} else {
				_URLindentify(checkedUrl);
				webview.loadUrl(checkedUrl);
				_history_saviour();
			}
		} else {
			_URLindentify(page_before_error);
			webview.loadUrl(page_before_error);
			page_before_error = getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_no_error));
			_history_saviour();
		}
	}

	/**
	 * Set Dark Mode for WebView
	 *
	 * @param webview WebView to set
	 * @param turnOn Turn on or off the WebView dark mode
	 */
	private void setDarkModeWebView(WebView webview, Boolean turnOn) {
		if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
			WebSettingsCompat.setForceDark(webview.getSettings(), turnOn ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF);
		}
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
				setDarkModeWebView(webview, true);
				break;
			case Configuration.UI_MODE_NIGHT_UNDEFINED:
			case Configuration.UI_MODE_NIGHT_NO:
				setDarkModeWebView(webview, false);
				break;
		}

		AppCompatDelegate.setDefaultNightMode(android.os.Build.VERSION.SDK_INT <= 27 ? AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (android.os.Build.VERSION.SDK_INT <= 27) {
			setDarkModeWebView(webview, powerManager.isPowerSaveMode());
		}

		if (BrowservioSaverUtils.getPref(browservio_saver, "isFirstLaunch").equals("1")) {
			final ProgressDialog prog = new ProgressDialog(MainActivity.this);
			prog.setMax(100);
			prog.setMessage(getResources().getString(R.string.dialog_resetting_message));
			prog.setIndeterminate(true);
			prog.setCancelable(false);
			prog.show();
			mainClearHistory();
			mainClearCache();
			mainClearCookies();
			restart_app();
			BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.reset_complete));
		}
		if ((BrowservioSaverUtils.getPref(browservio_saver, "isFirstLaunch").equals("") || BrowservioSaverUtils.getPref(browservio_saver, "isFirstLaunch").equals("1"))) {
			boolean isEqualToOneFirstLaunch = BrowservioSaverUtils.getPref(browservio_saver, "isFirstLaunch").equals("1");
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "isJavaScriptEnabled", "1", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "defaultHomePage", getResources().getString(R.string.url_default_homepage, ""), isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "defaultSearch", getResources().getString(R.string.url_default_homepage, getResources().getString(R.string.url_default_search_subfix)), isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "showFavicon", "1", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "showBrowseBtn", "0", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "showZoomKeys", "0", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "showCustomError", "1", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, "isFirstLaunch", "0", isEqualToOneFirstLaunch);
		}
		// Settings check
		webview.getSettings().setJavaScriptEnabled(BrowservioSaverUtils.getPref(browservio_saver, "isJavaScriptEnabled").equals("1"));
		webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(BrowservioSaverUtils.getPref(browservio_saver, "isJavaScriptEnabled").equals("1"));

		if (BrowservioSaverUtils.getPref(browservio_saver, "needReload").equals("1")) {
			linear_control_b2.performClick();
			BrowservioSaverUtils.setPref(browservio_saver, "needReload", "0");
		}

		browse.setVisibility(BrowservioSaverUtils.getPref(browservio_saver, "showBrowseBtn").equals("1") ? View.VISIBLE : View.GONE);
		favicon.setVisibility(BrowservioSaverUtils.getPref(browservio_saver, "showFavicon").equals("1") ? View.VISIBLE : View.GONE);
		webview.getSettings().setDisplayZoomControls(BrowservioSaverUtils.getPref(browservio_saver, "showZoomKeys").equals("1"));
		defaulterror = !BrowservioSaverUtils.getPref(browservio_saver, "showCustomError").equals("1");

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

	/**
	 * Restarts the application
	 */
	private void restart_app() {
		Intent i = getIntent();
		finish();
		startActivity(i);
	}

	/**
	 * Error Page Loader
	 */
	private void _errorpage () {
		webview.loadUrl(getResources().getString(R.string.url_error_real));
		// Media player
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.release();
			}
		}
		mediaPlayer = new MediaPlayer();
		mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.chord);
		mediaPlayer.start();
	}

	/**
	 * URL identify module
	 *
	 * This module/function identifies a supplied
	 * URL to check for it's nature.
	 *
	 * @param url is supplied for the url to check
	 */
	private void _URLindentify(String url) {
		if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_no_error)))) {
			throw new RuntimeException(getResources().getString(R.string.no_error_elog));
		}
		if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_subfix_error))) || url.equals(getResources().getString(R.string.url_error_real))) {
			_errorpage();
		}
	}
}
