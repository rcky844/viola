package tipz.browservio.webview;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.CallSuper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import tipz.browservio.R;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.CommonUtils;

public class VioWebViewActivity extends AppCompatActivity implements VioWebViewInterface {
    private SharedPreferences pref;

    public VioWebView webview;
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

        // Setup swipe refresh layout
        swipeRefreshLayout.setOnRefreshListener(() -> webview.webviewReload());
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
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

    }

    @Override
    public void onFaviconProgressUpdated(boolean isLoading) {

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
