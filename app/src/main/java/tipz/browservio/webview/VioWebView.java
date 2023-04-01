package tipz.browservio.webview;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import androidx.webkit.WebViewRenderProcess;
import androidx.webkit.WebViewRenderProcessClient;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

import tipz.browservio.Application;
import tipz.browservio.BrowservioActivity;
import tipz.browservio.BuildConfig;
import tipz.browservio.R;
import tipz.browservio.broha.api.HistoryApi;
import tipz.browservio.broha.api.HistoryUtils;
import tipz.browservio.broha.database.Broha;
import tipz.browservio.broha.database.icons.IconHashClient;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.BrowservioURLs;
import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.DownloadUtils;
import tipz.browservio.utils.DownloaderThread;
import tipz.browservio.utils.UrlUtils;
import tipz.browservio.webview.tabbies.BrowserActivity;

@SuppressLint("SetJavaScriptEnabled")
public class VioWebView extends WebView {
    private final Context mContext;
    private VioWebViewActivity mVioWebViewActivity;
    private final IconHashClient iconHashClient;
    private final WebSettings webSettings;
    private final WebViewRenderProcess mWebViewRenderProcess;

    private String currentUrl;
    private String adServers;
    private Broha currentBroha;
    private boolean updateHistory = true;
    private boolean historyCommitted = false;
    private final SharedPreferences pref;
    private ValueCallback<Uri[]> mUploadMessage;
    private final ActivityResultLauncher<String> mFileChooser;
    private final HashMap<String, String> mRequestHeaders = new HashMap<>();

    private static final String template = "<html>\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n<head>\n<title>$0</title>\n</head>\n<body>\n<div style=\"padding-left: 8vw; padding-top: 12vh;\">\n<div>\n<svg xmlns=\"http://www.w3.org/2000/svg\" enable-background=\"new 0 0 24 24\" height=\"96px\" viewBox=\"0 0 24 24\" width=\"96px\" fill=\"currentColor\">\n<g>\n<rect fill=\"none\" height=\"24\" width=\"24\"/>\n<path d=\"M11,8.17L6.49,3.66C8.07,2.61,9.96,2,12,2c5.52,0,10,4.48,10,10c0,2.04-0.61,3.93-1.66,5.51l-1.46-1.46 C19.59,14.87,20,13.48,20,12c0-3.35-2.07-6.22-5-7.41V5c0,1.1-0.9,2-2,2h-2V8.17z M21.19,21.19l-1.41,1.41l-2.27-2.27 C15.93,21.39,14.04,22,12,22C6.48,22,2,17.52,2,12c0-2.04,0.61-3.93,1.66-5.51L1.39,4.22l1.41-1.41L21.19,21.19z M11,18 c-1.1,0-2-0.9-2-2v-1l-4.79-4.79C4.08,10.79,4,11.38,4,12c0,4.08,3.05,7.44,7,7.93V18z\"/>\n</g>\n</svg>\n</div>\n<div>\n<p style=\"font-family:sans-serif; font-weight: bold; font-size: 24px; margin-top: 24px; margin-bottom: 8px;\">$1</p>\n<p style=\"font-family:sans-serif; font-size: 16px; margin-top: 8px; margin-bottom: 24px;\">$2</p>\n<p style=\"font-family:sans-serif; font-weight: bold; font-size: 16px; margin-bottom: 8px;\">$3</p>\n<ul style=\"font-family:sans-serif; font-size: 16px; margin-top: 0px; margin-bottom: 0px;\">\n<li>$4</li>\n<li>$5</li>\n</ul>\n<p style=\"font-family:sans-serif; font-size: 12px; margin-bottom: 8px; color: #808080;\">$6</p>\n</div>\n</div>\n</body>\n</html>";

