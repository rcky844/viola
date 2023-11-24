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
package tipz.viola.settings;

import static tipz.viola.utils.ApkInstaller.installApplication;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;

import tipz.viola.Application;
import tipz.viola.BrowservioActivity;
import tipz.viola.BuildConfig;
import tipz.viola.R;
import tipz.viola.utils.BrowservioURLs;
import tipz.viola.utils.CommonUtils;
import tipz.viola.utils.DownloadUtils;
import tipz.viola.utils.DownloaderThread;

public class SettingsActivity extends BrowservioActivity {

    public final Intent needLoad = new Intent();
    public static SettingsPrefHandler settingsPrefHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_settings);

        Toolbar toolbar = findViewById(R.id._toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onStart() {
        super.onStart();
        settingsPrefHandler = new SettingsPrefHandler(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.list_container, settingsPrefHandler).commit();
    }

    @Override
    public void onBackPressed() {
        if (SettingsPrefHandler.needReload) {
            needLoad.putExtra("needLoadUrl", BrowservioURLs.reloadUrl);
            setResult(0, needLoad);
        }
        finish();
    }

    // TODO: Investigate why running at onSaveInstanceState doesn't work (API = 33)
    @Override
    protected void onStop() {
        try {
            getSupportFragmentManager().beginTransaction().remove(settingsPrefHandler).commit();
        } catch (IllegalStateException ignored) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        }
        super.onStop();
    }

    // TODO: Investigate why running at onStop doesn't work (API = 23, 26)
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        try {
            getSupportFragmentManager().beginTransaction().remove(settingsPrefHandler).commit();
        } catch (IllegalStateException ignored) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        }
        super.onSaveInstanceState(outState);
    }

    public static class SettingsPrefHandler extends PreferenceFragmentCompat {
        private final AppCompatActivity settingsActivity;
        private final SharedPreferences pref;

        public SettingsPrefHandler(AppCompatActivity act) {
            WeakReference<AppCompatActivity> activity = new WeakReference<>(act);
            settingsActivity = activity.get();
            pref = ((Application) settingsActivity.getApplicationContext()).pref;
        }

        public static boolean needReload = false;
        private long downloadID;
        private final String updateDownloadPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                .concat("/").concat(Environment.DIRECTORY_DOWNLOADS).concat("/browservio-update.apk");

        final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadID == id) {
                    installApplication(settingsActivity, updateDownloadPath);
                }
            }
        };

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_settings, rootKey);
            initializeLogic();
            settingsActivity.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            settingsActivity.unregisterReceiver(onDownloadComplete);
        }

        private void needLoad(String Url) {
            Intent needLoad = new Intent();
            needLoad.putExtra("needLoadUrl", Url);
            settingsActivity.setResult(0, needLoad);
            settingsActivity.finish();
        }

        /**
         * Initialize Logic
         */
        private void initializeLogic() {
            /* Common */
            final String[] searchHomePageList = settingsActivity.getResources().getStringArray(R.array.search_entries);
            final String[] themeList = settingsActivity.getResources().getStringArray(R.array.themes);

            /* General category */
            Preference search_engine = Objects.requireNonNull(findPreference("search_engine"));
            //MaterialDialogPreference dont_use_start_page = Objects.requireNonNull(findPreference("dont_use_start_page"));
            Preference homepage = Objects.requireNonNull(findPreference("homepage"));
            Preference search_suggestions = Objects.requireNonNull(findPreference("search_suggestions"));

            /* Data & Privacy category */
            //MaterialSwitchPreference adBlocker = Objects.requireNonNull(findPreference("adBlocker"));
            //MaterialSwitchPreference do_not_track = Objects.requireNonNull(findPreference("do_not_track"));
            //MaterialSwitchPreference enforce_https = Objects.requireNonNull(findPreference("enforce_https"));
            //MaterialSwitchPreference google_safe_browsing = Objects.requireNonNull(findPreference("google_safe_browsing"));
            MaterialDialogPreference clear_cache = Objects.requireNonNull(findPreference("clear_cache"));
            MaterialDialogPreference clear_cookies = Objects.requireNonNull(findPreference("clear_cookies"));
            MaterialDialogPreference reset_to_default = Objects.requireNonNull(findPreference("reset_to_default"));

            /* Visuals category */
            Preference theme = Objects.requireNonNull(findPreference("theme"));
            //MaterialSwitchPreference show_favicon = Objects.requireNonNull(findPreference("show_favicon"));
            //MaterialSwitchPreference center_action = Objects.requireNonNull(findPreference("center_action"));
            //MaterialSwitchPreference reverse_layout = Objects.requireNonNull(findPreference("reverse_layout"));
            //MaterialSwitchPreference reverse_only_action = Objects.requireNonNull(findPreference("reverse_only_action"));
            //MaterialSwitchPreference enable_swipe_refresh = Objects.requireNonNull(findPreference("enable_swipe_refresh"));
            //MaterialSwitchPreference update_recents_icon = Objects.requireNonNull(findPreference("update_recents_icon"));

            /* Advanced category */
            //MaterialSwitchPreference javascript = Objects.requireNonNull(findPreference("javascript"));
            //MaterialSwitchPreference use_custom_tabs = Objects.requireNonNull(findPreference("use_custom_tabs"));
            //MaterialSwitchPreference close_app_after_download = Objects.requireNonNull(findPreference("close_app_after_download"));

            /* Help category */
            Preference version = Objects.requireNonNull(findPreference("version"));
            Preference feedback = Objects.requireNonNull(findPreference("feedback"));
            Preference source_code = Objects.requireNonNull(findPreference("source_code"));

            search_engine.setOnPreferenceClickListener(preference -> {
                final int[] checkedItem = {SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSearchId)};
                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.search_engine))
                        .setSingleChoiceItems(searchHomePageList,
                                SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSearchId), (dialog, which) -> checkedItem[0] = which)
                        .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                            if (checkedItem[0] == 8) {
                                final LayoutInflater layoutInflater = LayoutInflater.from(settingsActivity);
                                @SuppressLint("InflateParams") final View root = layoutInflater.inflate(R.layout.dialog_edittext, null);
                                final AppCompatEditText custom_se = root.findViewById(R.id.edittext);
                                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.search_engine))
                                        .setMessage(settingsActivity.getResources().getString(R.string.custom_search_guide))
                                        .setView(root)
                                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                            if (!Objects.requireNonNull(custom_se.getText()).toString().isEmpty()) {
                                                SettingsUtils.setPref(pref, SettingsKeys.defaultSearch, custom_se.getText().toString());
                                                SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSearchId, checkedItem[0]);
                                                search_engine.setSummary(searchHomePageList[checkedItem[0]]);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .create().show();
                            }

                            if (checkedItem[0] != 8) {
                                SettingsUtils.setPref(pref, SettingsKeys.defaultSearch, CommonUtils.EMPTY_STRING);
                                SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSearchId, checkedItem[0]);
                                search_engine.setSummary(searchHomePageList[checkedItem[0]]);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            homepage.setOnPreferenceClickListener(preference -> {
                final int[] checkedItem = {SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSearchId)};
                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.homepage))
                        .setSingleChoiceItems(searchHomePageList,
                                SettingsUtils.getPrefNum(pref, SettingsKeys.defaultHomePageId), (dialog, which) -> checkedItem[0] = which)
                        .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                            if (checkedItem[0] == 8) {
                                final LayoutInflater layoutInflater = LayoutInflater.from(settingsActivity);
                                @SuppressLint("InflateParams") final View root = layoutInflater.inflate(R.layout.dialog_edittext, null);
                                final AppCompatEditText custom_se = root.findViewById(R.id.edittext);
                                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.homepage))
                                        .setView(root)
                                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                            if (!Objects.requireNonNull(custom_se.getText()).toString().isEmpty()) {
                                                SettingsUtils.setPref(pref, SettingsKeys.defaultHomePage, custom_se.getText().toString());
                                                SettingsUtils.setPrefNum(pref, SettingsKeys.defaultHomePageId, checkedItem[0]);
                                                homepage.setSummary(searchHomePageList[checkedItem[0]]);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .create().show();
                            }

                            if (checkedItem[0] != 8) {
                                SettingsUtils.setPref(pref, SettingsKeys.defaultHomePage, CommonUtils.EMPTY_STRING);
                                SettingsUtils.setPrefNum(pref, SettingsKeys.defaultHomePageId, checkedItem[0]);
                                homepage.setSummary(searchHomePageList[checkedItem[0]]);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            search_suggestions.setOnPreferenceClickListener(preference -> {
                final int[] checkedItem = {SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSuggestionsId)};
                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.search_suggestions_title))
                        .setSingleChoiceItems(searchHomePageList,
                                SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSuggestionsId), (dialog, which) -> checkedItem[0] = which)
                        .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                            if (checkedItem[0] == 8) {
                                final LayoutInflater layoutInflater = LayoutInflater.from(settingsActivity);
                                @SuppressLint("InflateParams") final View root = layoutInflater.inflate(R.layout.dialog_edittext, null);
                                final AppCompatEditText custom_sg = root.findViewById(R.id.edittext);
                                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.search_suggestions_title))
                                        .setMessage(settingsActivity.getResources().getString(R.string.custom_search_guide))
                                        .setView(root)
                                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                            if (!Objects.requireNonNull(custom_sg.getText()).toString().isEmpty()) {
                                                SettingsUtils.setPref(pref, SettingsKeys.defaultSuggestions, custom_sg.getText().toString());
                                                SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSuggestionsId, checkedItem[0]);
                                                search_suggestions.setSummary(searchHomePageList[checkedItem[0]]);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .create().show();
                            }

                            if (checkedItem[0] != 8) {
                                SettingsUtils.setPref(pref, SettingsKeys.defaultSuggestions, CommonUtils.EMPTY_STRING);
                                SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSuggestionsId, checkedItem[0]);
                                search_suggestions.setSummary(searchHomePageList[checkedItem[0]]);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            clear_cache.setMaterialDialogPreferenceListener(positiveResult -> {
                if (!positiveResult)
                    return;

                WebStorage.getInstance().deleteAllData();
                CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.cleared_toast));
            });

            clear_cookies.setMaterialDialogPreferenceListener(positiveResult -> {
                if (!positiveResult)
                    return;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();
                } else {
                    CookieSyncManager cookieSyncMgr = CookieSyncManager.createInstance(settingsActivity);
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieSyncMgr.startSync();
                    cookieManager.removeAllCookie();
                    cookieManager.removeSessionCookie();
                    cookieSyncMgr.stopSync();
                    cookieSyncMgr.sync();
                }
                CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.cleared_toast));
            });

            reset_to_default.setMaterialDialogPreferenceListener(positiveResult -> {
                if (!positiveResult)
                    return;

                CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.reset_complete));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    ((ActivityManager) settingsActivity.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                } else {
                    String packageName = settingsActivity.getPackageName();
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        runtime.exec("pm clear " + packageName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            theme.setOnPreferenceClickListener(preference -> {
                final int[] checkedItem = {SettingsUtils.getPrefNum(pref, SettingsKeys.themeId)};
                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.pref_theme))
                        .setSingleChoiceItems(themeList,
                                SettingsUtils.getPrefNum(pref, SettingsKeys.themeId), (dialog, which) -> checkedItem[0] = which)
                        .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                            SettingsUtils.setPrefNum(pref, SettingsKeys.themeId, checkedItem[0]);
                            theme.setSummary(themeList[checkedItem[0]]);
                            darkModeCheck(settingsActivity);
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            version.setOnPreferenceClickListener(preference -> {
                @SuppressLint("InflateParams") View dialogView = this.getLayoutInflater().inflate(R.layout.about_dialog, null);
                AlertDialog dialog = new MaterialAlertDialogBuilder(settingsActivity).setView(dialogView)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();

                AppCompatTextView dialog_text = dialogView.findViewById(R.id.dialog_text);
                AppCompatButton update_btn = dialogView.findViewById(R.id.update_btn);
                AppCompatButton changelog_btn = dialogView.findViewById(R.id.changelog_btn);
                AppCompatButton license_btn = dialogView.findViewById(R.id.license_btn);
                dialog_text.setText(getResources().getString(R.string.version_info_message,
                        getResources().getString(R.string.app_name),
                        BuildConfig.VERSION_NAME.concat(BuildConfig.VERSION_NAME_EXTRA),
                        BuildConfig.VERSION_CODENAME,
                        BuildConfig.VERSION_BUILD_DATE,
                        BuildConfig.VERSION_BUILD_YEAR));
                update_btn.setOnClickListener(_update_btn -> {
                    DownloaderThread mHandlerThread = new DownloaderThread("updater");
                    mHandlerThread.start();
                    mHandlerThread.setCallerHandler(new Handler(mHandlerThread.getLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            switch (msg.what) {
                                case DownloaderThread.TYPE_SUCCESS:
                                    String data = msg.getData().getString("response");
                                    File apkFile = new File(updateDownloadPath);

                                    if (data == null) {
                                        CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.network_unavailable_toast));
                                        return;
                                    }
                                    String[] array = data.split(CommonUtils.LINE_SEPARATOR());

                                    if (Integer.parseInt(array[0]) <= BuildConfig.VERSION_CODE) {
                                        CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.version_latest_toast));
                                        return;
                                    }
                                    new Handler(Looper.getMainLooper()).post(() -> new MaterialAlertDialogBuilder(settingsActivity)
                                            .setTitle(getResources().getString(R.string.new_update_detect_title))
                                            .setMessage(getResources().getString(R.string.new_update_detect_message, array[2], array[0]))
                                            .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                                                if (!apkFile.exists() || apkFile.delete())
                                                    downloadID = DownloadUtils.dmDownloadFile(settingsActivity, array[1],
                                                            null, "application/vnd.android.package-archive",
                                                            getResources().getString(R.string.download_title), "browservio-update.apk", null);
                                                else
                                                    CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.update_down_failed_toast));
                                            })
                                            .setNegativeButton(android.R.string.cancel, null)
                                            .create().show());
                                    break;
                                case DownloaderThread.TYPE_FAILED:
                                    CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.network_unavailable_toast));
                                    break;
                            }
                            mHandlerThread.quit();
                            super.handleMessage(msg);
                        }
                    });
                    mHandlerThread.startDownload("https://gitlab.com/TipzTeam/browservio/-/raw/update_files/api2.cfg");
                });
                changelog_btn.setVisibility(BuildConfig.DEBUG ? View.GONE : View.VISIBLE);
                changelog_btn.setOnClickListener(_license_btn -> {
                    needLoad(BrowservioURLs.realChangelogUrl);
                    dialog.dismiss();
                });
                license_btn.setOnClickListener(_license_btn -> {
                    needLoad(BrowservioURLs.licenseUrl);
                    dialog.dismiss();
                });

                dialog.show();
                return true;
            });

            feedback.setOnPreferenceClickListener(preference -> {
                needLoad(BrowservioURLs.feedbackUrl);
                return true;
            });

            source_code.setOnPreferenceClickListener(preference -> {
                needLoad(BrowservioURLs.sourceUrl);
                return true;
            });

            search_engine.setSummary(searchHomePageList[SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSearchId)]);
            homepage.setSummary(searchHomePageList[SettingsUtils.getPrefNum(pref, SettingsKeys.defaultHomePageId)]);
            search_suggestions.setSummary(searchHomePageList[SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSuggestionsId)]);
            theme.setSummary(themeList[SettingsUtils.getPrefNum(pref, SettingsKeys.themeId)]);
            version.setSummary(getResources().getString(R.string.app_name).concat(" ").concat(BuildConfig.VERSION_NAME.concat(BuildConfig.VERSION_NAME_EXTRA)));
            needReload = false;
        }

        @Override
        public void onDisplayPreferenceDialog(@NonNull Preference preference) {
            DialogFragment dialogFragment = null;
            if (preference instanceof MaterialDialogPreference) {
                dialogFragment = MaterialPreferenceDialogFragmentCompat
                        .newInstance(preference.getKey(), ((MaterialDialogPreference) preference).getMaterialDialogPreferenceListener());
            }

            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }
    }
}
