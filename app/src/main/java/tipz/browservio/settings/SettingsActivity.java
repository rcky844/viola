package tipz.browservio.settings;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;
import static tipz.browservio.utils.ApkInstaller.installApplication;
import static tipz.browservio.utils.urls.SearchEngineEntries.getHomepageUrl;
import static tipz.browservio.utils.urls.SearchEngineEntries.getSearchEngineUrl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;

import tipz.browservio.BuildConfig;
import tipz.browservio.R;
import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.DownloadUtils;
import tipz.browservio.utils.urls.BrowservioURLs;
import tipz.browservio.utils.urls.SearchEngineEntries;

public class SettingsActivity extends AppCompatActivity {

    public final Intent needLoad = new Intent();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_settings);

        Toolbar _toolbar = findViewById(R.id._toolbar);
        setSupportActionBar(_toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        _toolbar.setNavigationOnClickListener(_v -> onBackPressed());

        SettingsPrefHandler fragment = new SettingsPrefHandler(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.list_container, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        if (SettingsPrefHandler.needReload) {
            needLoad.putExtra("needLoadUrl", BrowservioURLs.reloadUrl);
            setResult(0, needLoad);
        }
        finish();
    }

    public static class SettingsPrefHandler extends PreferenceFragmentCompat {
        private final Activity settingsActivity;

        public SettingsPrefHandler(Activity act) {
            WeakReference<Activity> activity = new WeakReference<>(act);
            settingsActivity = activity.get();
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
            final String searchEntriesDefault = settingsActivity.getResources().getString(R.string.search_entries_default);
            final String[] searchHomePageList = settingsActivity.getResources().getStringArray(R.array.search_entries);
            if (SettingsUtils.doesNotHaveGoogle)
                searchHomePageList[1] = searchHomePageList[1].concat(searchEntriesDefault);
            else
                searchHomePageList[0] = searchHomePageList[0].concat(searchEntriesDefault);

            final String[] suggestionsList = {
                    searchHomePageList[0],
                    SettingsUtils.doesNotHaveGoogle ? searchHomePageList[1].concat(searchEntriesDefault) : searchHomePageList[1],
                    searchHomePageList[3],
                    searchHomePageList[4],
                    searchHomePageList[5],
                    searchHomePageList[6]
            };
            final String[] themeList = settingsActivity.getResources().getStringArray(R.array.themes);

            /* General category */
            Preference search_engine = Objects.requireNonNull(findPreference("search_engine"));
            Preference homepage = Objects.requireNonNull(findPreference("homepage"));
            Preference search_suggestions = Objects.requireNonNull(findPreference("search_suggestions"));

            /* Data & Privacy category */
            CheckBoxPreference adBlocker = Objects.requireNonNull(findPreference("adBlocker"));
            CheckBoxPreference do_not_track = Objects.requireNonNull(findPreference("do_not_track"));
            Preference reset_to_default = Objects.requireNonNull(findPreference("reset_to_default"));

            /* Visuals category */
            Preference theme = Objects.requireNonNull(findPreference("theme"));
            CheckBoxPreference show_favicon = Objects.requireNonNull(findPreference("show_favicon"));
            CheckBoxPreference center_action = Objects.requireNonNull(findPreference("center_action"));

            /* Advanced category */
            CheckBoxPreference javascript = Objects.requireNonNull(findPreference("javascript"));

            /* Help category */
            Preference version = Objects.requireNonNull(findPreference("version"));
            Preference feedback = Objects.requireNonNull(findPreference("feedback"));
            Preference source_code = Objects.requireNonNull(findPreference("source_code"));

            search_engine.setOnPreferenceClickListener(preference -> {
                final int[] checkedItem = {SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultSearchId)};
                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.search_engine))
                        .setSingleChoiceItems(searchHomePageList,
                                SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultSearchId), (dialog, which) -> checkedItem[0] = which)
                        .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                            if (checkedItem[0] == 7) {
                                final LayoutInflater layoutInflater = LayoutInflater.from(settingsActivity);
                                @SuppressLint("InflateParams") final View root = layoutInflater.inflate(R.layout.dialog_edittext, null);
                                final AppCompatEditText custom_se = root.findViewById(R.id.edittext);
                                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.search_engine))
                                        .setView(root)
                                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                            if (!Objects.requireNonNull(custom_se.getText()).toString().isEmpty()) {
                                                SettingsUtils.setPref(browservio_saver(settingsActivity), SettingsKeys.defaultSearch, custom_se.getText().toString());
                                                SettingsUtils.setPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultSearchId, checkedItem[0]);
                                                search_engine.setSummary(getResources().getString(R.string.search_engine_current, searchHomePageList[checkedItem[0]]));
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .create().show();
                            }

                            if (checkedItem[0] != 7) {
                                SettingsUtils.setPref(browservio_saver(settingsActivity), SettingsKeys.defaultSearch, getSearchEngineUrl(SearchEngineEntries.baseSearch[checkedItem[0]], SearchEngineEntries.searchSuffix[checkedItem[0]]));
                                SettingsUtils.setPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultSearchId, checkedItem[0]);
                                search_engine.setSummary(getResources().getString(R.string.search_engine_current, searchHomePageList[checkedItem[0]]));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            homepage.setOnPreferenceClickListener(preference -> {
                final int[] checkedItem = {SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultSearchId)};
                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.homepage))
                        .setSingleChoiceItems(searchHomePageList,
                                SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultHomePageId), (dialog, which) -> checkedItem[0] = which)
                        .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                            if (checkedItem[0] == 7) {
                                final LayoutInflater layoutInflater = LayoutInflater.from(settingsActivity);
                                @SuppressLint("InflateParams") final View root = layoutInflater.inflate(R.layout.dialog_edittext, null);
                                final AppCompatEditText custom_se = root.findViewById(R.id.edittext);
                                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.homepage))
                                        .setView(root)
                                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                            if (!Objects.requireNonNull(custom_se.getText()).toString().isEmpty()) {
                                                SettingsUtils.setPref(browservio_saver(settingsActivity), SettingsKeys.defaultHomePage, custom_se.getText().toString());
                                                SettingsUtils.setPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultHomePageId, checkedItem[0]);
                                                homepage.setSummary(getResources().getString(R.string.homepage_current, searchHomePageList[checkedItem[0]]));
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .create().show();
                            }

                            if (checkedItem[0] != 7) {
                                SettingsUtils.setPref(browservio_saver(settingsActivity), SettingsKeys.defaultHomePage, getHomepageUrl(SearchEngineEntries.baseSearch[checkedItem[0]]));
                                SettingsUtils.setPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultHomePageId, checkedItem[0]);
                                homepage.setSummary(getResources().getString(R.string.homepage_current, searchHomePageList[checkedItem[0]]));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            search_suggestions.setOnPreferenceClickListener(preference -> {
                final int[] checkedItem = {SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultSuggestionsId)};
                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.search_suggestions_title))
                        .setSingleChoiceItems(suggestionsList,
                                SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultSuggestionsId), (dialog, which) -> checkedItem[0] = which)
                        .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                            SettingsUtils.setPref(browservio_saver(settingsActivity), SettingsKeys.defaultSuggestions, SearchEngineEntries.searchSuggestionsUrl[checkedItem[0]]);
                            SettingsUtils.setPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultSuggestionsId, checkedItem[0]);
                            search_suggestions.setSummary(getResources().getString(R.string.search_suggestions_current, suggestionsList[checkedItem[0]]));
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            adBlocker.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefIntBoolAccBool(browservio_saver(settingsActivity),
                        SettingsKeys.enableAdBlock, adBlocker.isChecked(), false);
                needReload = true;
                return true;
            });

            do_not_track.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefStringBoolAccBool(browservio_saver(settingsActivity),
                        SettingsKeys.sendDNT, do_not_track.isChecked(), false);
                needReload = true;
                return true;
            });

            reset_to_default.setOnPreferenceClickListener(preference -> {
                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.reset_btn))
                        .setMessage(getResources().getString(R.string.reset_dialog).concat(getResources().getString(R.string.to_continue)))
                        .setPositiveButton(getResources().getString(R.string.clear, CommonUtils.EMPTY_STRING).trim(), (_dialog, _which) -> {
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
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            theme.setOnPreferenceClickListener(preference -> {
                final int[] checkedItem = {SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.themeId)};
                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.pref_theme))
                        .setSingleChoiceItems(themeList,
                                SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.themeId), (dialog, which) -> checkedItem[0] = which)
                        .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                            SettingsUtils.setPrefNum(browservio_saver(settingsActivity), SettingsKeys.themeId, checkedItem[0]);
                            theme.setSummary(getResources().getString(R.string.pref_theme_desp, themeList[checkedItem[0]]));
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            show_favicon.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefStringBoolAccBool(browservio_saver(settingsActivity),
                        SettingsKeys.showFavicon, show_favicon.isChecked(), false);
                return true;
            });

            center_action.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefIntBoolAccBool(browservio_saver(settingsActivity),
                        SettingsKeys.centerActionBar, center_action.isChecked(), false);
                return true;
            });

            javascript.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefStringBoolAccBool(browservio_saver(settingsActivity),
                        SettingsKeys.isJavaScriptEnabled, javascript.isChecked(), false);
                needReload = true;
                return true;
            });

            version.setOnPreferenceClickListener(preference -> {
                @SuppressLint("InflateParams") View dialogView = this.getLayoutInflater().inflate(R.layout.about_dialog, null);
                AlertDialog dialog = new MaterialAlertDialogBuilder(settingsActivity).setView(dialogView)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();

                AppCompatImageView easter_banner = dialogView.findViewById(R.id.easter_banner);
                AppCompatTextView dialog_text = dialogView.findViewById(R.id.dialog_text);
                AppCompatButton update_btn = dialogView.findViewById(R.id.update_btn);
                AppCompatButton changelog_btn = dialogView.findViewById(R.id.changelog_btn);
                AppCompatButton license_btn = dialogView.findViewById(R.id.license_btn);
                boolean updateTesting = BuildConfig.DEBUG && SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.updateTesting) == 1;
                if (BuildConfig.DEBUG && !updateTesting) {
                    update_btn.setVisibility(View.GONE);
                    changelog_btn.setVisibility(View.GONE);
                }
                easter_banner.setOnClickListener(_update_btn -> {
                    CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.app_name).concat(" ").concat(BuildConfig.VERSION_NAME).concat(BuildConfig.VERSION_TECHNICAL_EXTRA));
                    if (!updateTesting)
                        SettingsUtils.setPrefNum(browservio_saver(settingsActivity), SettingsKeys.updateTesting, 1);
                });
                dialog_text.setText(getResources().getString(R.string.version_info_message,
                        getResources().getString(R.string.app_name),
                        BuildConfig.VERSION_NAME.concat(BuildConfig.VERSION_NAME_EXTRA),
                        String.valueOf(BuildConfig.VERSION_CODE).concat(".").concat(BuildConfig.BUILD_TYPE).concat(".").concat(BuildConfig.VERSION_BUILD_DATE),
                        BuildConfig.VERSION_CODENAME,
                        BuildConfig.VERSION_BUILD_YEAR));
                update_btn.setOnClickListener(_update_btn -> {
                    if (CommonUtils.isNetworkAvailable(settingsActivity.getApplicationContext())) {
                        CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.network_unavailable_toast));
                    } else {
                        File apkFile = new File(updateDownloadPath);

                        String[] array = DownloadUtils.
                                downloadToString("https://gitlab.com/TipzTeam/browservio/-/raw/update_files/api2.cfg")
                                .split(CommonUtils.LINE_SEPARATOR());

                        if (Integer.parseInt(array[0]) <= BuildConfig.VERSION_CODE && !updateTesting) {
                            CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.version_latest_toast));
                            return;
                        }

                        new MaterialAlertDialogBuilder(settingsActivity)
                                .setTitle(getResources().getString(R.string.new_update_detect_title))
                                .setMessage(getResources().getString(R.string.new_update_detect_message, array[2], array[0]))
                                .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                                    if (!apkFile.exists() || apkFile.delete())
                                        downloadID = DownloadUtils.dmDownloadFile(settingsActivity, array[1],
                                                null, "application/vnd.android.package-archive",
                                                getResources().getString(R.string.download_title), "browservio-update.apk");
                                    else
                                        CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.update_down_failed_toast));
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .create().show();
                    }
                });
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

            checkIfPrefIntIsTrue(SettingsKeys.enableAdBlock, adBlocker, true);
            checkIfPrefIntIsTrue(SettingsKeys.sendDNT, do_not_track, false);
            checkIfPrefIntIsTrue(SettingsKeys.showFavicon, show_favicon, false);
            checkIfPrefIntIsTrue(SettingsKeys.centerActionBar, center_action, true);
            checkIfPrefIntIsTrue(SettingsKeys.isJavaScriptEnabled, javascript, false);
            search_engine.setSummary(getResources().getString(R.string.search_engine_current, searchHomePageList[SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultSearchId)]));
            homepage.setSummary(getResources().getString(R.string.homepage_current, searchHomePageList[SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultHomePageId)]));
            search_suggestions.setSummary(getResources().getString(R.string.search_suggestions_current, suggestionsList[SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.defaultSuggestionsId)]));
            theme.setSummary(getResources().getString(R.string.pref_theme_desp, themeList[SettingsUtils.getPrefNum(browservio_saver(settingsActivity), SettingsKeys.themeId)]));
            version.setSummary(getResources().getString(R.string.app_name).concat(" ").concat(BuildConfig.VERSION_NAME.concat(BuildConfig.VERSION_NAME_EXTRA)));
            needReload = false;
        }

        private void checkIfPrefIntIsTrue(String tag, CheckBoxPreference checkBox, boolean isInt) {
            checkBox.setChecked(CommonUtils.isIntStrOne(isInt ? SettingsUtils.getPrefNum(browservio_saver(settingsActivity), tag) : SettingsUtils.getPref(browservio_saver(settingsActivity), tag)));
        }
    }
}
