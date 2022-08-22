package tipz.browservio.webview;

import static android.content.Context.MODE_PRIVATE;
import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewClientCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

import tipz.browservio.Application;
import tipz.browservio.BuildConfig;
import tipz.browservio.R;
import tipz.browservio.broha.api.HistoryUtils;
import tipz.browservio.broha.database.icons.IconHashClient;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.BrowservioURLs;
import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.DownloadUtils;
import tipz.browservio.utils.UrlUtils;

public class VioWebView extends WebView {
    private final Context mContext;
    private ProgressBar progressBar;
    private AppCompatImageView favicon;
    private ProgressBar faviconProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final IconHashClient iconHashClient;
    private final WebSettings webSettings;

    public String UrlTitle;
    public String currentUrl;
    private String adServers;
    private boolean customBrowse = false;
    private boolean updateHistory = true;
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
        pref = browservio_saver(context);
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
        this.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) ->
                DownloadUtils.dmDownloadFile(mContext, url, contentDisposition,
                        mimeType, currentUrl));

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

        this.removeJavascriptInterface("searchBoxJavaBridge_"); /* CVE-2014-1939 */
        this.removeJavascriptInterface("accessibility"); /* CVE-2014-7224 */
        this.removeJavascriptInterface("accessibilityTraversal"); /* CVE-2014-7224 */
    }

    public void doSettingsCheck() {
        // Dark mode
        boolean darkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES;
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

        // Do Not Track request
        mRequestHeaders.put("DNT", String.valueOf(SettingsUtils.getPrefNum(pref, SettingsKeys.sendDNT)));
    }

    public void setUpFavicon(AppCompatImageView favicon, ProgressBar faviconProgressBar) {
        this.favicon = favicon;
        this.faviconProgressBar = faviconProgressBar;
    }

    public void setUpProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setUpSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    public void setUpdateHistory(boolean updateHistory) {
        this.updateHistory = updateHistory;
    }

    @Override
    public void loadUrl(String url) {
        if (url == null || url.isEmpty())
            return;

        String urlIdentify = URLIdentify(url);
        if (urlIdentify != null) {
            if (!urlIdentify.equals(CommonUtils.EMPTY_STRING)) {
                currentUrl = urlIdentify;
                super.loadUrl(urlIdentify);
            }
            return;
        }

        String checkedUrl = UrlUtils.UrlChecker(pref, url,
                CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.enforceHttps)));

        currentUrl = checkedUrl;
        // Load URL
        super.loadUrl(checkedUrl, mRequestHeaders);
        customBrowse = true;
    }

    /**
     * WebViewClient
     */
    public class WebClient extends WebViewClientCompat {
        private void UrlSet(String url, boolean update) {
            if (!currentUrl.equals(url) && urlShouldSet(url) || currentUrl == null) {
                ((VioWebViewActivity) mContext).onUrlUpdated(url);
                currentUrl = url;
                if (update)
                    HistoryUtils.updateData(mContext, null, null, url, null);
                else if (HistoryUtils.isEmptyCheck(mContext) || !HistoryUtils.lastUrl(mContext).equals(url))
                    HistoryUtils.appendData(mContext, url);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap icon) {
            UrlSet(url, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                ((AppCompatActivity) mContext).setTaskDescription(new ActivityManager.TaskDescription(CommonUtils.EMPTY_STRING));
            if (favicon != null) {
                if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon))
                        && urlShouldSet(url)) {
                    favicon.setVisibility(View.GONE);
                    faviconProgressBar.setVisibility(View.VISIBLE);
                }
                favicon.setImageResource(R.drawable.default_favicon);
            }
            ((VioWebViewActivity) mContext).onDropDownDismissed();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (view.getOriginalUrl() == null || view.getOriginalUrl().equals(url))
                this.doUpdateVisitedHistory(view, url, true);

            if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon))
                && favicon != null) {
                favicon.setVisibility(View.VISIBLE);
                faviconProgressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            UrlSet(url, true);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH)
                CookieSyncManager.getInstance().sync();
            else
                CookieManager.getInstance().flush();
            if (favicon != null) {
                if (!(favicon.getDrawable() instanceof BitmapDrawable))
                    favicon.setImageResource(R.drawable.default_favicon);
            }
            if (swipeRefreshLayout != null)
                swipeRefreshLayout.setRefreshing(false);
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
            boolean returnVal = false;
            boolean normalSchemes = UrlUtils.startsWithMatch(url);
            if (!normalSchemes) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(UrlUtils.cve_2017_13274(url)));
                    ((AppCompatActivity) mContext).startActivity(intent);
                } catch (ActivityNotFoundException ignored) {
                    view.stopLoading();
                }
                returnVal = true;
            }
            if (!customBrowse && normalSchemes) {
                view.loadUrl(url);
                returnVal = true;
            }
            customBrowse = false;
            return returnVal;
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
            if (progressBar != null)
                progressBar.setProgress(progress == 100 ? 0 : progress);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            if (favicon != null)
                favicon.setImageBitmap(icon);
            if (updateHistory)
                HistoryUtils.updateData(mContext, iconHashClient, null, null, icon);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            UrlTitle = title;
            if (updateHistory && urlShouldSet(currentUrl) && title != null)
                HistoryUtils.updateData(mContext, null, title, null, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                ((AppCompatActivity) mContext).setTaskDescription(new ActivityManager.TaskDescription(title));
            ((VioWebViewActivity) mContext).onTitleUpdated(title);
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
    @Nullable
    private String URLIdentify(String url) {
        if (url.equals(BrowservioURLs.licenseUrl) || url.equals(BrowservioURLs.realLicenseUrl))
            return BrowservioURLs.realLicenseUrl;

        if (url.equals(BrowservioURLs.reloadUrl)) {
            webviewReload();
            return CommonUtils.EMPTY_STRING;
        }

        if (url.startsWith(BrowservioURLs.yhlPrefix))
            return "http://119.28.42.46:8886/chaxun_web.asp?kd_id=".concat(url.replace(BrowservioURLs.yhlPrefix, CommonUtils.EMPTY_STRING));

        return null;
    }
}
