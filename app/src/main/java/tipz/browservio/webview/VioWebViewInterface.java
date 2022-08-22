package tipz.browservio.webview;

public interface VioWebViewInterface {
    void onUrlUpdated(String url);
    void onTitleUpdated(String title);
    void onDropDownDismissed();
}
