package tipz.browservio;

import static tipz.browservio.utils.BrowservioBasicUtil.RotateAlphaAnim;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DownloadManager;
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
import android.net.http.SslError;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.Menu;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewFeature;

import java.io.IOException;
import java.util.Objects;

import tipz.browservio.sharedprefs.AllPrefs;
import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;
import tipz.browservio.utils.BrowservioBasicUtil;
import tipz.browservio.utils.UrlUtils;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {

	private String page_before_error;
	private boolean defaultError = true;

	private AppCompatImageView browse;
	private AppCompatEditText UrlEdit;
	private ProgressBar MainProg;
	private WebView webview;
	private HorizontalScrollView hscroll_control;
	private AppCompatImageView reload;
	private AppCompatImageView desktop_switch;
	private AppCompatImageView favicon;

	private SharedPreferences browservio_saver;
	private AlertDialog.Builder dialog;
	private final Intent i = new Intent();
	private MediaPlayer mediaPlayer;
	private final ObjectAnimator fabAnimate = new ObjectAnimator();
	private final ObjectAnimator barAnimate = new ObjectAnimator();
	private SharedPreferences bookmarks;

	boolean bitmapUpdated_q = false;
	String UrlTitle;
	String beforeNextUrl;

	private String userAgent_full(String mid) {
		return "Mozilla/5.0 (".concat(mid).concat(") AppleWebKit/605.1.15 (KHTML, like Gecko) Safari/605.1.15 ".concat("Browservio/".concat(BuildConfig.VERSION_NAME).concat(BuildConfig.VERSION_TECHNICAL_EXTRA)));
	}

	/**
	 * An array used for intent filtering
	 */
	private static final String[] TypeSchemeMatch = {
			"text/html", "text/plain", "application/xhtml+xml", "application/vnd.wap.xhtml+xml",
			"http", "https", "ftp", "file"};

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);

		// Don't allow the app to start if the API_TYPE is unknown
		if (BrowservioBasicUtil.API_TYPE().equals("unknown"))
			finish();

		setContentView(R.layout.main);
		initialize();
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
				ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);

			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
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

	private void setDesktopMode(Boolean enableDesktop, String ua, Integer image, boolean noReload) {
		webview.getSettings().setUserAgentString(ua);
		webview.getSettings().setLoadWithOverviewMode(enableDesktop);
		webview.getSettings().setUseWideViewPort(enableDesktop);
		webview.setScrollBarStyle(enableDesktop ? WebView.SCROLLBARS_OUTSIDE_OVERLAY : View.SCROLLBARS_INSIDE_OVERLAY);
		desktop_switch.setImageResource(image);
		if (!noReload) {
			reload.performClick();
		}
	}

	private void deskModeSet(double mode, boolean noReload) {
		if (mode == 0) {
			setDesktopMode(false,
					userAgent_full("Linux; Android 12"),
					R.drawable.outline_smartphone_24,
					noReload);
		} else if (mode == 1) {
			setDesktopMode(true,
					userAgent_full("X11; Linux x86_64"),
					R.drawable.outline_desktop_windows_24,
					noReload);
		}
	}

	/**
	 * Initialize function
	 */
	private void initialize() {

		AppCompatImageView fab = findViewById(R.id.fab);
		browse = findViewById(R.id.browse);
		UrlEdit = findViewById(R.id.UrlEdit);
		MainProg = findViewById(R.id.MainProg);
		webview = findViewById(R.id.webview);
		hscroll_control = findViewById(R.id.hscroll_control);
		AppCompatImageView back = findViewById(R.id.back);
		AppCompatImageView forward = findViewById(R.id.forward);
		reload = findViewById(R.id.reload);
		AppCompatImageView homepage = findViewById(R.id.homepage);
		AppCompatImageView clear = findViewById(R.id.clear);
		AppCompatImageView share = findViewById(R.id.share);
		AppCompatImageView settings = findViewById(R.id.settings);
		AppCompatImageView history = findViewById(R.id.history);
		AppCompatImageView fav = findViewById(R.id.fav);
		AppCompatImageView exit = findViewById(R.id.exit);
		desktop_switch = findViewById(R.id.desktop_switch);
		favicon = findViewById(R.id.favicon);
		browservio_saver = getSharedPreferences(AllPrefs.browservio_saver, Activity.MODE_PRIVATE);
		dialog = new AlertDialog.Builder(this);
		bookmarks = getSharedPreferences(AllPrefs.bookmarks, Activity.MODE_PRIVATE);
		
		browse.setOnClickListener(_view -> _browservio_browse(Objects.requireNonNull(UrlEdit.getText()).toString()));

		back.setOnClickListener(_view -> {
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

		forward.setOnClickListener(_view -> {
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

		reload.setOnClickListener(_view -> {
			if (page_before_error.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_no_error)))) {
				if (!webview.getUrl().isEmpty()) {
					URLIdentify(webview.getUrl());
					webview.reload();
				}
			} else {
				URLIdentify(page_before_error);
				webview.loadUrl(page_before_error);
				page_before_error = getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_no_error));
			}
		});

		homepage.setOnClickListener(_view -> _browservio_browse(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.defaultHomePage)));

		desktop_switch.setOnClickListener(_view -> {
			PopupMenu popup1 = new PopupMenu(MainActivity.this, desktop_switch);
			Menu menu1 = popup1.getMenu();
			menu1.add(getResources().getString(R.string.linear_control_b3_desk));
			menu1.add(getResources().getString(R.string.linear_control_b3_mobi));
			menu1.add(getResources().getString(R.string.linear_control_b3_cus));
			popup1.setOnMenuItemClickListener(item -> {
				if (item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_desk))) {
					deskModeSet(1, false);
				} else if (item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_mobi))) {
					deskModeSet(0, false);
				} else if (item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_cus))) {
					dialog.setTitle(getResources().getString(R.string.ua));
					dialog.setMessage(getResources().getString(R.string.cus_ua_choose));
					final AppCompatEditText custom_ua = new AppCompatEditText(MainActivity.this);
					LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.MATCH_PARENT);
					custom_ua.setLayoutParams(lp);
					dialog.setView(custom_ua);
					dialog.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
						if (custom_ua.length() == 0) {
							deskModeSet(0, false);
						} else {
							webview.getSettings().setUserAgentString(Objects.requireNonNull(custom_ua.getText()).toString());
							reload.performClick();
						}
					});
					dialog.setNegativeButton(android.R.string.cancel, (_dialog, _which) -> deskModeSet(0, false));
					if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
						dialog.setOnDismissListener((_dialog) -> deskModeSet(0, false));
					dialog.create().show();
					desktop_switch.setImageResource(R.drawable.outline_mode_edit_24);
				}
				return false;
			});
			popup1.show();
		});

		clear.setOnClickListener(_view -> {
			PopupMenu popup2 = new PopupMenu(MainActivity.this, clear);
			Menu menu2 = popup2.getMenu();
			menu2.add(getResources().getString(R.string.clear, getResources().getString(R.string.cache)));
			menu2.add(getResources().getString(R.string.clear, getResources().getString(R.string.history)));
			menu2.add(getResources().getString(R.string.clear, getResources().getString(R.string.cookies)));
			menu2.add(getResources().getString(R.string.reset_btn));
			popup2.setOnMenuItemClickListener(item -> {
				if (item.getTitle().toString().contains(getResources().getString(R.string.cache))) {
					webview.clearCache(true);
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cache)));
					reload.performClick();
				} else if (item.getTitle().toString().contains(getResources().getString(R.string.history))) {
					webview.clearHistory();
					BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.history, BrowservioBasicUtil.EMPTY_STRING);
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.history)));
					reload.performClick();
				} else if (item.getTitle().toString().contains(getResources().getString(R.string.cookies))) {
					BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.history, BrowservioBasicUtil.EMPTY_STRING);
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cookies)));
					reload.performClick();
				} else if (item.getTitle().toString().equals(getResources().getString(R.string.reset_btn))) {
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.reset_complete));
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
					} else {
						String packageName = getApplicationContext().getPackageName();
						Runtime runtime = Runtime.getRuntime();
						try {
							runtime.exec("pm clear " + packageName);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				return false;
			});
			popup2.show();
		});

		share.setOnClickListener(_view -> {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_TEXT, webview.getUrl());
			startActivity(Intent.createChooser(i, getResources().getString(R.string.linear_control_b5_title)));
		});

		settings.setOnClickListener(_view -> {
			i.setClass(getApplicationContext(), SettingsActivity.class);
			startActivity(i);
		});

		history.setOnClickListener(_view -> {
			i.setClass(getApplicationContext(), HistoryActivity.class);
			startActivity(i);
		});

		fav.setOnClickListener(_view -> {
			PopupMenu popup3 = new PopupMenu(MainActivity.this, fav);
			Menu menu3 = popup3.getMenu();
			menu3.add(getResources().getString(R.string.add_dot));
			menu3.add(getResources().getString(R.string.fav));
			popup3.setOnMenuItemClickListener(item -> {
				if (item.getTitle().toString().equals(getResources().getString(R.string.add_dot))) {
					BrowservioSaverUtils.setPref(bookmarks, AllPrefs.bookmarked_count, BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmarked_count).isEmpty() ? "0" : String.valueOf((long) (Double.parseDouble(BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmarked_count)) + 1)));
					BrowservioSaverUtils.setPref(bookmarks, AllPrefs.bookmarked.concat(BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmarked_count)), webview.getUrl());
					BrowservioSaverUtils.setPref(bookmarks, AllPrefs.bookmarked.concat(BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmarked_count)).concat(AllPrefs.bookmarked_count_title), UrlTitle);
					BrowservioSaverUtils.setPref(bookmarks, AllPrefs.bookmarked.concat(BrowservioSaverUtils.getPref(bookmarks, AllPrefs.bookmarked_count)).concat(AllPrefs.bookmarked_count_show), "1");
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.saved_su));
				} else if (item.getTitle().toString().equals(getResources().getString(R.string.fav))) {
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

		exit.setOnClickListener(_view -> finish());

		favicon.setOnClickListener(_view -> {
			PopupMenu popup4 = new PopupMenu(MainActivity.this, favicon);
			Menu menu4 = popup4.getMenu();
			menu4.add(UrlTitle).setEnabled(false);
			menu4.add(getResources().getString(android.R.string.copy).concat(" ").concat(getResources().getString(R.string.favicondialog_title)));
			popup4.setOnMenuItemClickListener(item -> {
				if (item.getTitle().toString().equals(getResources().getString(android.R.string.copy).concat(" ").concat(getResources().getString(R.string.favicondialog_title)))) {
					((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", UrlTitle));
					BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.copied_clipboard));
					return true;
				}
				return false;
			});
			popup4.show();
		});
		
		fab.setOnClickListener(_view -> RotateAlphaAnim(fabAnimate, barAnimate, fab, hscroll_control));
	}

	private void initializeLogic() {
		/*
		 * Welcome to the Browservio (The Shrek Browser without Shrek)
		 * This browser was originally designed with Sketchware
		 * This project was started on Aug 13 2020
		 *
		 * sur wen real Shrek browser pls sand me sum
		 */
		webview.setWebViewClient(new WebClient());
		webview.setWebChromeClient(new ChromeWebClient());

		/* Code for detecting return key presses */
		UrlEdit.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
				_browservio_browse(Objects.requireNonNull(UrlEdit.getText()).toString());
				return true;
			}
			return false;
		});

		/* Page reloading stuff */
		page_before_error = getResources().getString(R.string.url_prefix,
				getResources().getString(R.string.url_suffix_no_error));

		deskModeSet(0, true); /* User agent init code */

		_downloadManager(webview); /* Start the download manager service */
		_browservio_browse(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.defaultHomePage)); /* Load default webpage */

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
					_browservio_browse(sharedText != null ? sharedText : BrowservioBasicUtil.EMPTY_STRING);
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
			if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_error))) || url.equals(getResources().getString(R.string.url_error_real))) {
				UrlEdit.setText(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_error)));
			} else if (!Objects.requireNonNull(UrlEdit.getText()).toString().equals(url)) {
				UrlEdit.setText(url);
				_history_saviour(url);
			}
		}
		public void onPageStarted (WebView view, String url, Bitmap icon) {
			favicon.setImageResource(R.drawable.outline_public_24); // Set favicon as default before getting real favicon
			UrlSet(url);
		}
		public void onPageFinished (WebView view, String url) {
			if (bitmapUpdated_q)
				favicon.setImageResource(R.drawable.outline_public_24);
			UrlSet(url);
			bitmapUpdated_q = false;
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
				android.webkit.CookieSyncManager.getInstance().sync();
			} else {
				CookieManager.getInstance().flush();
			}
		}
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			if (!defaultError) {
				page_before_error = beforeNextUrl;
				errorPage();
			}
		}
		@Override
		@RequiresApi(21)
		public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
			if (request.getUrl() != null)
				if (request.getUrl().toString().length() != 0)
					return false;
			if (BrowservioBasicUtil.appInstalledOrNot(getApplicationContext(), request.getUrl().toString())) {
				Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
				startActivity(intent);
			} else {
				BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.app_not_installed));
			}
			return true;
		}
		@SuppressLint("WebViewClientOnReceivedSslError")
		@Override
		public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			String message = "SSL Certificate error.";
			switch (error.getPrimaryError()) {
				case SslError.SSL_DATE_INVALID:
					message = "The date of the certificate is invalid.";
					break;
				case SslError.SSL_INVALID:
					message = "A generic SSL error occurred.";
					break;
				case SslError.SSL_EXPIRED:
					message = "The certificate has expired.";
					break;
				case SslError.SSL_IDMISMATCH:
					message = "The certificate hostname mismatch.";
					break;
				case SslError.SSL_NOTYETVALID:
					message = "The certificate is not yet valid.";
					break;
				case SslError.SSL_UNTRUSTED:
					message = "The certificate authority is not trusted.";
					break;
				case -1:
					message = "An unknown SSL error occurred.";
					break;
			}
			message += " Do you want to continue anyway?";

			builder.setTitle("SSL Certificate Error");
			builder.setMessage(message);
			builder.setPositiveButton(getResources().getString(android.R.string.ok), (dialog, which) -> handler.proceed());
			builder.setNegativeButton(getResources().getString(android.R.string.cancel), (dialog, which) -> handler.cancel());
			final AlertDialog dialog = builder.create();
			dialog.show();
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
			MainProg.setProgress(progress == 100 ? 0 : progress);
		}
		public void onReceivedIcon(WebView view, Bitmap icon) {
			bitmapUpdated_q = true;
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
		} else {
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

			// Let this downloaded file be scanned by MediaScanner - so that it can
			// show up in Gallery app, for example.
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
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
	private void _history_saviour(String url) {
		String history_data = BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.history);
		BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.history, (history_data.concat(history_data.isEmpty() ? BrowservioBasicUtil.EMPTY_STRING : BrowservioBasicUtil.LINE_SEPARATOR()).concat(url)));
	}

	/**
	 * Browservio Browse URL checker & loader
	 *
	 * @param url is for strings of URL to check and load
	 */
	private void _browservio_browse(String url) {
		beforeNextUrl = url;
		String checkedUrl = UrlUtils.UrlChecker(url, true, BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.defaultSearch));
		if (page_before_error.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_no_error)))) {
			// Load URL
			if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_error))) || url.equals(getResources().getString(R.string.url_error_real))) {
				URLIdentify(url);
			} else {
				URLIdentify(checkedUrl);
				webview.loadUrl(checkedUrl);
			}
		} else {
			URLIdentify(page_before_error);
			webview.loadUrl(page_before_error);
			page_before_error = getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_no_error));
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
		if (BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.needRestart))) {
			BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.needRestart, "0");
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

		AppCompatDelegate.setDefaultNightMode(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1 ? AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
			setDarkModeWebView(webview, false);
		} else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
			setDarkModeWebView(webview, powerManager.isPowerSaveMode());
		}

		if ((BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.isFirstLaunch).isEmpty() || BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.isFirstLaunch)))) {
			boolean isEqualToOneFirstLaunch = BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.isFirstLaunch));
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, AllPrefs.isJavaScriptEnabled, "1", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, AllPrefs.defaultHomePage, getResources().getString(R.string.url_default_homepage, BrowservioBasicUtil.EMPTY_STRING), isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, AllPrefs.defaultSearch, getResources().getString(R.string.url_default_homepage, getResources().getString(R.string.url_default_search_suffix)), isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, AllPrefs.showFavicon, "1", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, AllPrefs.showBrowseBtn, "0", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, AllPrefs.showZoomKeys, "0", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, AllPrefs.showCustomError, "1", isEqualToOneFirstLaunch);
			BrowservioSaverUtils.checkIfEmpty(browservio_saver, AllPrefs.isFirstLaunch, "0", isEqualToOneFirstLaunch);
		}

		// Settings check
		webview.getSettings().setJavaScriptEnabled(BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.isJavaScriptEnabled)));
		webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.isJavaScriptEnabled)));

		if (BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.needReload))) {
			reload.performClick();
			BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.needReload, "0");
		}

		browse.setVisibility(BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.showBrowseBtn)) ? View.VISIBLE : View.GONE);
		favicon.setVisibility(BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.showFavicon)) ? View.VISIBLE : View.GONE);
		webview.getSettings().setDisplayZoomControls(BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.showZoomKeys)));
		defaultError = !BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.showCustomError));

		// Need load
		if (BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.needLoad))) {
			_browservio_browse(BrowservioSaverUtils.getPref(browservio_saver, AllPrefs.needLoadUrl));
			BrowservioSaverUtils.setPref(browservio_saver, AllPrefs.needLoad, "0");
		}

		// HTML5 API flags
		webview.getSettings().setAppCacheEnabled(true);
		webview.getSettings().setDatabaseEnabled(true);
		webview.getSettings().setDomStorageEnabled(true);
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
	private void errorPage() {
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
	private void URLIdentify(String url) {
		if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_no_error)))) {
			throw new RuntimeException(getResources().getString(R.string.no_error_elog));
		}
		if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_error))) || url.equals(getResources().getString(R.string.url_error_real))) {
			errorPage();
		}
	}
}
