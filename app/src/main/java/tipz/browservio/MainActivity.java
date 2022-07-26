package tipz.browservio;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
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
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import tipz.browservio.broha.icons.IconHashClient;
import tipz.browservio.fav.FavActivity;
import tipz.browservio.fav.FavUtils;
import tipz.browservio.history.HistoryActivity;
import tipz.browservio.history.HistoryUtils;
import tipz.browservio.settings.SettingsActivity;
import tipz.browservio.settings.SettingsInit;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.suggestions.SuggestionAdapter;
import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.DownloadUtils;
import tipz.browservio.utils.UrlUtils;
import tipz.browservio.utils.urls.BrowservioURLs;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {
    private MaterialAutoCompleteTextView UrlEdit;
    private ProgressBar MainProg;
    private ProgressBar faviconProgressBar;
    private AppCompatImageView fab;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WebView webview;
    private RelativeLayout actionBarBack;
    private AppCompatImageView favicon;

    private String UrlTitle;
    private String currentUrl;
    private String adServers;
    private boolean currentPrebuiltUAState = false;
    private String currentCustomUA;
    private boolean currentCustomUAWideView = false;
    private boolean customBrowse = false;
    private IconHashClient iconHashClient;
    private SharedPreferences pref;

    private static final String template = "<html>\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n<head>\n<title>$0</title>\n</head>\n<body>\n<div style=\"padding-left: 8vw; padding-top: 12vh;\">\n<div>\n<svg xmlns=\"http://www.w3.org/2000/svg\" enable-background=\"new 0 0 24 24\" height=\"96px\" viewBox=\"0 0 24 24\" width=\"96px\" fill=\"currentColor\">\n<g>\n<rect fill=\"none\" height=\"24\" width=\"24\"/>\n<path d=\"M11,8.17L6.49,3.66C8.07,2.61,9.96,2,12,2c5.52,0,10,4.48,10,10c0,2.04-0.61,3.93-1.66,5.51l-1.46-1.46 C19.59,14.87,20,13.48,20,12c0-3.35-2.07-6.22-5-7.41V5c0,1.1-0.9,2-2,2h-2V8.17z M21.19,21.19l-1.41,1.41l-2.27-2.27 C15.93,21.39,14.04,22,12,22C6.48,22,2,17.52,2,12c0-2.04,0.61-3.93,1.66-5.51L1.39,4.22l1.41-1.41L21.19,21.19z M11,18 c-1.1,0-2-0.9-2-2v-1l-4.79-4.79C4.08,10.79,4,11.38,4,12c0,4.08,3.05,7.44,7,7.93V18z\"/>\n</g>\n</svg>\n</div>\n<div>\n<p style=\"font-family:sans-serif; font-weight: bold; font-size: 24px; margin-top: 24px; margin-bottom: 8px;\">$1</p>\n<p style=\"font-family:sans-serif; font-size: 16px; margin-top: 8px; margin-bottom: 24px;\">$2</p>\n<p style=\"font-family:sans-serif; font-weight: bold; font-size: 16px; margin-bottom: 8px;\">$3</p>\n<ul style=\"font-family:sans-serif; font-size: 16px; margin-top: 0px; margin-bottom: 0px;\">\n<li>$4</li>\n<li>$5</li>\n</ul>\n<p style=\"font-family:sans-serif; font-size: 12px; margin-bottom: 8px; color: #808080;\">$6</p>\n</div>\n</div>\n</body>\n</html>";

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

    private String userAgentFull(double mode) {
        return "Mozilla/5.0 ("
                .concat(mode == 0 ? "Linux; Android ".concat(Build.VERSION.RELEASE)
                        .concat("; Device with Browservio ".concat(BuildConfig.VERSION_NAME)
                                .concat(BuildConfig.VERSION_TECHNICAL_EXTRA)) : "X11; Linux x86_64")
                .concat(") AppleWebKit/537.36 (KHTML, like Gecko)"
                        .concat(" Chrome/103.0.0.0 ").concat(mode == 0 ? "Mobile " : CommonUtils.EMPTY_STRING)
                        .concat("Safari/537.36"));
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

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            webview.freeMemory();
    }

    private void setUA(AppCompatImageView view, Boolean enableDesktop, String ua, Integer image, boolean noReload) {
        webview.getSettings().setUserAgentString(ua);
        webview.getSettings().setLoadWithOverviewMode(enableDesktop);
        webview.getSettings().setUseWideViewPort(enableDesktop);
        webview.setScrollBarStyle(enableDesktop ? WebView.SCROLLBARS_OUTSIDE_OVERLAY : View.SCROLLBARS_INSIDE_OVERLAY);
        if (view != null) {
            view.setImageResource(image);
            view.setTag(image);
        }
        if (!noReload)
            webviewReload();
    }

    private void setPrebuiltUAMode(AppCompatImageView view, double mode, boolean noReload) {
        setUA(view,
                mode == 1,
                userAgentFull(mode),
                mode == 0 ? R.drawable.smartphone : R.drawable.desktop,
                noReload);
    }

    private void webviewReload() {
        browservioBrowse(currentUrl);
    }

    private void shareUrl(@Nullable String url) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, url == null ? currentUrl : url);
        startActivity(Intent.createChooser(i, getResources().getString(R.string.share_url_dialog_title)));
    }

    public void itemSelected(AppCompatImageView view, int item) {
        if (item == 0 && webview.canGoBack()) {
            webview.goBack();
        } else if (item == 1 && webview.canGoForward()) {
            webview.goForward();
        } else if (item == 2) {
            webviewReload();
        } else if (item == 3) {
            browservioBrowse(SettingsUtils.getPref(pref, SettingsKeys.defaultHomePage));
        } else if (item == 4) {
            currentPrebuiltUAState = !currentPrebuiltUAState;
            setPrebuiltUAMode(view, currentPrebuiltUAState ? 1 : 0, false);
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
                    HistoryUtils.clear(MainActivity.this);
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
            Drawable icon = favicon.getDrawable();
            FavUtils.appendData(this, iconHashClient, UrlTitle, currentUrl, icon instanceof BitmapDrawable ? ((BitmapDrawable) icon).getBitmap() : null);
            CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.save_successful));
        } else if (item == 12) {
            finish();
        }
    }

    public void itemLongSelected(AppCompatImageView view, int item) {
        if (item == 4) {
            final LayoutInflater layoutInflater = LayoutInflater.from(this);
            @SuppressLint("InflateParams") final View root = layoutInflater.inflate(R.layout.dialog_ua_edit, null);
            final AppCompatEditText customUserAgent = root.findViewById(R.id.edittext);
            final AppCompatCheckBox deskMode = root.findViewById(R.id.deskMode);
            deskMode.setChecked(currentCustomUAWideView);
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
            dialog.setTitle(getResources().getString(R.string.customUA))
                    .setView(root)
                    .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                        if (customUserAgent.length() != 0)
                            setUA(view, deskMode.isChecked(),
                                    Objects.requireNonNull(customUserAgent.getText()).toString(),
                                    R.drawable.custom, false);
                        currentCustomUA = Objects.requireNonNull(customUserAgent.getText()).toString();
                        currentCustomUAWideView = deskMode.isChecked();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show();
            if (currentCustomUA != null)
                customUserAgent.setText(currentCustomUA);
        } else if (item == 11) {
            if (FavUtils.isEmptyCheck(this)) {
                CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.fav_list_empty));
            } else {
                Intent intent = new Intent(MainActivity.this, FavActivity.class);
                mGetNeedLoad.launch(intent);
            }
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
        swipeRefreshLayout = findViewById(R.id.swipe);
        webview = findViewById(R.id.webview);
        RecyclerView actionBar = findViewById(R.id.actionBar);
        actionBarBack = findViewById(R.id.actionBarBack);
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
            if (actionBarBack.getVisibility() == View.VISIBLE) {
                fab.animate().rotation(180).setDuration(250).start();
                actionBarBack.animate().alpha(0f).setDuration(250).start();
                actionBarBack.setVisibility(View.GONE);
            } else {
                fab.animate().rotation(0).setDuration(250).start();
                actionBarBack.animate().alpha(1f).setDuration(250).start();
                actionBarBack.setVisibility(View.VISIBLE);
            }
        });

        webview.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            final WebView.HitTestResult hr = webview.getHitTestResult();
            final String url = hr.getExtra();
            final int type = hr.getType();

            if (type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.EDIT_TEXT_TYPE)
                return;

            MaterialAlertDialogBuilder webLongPress = new MaterialAlertDialogBuilder(MainActivity.this);
            webLongPress.setTitle(url.length() > 48 ? url.substring(0, 47).concat("…") : url);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.recycler_list_item_1);
            if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE)
                arrayAdapter.add(getResources().getString(R.string.open_in_new_tab));
            if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                arrayAdapter.add(getResources().getString(R.string.download_image));
                arrayAdapter.add(getResources().getString(R.string.search_image));
            }
            arrayAdapter.add(getResources().getString(R.string.copy_url));
            arrayAdapter.add(getResources().getString(R.string.share_url));

            webLongPress.setAdapter(arrayAdapter, (dialog, which) -> {
                String strName = arrayAdapter.getItem(which);

                if (strName.equals(getResources().getString(R.string.copy_url))) {
                    CommonUtils.copyClipboard(MainActivity.this, url);
                } else if (strName.equals(getResources().getString(R.string.download_image))) {
                    DownloadUtils.dmDownloadFile(MainActivity.this, url, null, null);
                } else if (strName.equals(getResources().getString(R.string.search_image))) {
                    browservioBrowse("http://images.google.com/searchbyimage?image_url=".concat(url));
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

        swipeRefreshLayout.setOnRefreshListener(this::webviewReload);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        /* Code for detecting return key presses */
        UrlEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == KeyEvent.ACTION_DOWN) {
                browservioBrowse(UrlEdit.getText().toString());
                closeKeyboard();
                return true;
            }
            return false;
        });

        UrlEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !UrlEdit.getText().toString().equals(currentUrl))
                UrlEdit.setText(currentUrl);
        });

        UrlEdit.setOnItemClickListener((adapterView, view, pos, l) -> {
            browservioBrowse(((AppCompatTextView) view.findViewById(android.R.id.text1)).getText().toString());
            closeKeyboard();
        });

        UrlEdit.setAdapter(new SuggestionAdapter(MainActivity.this, R.layout.recycler_list_item_1));

        webview.setWebViewClient(new WebClient());
        webview.setWebChromeClient(new ChromeWebClient());

        webview.removeJavascriptInterface("searchBoxJavaBridge_"); /* CVE-2014-1939 */
        webview.removeJavascriptInterface("accessibility"); /* CVE-2014-7224 */
        webview.removeJavascriptInterface("accessibilityTraversal"); /* CVE-2014-7224 */
    }

    private void closeKeyboard() {
        WindowCompat.getInsetsController(getWindow(), UrlEdit).hide(WindowInsetsCompat.Type.ime());
    }

    /**
     * Welcome to the Browservio (The Shrek Browser)
     * This browser was originally designed with Sketchware
     * This project was started on Aug 13 2020
     * <p>
     * sur wen reel Sherk brower pls sand meme sum
     */
    private void initializeLogic() {
        pref = browservio_saver(this);
        iconHashClient = ((Application) getApplicationContext()).iconHashClient;

        /* User agent init code */
        setPrebuiltUAMode(null, 0, true);

        /* Start the download manager service */
        webview.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) ->
                DownloadUtils.dmDownloadFile(MainActivity.this, url, contentDisposition, mimeType));

        /* Init settings check */
        new SettingsInit(MainActivity.this);
        configChecker();

        /* zoom related stuff - From SCMPNews project */
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);

        webview.setLayerType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
                View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE, null);
        webview.getSettings().setDisplayZoomControls(false);
        webview.getSettings().setAllowFileAccess(false);

        /* HTML5 API flags */
        webview.getSettings().setDatabaseEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);

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
        } else { /* Load default webpage */
            browservioBrowse(SettingsUtils.getPref(pref, SettingsKeys.defaultHomePage));
        }
    }

    /* Function to update the list of Ad servers */
    private void updateAdServerList() {
        String data = DownloadUtils.downloadToString("https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt");
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

    private boolean urlShouldSet(String url) {
        return !(url.equals("about:blank")
                || url.equals(BrowservioURLs.realLicenseUrl));
    }

    /**
     * WebViewClient
     */
    public class WebClient extends WebViewClientCompat {
        private void UrlSet(String url, boolean update) {
            if (!UrlEdit.getText().toString().equals(url)
                    && urlShouldSet(url) || currentUrl == null) {
                UrlEdit.setText(url);
                currentUrl = url;
                if (update)
                    HistoryUtils.updateData(MainActivity.this, null, null, url, null);
                else if (HistoryUtils.isEmptyCheck(MainActivity.this) || !HistoryUtils.lastUrl(MainActivity.this).equals(url))
                    HistoryUtils.appendData(MainActivity.this, url);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap icon) {
            UrlSet(url, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                setTaskDescription(new ActivityManager.TaskDescription(CommonUtils.EMPTY_STRING));
            if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon))
                    && urlShouldSet(url)) {
                favicon.setVisibility(View.GONE);
                faviconProgressBar.setVisibility(View.VISIBLE);
            }
            favicon.setImageResource(R.drawable.default_favicon);
            UrlEdit.dismissDropDown();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            UrlSet(url, true);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
                CookieSyncManager.getInstance().sync();
            else
                CookieManager.getInstance().flush();
            if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon))) {
                favicon.setVisibility(View.VISIBLE);
                faviconProgressBar.setVisibility(View.GONE);
            }
            if (!(favicon.getDrawable() instanceof BitmapDrawable))
                favicon.setImageResource(R.drawable.default_favicon);
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            String returnVal = template;
            for (int i = 0; i < 6; i++)
                returnVal = returnVal.replace("$".concat(Integer.toString(i)),
                        MainActivity.this.getResources().getStringArray(R.array.errMsg)[i]);
            returnVal = returnVal.replace("$6", description);

            webview.loadDataWithBaseURL(null, returnVal, "text/html", "UTF-8", null);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean returnVal = false;
            boolean normalSchemes = UrlUtils.startsWithMatch(url);
            if (!normalSchemes) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (ActivityNotFoundException ignored) {
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
                    if (adServers.contains(" ".concat(new URL(url).getHost())) && SettingsUtils.getPrefNum(pref, SettingsKeys.enableAdBlock) == 1)
                        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(CommonUtils.EMPTY_STRING.getBytes()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return super.shouldInterceptRequest(view, url);
        }
    }

    private void setImmersiveMode(boolean enable) {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

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

        @Override
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

        @Override
        public void onHideCustomView() {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            ((FrameLayout) getWindow().getDecorView()).removeView(mCustomView);
            mCustomView = null;
            setImmersiveMode(false);
            setRequestedOrientation(getResources().getConfiguration().orientation);
            mCustomViewCallback.onCustomViewHidden();
            mCustomViewCallback = null;
        }

        @Override
        public void onProgressChanged(WebView view, int progress) {
            MainProg.setProgress(progress == 100 ? 0 : progress);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            favicon.setImageBitmap(icon);
            HistoryUtils.updateData(MainActivity.this, iconHashClient, null, null, icon);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            UrlTitle = title;
            if (urlShouldSet(webview.getUrl()) && title != null)
                HistoryUtils.updateData(MainActivity.this, null, title, null, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                setTaskDescription(new ActivityManager.TaskDescription(title));
        }

        @Override
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

    /**
     * Browservio Browse URL checker & loader
     *
     * @param url is for strings of URL to check and load
     */
    private void browservioBrowse(String url) {
        if (url == null || url.isEmpty())
            return;

        String urlIdentify = URLIdentify(url);
        if (urlIdentify != null) {
            currentUrl = urlIdentify;
            if (!urlIdentify.equals(CommonUtils.EMPTY_STRING))
                webview.loadUrl(urlIdentify);
            return;
        }

        String checkedUrl = UrlUtils.UrlChecker(url, true, SettingsUtils.getPref(pref, SettingsKeys.defaultSearch));
        currentUrl = checkedUrl;
        // Load URL
        webview.loadUrl(checkedUrl, mRequestHeaders);
        customBrowse = true;
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
        if (SettingsUtils.getPrefNum(pref, SettingsKeys.themeId) == 0)
            AppCompatDelegate.setDefaultNightMode(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1 ?
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        else
            AppCompatDelegate.setDefaultNightMode(SettingsUtils.getPrefNum(
                    pref, SettingsKeys.themeId) == 2 ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        boolean darkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING))
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webview.getSettings(), darkMode);
        else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK))
            WebSettingsCompat.setForceDark(webview.getSettings(),
                    darkMode ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF);

        // Settings check
        webview.getSettings().setJavaScriptEnabled(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.isJavaScriptEnabled)));
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.isJavaScriptEnabled)));
        favicon.setVisibility(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon)) ? View.VISIBLE : View.GONE);
        actionBarBack.setGravity(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.centerActionBar)) ? Gravity.CENTER_HORIZONTAL : Gravity.NO_GRAVITY);

        // Do Not Track request
        mRequestHeaders.put("DNT", String.valueOf(SettingsUtils.getPrefNum(pref, SettingsKeys.sendDNT)));

        if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon))
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
        private final WeakReference<MainActivity> mMainActivity;

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final AppCompatImageView mImageView;

            public ViewHolder(View view) {
                super(view);
                mImageView = view.findViewById(R.id.imageView);
            }
        }

        public ItemsAdapter(MainActivity mainActivity) {
            mMainActivity = new WeakReference<>(mainActivity);
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
            holder.mImageView.setOnClickListener(view -> mMainActivity.get().itemSelected(holder.mImageView, position));
            holder.mImageView.setOnLongClickListener(view -> {
                mMainActivity.get().itemLongSelected(holder.mImageView, position);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return actionBarItemList.size();
        }
    }
}
