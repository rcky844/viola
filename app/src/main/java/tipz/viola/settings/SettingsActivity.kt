// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("DEPRECATION")

package tipz.viola.settings

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebStorage
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import tipz.build.info.BuildInfoDialog
import tipz.viola.Application
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.download.DownloadClient
import tipz.viola.download.DownloadObject
import tipz.viola.download.DownloadProvider
import tipz.viola.download.DownloadUtils
import tipz.viola.download.MiniDownloadHelper
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.MaterialPreferenceDialogFragmentCompat.Companion.newInstance
import tipz.viola.settings.MaterialPreferenceDialogFragmentCompat.MaterialDialogPreferenceListener
import tipz.viola.utils.ApkInstaller
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.CommonUtils.showMessage
import tipz.viola.webview.pages.ExportedUrls
import tipz.viola.webviewui.BaseActivity
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference


class SettingsActivity : BaseActivity() {
    private val needLoad = Intent()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val toolbar = findViewById<Toolbar>(R.id._toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        onBackPressedDispatcher.addCallback(this) {
            needLoad.putExtra(SettingsKeys.needReload, SettingsPrefHandler.needReload)
            setResult(0, needLoad)
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
        private var settingsActivity: AppCompatActivity
        private val settingsPreference: SettingsSharedPreference
        private val updateConfigLiveData = MutableLiveData<JSONObject>()
        private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

        private val dialogVersionDetails = BuildInfoDialog.BuildInfoDialogDetails.also {
            it.loader = this::needLoad
            it.changelogUrl = ExportedUrls.changelogUrl
            it.licenseUrl = ExportedUrls.actualLicenseUrl
        }

        init {
            val activity = WeakReference(act)
            settingsActivity = activity.get()!!
            settingsPreference =
                (settingsActivity.applicationContext as Application).settingsPreference

            @RequiresApi(Build.VERSION_CODES.KITKAT)
            pickMedia =
                registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    if (uri == null) return@registerForActivityResult
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    settingsActivity.contentResolver.takePersistableUriPermission(uri, flag)
                    settingsPreference.setString(SettingsKeys.startPageWallpaper, uri.toString())
                }
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag") // For older SDKs
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_settings, rootKey)
            initializeLogic()
        }

        override fun onDestroyView() {
            super.onDestroyView()
        }

        private fun needLoad(url: String) {
            val needLoad = Intent()
            needLoad.putExtra(SettingsKeys.needLoadUrl, url)
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
            val adBlockerHostsEntries =
                settingsActivity.resources.getStringArray(R.array.ad_blocker_hosts_entries)
            val themeList = settingsActivity.resources.getStringArray(R.array.themes)

            /* Settings */
            val search_engine = findPreference<Preference>("search_engine")!!
            val homepage = findPreference<Preference>("homepage")!!
            val search_suggestions = findPreference<Preference>("search_suggestions")!!
            val adBlockerSource = findPreference<Preference>("adBlockerSource")!!
            val adBlockerDownload = findPreference<Preference>("adBlockerDownload")!!
            val clear_cache = findPreference<MaterialDialogPreference>("clear_cache")!!
            val clear_cookies = findPreference<MaterialDialogPreference>("clear_cookies")!!
            val reset_to_default = findPreference<MaterialDialogPreference>("reset_to_default")!!
            val theme = findPreference<Preference>("theme")!!
            val start_page_wallpaper = findPreference<Preference>("start_page_wallpaper")!!
            val check_for_updates = findPreference<Preference>("check_for_updates")!!
            val update_channel = findPreference<Preference>("update_channel")!!
            val version = findPreference<Preference>("version")!!
            val website = findPreference<Preference>("website")!!
            val feedback = findPreference<Preference>("feedback")!!
            val source_code = findPreference<Preference>("source_code")!!

            search_engine.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val listPickerAlertDialog =
                        ListPickerAlertDialog(settingsActivity, settingsPreference)
                    val listPickerObject = listPickerAlertDialog.getListPickerObject()
                    listPickerObject.preference = search_engine
                    listPickerObject.nameList = searchHomePageList
                    listPickerObject.namePreference = SettingsKeys.searchName
                    listPickerObject.nameToIdFunction = SearchEngineEntries::getIndexByName
                    listPickerObject.stringPreference = SettingsKeys.searchCustomUrl
                    listPickerObject.dialogTitleResId = R.string.search_engine
                    listPickerObject.dialogCustomMessageResId = R.string.custom_search_guide
                    listPickerObject.customIndexEnabled = true
                    listPickerObject.customIndex = SearchEngineEntries.customIndex

                    listPickerAlertDialog.setupDialogForShowing()
                    listPickerAlertDialog.create().show()
                    true
                }
            homepage.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val listPickerAlertDialog =
                        ListPickerAlertDialog(settingsActivity, settingsPreference)
                    val listPickerObject = listPickerAlertDialog.getListPickerObject()
                    listPickerObject.preference = homepage
                    listPickerObject.nameList = searchHomePageList
                    listPickerObject.namePreference = SettingsKeys.homePageName
                    listPickerObject.nameToIdFunction = SearchEngineEntries::getIndexByName
                    listPickerObject.stringPreference = SettingsKeys.homePageCustomUrl
                    listPickerObject.dialogTitleResId = R.string.homepage
                    listPickerObject.customIndexEnabled = true
                    listPickerObject.customIndex = SearchEngineEntries.customIndex

