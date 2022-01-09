package tipz.browservio;

import static tipz.browservio.sharedprefs.utils.BrowservioSaverUtils.browservio_saver;
import static tipz.browservio.utils.ApkInstaller.installApplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import tipz.browservio.sharedprefs.AllPrefs;
import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;
import tipz.browservio.utils.BrowservioBasicUtil;

public class NewSettings extends PreferenceFragmentCompat {
    public final Activity activity;

    public NewSettings(Activity act) {
        this.activity = act;
    }

    private AlertDialog.Builder SearchSettingsDialog;
    private AlertDialog.Builder CustomSearchSettingsDialog;
    private AlertDialog.Builder HomepageSettingsDialog;
    private AlertDialog.Builder CustomHomepageSettingsDialog;
    private AlertDialog.Builder ZoomUpdateDialog;
    private AlertDialog.Builder InfoDialog;

    private Preference search_engine;
    private Preference homepage;
    private CheckBoxPreference show_favicon;
    private CheckBoxPreference show_pinch_btn;
    private CheckBoxPreference javascript;
    private CheckBoxPreference show_cus_error;

    private static boolean needReload = false;
    private long downloadID;
    private final String updateDownloadPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            .concat("/").concat(Environment.DIRECTORY_DOWNLOADS).concat("/browservio-update.apk");

