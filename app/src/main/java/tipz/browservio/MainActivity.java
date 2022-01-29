package tipz.browservio;

import static tipz.browservio.fav.FavApi.bookmarks;
import static tipz.browservio.history.HistoryApi.historyPref;
import static tipz.browservio.sharedprefs.utils.BrowservioSaverUtils.browservio_saver;
import static tipz.browservio.utils.BrowservioBasicUtil.RotateAlphaAnim;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import tipz.browservio.history.HistoryInit;
import tipz.browservio.history.HistoryReader;
import tipz.browservio.sharedprefs.AllPrefs;
import tipz.browservio.sharedprefs.FirstTimeInit;
import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;
import tipz.browservio.utils.BrowservioBasicUtil;
import tipz.browservio.utils.UrlUtils;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {

    private String pageBeforeError;
    private boolean defaultError = true;

    private MaterialAutoCompleteTextView UrlEdit;
    private ProgressBar MainProg;
    private ProgressBar faviconProgressBar;
    private ImageView fab;
    private WebView webview;
    private HorizontalScrollView actionBar;
    private AppCompatImageView reload;
    private AppCompatImageView desktop_switch;
    private AppCompatImageView favicon;

    private MediaPlayer mediaPlayer;
    private final ObjectAnimator fabAnimate = new ObjectAnimator();
    private final ObjectAnimator barAnimate = new ObjectAnimator();

    private String UrlTitle;
    private String previousUrl;

    private final static int FILECHOOSER_RESULTCODE = 1;
    private ValueCallback<Uri[]> mUploadMessage;

    private String userAgentFull(String mid) {
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
        setContentView(R.layout.main);
        initialize();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        } else {
            initializeLogic();
        }

        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM)
                .enabled(true)
                .showErrorDetails(true)
                .showRestartButton(true)
                .logErrorOnRestart(true)
                .trackActivities(true)
                .minTimeBetweenCrashesMs(2000)
                .restartActivity(MainActivity.class)
                .errorActivity(null)
                .apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            initializeLogic();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage || intent == null || resultCode != RESULT_OK)
                return;

            Uri[] result = null;
            String dataString = intent.getDataString();

            if (dataString != null)
                result = new Uri[]{Uri.parse(dataString)};

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
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

    private void setDeskMode(double mode, boolean noReload) {
        if (mode == 0) {
            setDesktopMode(false,
                    userAgentFull("Linux; Android 12"),
                    R.drawable.smartphone,
                    noReload);
        } else if (mode == 1) {
            setDesktopMode(true,
                    userAgentFull("X11; Linux x86_64"),
                    R.drawable.desktop,
                    noReload);
        }
    }

    /**
     * Initialize function
     */
    private void initialize() {

        fab = findViewById(R.id.fab);
        UrlEdit = findViewById(R.id.UrlEdit);
        MainProg = findViewById(R.id.MainProg);
        faviconProgressBar = findViewById(R.id.faviconProgressBar);
        webview = findViewById(R.id.webview);
        actionBar = findViewById(R.id.actionBar);
        AppCompatImageView back = findViewById(R.id.back);
        AppCompatImageView forward = findViewById(R.id.forward);
        reload = findViewById(R.id.reload);
        AppCompatImageView homepage = findViewById(R.id.homepage);
        AppCompatImageView new_tab = findViewById(R.id.new_tab);
        AppCompatImageView clear = findViewById(R.id.clear);
        AppCompatImageView share = findViewById(R.id.share);
        AppCompatImageView settings = findViewById(R.id.settings);
        AppCompatImageView history = findViewById(R.id.history);
        AppCompatImageView fav = findViewById(R.id.fav);
        AppCompatImageView exit = findViewById(R.id.exit);
        desktop_switch = findViewById(R.id.desktop_switch);
        favicon = findViewById(R.id.favicon);

		/*
		  On back button being clicked, go backwards in history
		 */
        back.setOnClickListener(_view -> {
            if (webview.canGoBack()) // can go back
                webview.goBack();
            else // cannot go backwards
                BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.error_already_page, getResources().getString(R.string.first)));
        });

		/*
		  On forward button being clicked, go forward in history
		 */
        forward.setOnClickListener(_view -> {
            if (webview.canGoForward()) // can go forward
                webview.goForward();
            else // cannot go forward
                BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.error_already_page, getResources().getString(R.string.last)));
        });

        reload.setOnClickListener(_view -> {
            if (pageBeforeError.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_no_error)))
                    && !webview.getUrl().isEmpty()) {
                URLIdentify(webview.getUrl());
                webview.reload();
            } else {
                URLIdentify(pageBeforeError);
                webview.loadUrl(UrlUtils.UrlChecker(webview.getUrl()));
                pageBeforeError = getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_no_error));
            }
        });

        homepage.setOnClickListener(_view -> browservioBrowse(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.defaultHomePage)));

        desktop_switch.setOnClickListener(_view -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, desktop_switch);
            Menu menu = popupMenu.getMenu();
            menu.add(getResources().getString(R.string.linear_control_b3_desk));
            menu.add(getResources().getString(R.string.linear_control_b3_mobi));
            menu.add(getResources().getString(R.string.linear_control_b3_cus));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_desk)))
                    setDeskMode(1, false);
                else if (item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_mobi)))
                    setDeskMode(0, false);
                else if (item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_cus))) {
                    final LayoutInflater layoutInflater = LayoutInflater.from(this);
                    @SuppressLint("InflateParams") final View root = layoutInflater.inflate(R.layout.dialog_edittext, null);
                    final AppCompatEditText customUserAgent = root.findViewById(R.id.edittext);
                    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
                    dialog.setTitle(getResources().getString(R.string.ua))
                            .setMessage(getResources().getString(R.string.cus_ua_choose))
                            .setView(root)
                            .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                                if (customUserAgent.length() == 0) {
                                    setDeskMode(0, false);
                                } else {
                                    desktop_switch.setImageResource(R.drawable.custom);
                                    webview.getSettings().setUserAgentString(Objects.requireNonNull(customUserAgent.getText()).toString());
                                    reload.performClick();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create().show();
                }
                return false;
            });
            popupMenu.show();
        });

        new_tab.setOnClickListener(_view -> {
            Intent i = new Intent(this, MainActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            else
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            startActivity(i);
        });

        clear.setOnClickListener(_view -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, clear);
            Menu menu = popupMenu.getMenu();
            menu.add(getResources().getString(R.string.clear, getResources().getString(R.string.cache)));
            menu.add(getResources().getString(R.string.clear, getResources().getString(R.string.history)));
            menu.add(getResources().getString(R.string.clear, getResources().getString(R.string.cookies)));
            menu.add(getResources().getString(R.string.reset_btn));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().contains(getResources().getString(R.string.cache))) {
                    webview.clearCache(true);
                    BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cache)));
                    reload.performClick();
                } else if (item.getTitle().toString().contains(getResources().getString(R.string.history))) {
                    webview.clearHistory();
                    HistoryReader.clear(historyPref(MainActivity.this));
                    BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.history)));
                    reload.performClick();
                } else if (item.getTitle().toString().contains(getResources().getString(R.string.cookies))) {
                    HistoryReader.clear(historyPref(MainActivity.this));
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
            popupMenu.show();
        });

        share.setOnClickListener(_view -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, webview.getUrl());
            startActivity(Intent.createChooser(i, getResources().getString(R.string.linear_control_b5_title)));
        });

        settings.setOnClickListener(_view -> {
            Intent intent = new Intent(this, NewSettingsActivity.class);
            mGetNeedLoad.launch(intent);
        });

        history.setOnClickListener(_view -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            mGetNeedLoad.launch(intent);
        });

        fav.setOnClickListener(_view -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, fav);
            Menu menu = popupMenu.getMenu();
            menu.add(getResources().getString(R.string.add_dot));
            menu.add(getResources().getString(R.string.fav));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equals(getResources().getString(R.string.add_dot))) {
                    BrowservioSaverUtils.setPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count, BrowservioSaverUtils.getPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count).isEmpty() ? "0" : String.valueOf((long) (Double.parseDouble(BrowservioSaverUtils.getPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count)) + 1)));
                    BrowservioSaverUtils.setPref(bookmarks(MainActivity.this), AllPrefs.bookmarked.concat(BrowservioSaverUtils.getPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count)), webview.getUrl());
                    BrowservioSaverUtils.setPref(bookmarks(MainActivity.this), AllPrefs.bookmarked.concat(BrowservioSaverUtils.getPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count)).concat(AllPrefs.bookmarked_count_title), UrlTitle);
                    BrowservioSaverUtils.setPref(bookmarks(MainActivity.this), AllPrefs.bookmarked.concat(BrowservioSaverUtils.getPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count)).concat(AllPrefs.bookmarked_count_show), "1");
                    BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.saved_su));
                } else if (item.getTitle().toString().equals(getResources().getString(R.string.fav))) {
                    if (bookmarks(MainActivity.this).getAll().size() == 0) {
                        BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.fav_list_empty));
                    } else {
                        Intent intent = new Intent(this, FavActivity.class);
                        mGetNeedLoad.launch(intent);
                    }
                }
                return false;
            });
            popupMenu.show();
        });

        exit.setOnClickListener(_view -> finish());

        favicon.setOnClickListener(_view -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, favicon);
            Menu menu = popupMenu.getMenu();
            menu.add(UrlTitle).setEnabled(false);
            menu.add(getResources().getString(android.R.string.copy).concat(" ").concat(getResources().getString(R.string.favicondialog_title)));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equals(getResources().getString(android.R.string.copy).concat(" ").concat(getResources().getString(R.string.favicondialog_title)))) {
                    ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", UrlTitle));
                    BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.copied_clipboard));
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        faviconProgressBar.setOnClickListener(_view -> favicon.performClick());

        fab.setOnClickListener(_view -> RotateAlphaAnim(fabAnimate, barAnimate, fab, actionBar));
    }

    /**
     * Welcome to the Browservio (The Shrek Browser)
     * This browser was originally designed with Sketchware
     * This project was started on Aug 13 2020
     * <p>
     * sur wen reel Sherk brower pls sand meme sum
     */
    private void initializeLogic() {
        webview.setWebViewClient(new WebClient());
        webview.setWebChromeClient(new ChromeWebClient());

        /* Code for detecting return key presses */
        UrlEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                browservioBrowse(Objects.requireNonNull(UrlEdit.getText()).toString());
                return true;
            }
            return false;
        });

        UrlEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (text.toString().isEmpty())
                    return;
                new Thread() {
                    @Override
                    public void run() {
                        String path = "http://suggestqueries.google.com/complete/search?client=firefox&q=".concat(text.toString());
                        URL u;
                        try {
                            u = new URL(path);
                            HttpURLConnection c = (HttpURLConnection) u.openConnection();
                            c.setRequestMethod("GET");
                            c.connect();
                            final ByteArrayOutputStream bo = new ByteArrayOutputStream();
                            byte[] buffer = new byte[65536];
                            c.getInputStream().read(buffer);
                            bo.write(buffer);
                            MainActivity.this.runOnUiThread(() -> {
                                try {
                                    JSONArray jsonArray = new JSONArray(bo.toString());

                                    jsonArray = jsonArray.optJSONArray(1);
                                    if (jsonArray == null) {
                                        throw new RuntimeException("jsonArray is null.");
                                    }
                                    final int MAX_RESULTS = 10;
                                    ArrayList<String> result = new ArrayList<>(Math.min(jsonArray.length(), MAX_RESULTS));
                                    for (int i = 0; i < jsonArray.length() && result.size() < MAX_RESULTS; i++) {
                                        String s = jsonArray.optString(i);
                                        if (s != null && !s.isEmpty()) {
                                            result.add(s);
                                        }
                                    }
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), R.layout.simple_list_item_1_daynight, result);
                                    UrlEdit.setAdapter(adapter);
                                    bo.close();
                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        /* Page reloading stuff */
        pageBeforeError = getResources().getString(R.string.url_prefix,
                getResources().getString(R.string.url_suffix_no_error));

        setDeskMode(0, true); /* User agent init code */

        downloadManager(webview); /* Start the download manager service */
        browservioBrowse(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.defaultHomePage)); /* Load default webpage */

        /* zoom related stuff - From SCMPNews project */
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);

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
                    browservioBrowse(sharedText != null ? sharedText : BrowservioBasicUtil.EMPTY_STRING);
                }
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) { /* From default browser */
            for (String match : TypeSchemeMatch) {
                if (match.equals(type) || match.equals(scheme)) {
                    Uri uri = getIntent().getData();
                    browservioBrowse(uri.toString());
                }
            }
        }

        new HistoryInit(browservio_saver(MainActivity.this), historyPref(MainActivity.this));
    }

    /**
     * WebViewClient
     */
    public class WebClient extends WebViewClientCompat {
        private void UrlSet(String url, Boolean addToHist) {
            if (!Objects.requireNonNull(UrlEdit.getText()).toString().equals(url)
                    && !(url.startsWith(getResources().getString(R.string.url_prefix, ""))
                    || url.equals("about:blank")
                    || url.equals(getResources().getString(R.string.url_error_real)))) {
                UrlEdit.setText(url);
                if (addToHist)
                    HistoryReader.appendData(historyPref(MainActivity.this), url);
            }
        }

        public void onPageStarted(WebView view, String url, Bitmap icon) {
            UrlSet(url, false);
            favicon.setImageResource(R.drawable.default_favicon); // Set favicon as default before getting real favicon
            if (BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.showFavicon))) {
                favicon.setVisibility(View.GONE);
                faviconProgressBar.setVisibility(View.VISIBLE);
            }
            UrlEdit.dismissDropDown();
        }

        public void onPageFinished(WebView view, String url) {
            UrlSet(url, true);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
                android.webkit.CookieSyncManager.getInstance().sync();
            else
                CookieManager.getInstance().flush();
            if (BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.showFavicon))) {
                favicon.setVisibility(View.VISIBLE);
                faviconProgressBar.setVisibility(View.GONE);
            }
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (!defaultError) {
                pageBeforeError = previousUrl;
                errorPage();
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null)
                if (url.length() != 0)
                    return false;
            if (BrowservioBasicUtil.appInstalledOrNot(getApplicationContext(), url)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } else {
                BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.app_not_installed));
            }
            return true;
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(MainActivity.this);
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

            dialog.setTitle("SSL Certificate Error")
                    .setMessage(message)
                    .setPositiveButton(getResources().getString(android.R.string.ok), (_dialog, _which) -> handler.proceed())
                    .setNegativeButton(getResources().getString(android.R.string.cancel), (_dialog, _which) -> handler.cancel())
                    .create().show();
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
        public ChromeWebClient() {
        }

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
            favicon.setImageBitmap(icon);
        }

        public void onReceivedTitle(WebView view, String title) {
            UrlTitle = title;
        }

        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (mUploadMessage != null)
                mUploadMessage.onReceiveValue(null);

            mUploadMessage = filePathCallback;

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");

            MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FILECHOOSER_RESULTCODE);

            return true;
        }
    }

    /**
     * When back button is pressed, go back in history or finish activity
     */
    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) // can go back
            webview.goBack();
        else // finish activity
            finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        _configChecker();
    }

    /**
     * Download Manager
     * <p>
     * Module to monitor downloads from a webview.
     *
     * @param webview to monitor
     */
    private void downloadManager(final WebView webview) {
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
     * Browservio Browse URL checker & loader
     *
     * @param url is for strings of URL to check and load
     */
    private void browservioBrowse(String url) {
        if (url == null)
            return;
        previousUrl = url;
        String checkedUrl = UrlUtils.UrlChecker(url, true, BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.defaultSearch));
        if (pageBeforeError.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_no_error)))) {
            // Load URL
            if (url.startsWith(getResources().getString(R.string.url_prefix, ""))
                    || url.equals(getResources().getString(R.string.url_error_real))) {
                URLIdentify(url);
            } else {
                URLIdentify(checkedUrl);
                webview.loadUrl(checkedUrl);
            }
        } else {
            URLIdentify(pageBeforeError);
            webview.loadUrl(UrlUtils.UrlChecker(webview.getUrl()));
            pageBeforeError = getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_no_error));
        }
    }

    /**
     * Set Dark Mode for WebView
     *
     * @param webview WebView to set
     * @param turnOn  Turn on or off the WebView dark mode
     */
    private void setDarkModeWebView(WebView webview, Boolean turnOn) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK))
            WebSettingsCompat.setForceDark(webview.getSettings(), turnOn ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF);
    }

    /**
     * Need Load Info Receiver
     * <p>
     * Receive needLoadUrl for loading.
     */
    final ActivityResultLauncher<Intent> mGetNeedLoad = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> browservioBrowse(result.getData() != null ? result.getData().getStringExtra("needLoadUrl") : null));

    /**
     * Config Checker
     * <p>
     * Used to check if anything has been changed
     * after resume of restart.
     */
    private void _configChecker() {
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
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
            setDarkModeWebView(webview, false);
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
            setDarkModeWebView(webview, powerManager.isPowerSaveMode());

        if (!BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.isFirstLaunch).equals("0"))
            new FirstTimeInit(MainActivity.this);

        // Settings check
        webview.getSettings().setJavaScriptEnabled(BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.isJavaScriptEnabled)));
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.isJavaScriptEnabled)));

        favicon.setVisibility(BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.showFavicon)) ? View.VISIBLE : View.GONE);
        webview.getSettings().setDisplayZoomControls(BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.showZoomKeys)));
        defaultError = !BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.showCustomError));

        // HTML5 API flags
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setDatabaseEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
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
     * <p>
     * This module/function identifies a supplied
     * URL to check for it's nature.
     *
     * @param url is supplied for the url to check
     */
    private void URLIdentify(String url) {
        if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_no_error))))
            throw new RuntimeException("Resource access denied, reason: \"browservio://no_error\" is a protected webpage.");

        if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_error))) || url.equals(getResources().getString(R.string.url_error_real)))
            errorPage();

        if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_license))))
            browservioBrowse(getResources().getString(R.string.url_license_real));

        if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_reload))))
            reload.performClick();

        if (url.equals(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_restart)))) {
            Intent i = getIntent();
            finish();
            startActivity(i);
        }
    }
}
