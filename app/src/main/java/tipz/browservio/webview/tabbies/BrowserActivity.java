/*
 * Copyright (C) 2020-2023 Tipz Team
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
package tipz.browservio.webview.tabbies;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Build;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import tipz.browservio.Application;
import tipz.browservio.R;
import tipz.browservio.broha.BrohaListInterfaceActivity;
import tipz.browservio.broha.api.FavUtils;
import tipz.browservio.broha.database.icons.IconHashClient;
import tipz.browservio.search.SearchEngineEntries;
import tipz.browservio.search.SuggestionAdapter;
import tipz.browservio.settings.SettingsActivity;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.BrowservioURLs;
import tipz.browservio.utils.CommonUtils;
import tipz.browservio.webview.VioWebView;
import tipz.browservio.webview.VioWebViewActivity;

/**
 * Welcome to the Browservio (The Shrek Browser)
 * This browser was originally designed with Sketchware
 * This project was started on Aug 13 2020
 * <p>
 * sur wen reel Sherk brower pls sand meme sum
 */
public class BrowserActivity extends VioWebViewActivity {
    private MaterialAutoCompleteTextView UrlEdit;
    private AppCompatImageView tabs;
    private AppCompatImageView fab;

    private boolean currentPrebuiltUAState = false;
    private String currentCustomUA;
    private boolean currentCustomUAWideView = false;
    private IconHashClient iconHashClient;
    private int retractedRotation;

    private static final List<Integer> actionBarItemList = Arrays.asList(R.drawable.arrow_back_alt,
            R.drawable.arrow_forward_alt,
            R.drawable.refresh,
            R.drawable.home,
            R.drawable.smartphone,
            R.drawable.share,
            R.drawable.app_shortcut,
            R.drawable.settings,
            R.drawable.history,
            R.drawable.favorites,
            R.drawable.close);

    private List<VioWebView> tabsViews = new ArrayList<VioWebView>();