    final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID == id) {
                installApplication(activity, updateDownloadPath);
            }
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_settings, rootKey);
        initializeLogic();
        activity.registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public static boolean getNeedReload() {
        return needReload;
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void needLoad(String Url) {
        Intent needLoad = new Intent();
        needLoad.putExtra("needLoadUrl", Url);
        activity.setResult(0, needLoad);
        activity.finish();
    }

    /**
     * Initialize Logic
     */
    private void initializeLogic() {
        /* Common */
        final String[] searchHomePageList = {
                activity.getResources().getString(R.string.google_search),
                activity.getResources().getString(R.string.baidu_search),
                activity.getResources().getString(R.string.duck_search),
                activity.getResources().getString(R.string.bing_search),
                activity.getResources().getString(R.string.yahoo_search),
                activity.getResources().getString(R.string.custom_search)
        };

        /* General category */
        search_engine = Objects.requireNonNull(findPreference("search_engine"));
        homepage = Objects.requireNonNull(findPreference("homepage"));
        show_favicon = Objects.requireNonNull(findPreference("show_favicon"));
        show_pinch_btn = Objects.requireNonNull(findPreference("show_pinch_btn"));

        /* Advanced category */
        javascript = Objects.requireNonNull(findPreference("javascript"));
        show_cus_error = Objects.requireNonNull(findPreference("show_cus_error"));

        /* Help category */
        Preference version = Objects.requireNonNull(findPreference("version"));
        Preference feedback = Objects.requireNonNull(findPreference("feedback"));
        Preference source_code = Objects.requireNonNull(findPreference("source_code"));

        /* General category dialog */
        SearchSettingsDialog = new AlertDialog.Builder(activity);
        CustomSearchSettingsDialog = new AlertDialog.Builder(activity);
        HomepageSettingsDialog = new AlertDialog.Builder(activity);
        CustomHomepageSettingsDialog = new AlertDialog.Builder(activity);
        ZoomUpdateDialog = new AlertDialog.Builder(activity);

        /* Help category dialog */
        InfoDialog = new AlertDialog.Builder(activity);

        search_engine.setOnPreferenceClickListener(preference -> {
            SearchSettingsDialog.setTitle(getResources().getString(R.string.search_engine));
            final int[] checkedItem = {BrowservioSaverUtils.getPrefNum(browservio_saver(activity), AllPrefs.defaultSearchId)};
            final String[] searchEngine = new String[1];
            SearchSettingsDialog.setSingleChoiceItems(searchHomePageList,
                    BrowservioSaverUtils.getPrefNum(browservio_saver(activity), AllPrefs.defaultSearchId), (dialog, which) -> checkedItem[0] = which);
            SearchSettingsDialog.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                if (checkedItem[0] == 0)
                    searchEngine[0] = getResources().getString(R.string.url_default_homepage,
                            getResources().getString(R.string.url_default_search_suffix));
                else if (checkedItem[0] == 1)
                    searchEngine[0] = getResources().getString(R.string.url_baidu_homepage,
                            getResources().getString(R.string.url_baidu_search_suffix));
                else if (checkedItem[0] == 2)
                    searchEngine[0] = getResources().getString(R.string.url_duck_homepage,
                            getResources().getString(R.string.url_duck_search_suffix));
                else if (checkedItem[0] == 3)
                    searchEngine[0] = getResources().getString(R.string.url_bing_homepage,
                            getResources().getString(R.string.url_bing_search_suffix));
                else if (checkedItem[0] == 4)
                    searchEngine[0] = getResources().getString(R.string.url_yahoo_homepage,
                            getResources().getString(R.string.url_yahoo_search_suffix));
                else if (checkedItem[0] == 5) {
                    CustomSearchSettingsDialog.setTitle(getResources().getString(R.string.search_engine));
                    final AppCompatEditText custom_se = new AppCompatEditText(activity);
                    LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.MATCH_PARENT);
                    custom_se.setLayoutParams(lp);
                    CustomSearchSettingsDialog.setView(custom_se);
                    CustomSearchSettingsDialog.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        if (!Objects.requireNonNull(custom_se.getText()).toString().isEmpty()) {
                            BrowservioSaverUtils.setPref(browservio_saver(activity), AllPrefs.defaultSearch, custom_se.getText().toString());
                            BrowservioSaverUtils.setPrefNum(browservio_saver(activity), AllPrefs.defaultSearchId, checkedItem[0]);
                            search_engine.setSummary(getResources().getString(R.string.search_engine_current, searchHomePageList[checkedItem[0]]));
                        }
                    });
                    CustomSearchSettingsDialog.setNegativeButton(android.R.string.cancel, null);
                    CustomSearchSettingsDialog.create().show();
                }

                if (checkedItem[0] != 5) {
                    BrowservioSaverUtils.setPref(browservio_saver(activity), AllPrefs.defaultSearch, searchEngine[0]);
                    BrowservioSaverUtils.setPrefNum(browservio_saver(activity), AllPrefs.defaultSearchId, checkedItem[0]);
                    search_engine.setSummary(getResources().getString(R.string.search_engine_current, searchHomePageList[checkedItem[0]]));
                }
            });
            SearchSettingsDialog.setNegativeButton(android.R.string.cancel, null);
            SearchSettingsDialog.create().show();
            return true;
        });

        homepage.setOnPreferenceClickListener(preference -> {
            HomepageSettingsDialog.setTitle(getResources().getString(R.string.homepage));
            final int[] checkedItem = {BrowservioSaverUtils.getPrefNum(browservio_saver(activity), AllPrefs.defaultSearchId)};
            final String[] homePage = new String[1];
            HomepageSettingsDialog.setSingleChoiceItems(searchHomePageList,
                    BrowservioSaverUtils.getPrefNum(browservio_saver(activity), AllPrefs.defaultHomePageId), (dialog, which) -> checkedItem[0] = which);
            HomepageSettingsDialog.setPositiveButton(android.R.string.ok, (_dialog, _which) -> {
                if (checkedItem[0] == 0)
                    homePage[0] = getResources().getString(R.string.url_default_homepage,
                            BrowservioBasicUtil.EMPTY_STRING);
                else if (checkedItem[0] == 1)
                    homePage[0] = getResources().getString(R.string.url_baidu_homepage,
                            BrowservioBasicUtil.EMPTY_STRING);
                else if (checkedItem[0] == 2)
                    homePage[0] = getResources().getString(R.string.url_duck_homepage,
                            BrowservioBasicUtil.EMPTY_STRING);
                else if (checkedItem[0] == 3)
                    homePage[0] = getResources().getString(R.string.url_bing_homepage,
                            BrowservioBasicUtil.EMPTY_STRING);
                else if (checkedItem[0] == 4)
                    homePage[0] = getResources().getString(R.string.url_yahoo_homepage,
                            BrowservioBasicUtil.EMPTY_STRING);
                else if (checkedItem[0] == 5) {
                    CustomHomepageSettingsDialog.setTitle(getResources().getString(R.string.homepage));
                    final AppCompatEditText custom_se = new AppCompatEditText(activity);
                    LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.MATCH_PARENT);
                    custom_se.setLayoutParams(lp);
                    CustomHomepageSettingsDialog.setView(custom_se);
                    CustomHomepageSettingsDialog.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        if (!Objects.requireNonNull(custom_se.getText()).toString().isEmpty()) {
                            BrowservioSaverUtils.setPref(browservio_saver(activity), AllPrefs.defaultHomePage, custom_se.getText().toString());
                            BrowservioSaverUtils.setPrefNum(browservio_saver(activity), AllPrefs.defaultHomePageId, checkedItem[0]);
                            homepage.setSummary(getResources().getString(R.string.homepage_current, searchHomePageList[checkedItem[0]]));
                        }
                    });
                    CustomHomepageSettingsDialog.setNegativeButton(android.R.string.cancel, null);
                    CustomHomepageSettingsDialog.create().show();
                }

                if (checkedItem[0] != 5) {
                    BrowservioSaverUtils.setPref(browservio_saver(activity), AllPrefs.defaultHomePage, homePage[0]);
                    BrowservioSaverUtils.setPrefNum(browservio_saver(activity), AllPrefs.defaultHomePageId, checkedItem[0]);
                    homepage.setSummary(getResources().getString(R.string.homepage_current, searchHomePageList[checkedItem[0]]));
                }
            });
            HomepageSettingsDialog.setNegativeButton(android.R.string.cancel, null);
            HomepageSettingsDialog.create().show();
            return true;
        });

        show_favicon.setOnPreferenceClickListener(preference -> {
            BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver(activity),
                    AllPrefs.showBrowseBtn, show_favicon.isChecked(), false);
            return true;
        });

        show_pinch_btn.setOnPreferenceClickListener(preference -> {
            BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver(activity), AllPrefs.showZoomKeys, show_pinch_btn.isChecked(), false);
            ZoomUpdateDialog.setTitle(getResources().getString(R.string.restart_app_q));
            ZoomUpdateDialog.setMessage(getResources().getString(R.string.restart_app_qmsg));
            ZoomUpdateDialog.setPositiveButton(getResources().getString(R.string.restart_app_now), (_dialog, _which) ->
                    needLoad(getResources().getString(R.string.url_prefix, getResources().getString(R.string.url_suffix_restart))));
            ZoomUpdateDialog.setNegativeButton(android.R.string.cancel, null);
            ZoomUpdateDialog.create().show();
            return true;
        });

        javascript.setOnPreferenceClickListener(preference -> {
            BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver(activity),
                    AllPrefs.isJavaScriptEnabled, javascript.isChecked(), false);
            needReload = true;
            return true;
        });

        show_cus_error.setOnPreferenceClickListener(preference -> {
            BrowservioSaverUtils.setPrefStringBoolAccBool(browservio_saver(activity),
                    AllPrefs.showCustomError, show_cus_error.isChecked(), false);
            return true;
        });

        version.setOnPreferenceClickListener(preference -> {
            @SuppressLint("InflateParams") View dialogView = this.getLayoutInflater().inflate(R.layout.about_dialog, null);
            AppCompatTextView dialog_text = dialogView.findViewById(R.id.dialog_text);
            AppCompatButton update_btn = dialogView.findViewById(R.id.update_btn);
            if (BuildConfig.BUILD_TYPE.equals("debug") && !BuildConfig.UPDATE_TESTING)
                update_btn.setVisibility(View.GONE);
            dialog_text.setText(getResources().getString(R.string.version_info_message,
                    getResources().getString(R.string.app_name),
                    BuildConfig.VERSION_NAME.concat(BuildConfig.VERSION_NAME_EXTRA),
                    String.valueOf(BuildConfig.VERSION_CODE).concat(".").concat(BuildConfig.BUILD_TYPE).concat(".").concat(BuildConfig.VERSION_BUILD_DATE),
                    BuildConfig.VERSION_CODENAME,
                    BuildConfig.VERSION_BUILD_YEAR));
            update_btn.setOnClickListener(_update_btn -> {
                if (!isNetworkAvailable(activity.getApplicationContext())) {
                    BrowservioBasicUtil.showMessage(activity.getApplicationContext(), getResources().getString(R.string.network_unavailable_toast));
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            String path = "https://gitlab.com/TipzTeam/browservio/-/raw/update_files/api2.cfg";
                            File apkFile = new File(updateDownloadPath);
                            URL u;
                            try {
                                u = new URL(path);
                                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                                c.setRequestMethod("GET");
                                c.connect();
                                final ByteArrayOutputStream bo = new ByteArrayOutputStream();
                                byte[] buffer = new byte[1024];
                                int inputStreamTest = c.getInputStream().read(buffer);
                                if (inputStreamTest >= 5) {
                                    bo.write(buffer);

                                    activity.runOnUiThread(() -> {
                                        int position = 0;
                                        boolean isLatest = false;
                                        String[] array = bo.toString().split(BrowservioBasicUtil.LINE_SEPARATOR());
                                        for (String obj : array) {
                                            if (position == 0) {
                                                if (Integer.parseInt(obj) <= BuildConfig.VERSION_CODE && !BuildConfig.UPDATE_TESTING) {
                                                    isLatest = true;
                                                    BrowservioBasicUtil.showMessage(activity.getApplicationContext(), getResources().getString(R.string.version_latest_toast));
                                                }
                                            }
                                            if (position == 1 && !isLatest) {
                                                BrowservioBasicUtil.showMessage(activity.getApplicationContext(), getResources().getString(R.string.new_update_detect_toast));

                                                if (apkFile.exists())
                                                    apkFile.delete();

                                                if (!apkFile.exists()) {
                                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(obj));
                                                    request.setTitle(getResources().getString(R.string.download_title));
                                                    request.setMimeType("application/vnd.android.package-archive");
                                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "browservio-update.apk");
                                                    DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                                                    downloadID = dm.enqueue(request);
                                                } else {
                                                    BrowservioBasicUtil.showMessage(activity.getApplicationContext(), getResources().getString(R.string.update_down_failed_toast));
                                                }
                                            }
                                            position += 1;
                                        }
                                        try {
                                            bo.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                } else {
                                    BrowservioBasicUtil.showMessage(activity.getApplicationContext(), getResources().getString(R.string.update_down_failed_toast));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }.start();
                }
            });
            InfoDialog.setView(dialogView);
            InfoDialog.setPositiveButton(android.R.string.ok, null);
            InfoDialog.create().show();
            return true;
        });

        feedback.setOnPreferenceClickListener(preference -> {
            needLoad(getResources().getString(R.string.url_source_code,
                    getResources().getString(R.string.url_bug_report_suffix)));
            return true;
        });

        source_code.setOnPreferenceClickListener(preference -> {
            needLoad(getResources().getString(R.string.url_source_code,
                    BrowservioBasicUtil.EMPTY_STRING));
            return true;
        });

        checkIfPrefIntIsTrue("showFavicon", show_favicon);
        checkIfPrefIntIsTrue("showZoomKeys", show_pinch_btn);
        checkIfPrefIntIsTrue("isJavaScriptEnabled", javascript);
        checkIfPrefIntIsTrue("showCustomError", show_cus_error);
        search_engine.setSummary(getResources().getString(R.string.search_engine_current, searchHomePageList[BrowservioSaverUtils.getPrefNum(browservio_saver(activity), AllPrefs.defaultSearchId)]));
        homepage.setSummary(getResources().getString(R.string.homepage_current, searchHomePageList[BrowservioSaverUtils.getPrefNum(browservio_saver(activity), AllPrefs.defaultHomePageId)]));
        version.setSummary(getResources().getString(R.string.app_name).concat(" ").concat(BuildConfig.VERSION_NAME.concat(BuildConfig.VERSION_NAME_EXTRA)));
        needReload = false;
    }

    private void checkIfPrefIntIsTrue(String tag, CheckBoxPreference checkBox) {
        checkBox.setChecked(BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver(activity), tag)));
    }
}
