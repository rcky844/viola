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
@file:Suppress("DEPRECATION")

package tipz.viola.settings

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebStorage
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import tipz.viola.Application
import tipz.viola.webviewui.BaseActivity
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.settings.MaterialPreferenceDialogFragmentCompat.Companion.newInstance
import tipz.viola.settings.MaterialPreferenceDialogFragmentCompat.MaterialDialogPreferenceListener
import tipz.viola.utils.ApkInstaller.installApplication
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.CommonUtils.showMessage
import tipz.viola.utils.DownloadUtils
import tipz.viola.utils.DownloadUtils.dmDownloadFile
import tipz.viola.utils.InternalUrls
import java.io.File
import java.lang.ref.WeakReference

class SettingsActivity : BaseActivity() {
    private val needLoad = Intent()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_settings)
        val toolbar = findViewById<Toolbar>(R.id._toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        onBackPressedDispatcher.addCallback(this) {
            if (SettingsPrefHandler.needReload) {
                needLoad.putExtra("needLoadUrl", InternalUrls.reloadUrl)
                setResult(0, needLoad)
            }
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        settingsPrefHandler = SettingsPrefHandler(this)
        supportFragmentManager.beginTransaction()
                .replace(R.id.list_container, settingsPrefHandler!!).commit()
    }

    // TODO: Investigate why running at onSaveInstanceState doesn't work (API = 33)
    override fun onStop() {
        try {
            supportFragmentManager.beginTransaction().remove(settingsPrefHandler!!).commit()
        } catch (ignored: IllegalStateException) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        }
        super.onStop()
    }

    // TODO: Investigate why running at onStop doesn't work (API = 23, 26)
    public override fun onSaveInstanceState(outState: Bundle) {
        try {
            supportFragmentManager.beginTransaction().remove(settingsPrefHandler!!).commit()
        } catch (ignored: IllegalStateException) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        }
        super.onSaveInstanceState(outState)
    }

    class SettingsPrefHandler(act: AppCompatActivity) : PreferenceFragmentCompat() {
        private lateinit var settingsActivity: AppCompatActivity
        private val settingsPreference: SettingsSharedPreference
        private var downloadID: Long = 0
        private val updateDownloadPathBase =
                Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_DOWNLOADS + "/"
        private var updateDownloadPath: String? = null
        private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadID == id) {
                    installApplication(settingsActivity, updateDownloadPath)
                }
            }
        }
        private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

        init {
            val activity = WeakReference(act)
            settingsActivity = activity.get()!!
            settingsPreference =
                    (settingsActivity.applicationContext as Application).settingsPreference!!
            pickMedia =
                    registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                        if (uri != null) {
                            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            settingsActivity.contentResolver.takePersistableUriPermission(
                                    uri,
                                    flag
                            )
                            settingsPreference.setString(
                                    SettingsKeys.startPageWallpaper,
                                    uri.toString()
                            )
                        }
                    }
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag") // For older SDKs
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_settings, rootKey)
            initializeLogic()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                settingsActivity.registerReceiver(
                        onDownloadComplete,
                        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                        RECEIVER_EXPORTED
                )
            } else {
                settingsActivity.registerReceiver(
                        onDownloadComplete,
                        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            settingsActivity.unregisterReceiver(onDownloadComplete)
        }

        private fun needLoad(Url: String) {
            val needLoad = Intent()
            needLoad.putExtra("needLoadUrl", Url)
            settingsActivity.setResult(0, needLoad)
            settingsActivity.finish()
        }

        /**
         * Initialize Logic
         */
        private fun initializeLogic() {
            /* Lists */
            val searchHomePageList =
                    settingsActivity.resources.getStringArray(R.array.search_entries)
            val themeList = settingsActivity.resources.getStringArray(R.array.themes)

            /* Settings */
            val search_engine = findPreference<Preference>("search_engine")!!
            val homepage = findPreference<Preference>("homepage")!!
            val search_suggestions = findPreference<Preference>("search_suggestions")!!
            val clear_cache = findPreference<MaterialDialogPreference>("clear_cache")!!
            val clear_cookies = findPreference<MaterialDialogPreference>("clear_cookies")!!
            val reset_to_default = findPreference<MaterialDialogPreference>("reset_to_default")!!
            val theme = findPreference<Preference>("theme")!!
            val start_page_wallpaper = findPreference<Preference>("start_page_wallpaper")!!
            val version = findPreference<Preference>("version")!!
            val feedback = findPreference<Preference>("feedback")!!
            val source_code = findPreference<Preference>("source_code")!!

            search_engine.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        val checkedItem =
                                intArrayOf(settingsPreference.getInt(SettingsKeys.defaultSearchId))
                        MaterialAlertDialogBuilder(settingsActivity).setTitle(resources.getString(R.string.search_engine))
                                .setSingleChoiceItems(
                                        searchHomePageList,
                                        settingsPreference.getInt(SettingsKeys.defaultSearchId)
                                ) { _: DialogInterface?, which: Int -> checkedItem[0] = which }
                                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                    if (checkedItem[0] == 8) {
                                        val layoutInflater = LayoutInflater.from(settingsActivity)
                                        @SuppressLint("InflateParams") val root =
                                                layoutInflater.inflate(R.layout.dialog_edittext, null)
                                        val customSearch =
                                                root.findViewById<AppCompatEditText>(R.id.edittext)
                                        MaterialAlertDialogBuilder(settingsActivity).setTitle(
                                                resources.getString(
                                                        R.string.search_engine
                                                )
                                        )
                                                .setMessage(settingsActivity.resources.getString(R.string.custom_search_guide))
                                                .setView(root)
                                                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                                    if (customSearch.text.toString().isNotEmpty()) {
                                                        settingsPreference.setString(
                                                                SettingsKeys.defaultSearch,
                                                                customSearch.text.toString()
                                                        )
                                                        settingsPreference.setInt(
                                                                SettingsKeys.defaultSearchId,
                                                                checkedItem[0]
                                                        )
                                                        search_engine.summary =
                                                                searchHomePageList[checkedItem[0]]
                                                    }
                                                }
                                                .setNegativeButton(android.R.string.cancel, null)
                                                .create().show()
                                    }
                                    if (checkedItem[0] != 8) {
                                        settingsPreference.setString(
                                                SettingsKeys.defaultSearch,
                                                CommonUtils.EMPTY_STRING
                                        )
                                        settingsPreference.setInt(
                                                SettingsKeys.defaultSearchId,
                                                checkedItem[0]
                                        )
                                        search_engine.summary = searchHomePageList[checkedItem[0]]
                                    }
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .create().show()
                        true
                    }
            homepage.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        val checkedItem =
                                intArrayOf(settingsPreference.getInt(SettingsKeys.defaultSearchId))
                        MaterialAlertDialogBuilder(settingsActivity).setTitle(resources.getString(R.string.homepage))
                                .setSingleChoiceItems(
                                        searchHomePageList,
                                        settingsPreference.getInt(SettingsKeys.defaultHomePageId)
                                ) { _: DialogInterface?, which: Int -> checkedItem[0] = which }
                                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                    if (checkedItem[0] == 8) {
                                        val layoutInflater = LayoutInflater.from(settingsActivity)
                                        @SuppressLint("InflateParams") val root =
                                                layoutInflater.inflate(R.layout.dialog_edittext, null)
                                        val customHomepage =
                                                root.findViewById<AppCompatEditText>(R.id.edittext)
                                        MaterialAlertDialogBuilder(settingsActivity)
                                                .setTitle(resources.getString(R.string.homepage))
                                                .setView(root)
                                                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                                    if (customHomepage.text.toString().isNotEmpty()) {
                                                        settingsPreference.setString(
                                                                SettingsKeys.defaultHomePage,
                                                                customHomepage.text.toString()
                                                        )
                                                        settingsPreference.setInt(
                                                                SettingsKeys.defaultHomePageId,
                                                                checkedItem[0]
                                                        )
                                                        homepage.summary = searchHomePageList[checkedItem[0]]
                                                    }
                                                }
                                                .setNegativeButton(android.R.string.cancel, null)
                                                .create().show()
                                    }
                                    if (checkedItem[0] != 8) {
                                        settingsPreference.setString(
                                                SettingsKeys.defaultHomePage,
                                                CommonUtils.EMPTY_STRING
                                        )
                                        settingsPreference.setInt(
                                                SettingsKeys.defaultHomePageId,
                                                checkedItem[0]
                                        )
                                        homepage.summary = searchHomePageList[checkedItem[0]]
                                    }
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .create().show()
                        true
                    }
            search_suggestions.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        val checkedItem =
                                intArrayOf(settingsPreference.getInt(SettingsKeys.defaultSuggestionsId))
                        MaterialAlertDialogBuilder(settingsActivity).setTitle(resources.getString(R.string.search_suggestions_title))
                                .setSingleChoiceItems(
                                        searchHomePageList,
                                        settingsPreference.getInt(SettingsKeys.defaultSuggestionsId)
                                ) { _: DialogInterface?, which: Int -> checkedItem[0] = which }
                                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                    if (checkedItem[0] == 8) {
                                        val layoutInflater = LayoutInflater.from(settingsActivity)
                                        @SuppressLint("InflateParams") val root =
                                                layoutInflater.inflate(R.layout.dialog_edittext, null)
                                        val customSearchSuggestions =
                                                root.findViewById<AppCompatEditText>(R.id.edittext)
                                        MaterialAlertDialogBuilder(settingsActivity)
                                                .setTitle(resources.getString(R.string.search_suggestions_title))
                                                .setMessage(settingsActivity.resources.getString(R.string.custom_search_guide))
                                                .setView(root)
                                                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                                    if (customSearchSuggestions.text.toString().isNotEmpty()) {
                                                        settingsPreference.setString(
                                                                SettingsKeys.defaultSuggestions,
                                                                customSearchSuggestions.text.toString()
                                                        )
                                                        settingsPreference.setInt(
                                                                SettingsKeys.defaultSuggestionsId,
                                                                checkedItem[0]
                                                        )
                                                        search_suggestions.summary =
                                                                searchHomePageList[checkedItem[0]]
                                                    }
                                                }
                                                .setNegativeButton(android.R.string.cancel, null)
                                                .create().show()
                                    }
                                    if (checkedItem[0] != 8) {
                                        settingsPreference.setString(
                                                SettingsKeys.defaultSuggestions,
                                                CommonUtils.EMPTY_STRING
                                        )
                                        settingsPreference.setInt(
                                                SettingsKeys.defaultSuggestionsId,
                                                checkedItem[0]
                                        )
                                        search_suggestions.summary = searchHomePageList[checkedItem[0]]
                                    }
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .create().show()
                        true
                    }
            clear_cache.materialDialogPreferenceListener =
                    object : MaterialDialogPreferenceListener {
                        override fun onDialogClosed(positiveResult: Boolean) {
                            if (!positiveResult) return
                            WebStorage.getInstance().deleteAllData()
                            showMessage(settingsActivity, resources.getString(R.string.cleared_toast))
                        }
                    }
            clear_cookies.materialDialogPreferenceListener =
                    object : MaterialDialogPreferenceListener {
                        override fun onDialogClosed(positiveResult: Boolean) {
                            if (!positiveResult) return
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                CookieManager.getInstance().removeAllCookies(null)
                                CookieManager.getInstance().flush()
                            } else {
                                val cookieSyncMgr = CookieSyncManager.createInstance(settingsActivity)
                                val cookieManager = CookieManager.getInstance()
                                cookieSyncMgr.startSync()
                                cookieManager.removeAllCookie()
                                cookieManager.removeSessionCookie()
                                cookieSyncMgr.stopSync()
                                cookieSyncMgr.sync()
                            }
                            showMessage(settingsActivity, resources.getString(R.string.cleared_toast))
                        }
                    }
            reset_to_default.materialDialogPreferenceListener =
                    object : MaterialDialogPreferenceListener {
                        override fun onDialogClosed(positiveResult: Boolean) {
                            if (!positiveResult) return
                            showMessage(settingsActivity, resources.getString(R.string.reset_complete))
                            (settingsActivity.getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                        }
                    }
            theme.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        val checkedItem =
                                intArrayOf(settingsPreference.getInt(SettingsKeys.themeId))
                        MaterialAlertDialogBuilder(settingsActivity).setTitle(resources.getString(R.string.pref_theme))
                                .setSingleChoiceItems(
                                        themeList,
                                        settingsPreference.getInt(SettingsKeys.themeId)
                                )
                                { _: DialogInterface?, which: Int -> checkedItem[0] = which }
                                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                    settingsPreference.setInt(
                                            SettingsKeys.themeId,
                                            checkedItem[0]
                                    )
                                    theme.summary = themeList[checkedItem[0]]
                                    darkModeCheck(settingsActivity)
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .create().show()
                        true
                    }
            start_page_wallpaper.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        if (settingsPreference.getString(SettingsKeys.startPageWallpaper)
                                        .isNullOrEmpty()
                        ) {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else {
                            start_page_wallpaper.setSummary(
                                    resources.getString(
                                            R.string.pref_start_page_wallpaper_summary,
                                            resources.getString(R.string.default_res)
                                    )
                            )
                            settingsPreference.setString(
                                    SettingsKeys.startPageWallpaper,
                                    CommonUtils.EMPTY_STRING
                            )
                        }
                        true
                    }
            version.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        @SuppressLint("InflateParams") val dialogView =
                                this.layoutInflater.inflate(R.layout.about_dialog, null)
                        val dialog = MaterialAlertDialogBuilder(settingsActivity).setView(dialogView)
                                .setPositiveButton(android.R.string.ok, null)
                                .create()
                        val dialog_text = dialogView.findViewById<AppCompatTextView>(R.id.dialog_text)
                        val update_btn = dialogView.findViewById<AppCompatButton>(R.id.update_btn)
                        val changelog_btn = dialogView.findViewById<AppCompatButton>(R.id.changelog_btn)
                        val license_btn = dialogView.findViewById<AppCompatButton>(R.id.license_btn)
                        dialog_text.text = resources.getString(
                                R.string.version_info_message,
                                resources.getString(R.string.app_name),
                                BuildConfig.VERSION_NAME + BuildConfig.VERSION_NAME_EXTRA,
                                BuildConfig.VERSION_CODENAME,
                                BuildConfig.VERSION_BUILD_DATE,
                                BuildConfig.VERSION_BUILD_YEAR
                        )
                        update_btn.setOnClickListener {
                            if (!DownloadUtils.isOnline(settingsActivity)) {
                                showMessage(
                                        settingsActivity,
                                        resources.getString(R.string.network_unavailable_toast)
                                )
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                val jObject = JSONObject(String(DownloadUtils.startFileDownload("https://gitlab.com/TipzTeam/viola/-/raw/update_files/updates.json")))
                                val jChannelObject =
                                        jObject.getJSONObject(BuildConfig.BUILD_TYPE).getJSONObject("latest_update")

                                CoroutineScope(Dispatchers.Main).launch {
                                    if (jChannelObject.getInt("code") <= BuildConfig.VERSION_CODE) {
                                        showMessage(
                                                settingsActivity,
                                                resources.getString(R.string.version_latest_toast)
                                        )
                                        return@launch
                                    }

                                    MaterialAlertDialogBuilder(
                                            settingsActivity
                                    )
                                            .setTitle(resources.getString(R.string.new_update_detect_title))
                                            .setMessage(
                                                    resources.getString(
                                                            R.string.new_update_detect_message,
                                                            jChannelObject.getString("name"),
                                                            jChannelObject.getInt("code").toString()
                                                    )
                                            )
                                            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                                val filename =
                                                        updateDownloadPathBase + DocumentFile.fromSingleUri(
                                                                settingsActivity,
                                                                Uri.parse(jChannelObject.getString("url"))
                                                        )?.name
                                                this@SettingsPrefHandler.updateDownloadPath =
                                                        updateDownloadPathBase + filename
                                                val apkFile = File(updateDownloadPath!!)

                                                if (!apkFile.exists() || apkFile.delete()) downloadID =
                                                        dmDownloadFile(
                                                                settingsActivity,
                                                                jChannelObject.getString("url"),
                                                                null,
                                                                "application/vnd.android.package-archive",
                                                                resources.getString(R.string.download_title),
                                                                filename,
                                                                null
                                                        )
                                                else
                                                    showMessage(
                                                            settingsActivity,
                                                            resources.getString(R.string.update_down_failed_toast)
                                                    )
                                            }
                                            .setNegativeButton(android.R.string.cancel, null)
                                            .create().show()
                                }
                            }
                        }
                        changelog_btn.visibility = if (BuildConfig.DEBUG) View.GONE else View.VISIBLE
                        changelog_btn.setOnClickListener {
                            needLoad(InternalUrls.realChangelogUrl)
                            dialog.dismiss()
                        }
                        license_btn.setOnClickListener {
                            needLoad(InternalUrls.licenseUrl)
                            dialog.dismiss()
                        }
                        dialog.show()
                        true
                    }
            Preference.OnPreferenceClickListener {
                needLoad(InternalUrls.feedbackUrl)
                true
            }.also { feedback.onPreferenceClickListener = it }
            source_code.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        needLoad(InternalUrls.sourceUrl)
                        true
                    }
            search_engine.summary =
                    searchHomePageList[settingsPreference.getInt(SettingsKeys.defaultSearchId)]
            homepage.summary =
                    searchHomePageList[settingsPreference.getInt(SettingsKeys.defaultHomePageId)]
            search_suggestions.summary =
                    searchHomePageList[settingsPreference.getInt(SettingsKeys.defaultSuggestionsId)]
            theme.summary = themeList[settingsPreference.getInt(SettingsKeys.themeId)]
            if (settingsPreference.getString(SettingsKeys.startPageWallpaper).isNullOrEmpty()) {
                start_page_wallpaper.setSummary(
                        resources.getString(
                                R.string.pref_start_page_wallpaper_summary,
                                resources.getString(R.string.default_res)
                        )
                )
            } else {
                start_page_wallpaper.setSummary(
                        resources.getString(
                                R.string.pref_start_page_wallpaper_summary,
                                DocumentFile.fromSingleUri(
                                        settingsActivity,
                                        Uri.parse(settingsPreference.getString(SettingsKeys.startPageWallpaper))
                                )?.name
                        )
                )
            }
            version.summary =
                    resources.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME + BuildConfig.VERSION_NAME_EXTRA
            needReload = false
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            var dialogFragment: DialogFragment? = null
            if (preference is MaterialDialogPreference) {
                dialogFragment =
                        newInstance(preference.getKey(), preference.materialDialogPreferenceListener!!)
            }
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(
                        requireFragmentManager(),
                        "androidx.preference.PreferenceFragment.DIALOG"
                )
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }

        companion object {
            var needReload = false
        }
    }

    companion object {
        var settingsPrefHandler: SettingsPrefHandler? = null
    }
}