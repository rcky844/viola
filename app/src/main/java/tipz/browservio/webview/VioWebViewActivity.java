package tipz.browservio.webview;

import android.graphics.Bitmap;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class VioWebViewActivity extends AppCompatActivity implements VioWebViewInterface {
    public VioWebView webview;
    public ProgressBar progressBar;
    public SwipeRefreshLayout swipeRefreshLayout;

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
