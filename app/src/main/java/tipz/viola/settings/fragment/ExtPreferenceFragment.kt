// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import tipz.viola.Application
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference
import tipz.viola.settings.activity.MaterialDialogPreference
import tipz.viola.settings.activity.MaterialPreferenceDialogFragmentCompat.Companion.newInstance
import tipz.viola.settings.activity.SettingsActivity

open class ExtPreferenceFragment(@StringRes private val titleResId: Int) : PreferenceFragmentCompat() {
    internal lateinit var settingsActivity: SettingsActivity
    internal lateinit var settingsPreference: SettingsSharedPreference

    constructor() : this(ResourcesCompat.ID_NULL)

    fun getPreferenceTitle(): String {
        if (titleResId == ResourcesCompat.ID_NULL) return ""
        return resources.getString(titleResId)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.settingsActivity = context as SettingsActivity
        this.settingsPreference =
            (settingsActivity.applicationContext as Application).settingsPreference
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // This is intentionally left empty
    }

    @Suppress("DEPRECATION")
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

    val getNeedLoadFromNonMain =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.data == null) return@registerForActivityResult
            result.data!!.getStringExtra(SettingsKeys.needLoadUrl)?.let { needLoad(it) }
        }

    private fun needLoad(url: String) {
        val needLoad = Intent()
        needLoad.putExtra(SettingsKeys.needLoadUrl, url)
        settingsActivity.setResult(0, needLoad)
        settingsActivity.finish()
    }

    companion object {
        var needReload = false
    }
}