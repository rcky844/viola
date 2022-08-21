package tipz.browservio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.UrlUtils;
import tipz.browservio.webview.VioWebView;

public class CustomTabsActivity extends AppCompatActivity {
    private AppCompatTextView title;
    private AppCompatTextView host;
    private VioWebView webview;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.custom_tab);

        /* Back button */
        AppCompatImageView actionBarBack = findViewById(R.id.actionBarBack);
        actionBarBack.setOnClickListener(v -> finish());

        /* Title and Host */
        title = findViewById(R.id.title);
        host = findViewById(R.id.host);

        /* Share */
        AppCompatImageView share = findViewById(R.id.share);
        share.setOnClickListener(v -> CommonUtils.shareUrl(this, webview.currentUrl));

        /* Open in Browser */
        AppCompatImageView open_browser = findViewById(R.id.open_browser);
        open_browser.setOnClickListener(v -> {
            Intent intent = new Intent(CustomTabsActivity.this, BrowserActivity.class);
            intent.setData(Uri.parse(UrlUtils.cve_2017_13274(webview.currentUrl)));
            startActivity(intent);
            finish();
        });

        /* Swipe Refresh Layout */
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe);

        /* Progress Bar */
        ProgressBar MainProg = findViewById(R.id.MainProg);

        /* WebView */
        webview = findViewById(R.id.webview);
        Uri dataUri = getIntent().getData();
        if (dataUri != null)
            webview.loadUrl(dataUri.toString());
        webview.setUpProgressBar(MainProg);
        webview.setUpSwipeRefreshLayout(swipeRefreshLayout);
        webview.setUrlTitleUpdates(true);
    }

    public void onReceivedTitle(String urlTitle) {
        title.setText(urlTitle);
        host.setText(Uri.parse(webview.currentUrl).getHost());
    }
}
