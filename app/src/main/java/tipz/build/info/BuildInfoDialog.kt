package tipz.build.info

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.BuildConfig
import tipz.viola.R

class BuildInfoDialog(context: Context, private val dialogDetails: BuildInfoDialogDetails) :
    MaterialAlertDialogBuilder(context) {
    val buildInfo = BuildInfo()
    val resources = context.resources!!

    @SuppressLint("SetTextI18n")
    fun setupDialogForShowing() {
        // Setup layouts
        val layoutInflater = LayoutInflater.from(context)
        @SuppressLint("InflateParams") val dialogView =
            layoutInflater.inflate(R.layout.about_dialog, null)
        setView(dialogView)

        val dialog_text = dialogView.findViewById<AppCompatTextView>(R.id.dialog_text)
        val changelog_btn = dialogView.findViewById<AppCompatButton>(R.id.changelog_btn)
        val license_btn = dialogView.findViewById<AppCompatButton>(R.id.license_btn)

        // Setup dialog text
        buildInfo.productName = resources.getString(R.string.app_name)

        val text_version = resources.getString(
            R.string.buildinfo_dialog_version,
            buildInfo.productName, buildInfo.productVersionCodename,
            buildInfo.productVersion, buildInfo.productBuildId
        )
        val text_copyright = resources.getString(
            R.string.buildinfo_dialog_copyright,
            buildInfo.productCopyrightYear
        )
        val text_license = resources.getString(
            R.string.buildinfo_dialog_license,
            buildInfo.productLicenseDocument
        )

        // We can do this because they are sections
        dialog_text.text = text_version + text_copyright + text_license

        // Setup dialog buttons
        changelog_btn.visibility =
            if (BuildConfig.DEBUG || dialogDetails.changelogUrl.isNullOrBlank()) View.GONE
            else View.VISIBLE
        changelog_btn.setOnClickListener {
            dialogDetails.loader(dialogDetails.changelogUrl!!)
        }

        license_btn.visibility =
            if (dialogDetails.licenseUrl.isNullOrBlank()) View.GONE
            else View.VISIBLE
        license_btn.setOnClickListener {
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