    @Override
    @SuppressLint("AddJavascriptInterface")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        /* Action bar component: Favicon */
        favicon = findViewById(R.id.favicon);
        faviconProgressBar = findViewById(R.id.faviconProgressBar);
        favicon.setOnClickListener(v -> {
            final SslCertificate cert = webview.getCertificate();
            PopupMenu popupMenu = new PopupMenu(BrowserActivity.this, favicon);
            Menu menu = popupMenu.getMenu();
            menu.add(webview.getTitle()).setEnabled(false);
            menu.add(getResources().getString(R.string.copy_title));
            if (cert != null)
                menu.add(getResources().getString(R.string.ssl_info));
            if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.isJavaScriptEnabled))
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                menu.add(getResources().getString(R.string.view_page_source));
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().toString().equals(getResources().getString(R.string.copy_title))) {
                    CommonUtils.copyClipboard(BrowserActivity.this, webview.getTitle());
                    return true;
                } else if (item.getTitle().toString().equals(getResources().getString(R.string.ssl_info))) {
                    assert cert != null;
                    final SslCertificate.DName issuedTo = cert.getIssuedTo();
                    final SslCertificate.DName issuedBy = cert.getIssuedBy();
                    final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(BrowserActivity.this);
                    dialog.setTitle(Uri.parse(webview.getUrl()).getHost())
                            .setMessage(getResources().getString(R.string.ssl_info_dialog_content,
                                    issuedTo.getCName(), issuedTo.getOName(), issuedTo.getUName(),
                                    issuedBy.getCName(), issuedBy.getOName(), issuedBy.getUName(),
                                    DateFormat.getDateTimeInstance().format(cert.getValidNotBeforeDate()),
                                    DateFormat.getDateTimeInstance().format(cert.getValidNotAfterDate())))
                            .setPositiveButton(getResources().getString(android.R.string.ok), null)
                            .create().show();
                    return true;
                } else if (item.getTitle().toString().equals(getResources().getString(R.string.view_page_source))) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webview.evaluateJavascript(
                                "document.documentElement.outerHTML", value -> {
                                    JsonReader reader = new JsonReader(new StringReader(value));
                                    reader.setLenient(true);
                                    try {
                                        if (reader.peek() == JsonToken.STRING) {
                                            String domStr = reader.nextString();
                                            reader.close();

                                            if (domStr == null)
                                                return;

                                            new MaterialAlertDialogBuilder(BrowserActivity.this)
                                                    .setTitle(getResources().getString(R.string.view_page_source))
                                                    .setMessage(domStr)
                                                    .setPositiveButton(getResources().getString(android.R.string.ok), null)
                                                    .setNegativeButton(getResources().getString(android.R.string.copy), (dialog, which) -> CommonUtils.copyClipboard(BrowserActivity.this, domStr))
                                                    .create().show();
                                        }
                                    } catch (IOException ignored) {
                                    }
                                });
                    }
                }
                return false;
            });
            popupMenu.show();
        });
        faviconProgressBar.setOnClickListener(v -> favicon.performClick());

        /* Action bar component: Fab */
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if (toolsContainer.getVisibility() == View.VISIBLE) {
                fab.animate().rotation(retractedRotation).setDuration(250).start();
                toolsContainer.setVisibility(View.GONE);
            } else {
                // TODO: Improve implementation
                if (tabsContainer.getVisibility() == View.VISIBLE)
                    tabs.performClick();
                fab.animate().rotation(retractedRotation - 180).setDuration(250).start();
                toolsContainer.setVisibility(View.VISIBLE);
            }
            reachModeCheck();
        });

        /* Action bar component: Tabs */
        tabs = findViewById(R.id.tabs);
        tabs.setOnClickListener(v -> {
            if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.useTraditionalTabs))) {
                Intent i = new Intent(this, BrowserActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                    i.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                } else {
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                }
                startActivity(i);
            } else {
                if (tabsContainer.getVisibility() == View.VISIBLE) {
                    tabsContainer.setVisibility(View.GONE);
                } else {
                    // TODO: Improve implementation
                    if (toolsContainer.getVisibility() == View.VISIBLE)
                        fab.performClick();
                    tabsContainer.setVisibility(View.VISIBLE);
                }
                reachModeCheck();
            }
        });

        /* Action bar component: Address Bar */
        UrlEdit = findViewById(R.id.UrlEdit);
        UrlEdit.setOnEditorActionListener((v, actionId, event) -> { /* Detect return key presses */
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == KeyEvent.ACTION_DOWN) {
                webview.loadUrl(UrlEdit.getText().toString());
                UrlEdit.clearFocus();
                return true;
            }
            return false;
        });
        UrlEdit.setAdapter(new SuggestionAdapter(BrowserActivity.this, R.layout.recycler_list_item_1));
        UrlEdit.setOnItemClickListener((adapterView, view, pos, l) -> { /* For suggest items */
            webview.loadUrl(((AppCompatTextView) view.findViewById(android.R.id.text1)).getText().toString());
            closeKeyboard();
        });
        UrlEdit.setOnFocusChangeListener((v, hasFocus) -> { /* Improve experience when unfocusing */
            if (!hasFocus) {
                if (!UrlEdit.getText().toString().equals(webview.getUrl()))
                    UrlEdit.setText(webview.getUrl());
                UrlEdit.setSelection(0);
                UrlEdit.setDropDownHeight(0);
                closeKeyboard();
            } else {
                UrlEdit.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        /* Action bar component: Progress bar */
        progressBar = findViewById(R.id.webviewProgressBar);

        /* Action bar component: Toolbar */
        toolsContainer = findViewById(R.id.toolsContainer);
        tabsContainer = findViewById(R.id.tabsContainer);
        RecyclerView actionBar = findViewById(R.id.actionBar);
        actionBar.setLayoutManager(new LinearLayoutManager(
                BrowserActivity.this, RecyclerView.HORIZONTAL, false));
        actionBar.setAdapter(new ItemsAdapter(BrowserActivity.this));

        /* Action bar component: WebView */
        swipeRefreshLayout = findViewById(R.id.swipe);

        /* Broha */
        iconHashClient = ((Application) getApplicationContext()).iconHashClient;

        /* Start Page Layout */
        startPageLayout = findViewById(R.id.layout_startpage);

        /* Initiate tabs */
        newTab(true);

        Intent intent = getIntent();
        Uri dataUri = intent.getData();

        if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.useWebHomePage))) {
            webview.loadUrl(dataUri != null ? dataUri.toString() :
                    SearchEngineEntries.getHomePageUrl(pref,
                        SettingsUtils.getPrefNum(pref, SettingsKeys.defaultHomePageId)));
        } else {
            webview.loadUrl(BrowservioURLs.startUrl);
        }

    }

    private void newTab(boolean bringToTop) {
        VioWebView newWebView = new VioWebView(this, null);
        tabsViews.add(newWebView);

        /* Init VioWebView */
        newWebView.notifyViewSetup();

        if (bringToTop) {
            // FIXME: Remove when properly implemented
            swipeRefreshLayout.addView(tabsViews.get(0), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            webview = tabsViews.get(0);
        }
    }

    // https://stackoverflow.com/a/57840629/10866268
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    public void itemSelected(AppCompatImageView view, int item) {
        if (item == R.drawable.arrow_back_alt && webview.canGoBack()) {
            webview.goBack();
        } else if (item == R.drawable.arrow_forward_alt && webview.canGoForward()) {
            webview.goForward();
        } else if (item == R.drawable.refresh) {
            webview.webviewReload();
        } else if (item == R.drawable.home) {
            if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.useWebHomePage))) {
                webview.loadUrl(SearchEngineEntries.getHomePageUrl(pref,
                        SettingsUtils.getPrefNum(pref, SettingsKeys.defaultHomePageId)));
            } else {
                UrlEdit.setText(CommonUtils.EMPTY_STRING);
                webview.loadUrl(BrowservioURLs.startUrl);
            }
        } else if (item == R.drawable.smartphone || item == R.drawable.desktop || item == R.drawable.custom) {
            currentPrebuiltUAState = !currentPrebuiltUAState;
            webview.setPrebuiltUAMode(view, currentPrebuiltUAState ? 1 : 0, false);
        } else if (item == R.drawable.share) {
            CommonUtils.shareUrl(this, webview.getUrl());
        } else if (item == R.drawable.app_shortcut) {
            if (webview.getTitle() != null && !webview.getTitle().isBlank())
                ShortcutManagerCompat.requestPinShortcut(this, new ShortcutInfoCompat.Builder(this, webview.getTitle())
                        .setShortLabel(webview.getTitle())
                        .setIcon(IconCompat.createWithBitmap(
                                CommonUtils.drawableToBitmap(favicon.getDrawable())))
                        .setIntent(new Intent(this, BrowserActivity.class)
                                .setData(Uri.parse(webview.getUrl()))
                                .setAction(Intent.ACTION_VIEW))
                        .build(), null);
        } else if (item == R.drawable.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            mGetNeedLoad.launch(intent);
        } else if (item == R.drawable.history) {
            Intent intent = new Intent(BrowserActivity.this, BrohaListInterfaceActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, BrohaListInterfaceActivity.mode_history);
            mGetNeedLoad.launch(intent);
        } else if (item == R.drawable.favorites) {
            Drawable icon = favicon.getDrawable();
            FavUtils.appendData(this, iconHashClient, webview.getTitle(), webview.getUrl(), icon instanceof BitmapDrawable ? ((BitmapDrawable) icon).getBitmap() : null);
            CommonUtils.showMessage(BrowserActivity.this, getResources().getString(R.string.save_successful));
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
            Intent intent = new Intent(BrowserActivity.this, BrohaListInterfaceActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, BrohaListInterfaceActivity.mode_favorites);
            mGetNeedLoad.launch(intent);
        }
    }

    private void closeKeyboard() {
        WindowCompat.getInsetsController(getWindow(), UrlEdit).hide(WindowInsetsCompat.Type.ime());
    }

    @Override
    public void onUrlUpdated(String url) {
        if (!UrlEdit.isFocused())
            UrlEdit.setText(url);
    }

    @Override
    public void onUrlUpdated(String url, int position) {
        UrlEdit.setText(url);
        UrlEdit.setSelection(position);
    }

    @Override
    public void onDropDownDismissed() {
        UrlEdit.dismissDropDown();
        UrlEdit.clearFocus();
    }

    @Override
    public void onStartPageEditTextPressed() {
        UrlEdit.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(UrlEdit, InputMethodManager.SHOW_FORCED);
    }

    @Override
    public void doSettingsCheck() {
        super.doSettingsCheck();

        // Settings check
        toolsContainer.setGravity(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.centerActionBar)) ? Gravity.CENTER_HORIZONTAL : Gravity.NO_GRAVITY);
        fab.setRotation(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.reverseLayout)) ? 0 : 180);
        retractedRotation = (int) fab.getRotation();
        if (toolsContainer.getVisibility() == View.VISIBLE)
            fab.setRotation(fab.getRotation() - 180);
        boolean reverseOnlyActionBar = CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.reverseLayout)) &&
                CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.reverseOnlyActionBar));
        fab.setVisibility(reverseOnlyActionBar ? View.GONE : View.VISIBLE);
        tabs.setImageResource(
                CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.useTraditionalTabs))
                        ? R.drawable.new_tab : R.drawable.tabs);
        if (!CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.useTraditionalTabs))
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // FIXME: Allow dynamic switching of Recents tabs
            List<ActivityManager.AppTask> appTaskList =
                    ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getAppTasks();
            for (int i = 1; i < appTaskList.size(); i++) {
                appTaskList.get(i).finishAndRemoveTask();
            }
        }

        // Set padding for UrlEdit
        int dp8 = (int) CommonUtils.getDisplayMetrics(BrowserActivity.this, 8);
        int dp72 = (int) CommonUtils.getDisplayMetrics(BrowserActivity.this, 72);
        UrlEdit.setPadding(dp8, dp8,
                reverseOnlyActionBar ? dp8 : dp72,
                dp8);
    }

    public static class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
        private final WeakReference<BrowserActivity> mMainActivity;

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final AppCompatImageView mImageView;

            public ViewHolder(View view) {
                super(view);
                mImageView = view.findViewById(R.id.imageView);
            }
        }

        public ItemsAdapter(BrowserActivity mainActivity) {
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

    public static class TabsAdapter extends RecyclerView.Adapter<TabsAdapter.ViewHolder> {
        static class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View view) {
                super(view);
            }
        }

        public TabsAdapter() {
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_tab_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }
}
