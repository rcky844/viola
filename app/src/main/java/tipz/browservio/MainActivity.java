package tipz.browservio;

import static tipz.browservio.fav.FavApi.bookmarks;
import static tipz.browservio.history.HistoryApi.historyPref;
import static tipz.browservio.sharedprefs.utils.BrowservioSaverUtils.browservio_saver;
import static tipz.browservio.utils.BrowservioBasicUtil.RotateAlphaAnim;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import tipz.browservio.history.HistoryActivity;
import tipz.browservio.history.HistoryInit;
import tipz.browservio.history.HistoryReader;
import tipz.browservio.sharedprefs.AllPrefs;
import tipz.browservio.sharedprefs.FirstTimeInit;
import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;
import tipz.browservio.urls.BrowservioURLs;
import tipz.browservio.utils.BrowservioBasicUtil;
import tipz.browservio.utils.UrlUtils;
import tipz.browservio.view.MainActionBarRecycler;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {
    private MaterialAutoCompleteTextView UrlEdit;
    private ProgressBar MainProg;
    private ProgressBar faviconProgressBar;
    private ImageView fab;
    private WebView webview;
    private RecyclerView actionBar;
    private AppCompatImageView favicon;

    private final ObjectAnimator fabAnimate = new ObjectAnimator();
    private final ObjectAnimator barAnimate = new ObjectAnimator();

    private String UrlTitle;
    private StringBuilder adServers;
    private boolean customBrowse;

    private final static int FILECHOOSER_RESULTCODE = 1;
    private ValueCallback<Uri[]> mUploadMessage;

    private final HashMap<String, String> mRequestHeaders = new HashMap<>();

    private String userAgentFull(String mid) {
        return "Mozilla/5.0 (".concat(mid).concat(") AppleWebKit/605.1.15 (KHTML, like Gecko) Safari/605.1.15 ".concat("Browservio/".concat(BuildConfig.VERSION_NAME).concat(BuildConfig.VERSION_TECHNICAL_EXTRA)));
    }

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
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        if (webViewEnabled()) {
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
        } else {
            BrowservioBasicUtil.showMessage(this, getResources().getString(R.string.no_webview));
            finish();
        }
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

    private void setDesktopMode(ImageView view, Boolean enableDesktop, String ua, Integer image, boolean noReload) {
        webview.getSettings().setUserAgentString(ua);
        webview.getSettings().setLoadWithOverviewMode(enableDesktop);
        webview.getSettings().setUseWideViewPort(enableDesktop);
        webview.setScrollBarStyle(enableDesktop ? WebView.SCROLLBARS_OUTSIDE_OVERLAY : View.SCROLLBARS_INSIDE_OVERLAY);
        if (view != null)
            view.setImageResource(image);
        if (!noReload)
            webviewReload();
    }

    private void setDeskMode(ImageView view, double mode, boolean noReload) {
        if (mode == 0) {
            setDesktopMode(view,
                    false,
                    userAgentFull("Linux; Android 12"),
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
        browservioBrowse(UrlEdit.getText().toString());
    }

    private void shareUrl(@Nullable String url) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, url == null ? webview.getUrl() : url);
        startActivity(Intent.createChooser(i, getResources().getString(R.string.linear_control_b5_title)));
    }

    public void itemSelected(ImageView view, int item) {
        if (item == 0) {
            if (webview.canGoBack())
                webview.goBack();
            else
                BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.error_already_page, getResources().getString(R.string.first)));
        } else if (item == 1) {
            if (webview.canGoForward())
                webview.goForward();
            else
                BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.error_already_page, getResources().getString(R.string.last)));
        } else if (item == 2) {
            webviewReload();
        } else if (item == 3) {
            browservioBrowse(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.defaultHomePage));
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
                    BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cache)));
                    webviewReload();
                } else if (_item.getTitle().toString().contains(getResources().getString(R.string.history))) {
                    webview.clearHistory();
                    HistoryReader.clear(MainActivity.this);
                    BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.history)));
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
                    BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cookies)));
                    webviewReload();
                }

                return false;
            });
            popupMenu.show();
        } else if (item == 7) {
            shareUrl(null);
        } else if (item == 8) {
            Intent intent = new Intent(this, NewSettingsActivity.class);
            mGetNeedLoad.launch(intent);
        } else if (item == 9) {
            Intent intent = new Intent(this, HistoryActivity.class);
            mGetNeedLoad.launch(intent);
        } else if (item == 10) {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
            Menu menu = popupMenu.getMenu();
            menu.add(getResources().getString(R.string.add_dot));
            menu.add(getResources().getString(R.string.fav));
            popupMenu.setOnMenuItemClickListener(_item -> {
                if (_item.getTitle().toString().equals(getResources().getString(R.string.add_dot))) {
                    BrowservioSaverUtils.setPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count, BrowservioSaverUtils.getPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count).isEmpty() ? "0" : String.valueOf((long) (Double.parseDouble(BrowservioSaverUtils.getPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count)) + 1)));
                    BrowservioSaverUtils.setPref(bookmarks(MainActivity.this), AllPrefs.bookmarked.concat(BrowservioSaverUtils.getPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count)), webview.getUrl());
                    BrowservioSaverUtils.setPref(bookmarks(MainActivity.this), AllPrefs.bookmarked.concat(BrowservioSaverUtils.getPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count)).concat(AllPrefs.bookmarked_count_title), UrlTitle);
                    BrowservioSaverUtils.setPref(bookmarks(MainActivity.this), AllPrefs.bookmarked.concat(BrowservioSaverUtils.getPref(bookmarks(MainActivity.this), AllPrefs.bookmarked_count)).concat(AllPrefs.bookmarked_count_show), "1");
                    BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.saved_su));
                } else if (_item.getTitle().toString().equals(getResources().getString(R.string.fav))) {
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
        } else if (item == 11) {
            finish();
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
        favicon = findViewById(R.id.favicon);

        MainActionBarRecycler.initMainActionBarRecycler(MainActivity.this, this, actionBar);

        favicon.setOnClickListener(_view -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, favicon);
            Menu menu = popupMenu.getMenu();
            menu.add(UrlTitle).setEnabled(false);
            menu.add(getResources().getString(R.string.copy_title));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equals(getResources().getString(R.string.copy_title))) {
                    BrowservioBasicUtil.copyClipboard(MainActivity.this, UrlTitle);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        faviconProgressBar.setOnClickListener(_view -> favicon.performClick());

        fab.setOnClickListener(_view -> RotateAlphaAnim(fabAnimate, barAnimate, fab, actionBar));

        webview.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            final WebView.HitTestResult hr = webview.getHitTestResult();
            final String url = hr.getExtra();
            final int type = hr.getType();

            if (type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.EDIT_TEXT_TYPE)
                return;

            MaterialAlertDialogBuilder webLongPress = new MaterialAlertDialogBuilder(MainActivity.this);
            webLongPress.setTitle(url);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.simple_list_item_1_daynight);
            if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE)
                arrayAdapter.add(getResources().getString(R.string.open_in_new_tab));
            if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)
                arrayAdapter.add(getResources().getString(R.string.download_image));
            arrayAdapter.add(getResources().getString(R.string.copy_url));
            arrayAdapter.add(getResources().getString(R.string.share_url));

            webLongPress.setAdapter(arrayAdapter, (dialog, which) -> {
                String strName = arrayAdapter.getItem(which);

                if (strName.equals(getResources().getString(R.string.copy_url))) {
                    BrowservioBasicUtil.copyClipboard(MainActivity.this, url);
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
    }

    /**
     * Welcome to the Browservio (The Shrek Browser)
     * This browser was originally designed with Sketchware
     * This project was started on Aug 13 2020
     * <p>
     * sur wen reel Sherk brower pls sand meme sum
     */
    @SuppressLint("AddJavascriptInterface")
    private void initializeLogic() {
        webview.setWebViewClient(new WebClient());
        webview.setWebChromeClient(new ChromeWebClient());

        webview.addJavascriptInterface(new browservioErrJsInterface(MainActivity.this, this), "browservioErr");

        /* Code for detecting return key presses */
        UrlEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == KeyEvent.ACTION_DOWN) {
                browservioBrowse(Objects.requireNonNull(UrlEdit.getText()).toString());
                return true;
            }
            return false;
        });

        UrlEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (text.toString().isEmpty() || BrowservioSaverUtils.getPrefNum(browservio_saver(MainActivity.this), AllPrefs.enableSuggestions) != 1)
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
                                    if (jsonArray == null)
                                        throw new RuntimeException("jsonArray is null.");
                                    final int MAX_RESULTS = 10;
                                    ArrayList<String> result = new ArrayList<>(Math.min(jsonArray.length(), MAX_RESULTS));
                                    for (int i = 0; i < jsonArray.length() && result.size() < MAX_RESULTS; i++) {
                                        String s = jsonArray.optString(i);
                                        if (s != null && !s.isEmpty())
                                            result.add(s);
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

        setDeskMode(null, 0, true); /* User agent init code */

        /* Start the download manager service */
        webview.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> downloadFile(url, contentDisposition, mimeType));

        /* Load default webpage */
        browservioBrowse(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.defaultHomePage));

        /* zoom related stuff - From SCMPNews project */
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);

        webview.setLayerType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
                View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE, null);

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
        } else if (Intent.ACTION_VIEW.equals(action)) { /* From default browser */
            for (String match : TypeSchemeMatch) {
                if (match.equals(type) || match.equals(scheme)) {
                    Uri uri = getIntent().getData();
                    browservioBrowse(uri.toString());
                }
            }
        }

        new HistoryInit(browservio_saver(MainActivity.this), historyPref(MainActivity.this));

        /* Import the list of Ad servers */
        String line;
        adServers = new StringBuilder();

        InputStream is = this.getResources().openRawResource(R.raw.hosts);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        if (is != null) {
            try {
                while ((line = br.readLine()) != null) {
                    adServers.append(line);
                    adServers.append(BrowservioBasicUtil.LINE_SEPARATOR());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class browservioErrJsInterface {
        final Context mContext;
        final MainActivity mMainActivity;

        browservioErrJsInterface(Context c, MainActivity mainActivity) {
            mContext = c;
            mMainActivity = mainActivity;
        }

        @JavascriptInterface
        public String errGetMsg(int msgId) {
            switch (msgId) {
                case 0:
                    return mContext.getResources().getString(R.string.errMsg0);
                case 1:
                    return mContext.getResources().getString(R.string.errMsg1);
                case 2:
                    return mContext.getResources().getString(R.string.errMsg2);
                case 3:
                    return mContext.getResources().getString(R.string.errMsg3);
                case 4:
                    return mContext.getResources().getString(R.string.errMsg4);
                default:
                    return BrowservioBasicUtil.EMPTY_STRING;
            }
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
                if (!HistoryReader.history_data(MainActivity.this).trim().endsWith(url))
                    HistoryReader.appendData(MainActivity.this, url);
            }
        }

        public void onPageStarted(WebView view, String url, Bitmap icon) {
            UrlSet(url);
            if (BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.showFavicon))) {
                favicon.setVisibility(View.GONE);
                faviconProgressBar.setVisibility(View.VISIBLE);
            }
            UrlEdit.dismissDropDown();
        }

        public void onPageFinished(WebView view, String url) {
            UrlSet(url);
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
            webview.loadUrl(BrowservioURLs.realErrUrl);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (BrowservioBasicUtil.appInstalledOrNot(getApplicationContext(), url) && !UrlUtils.startsWithMatch(url)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            } else {
                BrowservioBasicUtil.showMessage(getApplicationContext(), getResources().getString(R.string.app_not_installed));
            }
            if (customBrowse) {
                webview.loadUrl(url, mRequestHeaders);
                customBrowse = false;
                return true;
            }
            return false;
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

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            String list = String.valueOf(adServers);

            try {
                if (list.contains(new URL(url).getHost()) && BrowservioSaverUtils.getPrefNum(browservio_saver(MainActivity.this), AllPrefs.enableAdBlock) == 1)
                    return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(BrowservioBasicUtil.EMPTY_STRING.getBytes()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return super.shouldInterceptRequest(view, url);
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
        if (webview.canGoBack())
            webview.goBack();
        else
            finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (webViewEnabled()) {
            _configChecker();
        }
    }

    public void downloadFile(String url, String contentDisposition, String mimeType) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        // Let this downloaded file be scanned by MediaScanner - so that it can
        // show up in Gallery app, for example.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            request.allowScanningByMediaScanner();

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // Notify client once download is completed!
        final String filename = URLUtil.guessFileName(url, contentDisposition, mimeType);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(request);
    }

    /**
     * Browservio Browse URL checker & loader
     *
     * @param url is for strings of URL to check and load
     */
    private void browservioBrowse(String url) {
        if (url == null || url.isEmpty())
            return;
        String checkedUrl = UrlUtils.UrlChecker(url, true, BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.defaultSearch));
        // Load URL
        if (url.startsWith(BrowservioURLs.prefix)
                || url.equals(BrowservioURLs.realErrUrl)
                || url.equals(BrowservioURLs.realLicenseUrl)) {
            URLIdentify(url);
        } else {
            URLIdentify(checkedUrl);
            webview.loadUrl(checkedUrl, mRequestHeaders);
        }
        customBrowse = true;
        favicon.setImageResource(R.drawable.default_favicon); /* Reset favicon before getting real favicon */
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
        webview.getSettings().setDisplayZoomControls(false);

        // HTML5 API flags
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setAppCachePath(getCacheDir().getAbsolutePath());
        webview.getSettings().setDatabaseEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);

        // Do Not Track request
        mRequestHeaders.put("DNT", BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.sendDNT));

        if (BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver(MainActivity.this), AllPrefs.showFavicon))
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
     */
    private void URLIdentify(String url) {
        if (url.equals(BrowservioURLs.realErrUrl))
            webview.loadUrl(BrowservioURLs.realErrUrl);

        if (url.equals(BrowservioURLs.licenseUrl) || url.equals(BrowservioURLs.realLicenseUrl)) {
            UrlEdit.setText(BrowservioURLs.licenseUrl);
            webview.loadUrl(BrowservioURLs.realLicenseUrl);
        }

        if (url.equals(BrowservioURLs.reloadUrl))
            webviewReload();
    }
}
