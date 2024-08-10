// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.build.info

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.databinding.AboutDialogBinding
import tipz.viola.utils.CommonUtils

@SuppressLint("SetTextI18n")
class BuildInfoDialog(context: Context, private val dialogDetails: BuildInfoDialogDetails) :
    MaterialAlertDialogBuilder(context) {
    private var binding: AboutDialogBinding =
        AboutDialogBinding.inflate(LayoutInflater.from(context))
    private val buildInfo = BuildInfo()
    val resources = context.resources!!

    init {
        val view = binding.root
        setView(view)

        // Setup dialogs
        val dialogText = binding.dialogText
        val changelogBtn = binding.changelogBtn
        val licenseBtn = binding.licenseBtn
        val logo = binding.logo

        // Setup dialog text
        buildInfo.productName = resources.getString(R.string.app_name)

        var buildId = buildInfo.productBuildId
        if (buildId == null) {
            Log.d(LOG_TAG, "Build ID is missing, showing Git revision as build ID instead")
            buildId = buildInfo.productBuildGitRevision
        }

        var productBuildExtra = buildInfo.productBuildExtra
        productBuildExtra =
            if (productBuildExtra!!.isEmpty()) ""
            else " - $productBuildExtra"

        val textVersion = resources.getString(
            R.string.buildinfo_dialog_version,
            buildInfo.productName, buildInfo.productVersionCodename,
            buildInfo.productVersion, buildId,
            productBuildExtra
        )
        val textCopyright = resources.getString(
            R.string.buildinfo_dialog_copyright,
            buildInfo.productCopyrightYear
        )
        val textLicense = resources.getString(
            R.string.buildinfo_dialog_license,
            buildInfo.productLicenseDocument
        )

        // We can do this because they are sections
        dialogText.text = textVersion + textCopyright + textLicense

        // Setup dialog buttons
        changelogBtn.visibility =
            if (BuildConfig.DEBUG || dialogDetails.changelogUrl.isNullOrBlank()) View.GONE
            else View.VISIBLE
        changelogBtn.setOnClickListener {
            dialogDetails.loader(dialogDetails.changelogUrl!!)
        }
        logo.setOnLongClickListener {
            CommonUtils.showMessage(context, "Work in progress!")
            CommonUtils.showMessage(context, "Compiled: ${BuildConfig.VERSION_BUILD_DATE_TIME}")
            true
        }

        licenseBtn.visibility =
            if (dialogDetails.licenseUrl.isNullOrBlank()) View.GONE
            else View.VISIBLE
        licenseBtn.setOnClickListener {
            dialogDetails.loader(dialogDetails.licenseUrl!!)
        }

        // Setup extra buttons
        setPositiveButton(android.R.string.ok, null)
    }

    object BuildInfoDialogDetails {
        var loader: (url: String) -> Unit = this::stubLoader
        var changelogUrl: String? = null
        var licenseUrl: String? = null

        fun stubLoader(url: String) {
            Log.w(LOG_TAG, "stubLoader(): Attempted to load $url")
        }
    }

    companion object {
        private var LOG_TAG = "BuildInfoDialog"
    }
}