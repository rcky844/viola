// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.build.info

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.settings.SettingsKeys
import tipz.viola.webview.pages.ProjectUrls

class BuildInfoFragment : PreferenceFragmentCompat() {
    private lateinit var buildInfoActivity: BuildInfoActivity
    private val buildInfo = BuildInfo()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.buildInfoActivity = context as BuildInfoActivity
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_buildinfo, rootKey)

        findPreference<Preference>(PREF_LICENSE)?.setOnPreferenceClickListener {
            val textCopyright = resources.getString(
                R.string.buildinfo_copyright,
                buildInfo.productCopyrightYear
            )
            val textLicense = resources.getString(
                R.string.buildinfo_license,
                buildInfo.productLicenseDocument
            )

            MaterialAlertDialogBuilder(buildInfoActivity)
                .setTitle(R.string.buildinfo_license_button)
                .setMessage("$textCopyright\n$textLicense")
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.buildinfo_license_read_button) { _, _ ->
                    needLoad(ProjectUrls.actualLicenseUrl)
                }
                .create().show()
            true
        }

        findPreference<Preference>(PREF_CHANGELOG)?.apply {
            isVisible = !BuildConfig.DEBUG
            setOnPreferenceClickListener {
                needLoad(ProjectUrls.changelogUrl)
                true
            }
        }

        findPreference<Preference>(PREF_WEBSITE)?.setOnPreferenceClickListener {
            needLoad(ProjectUrls.websiteUrl)
            true
        }

        findPreference<Preference>(PREF_FEEDBACK)?.setOnPreferenceClickListener {
            needLoad(ProjectUrls.feedbackUrl)
            true
        }

        findPreference<Preference>(PREF_SOURCE_CODE)?.setOnPreferenceClickListener {
            needLoad(ProjectUrls.sourceUrl)
            true
        }
    }

    private fun needLoad(url: String) {
        val needLoad = Intent()
        needLoad.putExtra(SettingsKeys.needLoadUrl, url)
        buildInfoActivity.setResult(0, needLoad)
        buildInfoActivity.finish()
    }

    companion object {
        private const val PREF_LICENSE = "license"
        private const val PREF_CHANGELOG = "changelog"
        private const val PREF_WEBSITE = "website"
        private const val PREF_FEEDBACK = "feedback"
        private const val PREF_SOURCE_CODE = "source_code"
    }
}