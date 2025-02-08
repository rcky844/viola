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
import tipz.viola.webview.activity.BaseActivity.Companion.darkModeCheck
import java.io.IOException

class SettingsMainFragment : PreferenceFragmentCompat() {
    private lateinit var settingsActivity: SettingsActivity
    private lateinit var settingsPreference: SettingsSharedPreference

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) return@registerForActivityResult
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            settingsActivity.contentResolver.takePersistableUriPermission(uri, flag)
            settingsPreference.setString(SettingsKeys.startPageWallpaper, uri.toString())
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
        val about = findPreference<Preference>("about")!!

        search_engine.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = search_engine
                    nameList = searchHomePageList
                    namePreference = SettingsKeys.searchName
                    nameToIdFunction = SearchEngineEntries::getIndexByName
                    stringPreference = SettingsKeys.searchCustomUrl
                    dialogTitleResId = R.string.search_engine
                    dialogCustomMessageResId = R.string.custom_search_guide
                    customIndexEnabled = true
                    customIndex = SearchEngineEntries.customIndex
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
        homepage.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = homepage
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
        search_suggestions.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = search_suggestions
                    nameList = searchHomePageList
                    namePreference = SettingsKeys.suggestionsName
                    nameToIdFunction = SearchEngineEntries::getIndexByName
                    stringPreference = SettingsKeys.suggestionsCustomUrl
                    dialogTitleResId = R.string.search_suggestions_title
                    dialogCustomMessageResId = R.string.custom_search_guide
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
                    dialogTitleResId = R.string.pref_adBlockerSource_title
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
        clear_cache.materialDialogPreferenceListener =
            object : MaterialDialogPreferenceListener {
                override fun onDialogClosed(positiveResult: Boolean) {
                    if (!positiveResult) return
                    WebStorage.getInstance().deleteAllData()
                    settingsActivity.showMessage(R.string.cleared_toast)
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
                    settingsActivity.showMessage(R.string.cleared_toast)
                }
            }
        reset_to_default.materialDialogPreferenceListener =
            object : MaterialDialogPreferenceListener {
                override fun onDialogClosed(positiveResult: Boolean) {
                    if (!positiveResult) return
                    settingsActivity.showMessage(R.string.reset_complete)
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
                val listPickerObject = ListPickerAlertDialog.ListPickerObject().apply {
                    preference = theme
                    nameList = themeList
                    idPreference = SettingsKeys.themeId
                    dialogTitleResId = R.string.pref_theme
                    dialogPositivePressed = {
                        darkModeCheck(settingsActivity)
                    }
                }

                ListPickerAlertDialog(settingsActivity, settingsPreference, listPickerObject)
                    .create().show()
                true
            }
        start_page_wallpaper.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                if (settingsPreference.getString(SettingsKeys.startPageWallpaper).isEmpty()) {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    start_page_wallpaper.setSummary(
                        resources.getString(
                            R.string.pref_start_page_wallpaper_summary,
                            resources.getString(R.string.default_res)
                        )
                    )
                    settingsPreference.setString(SettingsKeys.startPageWallpaper, "")
                }
                true
            }

        check_for_updates.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                UpdateService(settingsActivity, false).checkUpdates()
                true
            }

        // TODO: Load update channels from online JSON
        update_channel.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val binding: DialogEdittextBinding = DialogEdittextBinding.inflate(layoutInflater)
                val view = binding.root

                val updateChannel = binding.edittext
                MaterialAlertDialogBuilder(settingsActivity)
                    .setTitle(R.string.pref_update_channel_title)
                    .setView(view)
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

        about.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val intent = Intent(context, BuildInfoActivity::class.java)
                getNeedLoadFromNonMain.launch(intent)
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