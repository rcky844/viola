package tipz.browservio.webview.tabbies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import tipz.browservio.R;
import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.UrlUtils;
import tipz.browservio.webview.VioWebViewActivity;

public class CustomTabsActivity extends VioWebViewActivity {
    private AppCompatTextView title;
    private AppCompatTextView host;

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
        share.setOnClickListener(v -> CommonUtils.shareUrl(this, webview.getUrl()));

        /* Open in Browser */
        AppCompatImageView open_browser = findViewById(R.id.open_browser);
        open_browser.setOnClickListener(v -> {
            Intent intent = new Intent(CustomTabsActivity.this, BrowserActivity.class);
            intent.setData(Uri.parse(UrlUtils.cve_2017_13274(webview.getUrl())));
            startActivity(intent);
            finish();
        });

        /* Progress Bar */
        progressBar = findViewById(R.id.MainProg);

        /* Swipe Refresh Layout */
        swipeRefreshLayout = findViewById(R.id.layout_webview);

        /* WebView */
        webview = swipeRefreshLayout.findViewById(R.id.webview);
        webview.notifyViewSetup();
        webview.setUpdateHistory(false);
        Uri dataUri = getIntent().getData();
        if (dataUri != null)
            webview.loadUrl(dataUri.toString());
    }

    @Override
    public void onUrlUpdated(String url) {
        this.host.setText(Uri.parse(UrlUtils.cve_2017_13274(url)).getHost());
    }

    @Override
    public void onTitleUpdated(String title) {
        this.title.setText(title);
    }
}