    private String userAgentFull(double mode) {
        PackageInfo info = WebViewCompat.getCurrentWebViewPackage(mContext);
        String webkitVersion = info == null ? "534.30" : "537.36";
        return "Mozilla/5.0 (".concat("Linux; Device with Browservio ").concat(BuildConfig.VERSION_NAME)
                .concat(BuildConfig.VERSION_TECHNICAL_EXTRA).concat(") AppleWebKit/")
                .concat(webkitVersion).concat(" KHTML, like Gecko) Chrome/")
                .concat(info == null ? "12.0.742" : info.versionName).concat(mode == 0 ? " Mobile " : " ")
                .concat("Safari/").concat(webkitVersion);
    }

    public VioWebView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mWebViewRenderProcess = WebViewFeature.isFeatureSupported(WebViewFeature.GET_WEB_VIEW_RENDERER) ?
                WebViewCompat.getWebViewRenderProcess(this) : null;
        pref = ((Application) context.getApplicationContext()).pref;
        iconHashClient = ((Application) mContext.getApplicationContext()).iconHashClient;
        webSettings = this.getSettings();
        mFileChooser = ((AppCompatActivity) mContext).registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (null == mUploadMessage || uri == null)
                        return;

                    mUploadMessage.onReceiveValue(new Uri[]{uri});
                    mUploadMessage = null;
                });

        /* User agent init code */
        this.setPrebuiltUAMode(null, 0, true);

        /* Start the download manager service */
        this.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            DownloadUtils.dmDownloadFile(mContext, url, contentDisposition,
                    mimeType, currentUrl);
            updateCurrentUrl(getOriginalUrl());
            mVioWebViewActivity.onPageLoadProgressChanged(0);
            if (!canGoBack() && getOriginalUrl() == null
                    && CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(
                    pref, SettingsKeys.closeAppAfterDownload)))
                mVioWebViewActivity.finish();
        });

        this.setLayerType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ?
                View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE, null);

        /* zoom related stuff - From SCMPNews project */
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        // Also increase text size to fill the viewport (this mirrors the behaviour of Firefox,
        // Chrome does this in the current Chrome Dev, but not Chrome release).
        webSettings.setLayoutAlgorithm(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                ? WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING : WebSettings.LayoutAlgorithm.NORMAL);

        webSettings.setDisplayZoomControls(false);
        webSettings.setAllowFileAccess(false);
        webSettings.setAllowContentAccess(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(false);
            webSettings.setAllowUniversalAccessFromFileURLs(false);
        }

        /* HTML5 API flags */
        webSettings.setDatabaseEnabled(false);
        webSettings.setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
            WebIconDatabase.getInstance().open(mContext.getDir("icons", MODE_PRIVATE).getPath());

        this.setWebViewClient(new WebClient());
        this.setWebChromeClient(new ChromeWebClient());
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_VIEW_RENDERER_CLIENT_BASIC_USAGE) && mWebViewRenderProcess != null)
            WebViewCompat.setWebViewRenderProcessClient(this, new RenderClient());

        this.removeJavascriptInterface("searchBoxJavaBridge_"); /* CVE-2014-1939 */
        this.removeJavascriptInterface("accessibility"); /* CVE-2014-7224 */
        this.removeJavascriptInterface("accessibilityTraversal"); /* CVE-2014-7224 */

        /* Hit Test Menu */
        this.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            final WebView.HitTestResult hr = this.getHitTestResult();
            final String url = hr.getExtra();
            final int type = hr.getType();

            if (type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.EDIT_TEXT_TYPE)
                return;

            MaterialAlertDialogBuilder webLongPress = new MaterialAlertDialogBuilder(mContext);
            webLongPress.setTitle(url.length() > 75 ? url.substring(0, 74).concat("…") : url);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, R.layout.recycler_list_item_1);
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
                    CommonUtils.copyClipboard(mContext, url);
                } else if (strName.equals(getResources().getString(R.string.download_image))) {
                    DownloadUtils.dmDownloadFile(mContext, url,
                            null, null, this.getUrl());
                } else if (strName.equals(getResources().getString(R.string.search_image))) {
                    this.loadUrl("http://images.google.com/searchbyimage?image_url=".concat(url));
                } else if (strName.equals(getResources().getString(R.string.open_in_new_tab))) {
                    Intent intent = new Intent(mContext, BrowserActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, url)
                            .setAction(Intent.ACTION_SEND)
                            .setType(UrlUtils.TypeSchemeMatch[1]);
                    mContext.startActivity(intent);
                } else if (strName.equals(getResources().getString(R.string.share_url))) {
                    CommonUtils.shareUrl(mContext, url);
                }
            });

            webLongPress.show();
        });
    }

    @SuppressWarnings("deprecation")
    public void doSettingsCheck() {
        // Dark mode
        boolean darkMode = BrowservioActivity.getDarkMode(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING))
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webSettings, darkMode);
        else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK))
            WebSettingsCompat.setForceDark(webSettings,
                    darkMode ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF);

        // Settings check
        webSettings.setJavaScriptEnabled(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.isJavaScriptEnabled)));
        webSettings.setJavaScriptCanOpenWindowsAutomatically(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.isJavaScriptEnabled)));

        // HTTPS enforce setting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            webSettings.setMixedContentMode(CommonUtils.isIntStrOne(
                    SettingsUtils.getPrefNum(pref, SettingsKeys.enforceHttps)) ?
                    WebSettings.MIXED_CONTENT_NEVER_ALLOW : WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Google's "Safe" Browsing
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE))
            WebSettingsCompat.setSafeBrowsingEnabled(webSettings,
                    CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.enableGoogleSafeBrowse)));

        // Do Not Track request
        mRequestHeaders.put("DNT", String.valueOf(SettingsUtils.getPrefNum(pref, SettingsKeys.sendDNT)));
    }

    public void notifyViewSetup() {
        mVioWebViewActivity = (VioWebViewActivity) mContext;
        doSettingsCheck();
    }

    public void setUpdateHistory(boolean value) {
        updateHistory = value;
    }

    @Override
    public void loadUrl(String url) {
        if (url == null || url.isEmpty())
            return;

        String urlIdentify = URLIdentify(url);
        if (urlIdentify.equals(CommonUtils.EMPTY_STRING))
            return;

        String checkedUrl = UrlUtils.toSearchOrValidUrl(mContext, urlIdentify);
        updateCurrentUrl(checkedUrl);

        // Load URL
        super.loadUrl(checkedUrl, mRequestHeaders);
    }

    @Override
    public String getUrl() {
        return currentUrl;
    }

    @Override
    public void goBack() {
        mVioWebViewActivity.onDropDownDismissed();
        super.goBack();
    }

    @Override
    public void goForward() {
        mVioWebViewActivity.onDropDownDismissed();
        super.goForward();
    }

    private void updateCurrentUrl(String url) {
        mVioWebViewActivity.onUrlUpdated(url);
        currentUrl = url;
    }

    /**
     * WebViewClient
     */
    public class WebClient extends WebViewClientCompat {
        private void UrlSet(String url) {
            if (!currentUrl.equals(url) && urlShouldSet(url) || currentUrl == null) {
                updateCurrentUrl(url);
                SettingsUtils.setPrefNum(pref, SettingsKeys.foolsCoinCount, SettingsUtils.getPrefNum(pref, SettingsKeys.foolsCoinCount) + 1);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap icon) {
            UrlSet(url);
            mVioWebViewActivity.onFaviconProgressUpdated(true);
            mVioWebViewActivity.onFaviconUpdated(null, false);
            mVioWebViewActivity.onDropDownDismissed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (view.getOriginalUrl() == null || view.getOriginalUrl().equals(url))
                this.doUpdateVisitedHistory(view, url, true);

            mVioWebViewActivity.onFaviconProgressUpdated(false);
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            UrlSet(url);
            if (updateHistory) {
                currentBroha = new Broha(getTitle(), currentUrl);
                historyCommitted = false;
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
                CookieSyncManager.getInstance().sync();
            else
                CookieManager.getInstance().flush();
            mVioWebViewActivity.onFaviconUpdated(null, true);
            mVioWebViewActivity.onSwipeRefreshLayoutRefreshingUpdated(false);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            String returnVal = template;
            for (int i = 0; i < 6; i++)
                returnVal = returnVal.replace("$".concat(Integer.toString(i)),
                        mContext.getResources().getStringArray(R.array.errMsg)[i]);
            returnVal = returnVal.replace("$6", description);

            view.loadDataWithBaseURL(null, returnVal, "text/html", "UTF-8", null);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (UrlUtils.isUriHttp(url))
                return false;

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(UrlUtils.cve_2017_13274(url)));
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
                CommonUtils.showMessage(mContext, getResources().getString(R.string.toast_no_app_to_handle));
            }
            return true;
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(mContext);
            String content_summary = getResources().getString(R.string.ssl_certificate_unknown);
            switch (error.getPrimaryError()) {
                case SslError.SSL_DATE_INVALID:
                    content_summary = getResources().getString(R.string.ssl_certificate_date_invalid);
                    break;
                case SslError.SSL_INVALID:
                    content_summary = getResources().getString(R.string.ssl_certificate_invalid);
                    break;
                case SslError.SSL_EXPIRED:
                    content_summary = getResources().getString(R.string.ssl_certificate_expired);
                    break;
                case SslError.SSL_IDMISMATCH:
                    content_summary = getResources().getString(R.string.ssl_certificate_idmismatch);
                    break;
                case SslError.SSL_NOTYETVALID:
                    content_summary = getResources().getString(R.string.ssl_certificate_notyetvalid);
                    break;
                case SslError.SSL_UNTRUSTED:
                    content_summary = getResources().getString(R.string.ssl_certificate_untrusted);
                    break;
            }

            dialog.setTitle(getResources().getString(R.string.ssl_certificate_error_dialog_title))
                    .setMessage(getResources().getString(R.string.ssl_certificate_error_dialog_content, content_summary))
                    .setPositiveButton(getResources().getString(android.R.string.ok), (_dialog, _which) -> handler.proceed())
                    .setNegativeButton(getResources().getString(android.R.string.cancel), (_dialog, _which) -> handler.cancel())
                    .create().show();
        }

        @Override
        public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            return false;
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
                WindowCompat.getInsetsController(((AppCompatActivity) mContext).getWindow(), ((AppCompatActivity) mContext).getWindow().getDecorView());

        WindowCompat.setDecorFitsSystemWindows(((AppCompatActivity) mContext).getWindow(), !enable);

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
            ((AppCompatActivity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mCustomViewCallback = viewCallback;
            setImmersiveMode(true);
            ((FrameLayout) ((AppCompatActivity) mContext).getWindow().getDecorView()).addView(mCustomView, new FrameLayout.LayoutParams(-1, -1));
            ((AppCompatActivity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public void onHideCustomView() {
            ((AppCompatActivity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            ((FrameLayout) ((Activity) mContext).getWindow().getDecorView()).removeView(mCustomView);
            mCustomView = null;
            setImmersiveMode(false);
            ((AppCompatActivity) mContext).setRequestedOrientation(getResources().getConfiguration().orientation);
            mCustomViewCallback.onCustomViewHidden();
            mCustomViewCallback = null;
        }

        @Override
        public void onProgressChanged(WebView view, int progress) {
            mVioWebViewActivity.onPageLoadProgressChanged(progress);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            mVioWebViewActivity.onFaviconUpdated(icon, false);
            if (!historyCommitted && updateHistory) {
                currentBroha.setIconHash(iconHashClient.save(icon));
                currentBroha.setTitle(getTitle()); // For making sure title is up to date
                if (!HistoryUtils.lastUrl(mContext).equals(currentUrl))
                    HistoryApi.historyBroha(mContext).insertAll(currentBroha);
                historyCommitted = true;
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            mVioWebViewActivity.onTitleUpdated(title);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
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

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            jsDialog(url, message, null, result, R.string.js_page_says);
            return true;
        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
            jsDialog(url, message, null, result, R.string.js_leave_page_prompt);
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            jsDialog(url, message, null, result, R.string.js_page_says);
            return true;
        }

        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            jsDialog(url, message, defaultValue, result, R.string.js_page_says);
            return true;
        }
    }

    /**
     * WebViewRenderProcessClient
     */
    public class RenderClient extends WebViewRenderProcessClient {
        AlertDialog dialog = new MaterialAlertDialogBuilder(mContext)
                .setTitle(R.string.dialog_page_unresponsive_title)
                .setMessage(R.string.dialog_page_unresponsive_message)
                .setPositiveButton(R.string.dialog_page_unresponsive_wait, null)
                .setNegativeButton(R.string.dialog_page_unresponsive_terminate, (_dialog, _which) ->
                        mWebViewRenderProcess.terminate())
                .create();

        @Override
        public void onRenderProcessUnresponsive(@NonNull WebView view, @Nullable WebViewRenderProcess renderer) {
            dialog.show();
        }

        @Override
        public void onRenderProcessResponsive(@NonNull WebView view, @Nullable WebViewRenderProcess renderer) {
            dialog.dismiss();
        }
    }

    private void jsDialog(String url, String message, String defaultValue, JsResult result, int titleResId) {
        final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        @SuppressLint("InflateParams") final View root = layoutInflater.inflate(R.layout.dialog_edittext, null);
        final AppCompatEditText jsMessage = root.findViewById(R.id.edittext);
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(mContext);
        dialog.setTitle(mContext.getResources().getString(titleResId, url))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                    if (defaultValue == null)
                        result.confirm();
                    else
                        ((JsPromptResult) result).confirm(
                                Objects.requireNonNull(jsMessage.getText()).toString());
                })
                .setNegativeButton(android.R.string.cancel, (_dialog, _which) -> {
                    result.cancel();
                    mVioWebViewActivity.onFaviconProgressUpdated(false);
                    mVioWebViewActivity.onPageLoadProgressChanged(0);
                });

        if (defaultValue != null)
            dialog.setView(root);

        dialog.create().show();
    }

    /* Function to update the list of Ad servers */
    private void updateAdServerList() {
        adServers = CommonUtils.EMPTY_STRING;
        DownloaderThread mHandlerThread = new DownloaderThread("adServers");
        mHandlerThread.start();
        mHandlerThread.setCallerHandler(new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DownloaderThread.TYPE_SUCCESS:
                        String data = msg.getData().getString(DownloaderThread.MSG_RESPONSE);
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
                        break;
                    case DownloaderThread.TYPE_FAILED:
                        adServers = null;
                        break;
                }
                mHandlerThread.quit();
                super.handleMessage(msg);
            }
        });
        mHandlerThread.startDownload("https://raw.githubusercontent.com/AdAway/adaway.github.io/master/hosts.txt");
    }

    private boolean urlShouldSet(String url) {
        return !(url.equals("about:blank")
                || url.equals(BrowservioURLs.realLicenseUrl));
    }

    public void setUA(AppCompatImageView view, Boolean enableDesktop, String ua, Integer image, boolean noReload) {
        webSettings.setUserAgentString(ua);
        webSettings.setLoadWithOverviewMode(enableDesktop);
        webSettings.setUseWideViewPort(enableDesktop);
        super.setScrollBarStyle(enableDesktop ? WebView.SCROLLBARS_OUTSIDE_OVERLAY : View.SCROLLBARS_INSIDE_OVERLAY);
        if (view != null) {
            view.setImageResource(image);
            view.setTag(image);
        }
        if (!noReload)
            webviewReload();
    }

    public void setPrebuiltUAMode(AppCompatImageView view, double mode, boolean noReload) {
        setUA(view,
                mode == 1,
                userAgentFull(mode),
                mode == 0 ? R.drawable.smartphone : R.drawable.desktop,
                noReload);
    }

    public void webviewReload() {
        super.loadUrl(currentUrl);
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
    @NonNull
    private String URLIdentify(String url) {
        if (url.equals(BrowservioURLs.licenseUrl) || url.equals(BrowservioURLs.realLicenseUrl))
            return BrowservioURLs.realLicenseUrl;

        if (url.equals(BrowservioURLs.reloadUrl)) {
            webviewReload();
            return CommonUtils.EMPTY_STRING;
        }

        return url;
    }
}
