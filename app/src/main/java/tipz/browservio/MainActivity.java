package tipz.browservio;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
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
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import tipz.browservio.broha.BrohaListInterfaceActivity;
import tipz.browservio.broha.api.FavUtils;
import tipz.browservio.broha.api.HistoryUtils;
import tipz.browservio.broha.database.icons.IconHashClient;
import tipz.browservio.search.SuggestionAdapter;
import tipz.browservio.settings.SettingsActivity;
import tipz.browservio.settings.SettingsInit;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.DownloadUtils;
import tipz.browservio.webview.VioWebView;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity {
    private MaterialAutoCompleteTextView UrlEdit;
    private ProgressBar MainProg;
    private ProgressBar faviconProgressBar;
    private AppCompatImageView fab;
    private SwipeRefreshLayout swipeRefreshLayout;
    private VioWebView webview;
    private RelativeLayout actionBarBack;
    private AppCompatImageView favicon;

    private boolean currentPrebuiltUAState = false;
    private String currentCustomUA;
    private boolean currentCustomUAWideView = false;
    private IconHashClient iconHashClient;
    private SharedPreferences pref;

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
    public void onDestroy() {
        super.onDestroy();
        if (webview != null) {
            webview.stopLoading();
            webview.setWebViewClient(null);
            webview.setWebChromeClient(null);
            // According to the doc of WebView#destroy(), webview should be removed from the view
            // system before calling the WebView#destroy().
            ((ViewGroup) webview.getParent()).removeView(webview);
            webview.destroy();
        }
        if (!isChangingConfigurations()) {
            // For removing all WebView thread
            System.exit(0);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            webview.freeMemory();
    }

    // https://stackoverflow.com/a/57840629/10866268
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    private void shareUrl(@Nullable String url) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, url == null ? webview.currentUrl : url);
        startActivity(Intent.createChooser(i, getResources().getString(R.string.share_url_dialog_title)));
    }

    public void itemSelected(AppCompatImageView view, int item) {
        if (item == R.drawable.arrow_back_alt && webview.canGoBack()) {
            webview.goBack();
        } else if (item == R.drawable.arrow_forward_alt && webview.canGoForward()) {
            webview.goForward();
        } else if (item == R.drawable.refresh) {
            webview.webviewReload();
        } else if (item == R.drawable.home) {
            webview.loadUrl(SettingsUtils.getPref(pref, SettingsKeys.defaultHomePage));
        } else if (item == R.drawable.smartphone || item == R.drawable.desktop || item == R.drawable.custom) {
            currentPrebuiltUAState = !currentPrebuiltUAState;
            webview.setPrebuiltUAMode(view, currentPrebuiltUAState ? 1 : 0, false);
        } else if (item == R.drawable.new_tab) {
            Intent i = new Intent(this, MainActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            else
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            startActivity(i);
        } else if (item == R.drawable.delete) {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
            Menu menu = popupMenu.getMenu();
            menu.add(getResources().getString(R.string.clear, getResources().getString(R.string.cache)));
            menu.add(getResources().getString(R.string.clear, getResources().getString(R.string.history)));
            menu.add(getResources().getString(R.string.clear, getResources().getString(R.string.cookies)));
            popupMenu.setOnMenuItemClickListener(_item -> {
                if (_item.getTitle().toString().contains(getResources().getString(R.string.cache))) {
                    webview.clearCache(true);
                    CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.cleared_toast, getResources().getString(R.string.cache)));
                    webview.webviewReload();
                } else if (_item.getTitle().toString().contains(getResources().getString(R.string.history))) {
                    webview.clearHistory();
                    HistoryUtils.clear(MainActivity.this);
                    CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.cleared_toast, getResources().getString(R.string.history)));
                    webview.webviewReload();
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
                    webview.webviewReload();
                }

                return false;
            });
            popupMenu.show();
        } else if (item == R.drawable.share) {
            shareUrl(null);
        } else if (item == R.drawable.app_shortcut) {
            Drawable originalIcon = favicon.getDrawable();
            Bitmap icon = Bitmap.createBitmap(originalIcon.getIntrinsicWidth(), originalIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(icon);

            originalIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            originalIcon.draw(canvas);

            ShortcutManagerCompat.requestPinShortcut(this, new ShortcutInfoCompat.Builder(this, webview.UrlTitle)
                    .setShortLabel(webview.UrlTitle)
                    .setIcon(IconCompat.createWithBitmap(icon))
                    .setIntent(new Intent(this, MainActivity.class)
                            .setData(Uri.parse(webview.currentUrl))
                            .setAction(Intent.ACTION_VIEW))
                    .build(), null);
        } else if (item == R.drawable.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            mGetNeedLoad.launch(intent);
        } else if (item == R.drawable.history) {
            Intent intent = new Intent(MainActivity.this, BrohaListInterfaceActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, BrohaListInterfaceActivity.mode_history);
            mGetNeedLoad.launch(intent);
        } else if (item == R.drawable.favorites) {
            Drawable icon = favicon.getDrawable();
            FavUtils.appendData(this, iconHashClient, webview.UrlTitle, webview.currentUrl, icon instanceof BitmapDrawable ? ((BitmapDrawable) icon).getBitmap() : null);
            CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.save_successful));
        } else if (item == R.drawable.close) {
            finish();
        }
    }

    public void itemLongSelected(AppCompatImageView view, int item) {
        if (item == R.drawable.smartphone || item == R.drawable.desktop || item == R.drawable.custom) {
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
                            webview.setUA(view, deskMode.isChecked(),
                                    Objects.requireNonNull(customUserAgent.getText()).toString(),
                                    R.drawable.custom, false);
                        currentCustomUA = Objects.requireNonNull(customUserAgent.getText()).toString();
                        currentCustomUAWideView = deskMode.isChecked();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show();
            if (currentCustomUA != null)
                customUserAgent.setText(currentCustomUA);
        } else if (item == R.drawable.favorites) {
            Intent intent = new Intent(MainActivity.this, BrohaListInterfaceActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, BrohaListInterfaceActivity.mode_favorites);
            mGetNeedLoad.launch(intent);
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
            menu.add(webview.UrlTitle).setEnabled(false);
            menu.add(getResources().getString(R.string.copy_title));
            if (cert != null)
                menu.add(getResources().getString(R.string.ssl_info));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equals(getResources().getString(R.string.copy_title))) {
                    CommonUtils.copyClipboard(MainActivity.this, webview.UrlTitle);
                    return true;
                } else if (item.getTitle().toString().equals(getResources().getString(R.string.ssl_info))) {
                    assert cert != null;
                    final SslCertificate.DName issuedTo = cert.getIssuedTo();
                    final SslCertificate.DName issuedBy = cert.getIssuedBy();
                    final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(MainActivity.this);
                    dialog.setTitle(Uri.parse(webview.currentUrl).getHost())
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
                    DownloadUtils.dmDownloadFile(MainActivity.this, url,
                            null, null, webview.currentUrl);
                } else if (strName.equals(getResources().getString(R.string.search_image))) {
                    webview.loadUrl("http://images.google.com/searchbyimage?image_url=".concat(url));
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

        swipeRefreshLayout.setOnRefreshListener(() -> webview.webviewReload());
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        /* Code for detecting return key presses */
        UrlEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == KeyEvent.ACTION_DOWN) {
                webview.loadUrl(UrlEdit.getText().toString());
                UrlEdit.clearFocus();
                return true;
            }
            return false;
        });

        UrlEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (!UrlEdit.getText().toString().equals(webview.currentUrl))
                    UrlEdit.setText(webview.currentUrl);
                UrlEdit.setSelection(0);
                closeKeyboard();
            }
        });

        UrlEdit.setOnItemClickListener((adapterView, view, pos, l) -> {
            webview.loadUrl(((AppCompatTextView) view.findViewById(android.R.id.text1)).getText().toString());
            closeKeyboard();
        });

        UrlEdit.setAdapter(new SuggestionAdapter(MainActivity.this, R.layout.recycler_list_item_1));
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

        /* Init settings check */
        new SettingsInit(MainActivity.this);
        configChecker();

        /* Init VioWebView */
        webview.setUpFavicon(favicon, faviconProgressBar);
        webview.setUpUrlBox(UrlEdit);
        webview.setUpProgressBar(MainProg);
        webview.setUpSwipeRefreshLayout(swipeRefreshLayout);

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
                    webview.loadUrl(sharedText != null ? sharedText : CommonUtils.EMPTY_STRING);
                }
            }
        } else if (Intent.ACTION_VIEW.equals(action)) { /* From default browser */
            for (String match : TypeSchemeMatch) {
                if (match.equals(type) || match.equals(scheme)) {
                    Uri uri = getIntent().getData();
                    webview.loadUrl(uri.toString());
                }
            }
        } else { /* Load default webpage */
            webview.loadUrl(SettingsUtils.getPref(pref, SettingsKeys.defaultHomePage));
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
     * Need Load Info Receiver
     * <p>
     * Receive needLoadUrl for loading.
     */
    final ActivityResultLauncher<Intent> mGetNeedLoad = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                configChecker();
                webview.loadUrl(result.getData() != null ? result.getData().getStringExtra("needLoadUrl") : null);
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

        // Pull to Refresh
        swipeRefreshLayout.setEnabled(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.enableSwipeRefresh)));

        // Settings check
        favicon.setVisibility(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon)) ? View.VISIBLE : View.GONE);
        actionBarBack.setGravity(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.centerActionBar)) ? Gravity.CENTER_HORIZONTAL : Gravity.NO_GRAVITY);

        if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon))
                && faviconProgressBar.getVisibility() == View.VISIBLE)
            favicon.setVisibility(View.GONE);

        // Do VioWebView settings check
        webview.doSettingsCheck();
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
            holder.mImageView.setOnClickListener(view -> mMainActivity.get().itemSelected(holder.mImageView, actionBarItemList.get(position)));
            holder.mImageView.setOnLongClickListener(view -> {
                mMainActivity.get().itemLongSelected(holder.mImageView, actionBarItemList.get(position));
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return actionBarItemList.size();
        }
    }
}
