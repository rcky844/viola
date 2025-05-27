// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.annotation.XmlRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import tipz.viola.R
import tipz.viola.databinding.ActivitySettingsBinding
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.ui.fragment.AppearanceFragment
import tipz.viola.settings.ui.fragment.DevelopmentFragment
import tipz.viola.settings.ui.fragment.DownloadsFragment
import tipz.viola.settings.ui.fragment.ExtPreferenceFragment
import tipz.viola.settings.ui.fragment.HomeFragment
import tipz.viola.settings.ui.fragment.MainFragment
import tipz.viola.settings.ui.fragment.PrivacySecurityFragment
import tipz.viola.settings.ui.fragment.SearchFragment
import tipz.viola.settings.ui.fragment.WebFeaturesFragment
import tipz.viola.webview.activity.BaseActivity


class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val needLoad = Intent()

    private class TitleUpdater : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            val settingsFragment = try {
                f as ExtPreferenceFragment
            } catch (_: Exception) {
                return
            }
            (f.activity as SettingsActivity).supportActionBar?.title = settingsFragment.getPreferenceTitle()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Setup toolbar
        val toolbar = binding.materialToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        if (savedInstanceState != null) {
            supportActionBar?.title = savedInstanceState.getString(BUNDLE_ACTION_BAR_TITLE)
        }
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        onBackPressedDispatcher.addCallback(this) {
            needLoad.putExtra(SettingsKeys.needReload, ExtPreferenceFragment.needReload)
            setResult(0, needLoad)
            if (supportFragmentManager.backStackEntryCount <= 1) finish()
            else supportFragmentManager.popBackStack()
        }
        ExtPreferenceFragment.needReload = false

        // Setup fragments
        supportFragmentManager.registerFragmentLifecycleCallbacks(TitleUpdater(), false)
        if (savedInstanceState == null) {
            val fragment = getPreferenceScreen(
                intent.getIntExtra(EXTRA_INITIAL_PREF_SCREEN, R.xml.preference_settings_main)
            ) ?: MainFragment()
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.shared_x_axis_open_enter, R.anim.shared_x_axis_open_exit,
                    R.anim.shared_x_axis_close_enter, R.anim.shared_x_axis_close_exit)
                .replace(R.id.list_container, fragment)
                .addToBackStack(null).commit()
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString(BUNDLE_ACTION_BAR_TITLE, supportActionBar?.title.toString())
    }


    private fun getPreferenceScreen(@XmlRes screen: Int): ExtPreferenceFragment? =
        when (screen) {
            R.xml.preference_settings_main -> MainFragment()
            R.xml.preference_settings_home -> HomeFragment()
            R.xml.preference_settings_search -> SearchFragment()
            R.xml.preference_settings_privacy_security -> PrivacySecurityFragment()
            R.xml.preference_settings_appearance -> AppearanceFragment()
            R.xml.preference_settings_downloads -> DownloadsFragment()
            R.xml.preference_settings_web_features -> WebFeaturesFragment()
            R.xml.preference_settings_development -> DevelopmentFragment()
            else -> null
        }

    fun openScreen(@XmlRes screen: Int): ExtPreferenceFragment? {
        val fragment = getPreferenceScreen(screen) ?: return null
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.shared_x_axis_open_enter, R.anim.shared_x_axis_open_exit,
                R.anim.shared_x_axis_close_enter, R.anim.shared_x_axis_close_exit)
            .replace(R.id.list_container, fragment)
            .addToBackStack(null).commit()
        return fragment
    }

    companion object {
        const val EXTRA_INITIAL_PREF_SCREEN = "initial_pref_screen"
        private const val BUNDLE_ACTION_BAR_TITLE = "action_bar_title"
    }
}