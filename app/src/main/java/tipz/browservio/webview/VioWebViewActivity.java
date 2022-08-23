package tipz.browservio.webview;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.CallSuper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import tipz.browservio.R;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class VioWebViewActivity extends AppCompatActivity implements VioWebViewInterface {
    private SharedPreferences pref;

    public VioWebView webview;
    public AppCompatImageView favicon;
    public ProgressBar faviconProgressBar;
    public ProgressBar progressBar;
    public SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = browservio_saver(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        /* Init VioWebView */
        webview.doSettingsCheck();
        doSettingsCheck();

        // Setup swipe refresh layout
        swipeRefreshLayout.setOnRefreshListener(() -> webview.webviewReload());
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        // Setup favicon
        faviconProgressBar.setOnClickListener(_view -> favicon.performClick());
    }

    /**
     * Need Load Info Receiver
     * <p>
     * Receive needLoadUrl for loading.
     */
    public final ActivityResultLauncher<Intent> mGetNeedLoad = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                doSettingsCheck();
                webview.loadUrl(result.getData() != null ? result.getData().getStringExtra("needLoadUrl") : null);
            });

    /**
     * Config Checker
     * <p>
     * Used to check if anything has been changed
     * after returning from settings.
     */
    @CallSuper
    public void doSettingsCheck() {
        // Pull to Refresh
        swipeRefreshLayout.setEnabled(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.enableSwipeRefresh)));

        // Favicon
        favicon.setVisibility(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon)) ? View.VISIBLE : View.GONE);
        if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon))
                && faviconProgressBar.getVisibility() == View.VISIBLE)
            favicon.setVisibility(View.GONE);
    }

    @Override
    public void onUrlUpdated(String url) {

    }

    @Override
    public void onTitleUpdated(String title) {

    }

    @Override
    public void onDropDownDismissed() {

    }

    @Override
    public void onFaviconUpdated(Bitmap icon, boolean checkInstance) {
        if (checkInstance && (favicon.getDrawable() instanceof BitmapDrawable))
            return;

        if (icon == null)
            favicon.setImageResource(R.drawable.default_favicon);
        else
            favicon.setImageBitmap(icon);
    }

    @Override
    public void onFaviconProgressUpdated(boolean isLoading) {
        if (!CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.showFavicon)))
            return;

        if (isLoading) {
            favicon.setVisibility(View.GONE);
            faviconProgressBar.setVisibility(View.VISIBLE);
        } else {
            favicon.setVisibility(View.VISIBLE);
            faviconProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSwipeRefreshLayoutRefreshingUpdated(boolean isRefreshing) {
        swipeRefreshLayout.setRefreshing(isRefreshing);
    }

    @Override
    public void onPageLoadProgressChanged(int progress) {
        progressBar.setProgress(progress == 100 ? 0 : progress);
    }
}
