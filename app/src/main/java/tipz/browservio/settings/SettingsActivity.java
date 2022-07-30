package tipz.browservio.settings;

import static tipz.browservio.search.SearchEngineEntries.getHomepageUrl;
import static tipz.browservio.search.SearchEngineEntries.getSearchEngineUrl;
import static tipz.browservio.settings.SettingsUtils.browservio_saver;
import static tipz.browservio.utils.ApkInstaller.installApplication;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;

import tipz.browservio.BuildConfig;
import tipz.browservio.R;
import tipz.browservio.search.SearchEngineEntries;
import tipz.browservio.utils.BrowservioURLs;
import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.DownloadUtils;

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
        private final SharedPreferences pref;

        public SettingsPrefHandler(Activity act) {
            WeakReference<Activity> activity = new WeakReference<>(act);
            settingsActivity = activity.get();
            pref = browservio_saver(settingsActivity);
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
            searchHomePageList[7] += settingsActivity.getResources().getString(R.string.search_entries_default);

            final String[] suggestionsList = {
                    searchHomePageList[0],
                    searchHomePageList[1],
                    searchHomePageList[3],
                    searchHomePageList[4],
                    searchHomePageList[5],
                    searchHomePageList[6],
                    searchHomePageList[7],
            };
            final String[] themeList = settingsActivity.getResources().getStringArray(R.array.themes);

            /* General category */
            Preference search_engine = Objects.requireNonNull(findPreference("search_engine"));
            Preference homepage = Objects.requireNonNull(findPreference("homepage"));
            Preference search_suggestions = Objects.requireNonNull(findPreference("search_suggestions"));

            /* Data & Privacy category */
            SwitchPreference adBlocker = Objects.requireNonNull(findPreference("adBlocker"));
            SwitchPreference do_not_track = Objects.requireNonNull(findPreference("do_not_track"));
            SwitchPreference enforce_https = Objects.requireNonNull(findPreference("enforce_https"));
            Preference reset_to_default = Objects.requireNonNull(findPreference("reset_to_default"));

            /* Visuals category */
            Preference theme = Objects.requireNonNull(findPreference("theme"));
            SwitchPreference show_favicon = Objects.requireNonNull(findPreference("show_favicon"));
            SwitchPreference center_action = Objects.requireNonNull(findPreference("center_action"));
            SwitchPreference enable_swipe_refresh = Objects.requireNonNull(findPreference("enable_swipe_refresh"));

            /* Advanced category */
            SwitchPreference javascript = Objects.requireNonNull(findPreference("javascript"));

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
                                        .setView(root)
                                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                            if (!Objects.requireNonNull(custom_se.getText()).toString().isEmpty()) {
                                                SettingsUtils.setPref(pref, SettingsKeys.defaultSearch, custom_se.getText().toString());
                                                SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSearchId, checkedItem[0]);
                                                search_engine.setSummary(getResources().getString(R.string.search_engine_current, searchHomePageList[checkedItem[0]]));
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .create().show();
                            }

                            if (checkedItem[0] != 8) {
                                SettingsUtils.setPref(pref, SettingsKeys.defaultSearch, getSearchEngineUrl(SearchEngineEntries.baseSearch[checkedItem[0]], SearchEngineEntries.searchSuffix[checkedItem[0]]));
                                SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSearchId, checkedItem[0]);
                                search_engine.setSummary(getResources().getString(R.string.search_engine_current, searchHomePageList[checkedItem[0]]));
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
                                                homepage.setSummary(getResources().getString(R.string.homepage_current, searchHomePageList[checkedItem[0]]));
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .create().show();
                            }

                            if (checkedItem[0] != 8) {
                                SettingsUtils.setPref(pref, SettingsKeys.defaultHomePage, getHomepageUrl(SearchEngineEntries.baseSearch[checkedItem[0]]));
                                SettingsUtils.setPrefNum(pref, SettingsKeys.defaultHomePageId, checkedItem[0]);
                                homepage.setSummary(getResources().getString(R.string.homepage_current, searchHomePageList[checkedItem[0]]));
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            search_suggestions.setOnPreferenceClickListener(preference -> {
                final int[] checkedItem = {SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSuggestionsId)};
                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.search_suggestions_title))
                        .setSingleChoiceItems(suggestionsList,
                                SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSuggestionsId), (dialog, which) -> checkedItem[0] = which)
                        .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                            SettingsUtils.setPref(pref, SettingsKeys.defaultSuggestions, SearchEngineEntries.searchSuggestionsUrl[checkedItem[0]]);
                            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSuggestionsId, checkedItem[0]);
                            search_suggestions.setSummary(getResources().getString(R.string.search_suggestions_current, suggestionsList[checkedItem[0]]));
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            adBlocker.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefIntBoolAccBool(pref,
                        SettingsKeys.enableAdBlock, adBlocker.isChecked(), false);
                needReload = true;
                return true;
            });

            do_not_track.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefIntBoolAccBool(pref,
                        SettingsKeys.sendDNT, do_not_track.isChecked(), false);
                needReload = true;
                return true;
            });

            enforce_https.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefIntBoolAccBool(pref,
                        SettingsKeys.enforceHttps, enforce_https.isChecked(), false);
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
                final int[] checkedItem = {SettingsUtils.getPrefNum(pref, SettingsKeys.themeId)};
                new MaterialAlertDialogBuilder(settingsActivity).setTitle(getResources().getString(R.string.pref_theme))
                        .setSingleChoiceItems(themeList,
                                SettingsUtils.getPrefNum(pref, SettingsKeys.themeId), (dialog, which) -> checkedItem[0] = which)
                        .setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                            SettingsUtils.setPrefNum(pref, SettingsKeys.themeId, checkedItem[0]);
                            theme.setSummary(getResources().getString(R.string.pref_theme_desp, themeList[checkedItem[0]]));
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show();
                return true;
            });

            show_favicon.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefIntBoolAccBool(pref,
                        SettingsKeys.showFavicon, show_favicon.isChecked(), false);
                return true;
            });

            center_action.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefIntBoolAccBool(pref,
                        SettingsKeys.centerActionBar, center_action.isChecked(), false);
                return true;
            });

            enable_swipe_refresh.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefIntBoolAccBool(pref,
                        SettingsKeys.enableSwipeRefresh, enable_swipe_refresh.isChecked(), false);
                return true;
            });

            javascript.setOnPreferenceClickListener(preference -> {
                SettingsUtils.setPrefIntBoolAccBool(pref,
                        SettingsKeys.isJavaScriptEnabled, javascript.isChecked(), false);
                needReload = true;
                return true;
            });

            version.setOnPreferenceClickListener(preference -> {
                @SuppressLint("InflateParams") View dialogView = this.getLayoutInflater().inflate(R.layout.about_dialog, null);
                AlertDialog dialog = new MaterialAlertDialogBuilder(settingsActivity).setView(dialogView)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();

                ConstraintLayout easter_banner = dialogView.findViewById(R.id.easter_banner);
                AppCompatImageView easter_banner_front = dialogView.findViewById(R.id.easter_banner_front);
                AppCompatImageView eagle = dialogView.findViewById(R.id.eagle);
                AppCompatTextView dialog_text = dialogView.findViewById(R.id.dialog_text);
                AppCompatButton update_btn = dialogView.findViewById(R.id.update_btn);
                AppCompatButton changelog_btn = dialogView.findViewById(R.id.changelog_btn);
                AppCompatButton license_btn = dialogView.findViewById(R.id.license_btn);
                final int[] pressed = {0, 0};
                easter_banner.setOnClickListener(_update_btn -> {
                    if (pressed[0] <= 4) {
                        eagle.animate().cancel();
                        eagle.setX(pressed[1] == 0 ?
                                (easter_banner.getLeft() - 200f) : (easter_banner.getRight() + 200f));
                        easter_banner_front.setImageResource(R.drawable.browservio_banner_front);
                        if (pressed[0] == 0)
                            CommonUtils.showMessage(settingsActivity,
                                    getResources().getString(R.string.app_name)
                                            .concat(" ").concat(BuildConfig.VERSION_NAME)
                                            .concat(BuildConfig.VERSION_TECHNICAL_EXTRA));
                        pressed[0]++;
                    } else {
                        eagle.setVisibility(View.VISIBLE);
                        easter_banner_front.setImageDrawable(null);
                        eagle.animate().translationX(pressed[1] == 0 ?
                                easter_banner.getRight() + 200f : easter_banner.getLeft() - 200f)
                                .setDuration(5000);
                        pressed[0] = 0;
                        pressed[1] = ~pressed[1] & 1;
                    }
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

                        String arrayString = DownloadUtils.downloadToString(
                                "https://gitlab.com/TipzTeam/browservio/-/raw/update_files/api2.cfg", 5000);
                        if (arrayString == null) {
                            CommonUtils.showMessage(settingsActivity, getResources().getString(R.string.network_unavailable_toast));
                            return;
                        }
                        String[] array = arrayString.split(CommonUtils.LINE_SEPARATOR());

                        if (Integer.parseInt(array[0]) <= BuildConfig.VERSION_CODE) {
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
                                                getResources().getString(R.string.download_title), "browservio-update.apk", null);
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

            checkIfPrefIntIsTrue(SettingsKeys.enableAdBlock, adBlocker);
            checkIfPrefIntIsTrue(SettingsKeys.sendDNT, do_not_track);
            checkIfPrefIntIsTrue(SettingsKeys.enforceHttps, enforce_https);
            checkIfPrefIntIsTrue(SettingsKeys.showFavicon, show_favicon);
            checkIfPrefIntIsTrue(SettingsKeys.centerActionBar, center_action);
            checkIfPrefIntIsTrue(SettingsKeys.enableSwipeRefresh, enable_swipe_refresh);
            checkIfPrefIntIsTrue(SettingsKeys.isJavaScriptEnabled, javascript);
            search_engine.setSummary(getResources().getString(R.string.search_engine_current, searchHomePageList[SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSearchId)]));
            homepage.setSummary(getResources().getString(R.string.homepage_current, searchHomePageList[SettingsUtils.getPrefNum(pref, SettingsKeys.defaultHomePageId)]));
            search_suggestions.setSummary(getResources().getString(R.string.search_suggestions_current, suggestionsList[SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSuggestionsId)]));
            theme.setSummary(getResources().getString(R.string.pref_theme_desp, themeList[SettingsUtils.getPrefNum(pref, SettingsKeys.themeId)]));
            version.setSummary(getResources().getString(R.string.app_name).concat(" ").concat(BuildConfig.VERSION_NAME.concat(BuildConfig.VERSION_NAME_EXTRA)));
            needReload = false;
        }

        private void checkIfPrefIntIsTrue(String tag, SwitchPreference checkBox) {
            checkBox.setChecked(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, tag)));
        }
    }
}
