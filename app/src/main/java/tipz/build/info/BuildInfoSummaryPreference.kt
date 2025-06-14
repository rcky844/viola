// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.build.info

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.databinding.PreferenceBuildInfoSummaryBinding
import tipz.viola.settings.SettingsKeys
import tipz.viola.webview.pages.ProjectUrls

class BuildInfoSummaryPreference(
    context: Context, attrs: AttributeSet?
) : Preference(context, attrs) {
    private lateinit var binding: PreferenceBuildInfoSummaryBinding
    private val activity: BuildInfoActivity
    private val buildInfo = BuildInfo()

    init {
        layoutResource = R.layout.preference_build_info_summary
        activity = context as BuildInfoActivity
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        binding = PreferenceBuildInfoSummaryBinding.bind(holder.itemView)

        // Prevent clicking / ripple animation
        holder.itemView.isClickable = false

        // Set-up views
        val aboutText = binding.aboutText
        val changelogBtn = binding.changelogBtn
        val licenseBtn = binding.licenseBtn

        // Set-up main text
        buildInfo.productName = context.resources.getString(R.string.app_name)

        var buildId = buildInfo.productBuildId
        if (buildId == null || buildInfo.productBuildBranch.startsWith("master")) {
            Log.d(LOG_TAG, "Build ID is missing, showing Git revision as build ID instead")
            buildId = buildInfo.productBuildGitRevision
        }

        var productBuildExtra = buildInfo.productBuildExtra
        productBuildExtra = if (productBuildExtra.isEmpty()) "" else " - $productBuildExtra"

        val textVersion = context.resources.getString(
            R.string.buildinfo_version,
            buildInfo.productName, buildInfo.productVersionCodename,
            buildInfo.productVersion, buildId,
            productBuildExtra
        )
        val textCopyright = context.resources.getString(
            R.string.buildinfo_copyright,
            buildInfo.productCopyrightYear
        )
        val textLicense = context.resources.getString(
            R.string.buildinfo_license,
            buildInfo.productLicenseDocument
        )

        // We can do this because they are sections
        aboutText.text = "$textVersion\n\n$textCopyright\n$textLicense"

        // Set-up buttons
        changelogBtn.visibility = if (BuildConfig.DEBUG) View.GONE else View.VISIBLE
        changelogBtn.setOnClickListener {
            needLoad(ProjectUrls.changelogUrl)
        }

        licenseBtn.setOnClickListener {
            needLoad(ProjectUrls.actualLicenseUrl)
        }
    }

    private fun needLoad(url: String) {
        val needLoad = Intent()
        needLoad.putExtra(SettingsKeys.needLoadUrl, url)
        activity.setResult(0, needLoad)
        activity.finish()
    }

    companion object {
        private var LOG_TAG = "BuildInfoSummaryPreference"
    }
}