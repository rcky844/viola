package tipz.browservio.webview;

import android.graphics.Bitmap;

public interface VioWebViewInterface {
    void onUrlUpdated(String url);
    void onUrlUpdated(String url, int position);
    void onTitleUpdated(String title);
    void onDropDownDismissed();
    void onFaviconUpdated(Bitmap icon, boolean checkInstance);
    void onFaviconProgressUpdated(boolean isLoading);
    void onSwipeRefreshLayoutRefreshingUpdated(boolean isRefreshing);
    void onPageLoadProgressChanged(int progress);
}