                    listPickerAlertDialog.setupDialogForShowing()
                    listPickerAlertDialog.create().show()
                    true
                }
            search_suggestions.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val listPickerAlertDialog =
                        ListPickerAlertDialog(settingsActivity, settingsPreference)
                    val listPickerObject = listPickerAlertDialog.getListPickerObject()
                    listPickerObject.preference = search_suggestions
                    listPickerObject.nameList = searchHomePageList
                    listPickerObject.namePreference = SettingsKeys.suggestionsName
                    listPickerObject.nameToIdFunction = SearchEngineEntries::getIndexByName
                    listPickerObject.stringPreference = SettingsKeys.suggestionsCustomUrl
                    listPickerObject.dialogTitleResId = R.string.search_suggestions_title
                    listPickerObject.dialogCustomMessageResId = R.string.custom_search_guide
                    listPickerObject.customIndexEnabled = true
                    listPickerObject.customIndex = SearchEngineEntries.customIndex

                    listPickerAlertDialog.setupDialogForShowing()
                    listPickerAlertDialog.create().show()
                    true
                }
            adBlockerSource.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val listPickerAlertDialog =
                        ListPickerAlertDialog(settingsActivity, settingsPreference)
                    val listPickerObject = listPickerAlertDialog.getListPickerObject()
                    listPickerObject.preference = adBlockerSource
                    listPickerObject.nameList = adBlockerHostsEntries
                    listPickerObject.idPreference = SettingsKeys.adServerId
                    listPickerObject.stringPreference = SettingsKeys.adServerUrl
                    listPickerObject.dialogTitleResId = R.string.pref_adBlockerSource_title
                    listPickerObject.customIndexEnabled = true
                    listPickerObject.customIndex = SearchEngineEntries.customIndex

                    listPickerAlertDialog.setupDialogForShowing()
                    listPickerAlertDialog.create().show()
                    true
                }
            adBlockerDownload.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val intent = Intent()
                    intent.putExtra(SettingsKeys.updateAdServers, 1)
                    settingsActivity.setResult(0, intent)
                    settingsActivity.finish()
                    true
                }
            clear_cache.materialDialogPreferenceListener =
                object : MaterialDialogPreferenceListener {
                    override fun onDialogClosed(positiveResult: Boolean) {
                        if (!positiveResult) return
                        WebStorage.getInstance().deleteAllData()
                        showMessage(settingsActivity, R.string.cleared_toast)
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
                        showMessage(settingsActivity, R.string.cleared_toast)
                    }
                }
            reset_to_default.materialDialogPreferenceListener =
                object : MaterialDialogPreferenceListener {
                    override fun onDialogClosed(positiveResult: Boolean) {
                        if (!positiveResult) return
                        showMessage(settingsActivity, R.string.reset_complete)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            (settingsActivity.getSystemService(ACTIVITY_SERVICE)
                                    as ActivityManager).clearApplicationUserData()
                        } else {
                            val packageName = settingsActivity.packageName
                            val runtime = Runtime.getRuntime()
                            try {
                                runtime.exec("pm clear $packageName")
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            theme.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val listPickerAlertDialog =
                        ListPickerAlertDialog(settingsActivity, settingsPreference)
                    val listPickerObject = listPickerAlertDialog.getListPickerObject()
                    listPickerObject.preference = theme
                    listPickerObject.nameList = themeList
                    listPickerObject.idPreference = SettingsKeys.themeId
                    listPickerObject.dialogTitleResId = R.string.pref_theme
                    listPickerObject.dialogPositivePressed = {
                        darkModeCheck(settingsActivity)
                    }

                    listPickerAlertDialog.setupDialogForShowing()
                    listPickerAlertDialog.create().show()
                    true
                }
            start_page_wallpaper.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (settingsPreference.getString(SettingsKeys.startPageWallpaper).isEmpty()) {
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

            check_for_updates.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (!DownloadUtils.isOnline(settingsActivity)) {
                        showMessage(settingsActivity, R.string.network_unavailable_toast)
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        updateConfigLiveData.postValue(JSONObject(String(
                            MiniDownloadHelper.startDownload(ExportedUrls.updateJSONUrl)!!)
                        ))
                    }
                    true
                }

            // Updates JSON Object observer
            updateConfigLiveData.observe(this, Observer {
                val jObject = it ?: return@Observer

                // Get update channel name
                var updateChannelName = settingsPreference.getString(SettingsKeys.updateChannelName)
                if (updateChannelName.isBlank()) updateChannelName = BuildConfig.BUILD_TYPE
                if (!jObject.has(updateChannelName)) {
                    showMessage(settingsActivity, R.string.update_down_failed_toast)
                    return@Observer
                }

                // Process the selected update channel data
                val jChannelObject = jObject.getJSONObject(updateChannelName)
                val jChannelDataString =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) "channel_data_modern"
                    else "channel_data_legacy"

                if (!jChannelObject.has(jChannelDataString)) {
                    showMessage(settingsActivity, R.string.version_latest_toast)
                    return@Observer
                }

                // Process the update channel object
                val jChannelUpdateObject = jChannelObject.getJSONObject(jChannelDataString)
                if (jChannelUpdateObject.getInt("code") <= BuildConfig.VERSION_CODE) {
                    showMessage(settingsActivity, R.string.version_latest_toast)
                    return@Observer
                }

                MaterialAlertDialogBuilder(settingsActivity)
                    .setTitle(R.string.new_update_detect_title)
                    .setMessage(
                        resources.getString(
                            R.string.new_update_detect_message,
                            jChannelUpdateObject.getString("name"),
                            jChannelUpdateObject.getInt("code").toString()
                        )
                    )
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        val filename = jChannelUpdateObject.getString("download_url")
                            .substringAfterLast('/')
                        val updateDownloadPath = DownloadClient.defaultDownloadPath + filename
                        val apkFile = File(updateDownloadPath)

                        if (!apkFile.exists() || apkFile.delete()) {
                            DownloadClient(settingsActivity).addToQueue(DownloadObject().apply {
                                // TODO: reimplement resources.getString(R.string.download_title)
                                // TODO: Move to mini-download client
                                uriString = jChannelUpdateObject.getString("download_url")
                                mimeType = "application/vnd.android.package-archive"
                                this.filename = filename
                                statusListener =
                                    DownloadProvider.Companion.DownloadStatusListener { status ->
                                        if (status == DownloadProvider.Companion.DownloadStatus.STOPPED)
                                            ApkInstaller.installApplication(
                                                settingsActivity,
                                                updateDownloadPath
                                            )
                                    }
                            })
                        } else {
                            showMessage(settingsActivity, R.string.update_down_failed_toast)
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show()

            })

            // TODO: Load update channels from online JSON
            update_channel.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val layoutInflater = LayoutInflater.from(settingsActivity)
                    @SuppressLint("InflateParams") val root =
                        layoutInflater.inflate(R.layout.dialog_edittext, null)
                    val updateChannel =
                        root.findViewById<AppCompatEditText>(R.id.edittext)
                    MaterialAlertDialogBuilder(settingsActivity)
                        .setTitle(R.string.pref_update_channel_title)
                        .setView(root)
                        .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                            if (updateChannel.text.toString().isNotEmpty()) {
                                settingsPreference.setString(
                                    SettingsKeys.updateChannelName,
                                    updateChannel.text.toString()
                                )
                                update_channel.summary = updateChannel.text.toString()
                            }
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show()
                    true
                }

            version.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    val buildInfoDialog = BuildInfoDialog(settingsActivity, dialogVersionDetails)
                    buildInfoDialog.setupDialogForShowing()
                    buildInfoDialog.create().show()
                    true
                }
            website.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    needLoad(ExportedUrls.websiteUrl)
                    true
                }
            Preference.OnPreferenceClickListener {
                needLoad(ExportedUrls.feedbackUrl)
                true
            }.also { feedback.onPreferenceClickListener = it }
            source_code.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    needLoad(ExportedUrls.sourceUrl)
                    true
                }
            search_engine.summary =
                searchHomePageList[SearchEngineEntries.getIndexByName(
                    settingsPreference.getString(SettingsKeys.searchName))]
            homepage.summary =
                searchHomePageList[SearchEngineEntries.getIndexByName(
                    settingsPreference.getString(SettingsKeys.homePageName))]
            search_suggestions.summary =
                searchHomePageList[SearchEngineEntries.getIndexByName(
                    settingsPreference.getString(SettingsKeys.suggestionsName))]
            adBlockerSource.summary =
                adBlockerHostsEntries[settingsPreference.getInt(SettingsKeys.adServerId)]
            theme.summary = themeList[settingsPreference.getInt(SettingsKeys.themeId)]
            update_channel.summary = settingsPreference.getString(SettingsKeys.updateChannelName)
            if (settingsPreference.getString(SettingsKeys.startPageWallpaper).isEmpty()) {
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
                resources.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME
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