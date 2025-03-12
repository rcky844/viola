// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("DEPRECATION")

package tipz.viola.settings.activity

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebStorage
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity.ACTIVITY_SERVICE
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.build.info.BuildInfoActivity
import tipz.viola.Application
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.databinding.DialogEdittextBinding
import tipz.viola.ext.showMessage
import tipz.viola.search.SearchEngineEntries
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.settings.activity.MaterialPreferenceDialogFragmentCompat.Companion.newInstance
import tipz.viola.settings.activity.MaterialPreferenceDialogFragmentCompat.MaterialDialogPreferenceListener
import tipz.viola.utils.UpdateService
import tipz.viola.webview.activity.BaseActivity.Companion.performThemeModeChecks
import java.io.IOException

class SettingsMainFragment : PreferenceFragmentCompat() {
    private lateinit var settingsActivity: SettingsActivity
    private lateinit var settingsPreference: SettingsSharedPreference

    /* Preferences */
    private lateinit var searchEngine: Preference
    private lateinit var homePage: Preference
    private lateinit var searchSuggestions: Preference
    private lateinit var adBlockerSource: Preference
    private lateinit var adBlockerDownload: Preference
    private lateinit var clearCache: MaterialDialogPreference
    private lateinit var clearCookies: MaterialDialogPreference
    private lateinit var resetToDefault: MaterialDialogPreference
    private lateinit var themePicker: Preference
    private lateinit var startPageWallpaper: Preference
    private lateinit var checkUpdates: Preference
    private lateinit var updateChannel: Preference
    private lateinit var about: Preference

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            settingsActivity.contentResolver.takePersistableUriPermission(uri, flag)
            settingsPreference.setString(SettingsKeys.startPageWallpaper, uri.toString())
            startPageWallpaper.summary = resources.getString(
                R.string.pref_start_page_wallpaper_summary,
                DocumentFile.fromSingleUri(settingsActivity, uri)?.name
            )
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.settingsActivity = context as SettingsActivity
        this.settingsPreference =
            (settingsActivity.applicationContext as Application).settingsPreference
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag") // For older SDKs
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings_main, rootKey)

        /* Lists */
        val searchHomePageList =
            settingsActivity.resources.getStringArray(R.array.search_entries)
        val adBlockerHostsEntries =
            settingsActivity.resources.getStringArray(R.array.ad_blocker_hosts_entries)
        val themeList = settingsActivity.resources.getStringArray(R.array.themes)

        /* Settings */
        searchEngine = findPreference("search_engine")!!
        homePage = findPreference("homepage")!!
        searchSuggestions = findPreference("search_suggestions")!!
        adBlockerSource = findPreference("adBlockerSource")!!
        adBlockerDownload = findPreference("adBlockerDownload")!!
        clearCache = findPreference("clear_cache")!!
        clearCookies = findPreference("clear_cookies")!!
        resetToDefault = findPreference("reset_to_default")!!
        themePicker = findPreference("theme")!!
        startPageWallpaper = findPreference("start_page_wallpaper")!!
        checkUpdates = findPreference("check_for_updates")!!
        updateChannel = findPreference("update_channel")!!
        about = findPreference("about")!!

        searchEngine.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = searchEngine
                    nameList = searchHomePageList
                    namePreference = SettingsKeys.searchName
                    nameToIdFunction = SearchEngineEntries::getIndexByName
                    stringPreference = SettingsKeys.searchCustomUrl
                    dialogTitleResId = R.string.search_engine
                    dialogCustomMessageResId = R.string.search_dialog_custom_message
                    customIndexEnabled = true
                    customIndex = SearchEngineEntries.customIndex
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
        homePage.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = homePage
                    nameList = searchHomePageList
                    namePreference = SettingsKeys.homePageName
                    nameToIdFunction = SearchEngineEntries::getIndexByName
                    stringPreference = SettingsKeys.homePageCustomUrl
                    dialogTitleResId = R.string.homepage
                    customIndexEnabled = true
                    customIndex = SearchEngineEntries.customIndex
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
        searchSuggestions.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = searchSuggestions
                    nameList = searchHomePageList
                    namePreference = SettingsKeys.suggestionsName
                    nameToIdFunction = SearchEngineEntries::getIndexByName
                    stringPreference = SettingsKeys.suggestionsCustomUrl
                    dialogTitleResId = R.string.search_suggestions_title
                    dialogCustomMessageResId = R.string.search_dialog_custom_message
                    customIndexEnabled = true
                    customIndex = SearchEngineEntries.customIndex
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
        adBlockerSource.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = adBlockerSource
                    nameList = adBlockerHostsEntries
                    idPreference = SettingsKeys.adServerId
                    stringPreference = SettingsKeys.adServerUrl
                    dialogTitleResId = R.string.pref_ad_blocker_source_title
                    customIndexEnabled = true
                    customIndex = SearchEngineEntries.customIndex
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
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
        clearCache.materialDialogPreferenceListener =
            object : MaterialDialogPreferenceListener {
                override fun onDialogClosed(positiveResult: Boolean) {
                    if (!positiveResult) return
                    WebStorage.getInstance().deleteAllData()
                    settingsActivity.showMessage(R.string.toast_cleared)
                }
            }
        clearCookies.materialDialogPreferenceListener =
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
                    settingsActivity.showMessage(R.string.toast_cleared)
                }
            }
        resetToDefault.materialDialogPreferenceListener =
            object : MaterialDialogPreferenceListener {
                override fun onDialogClosed(positiveResult: Boolean) {
                    if (!positiveResult) return
                    settingsActivity.showMessage(R.string.toast_reset_complete)
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
        themePicker.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = themePicker
                    nameList = themeList
                    idPreference = SettingsKeys.themeId
                    dialogTitleResId = R.string.pref_theme_title
                    dialogPositivePressed = {
                        performThemeModeChecks(settingsActivity)
                    }
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
        startPageWallpaper.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                if (settingsPreference.getString(SettingsKeys.startPageWallpaper).isEmpty()) {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    startPageWallpaper.setSummary(
                        resources.getString(
                            R.string.pref_start_page_wallpaper_summary,
                            resources.getString(R.string.default_res)
                        )
                    )
                    settingsPreference.setString(SettingsKeys.startPageWallpaper, "")
                }
                true
            }

        checkUpdates.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                UpdateService(settingsActivity, false).checkUpdates()
                true
            }

        // TODO: Load update channels from online JSON
        updateChannel.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val binding: DialogEdittextBinding = DialogEdittextBinding.inflate(layoutInflater)
                val view = binding.root

                val editText = binding.edittext
                editText.setText(settingsPreference.getString(SettingsKeys.updateChannelName))
                
                MaterialAlertDialogBuilder(settingsActivity)
                    .setTitle(R.string.pref_update_channel_title)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        settingsPreference.setString(
                            SettingsKeys.updateChannelName,
                            editText.text.toString().trim()
                        )
                        updateChannel.summary = editText.text.toString().trim()
                            .ifEmpty { BuildConfig.VERSION_BUILD_TYPE }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show()
                true
            }

        about.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val intent = Intent(context, BuildInfoActivity::class.java)
                getNeedLoadFromNonMain.launch(intent)
                true
            }
        searchEngine.summary =
            searchHomePageList[SearchEngineEntries.getIndexByName(
                settingsPreference.getString(SettingsKeys.searchName))]
        homePage.summary =
            searchHomePageList[SearchEngineEntries.getIndexByName(
                settingsPreference.getString(SettingsKeys.homePageName))]
        searchSuggestions.summary =
            searchHomePageList[SearchEngineEntries.getIndexByName(
                settingsPreference.getString(SettingsKeys.suggestionsName))]
        adBlockerSource.summary =
            adBlockerHostsEntries[settingsPreference.getInt(SettingsKeys.adServerId)]
        themePicker.summary = themeList[settingsPreference.getInt(SettingsKeys.themeId)]
        updateChannel.summary = settingsPreference.getString(SettingsKeys.updateChannelName)
            .ifEmpty { BuildConfig.VERSION_BUILD_TYPE }
        startPageWallpaper.summary = resources.getString(
            R.string.pref_start_page_wallpaper_summary,
            if (settingsPreference.getString(SettingsKeys.startPageWallpaper).isEmpty()) {
                resources.getString(R.string.default_res)
            } else {
                DocumentFile.fromSingleUri(
                    settingsActivity,
                    Uri.parse(settingsPreference.getString(SettingsKeys.startPageWallpaper))
                )?.name
            }
        )
        about.summary =
            resources.getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME
    }

    private fun needLoad(url: String) {
        val needLoad = Intent()
        needLoad.putExtra(SettingsKeys.needLoadUrl, url)
        settingsActivity.setResult(0, needLoad)
        settingsActivity.finish()
    }

    val getNeedLoadFromNonMain =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.data == null) return@registerForActivityResult
            result.data!!.getStringExtra(SettingsKeys.needLoadUrl)?.let { needLoad(it) }
        }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is MaterialDialogPreference) {
            dialogFragment =
                newInstance(preference.getKey(), preference.materialDialogPreferenceListener)
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