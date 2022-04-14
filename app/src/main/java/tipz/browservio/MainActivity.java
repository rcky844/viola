package tipz.browservio;

import static tipz.browservio.fav.FavApi.bookmarks;
import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import tipz.browservio.fav.FavActivity;
import tipz.browservio.history.HistoryActivity;
import tipz.browservio.history.HistoryApi;
import tipz.browservio.history.HistoryReader;
import tipz.browservio.settings.SettingsActivity;
import tipz.browservio.settings.SettingsInit;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.DownloadToStringUtils;
import tipz.browservio.utils.UrlUtils;
import tipz.browservio.utils.urls.BrowservioURLs;
import tipz.browservio.utils.urls.SearchEngineEntries;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {
    private MaterialAutoCompleteTextView UrlEdit;
    private ProgressBar MainProg;
    private ProgressBar faviconProgressBar;
    private AppCompatImageView fab;
    private WebView webview;
    private RecyclerView actionBar;
    private AppCompatImageView favicon;

    private String UrlTitle;
    private String currentUrl;
    private String adServers;
    private String currentError = CommonUtils.EMPTY_STRING;
    private boolean customBrowse = false;

    private ValueCallback<Uri[]> mUploadMessage;

    private final HashMap<String, String> mRequestHeaders = new HashMap<>();

    private static final List<Integer> actionBarItemList = Arrays.asList(R.drawable.arrow_back_alt,
            R.drawable.arrow_forward_alt,
            R.drawable.refresh,
            R.drawable.home,
            R.drawable.smartphone,
            R.drawable.new_tab,
            R.drawable.delete,
            R.drawable.share,
            R.drawable.app_shortcut,
            R.drawable.settings,
            R.drawable.history,
            R.drawable.favorites,
            R.drawable.close);

    private String userAgentFull(String mid) {
        return "Mozilla/5.0 (".concat(mid).concat(") AppleWebKit/537.36 (KHTML, like Gecko) Safari/537.36 ".concat("Browservio/".concat(BuildConfig.VERSION_NAME).concat(BuildConfig.VERSION_TECHNICAL_EXTRA)));
    }

    private final ActivityResultLauncher<String> mFileChooser = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (null == mUploadMessage || uri == null)
                    return;

                mUploadMessage.onReceiveValue(new Uri[]{uri});
                mUploadMessage = null;
            });

    /**
     * An array used for intent filtering
     */
    private static final String[] TypeSchemeMatch = {
            "text/html", "text/plain", "application/xhtml+xml", "application/vnd.wap.xhtml+xml",
            "http", "https", "ftp", "file"};

    public boolean webViewEnabled() {
        try {
            CookieManager.getInstance();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        if (webViewEnabled()) {
            setContentView(R.layout.main);
            initialize();
            initializeLogic();

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
        } else {
            CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.no_webview));
            finish();
        }
    }

    private void setDesktopMode(AppCompatImageView view, Boolean enableDesktop, String ua, Integer image, boolean noReload) {
        webview.getSettings().setUserAgentString(ua);
        webview.getSettings().setLoadWithOverviewMode(enableDesktop);
        webview.getSettings().setUseWideViewPort(enableDesktop);
        webview.setScrollBarStyle(enableDesktop ? WebView.SCROLLBARS_OUTSIDE_OVERLAY : View.SCROLLBARS_INSIDE_OVERLAY);
        if (view != null)
            view.setImageResource(image);
        if (!noReload)
            webviewReload();
    }

    private void setDeskMode(AppCompatImageView view, double mode, boolean noReload) {
        if (mode == 0) {
            setDesktopMode(view,
                    false,
                    userAgentFull("Linux; Android ".concat(Build.VERSION.RELEASE)),
                    R.drawable.smartphone,
                    noReload);
        } else if (mode == 1) {
            setDesktopMode(view,
                    true,
                    userAgentFull("X11; Linux x86_64"),
                    R.drawable.desktop,
                    noReload);
        }
    }

    private void webviewReload() {
        browservioBrowse(currentUrl);
    }

    private void shareUrl(@Nullable String url) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, url == null ? currentUrl : url);
        startActivity(Intent.createChooser(i, getResources().getString(R.string.linear_control_b5_title)));
    }

    public void itemSelected(AppCompatImageView view, int item) {
        if (item == 0 && webview.canGoBack()) {
            webview.goBack();
        } else if (item == 1 && webview.canGoForward()) {
            webview.goForward();
        } else if (item == 2) {
            webviewReload();
        } else if (item == 3) {
            browservioBrowse(SettingsUtils.getPref(browservio_saver(MainActivity.this), SettingsKeys.defaultHomePage));
        } else if (item == 4) {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
            Menu menu = popupMenu.getMenu();
            menu.add(getResources().getString(R.string.linear_control_b3_desk));
            menu.add(getResources().getString(R.string.linear_control_b3_mobi));
            menu.add(getResources().getString(R.string.linear_control_b3_cus));
            popupMenu.setOnMenuItemClickListener(_item -> {
                if (_item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_desk)))
                    setDeskMode(view, 1, false);
                else if (_item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_mobi)))
                    setDeskMode(view, 0, false);
                else if (_item.getTitle().toString().equals(getResources().getString(R.string.linear_control_b3_cus))) {
                    final LayoutInflater layoutInflater = LayoutInflater.from(this);
                    @SuppressLint("InflateParams") final View root = layoutInflater.inflate(R.layout.dialog_edittext, null);
                    final AppCompatEditText customUserAgent = root.findViewById(R.id.edittext);
                    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
                    dialog.setTitle(getResources().getString(R.string.ua))
                            .setMessage(getResources().getString(R.string.cus_ua_choose))
                            .setView(root)
                            .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                                if (customUserAgent.length() == 0) {
                                    setDeskMode(view, 0, false);
                                } else {
                                    view.setImageResource(R.drawable.custom);
                                    webview.getSettings().setUserAgentString(Objects.requireNonNull(customUserAgent.getText()).toString());
                                    webviewReload();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create().show();
                }
                return false;
            });
            popupMenu.show();
        } else if (item == 5) {
            Intent i = new Intent(this, MainActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            else
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            startActivity(i);
        } else if (item == 6) {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
            Menu menu = popupMenu.getMenu();
            menu.add(getResources().getString(R.string.clear, getResources().getString(R.string.cache)));
            menu.add(getResources().getString(R.string.clear, getResources().getString(R.string.history)));
            menu.add(getResources().getString(R.string.clear, getResources().getString(R.string.cookies)));
            popupMenu.setOnMenuItemClickListener(_item -> {
                if (_item.getTitle().toString().contains(getResources().getString(R.string.cache))) {
                    webview.clearCache(true);
                    CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cache)));
                    webviewReload();
                } else if (_item.getTitle().toString().contains(getResources().getString(R.string.history))) {
                    webview.clearHistory();
                    HistoryReader.clear(MainActivity.this);
                    CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.cleared_toast, getResources().getString(R.string.history)));
                    webviewReload();
                } else if (_item.getTitle().toString().contains(getResources().getString(R.string.cookies))) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        CookieManager.getInstance().removeAllCookies(null);
                        CookieManager.getInstance().flush();
                    } else {
                        CookieSyncManager cookieSyncMgr = CookieSyncManager.createInstance(this);
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieSyncMgr.startSync();
                        cookieManager.removeAllCookie();
                        cookieManager.removeSessionCookie();
                        cookieSyncMgr.stopSync();
                        cookieSyncMgr.sync();
                    }
                    CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cookies)));
                    webviewReload();
                }

                return false;
            });
            popupMenu.show();
        } else if (item == 7) {
            shareUrl(null);
        } else if (item == 8) {
            Drawable originalIcon = favicon.getDrawable();
            Bitmap icon = Bitmap.createBitmap(originalIcon.getIntrinsicWidth(), originalIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(icon);

            originalIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            originalIcon.draw(canvas);

            ShortcutManagerCompat.requestPinShortcut(this, new ShortcutInfoCompat.Builder(this, UrlTitle)
                    .setShortLabel(UrlTitle)
                    .setIcon(IconCompat.createWithBitmap(icon))
                    .setIntent(new Intent(this, MainActivity.class)
                            .setData(Uri.parse(currentUrl))
                            .setAction(Intent.ACTION_VIEW))
                    .build(), null);
        } else if (item == 9) {
            Intent intent = new Intent(this, SettingsActivity.class);
            mGetNeedLoad.launch(intent);
        } else if (item == 10) {
            Intent intent = new Intent(this, HistoryActivity.class);
            mGetNeedLoad.launch(intent);
        } else if (item == 11) {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
            Menu menu = popupMenu.getMenu();
            menu.add(getResources().getString(R.string.add));
            menu.add(getResources().getString(R.string.fav));
            popupMenu.setOnMenuItemClickListener(_item -> {
                if (_item.getTitle().toString().equals(getResources().getString(R.string.add))) {
                    SettingsUtils.setPref(bookmarks(MainActivity.this), SettingsKeys.bookmarked_count, SettingsUtils.getPref(bookmarks(MainActivity.this), SettingsKeys.bookmarked_count).isEmpty() ? "0" : String.valueOf((long) (Double.parseDouble(SettingsUtils.getPref(bookmarks(MainActivity.this), SettingsKeys.bookmarked_count)) + 1)));
                    SettingsUtils.setPref(bookmarks(MainActivity.this), SettingsKeys.bookmarked.concat(SettingsUtils.getPref(bookmarks(MainActivity.this), SettingsKeys.bookmarked_count)), webview.getUrl());
                    SettingsUtils.setPref(bookmarks(MainActivity.this), SettingsKeys.bookmarked.concat(SettingsUtils.getPref(bookmarks(MainActivity.this), SettingsKeys.bookmarked_count)).concat(SettingsKeys.bookmarked_title), UrlTitle);
                    SettingsUtils.setPref(bookmarks(MainActivity.this), SettingsKeys.bookmarked.concat(SettingsUtils.getPref(bookmarks(MainActivity.this), SettingsKeys.bookmarked_count)).concat(SettingsKeys.bookmarked_show), "1");
                    CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.saved_su));
                } else if (_item.getTitle().toString().equals(getResources().getString(R.string.fav))) {
                    if (bookmarks(MainActivity.this).getAll().size() == 0) {
                        CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.fav_list_empty));
                    } else {
                        Intent intent = new Intent(MainActivity.this, FavActivity.class);
                        mGetNeedLoad.launch(intent);
                    }
                }
                return false;
            });
            popupMenu.show();
        } else if (item == 12) {
            finish();
        }
    }

    /**
     * Initialize function
     */
    @SuppressLint("AddJavascriptInterface")
    private void initialize() {
        fab = findViewById(R.id.fab);
        UrlEdit = findViewById(R.id.UrlEdit);
        MainProg = findViewById(R.id.MainProg);
        faviconProgressBar = findViewById(R.id.faviconProgressBar);
        webview = findViewById(R.id.webview);
        actionBar = findViewById(R.id.actionBar);
        favicon = findViewById(R.id.favicon);

        actionBar.setLayoutManager(new LinearLayoutManager(
                MainActivity.this, RecyclerView.HORIZONTAL, false));
        actionBar.setAdapter(new ItemsAdapter(MainActivity.this));

        favicon.setOnClickListener(_view -> {
            final SslCertificate cert = webview.getCertificate();
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, favicon);
            Menu menu = popupMenu.getMenu();
            menu.add(UrlTitle).setEnabled(false);
            menu.add(getResources().getString(R.string.copy_title));
            if (cert != null)
                menu.add(getResources().getString(R.string.ssl_info));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equals(getResources().getString(R.string.copy_title))) {
                    CommonUtils.copyClipboard(MainActivity.this, UrlTitle);
                    return true;
                } else if (item.getTitle().toString().equals(getResources().getString(R.string.ssl_info))) {
                    assert cert != null;
                    final SslCertificate.DName issuedTo = cert.getIssuedTo();
                    final SslCertificate.DName issuedBy = cert.getIssuedBy();
                    final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(MainActivity.this);
                    dialog.setTitle(Uri.parse(currentUrl).getHost())
                            .setMessage(getResources().getString(R.string.ssl_info_dialog_content,
                                    issuedTo.getCName(), issuedTo.getOName(), issuedTo.getUName(),
                                    issuedBy.getCName(), issuedBy.getOName(), issuedBy.getUName(),
                                    DateFormat.getDateTimeInstance().format(cert.getValidNotBeforeDate()),
                                    DateFormat.getDateTimeInstance().format(cert.getValidNotAfterDate())))
                            .setPositiveButton(getResources().getString(android.R.string.ok), null)
                            .create().show();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        faviconProgressBar.setOnClickListener(_view -> favicon.performClick());

        fab.setOnClickListener(_view -> {
            if (actionBar.getVisibility() == View.VISIBLE) {
                fab.animate().rotation(180).setDuration(250).start();
                actionBar.animate().alpha(0f).setDuration(250).start();
                actionBar.setVisibility(View.GONE);
            } else {
                fab.animate().rotation(0).setDuration(250).start();
                actionBar.animate().alpha(1f).setDuration(250).start();
                actionBar.setVisibility(View.VISIBLE);
            }
        });

        webview.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            final WebView.HitTestResult hr = webview.getHitTestResult();
            final String url = hr.getExtra();
            final int type = hr.getType();

            if (type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.EDIT_TEXT_TYPE)
                return;

            MaterialAlertDialogBuilder webLongPress = new MaterialAlertDialogBuilder(MainActivity.this);
            webLongPress.setTitle(url);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.recycler_list_item_1);
            if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE)
                arrayAdapter.add(getResources().getString(R.string.open_in_new_tab));
            if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)
                arrayAdapter.add(getResources().getString(R.string.download_image));
            arrayAdapter.add(getResources().getString(R.string.copy_url));
            arrayAdapter.add(getResources().getString(R.string.share_url));

            webLongPress.setAdapter(arrayAdapter, (dialog, which) -> {
                String strName = arrayAdapter.getItem(which);

                if (strName.equals(getResources().getString(R.string.copy_url))) {
                    CommonUtils.copyClipboard(MainActivity.this, url);
                } else if (strName.equals(getResources().getString(R.string.download_image))) {
                    downloadFile(url, null, null);
                } else if (strName.equals(getResources().getString(R.string.open_in_new_tab))) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, url)
                            .setAction(Intent.ACTION_SEND)
                            .setType(TypeSchemeMatch[1]);
                    startActivity(intent);
                } else if (strName.equals(getResources().getString(R.string.share_url))) {
                    shareUrl(url);
                }

            });

            webLongPress.show();
        });

        /* Code for detecting return key presses */
        UrlEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == KeyEvent.ACTION_DOWN) {
                browservioBrowse(UrlEdit.getText().toString());
                closeKeyboard();
                return true;
            }
            return false;
        });

        UrlEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (text.toString().isEmpty() || CommonUtils.isNetworkAvailable(getApplicationContext()))
                    return;
                try {
                    String data = DownloadToStringUtils.downloadToString(
                            SearchEngineEntries.getSuggestionsUrl(SettingsUtils.getPref(
                                    browservio_saver(MainActivity.this), SettingsKeys.defaultSuggestions),
                                    text.toString()));
                    if (data == null)
                        return;
                    JSONArray jsonArray = new JSONArray(data);

                    jsonArray = jsonArray.optJSONArray(1);
                    if (jsonArray == null)
                        throw new RuntimeException("jsonArray is null.");
                    final int MAX_RESULTS = 10;
                    ArrayList<String> result = new ArrayList<>(Math.min(jsonArray.length(), MAX_RESULTS));
                    for (int i = 0; i < jsonArray.length() && result.size() < MAX_RESULTS; i++) {
                        String s = jsonArray.optString(i);
                        if (s != null && !s.isEmpty())
                            result.add(s);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.recycler_list_item_1, result);
                    UrlEdit.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        UrlEdit.setOnItemClickListener((adapterView, view, pos, l) -> {
            browservioBrowse(((AppCompatTextView) view.findViewById(android.R.id.text1)).getText().toString());
            closeKeyboard();
        });

        webview.setWebViewClient(new WebClient());
        webview.setWebChromeClient(new ChromeWebClient());

        webview.addJavascriptInterface(new browservioJsInterface(MainActivity.this), "browservio");
    }

    private void closeKeyboard() {
        Objects.requireNonNull(ViewCompat.getWindowInsetsController(UrlEdit)).hide(WindowInsetsCompat.Type.ime());
    }

    /**
     * Welcome to the Browservio (The Shrek Browser)
     * This browser was originally designed with Sketchware
     * This project was started on Aug 13 2020
     * <p>
     * sur wen reel Sherk brower pls sand meme sum
     */
    private void initializeLogic() {
        /* User agent init code */
        setDeskMode(null, 0, true);

        /* Start the download manager service */
        webview.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> downloadFile(url, contentDisposition, mimeType));

        /* Init settings check */
        if (!SettingsUtils.getPref(browservio_saver(MainActivity.this), SettingsKeys.isFirstLaunch).equals("0"))
            new SettingsInit(MainActivity.this);

        configChecker();

        /* zoom related stuff - From SCMPNews project */
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);

        webview.setLayerType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
                View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE, null);
        webview.getSettings().setDisplayZoomControls(false);

        // HTML5 API flags
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setAppCachePath(getCacheDir().getAbsolutePath());
        webview.getSettings().setDatabaseEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);

        new HistoryApi(this);

        updateAdServerList();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
            WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());

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
                    browservioBrowse(sharedText != null ? sharedText : CommonUtils.EMPTY_STRING);
                }
            }
        } else if (Intent.ACTION_VIEW.equals(action)) { /* From default browser */
            for (String match : TypeSchemeMatch) {
                if (match.equals(type) || match.equals(scheme)) {
                    Uri uri = getIntent().getData();
                    browservioBrowse(uri.toString());
                }
            }
        } else {
            /* Load default webpage */
            browservioBrowse(SettingsUtils.getPref(browservio_saver(MainActivity.this), SettingsKeys.defaultHomePage));
        }
    }

    /* Function to update the list of Ad servers */
    private void updateAdServerList() {
        String data = DownloadToStringUtils.downloadToString("https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt");
        if (data != null) {
            Scanner scanner = new Scanner(data);
            StringBuilder builder = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("127.0.0.1 "))
                    builder.append(line).append(CommonUtils.LINE_SEPARATOR());
            }
            adServers = builder.toString();
        }
    }

    public class browservioJsInterface {
        final MainActivity mMainActivity;

        browservioJsInterface(MainActivity mainActivity) {
            mMainActivity = mainActivity;
        }

        @JavascriptInterface
        public String errGetMsg(int msgId) {
            if (msgId >= 6)
                return currentError;
            return mMainActivity.getResources().getStringArray(R.array.errMsg)[msgId];
        }

        @JavascriptInterface
        public void reloadBtn() {
            runOnUiThread(mMainActivity::webviewReload);
        }
    }

    /**
     * WebViewClient
     */
    public class WebClient extends WebViewClientCompat {
        private void UrlSet(String url) {
            if (!Objects.requireNonNull(UrlEdit.getText()).toString().equals(url)
                    && !(url.equals("about:blank")
                    || url.equals(BrowservioURLs.realErrUrl)
                    || url.equals(BrowservioURLs.realLicenseUrl))) {
                UrlEdit.setText(url);
                currentUrl = url;
                if (!HistoryReader.history_data(MainActivity.this).trim().endsWith(url))
                    HistoryReader.appendData(MainActivity.this, url);
            }
        }

        public void onPageStarted(WebView view, String url, Bitmap icon) {
            UrlSet(url);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                setTaskDescription(new ActivityManager.TaskDescription(CommonUtils.EMPTY_STRING));
            if (CommonUtils.isIntStrOne(SettingsUtils.getPref(browservio_saver(MainActivity.this), SettingsKeys.showFavicon))) {
                favicon.setVisibility(View.GONE);
                faviconProgressBar.setVisibility(View.VISIBLE);
            }
            UrlEdit.dismissDropDown();
        }

        public void onPageFinished(WebView view, String url) {
            UrlSet(url);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
                CookieSyncManager.getInstance().sync();
            else
                CookieManager.getInstance().flush();
            if (CommonUtils.isIntStrOne(SettingsUtils.getPref(browservio_saver(MainActivity.this), SettingsKeys.showFavicon))) {
                favicon.setVisibility(View.VISIBLE);
                faviconProgressBar.setVisibility(View.GONE);
            }
            favicon.setImageResource(R.drawable.default_favicon);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            webview.loadUrl(BrowservioURLs.realErrUrl);
            currentError = description;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean returnVal = false;
            boolean normalSchemes = UrlUtils.startsWithMatch(url);
            if (!normalSchemes) {
                if (CommonUtils.appInstalledOrNot(getApplicationContext(), url)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    webview.stopLoading();
                }
                returnVal = true;
            }
            if (!customBrowse && normalSchemes) {
                webview.loadUrl(url, mRequestHeaders);
                returnVal = true;
            }
            customBrowse = false;
            return returnVal;
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(MainActivity.this);
            String message = getResources().getString(R.string.ssl_certificate_error_dialog_content,
                    getResources().getString(R.string.ssl_certificate_unknown));
            switch (error.getPrimaryError()) {
                case SslError.SSL_DATE_INVALID:
                    message = getResources().getString(R.string.ssl_certificate_error_dialog_content,
                            getResources().getString(R.string.ssl_certificate_date_invalid));
                    break;
                case SslError.SSL_INVALID:
                    message = getResources().getString(R.string.ssl_certificate_error_dialog_content,
                            getResources().getString(R.string.ssl_certificate_invalid));
                    break;
                case SslError.SSL_EXPIRED:
                    message = getResources().getString(R.string.ssl_certificate_error_dialog_content,
                            getResources().getString(R.string.ssl_certificate_expired));
                    break;
                case SslError.SSL_IDMISMATCH:
                    message = getResources().getString(R.string.ssl_certificate_error_dialog_content,
                            getResources().getString(R.string.ssl_certificate_idmismatch));
                    break;
                case SslError.SSL_NOTYETVALID:
                    message = getResources().getString(R.string.ssl_certificate_error_dialog_content,
                            getResources().getString(R.string.ssl_certificate_notyetvalid));
                    break;
                case SslError.SSL_UNTRUSTED:
                    message = getResources().getString(R.string.ssl_certificate_error_dialog_content,
                            getResources().getString(R.string.ssl_certificate_untrusted));
                    break;
            }

            dialog.setTitle(getResources().getString(R.string.ssl_certificate_error_dialog_title))
                    .setMessage(message)
                    .setPositiveButton(getResources().getString(android.R.string.ok), (_dialog, _which) -> handler.proceed())
                    .setNegativeButton(getResources().getString(android.R.string.cancel), (_dialog, _which) -> handler.cancel())
                    .create().show();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (adServers == null)
                updateAdServerList();
            try {
                if (adServers != null)
                    if (adServers.contains(" ".concat(new URL(url).getHost())) && SettingsUtils.getPrefNum(browservio_saver(MainActivity.this), SettingsKeys.enableAdBlock) == 1)
                        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(CommonUtils.EMPTY_STRING.getBytes()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return super.shouldInterceptRequest(view, url);
        }
    }

    private void setImmersiveMode(boolean enable) {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null)
            return;

        WindowCompat.setDecorFitsSystemWindows(getWindow(), !enable);

        if (enable) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
        }
    }

    /**
     * WebChromeClient
     */
    public class ChromeWebClient extends WebChromeClient {
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;

        // Constructor for ChromeWebClient
        public ChromeWebClient() {
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback viewCallback) {
            if (mCustomView != null) {
                onHideCustomView();
                return;
            }
            mCustomView = paramView;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mCustomViewCallback = viewCallback;
            setImmersiveMode(true);
            ((FrameLayout) getWindow().getDecorView()).addView(mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        public void onHideCustomView() {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            ((FrameLayout) getWindow().getDecorView()).removeView(mCustomView);
            mCustomView = null;
            setImmersiveMode(false);
            setRequestedOrientation(getResources().getConfiguration().orientation);
            mCustomViewCallback.onCustomViewHidden();
            mCustomViewCallback = null;
        }

        public void onProgressChanged(WebView view, int progress) {
            MainProg.setProgress(progress == 100 ? 0 : progress);
        }

        public void onReceivedIcon(WebView view, Bitmap icon) {
            if (!icon.isRecycled())
                favicon.setImageBitmap(icon);
        }

        public void onReceivedTitle(WebView view, String title) {
            UrlTitle = title;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                setTaskDescription(new ActivityManager.TaskDescription(title));
        }

        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                callback.invoke(origin, true, false);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (mUploadMessage != null)
                mUploadMessage.onReceiveValue(null);

            mUploadMessage = filePathCallback;
            mFileChooser.launch("*/*");

            return true;
        }
    }

    /**
     * When back button is pressed, go back in history or finish activity
     */
    @Override
    public void onBackPressed() {
        if (webview.canGoBack())
            webview.goBack();
        else
            finish();
    }

    public void downloadFile(String url, String contentDisposition, String mimeType) {
        if (url.startsWith("blob:")) { /* TODO: Make it actually handle blob: URLs */
            CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.ver3_blob_no_support));
        } else {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

            // Let this downloaded file be scanned by MediaScanner - so that it can
            // show up in Gallery app, for example.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                request.allowScanningByMediaScanner();

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // Notify client once download is completed!
            final String filename = UrlUtils.guessFileName(url, contentDisposition, mimeType);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(url)));
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
        }
    }

    /**
     * Browservio Browse URL checker & loader
     *
     * @param url is for strings of URL to check and load
     */
    private void browservioBrowse(String url) {
        if (url == null || url.isEmpty())
            return;

        String urlIdentify = URLIdentify(url);
        if (urlIdentify != null)
            if (urlIdentify.equals(CommonUtils.EMPTY_STRING))
                return;

        String checkedUrl = UrlUtils.UrlChecker(url, true, SettingsUtils.getPref(browservio_saver(MainActivity.this), SettingsKeys.defaultSearch));
        // Load URL
        webview.loadUrl(checkedUrl, mRequestHeaders);
        customBrowse = true;
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
            result -> {
                configChecker();
                browservioBrowse(result.getData() != null ? result.getData().getStringExtra("needLoadUrl") : null);
            });

    /**
     * Config Checker
     * <p>
     * Used to check if anything has been changed
     * after returning from settings.
     */
    private void configChecker() {
        // Dark mode
        if (SettingsUtils.getPrefNum(browservio_saver(MainActivity.this), SettingsKeys.themeId) == 0) {
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
        } else {
            boolean darkMode = SettingsUtils.getPrefNum(browservio_saver(MainActivity.this), SettingsKeys.themeId) == 2;
            setDarkModeWebView(webview, darkMode);
            AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Settings check
        webview.getSettings().setJavaScriptEnabled(CommonUtils.isIntStrOne(SettingsUtils.getPref(browservio_saver(MainActivity.this), SettingsKeys.isJavaScriptEnabled)));
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(CommonUtils.isIntStrOne(SettingsUtils.getPref(browservio_saver(MainActivity.this), SettingsKeys.isJavaScriptEnabled)));
        favicon.setVisibility(CommonUtils.isIntStrOne(SettingsUtils.getPref(browservio_saver(MainActivity.this), SettingsKeys.showFavicon)) ? View.VISIBLE : View.GONE);

        // Do Not Track request
        mRequestHeaders.put("DNT", SettingsUtils.getPref(browservio_saver(MainActivity.this), SettingsKeys.sendDNT));

        if (CommonUtils.isIntStrOne(SettingsUtils.getPref(browservio_saver(MainActivity.this), SettingsKeys.showFavicon))
                && faviconProgressBar.getVisibility() == View.VISIBLE)
            favicon.setVisibility(View.GONE);
    }

    /**
     * URL identify module
     * <p>
     * This module/function identifies a supplied
     * URL to check for it's nature.
     *
     * @param url is supplied for the url to check
     * @return url to load
     */
    @Nullable
    private String URLIdentify(String url) {
        if (url.equals(BrowservioURLs.realErrUrl))
            return BrowservioURLs.realErrUrl;

        if (url.equals(BrowservioURLs.licenseUrl) || url.equals(BrowservioURLs.realLicenseUrl)) {
            UrlEdit.setText(BrowservioURLs.licenseUrl);
            return BrowservioURLs.realLicenseUrl;
        }

        if (url.equals(BrowservioURLs.reloadUrl)) {
            webviewReload();
            return CommonUtils.EMPTY_STRING;
        }

        if (url.startsWith(BrowservioURLs.yhlPrefix))
            return "http://119.28.42.46:8886/chaxun_web.asp?kd_id=".concat(url.replace(BrowservioURLs.yhlPrefix, CommonUtils.EMPTY_STRING));

        return null;
    }

    public static class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
        private final MainActivity mMainActivity;

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final AppCompatImageView mImageView;

            public ViewHolder(View view) {
                super(view);
                mImageView = view.findViewById(R.id.imageView);
            }
        }

        public ItemsAdapter(MainActivity mainActivity) {
            mMainActivity = mainActivity;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_icon_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.mImageView.setImageResource(actionBarItemList.get(position));
            holder.mImageView.setOnClickListener(view -> mMainActivity.itemSelected(holder.mImageView, position));
        }

        @Override
        public int getItemCount() {
            return actionBarItemList.size();
        }
    }
}
