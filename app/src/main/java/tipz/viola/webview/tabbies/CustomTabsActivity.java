/*
 * Copyright (C) 2022-2023 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.webview.tabbies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import tipz.viola.R;
import tipz.viola.utils.CommonUtils;
import tipz.viola.utils.UrlUtils;
import tipz.viola.webview.VioWebViewActivity;

public class CustomTabsActivity extends VioWebViewActivity {
    private AppCompatTextView title;
    private AppCompatTextView host;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.custom_tab);

        /* Back button */
        AppCompatImageView actionBarBack = findViewById(R.id.toolsContainer);
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
            String url = webview.getUrl();
            if (url == null)
                return;
            Intent intent = new Intent(CustomTabsActivity.this, BrowserActivity.class);
            intent.setData(Uri.parse(UrlUtils.cve_2017_13274(url)));
            startActivity(intent);
            finish();
        });

        /* Progress Bar */
        progressBar = findViewById(R.id.webviewProgressBar);

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
        super.onTitleUpdated(title);
        this.title.setText(title);
    }
}
