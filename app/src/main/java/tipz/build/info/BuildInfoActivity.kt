package tipz.build.info

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.databinding.ActivityBuildinfoBinding
import tipz.viola.settings.SettingsKeys
import tipz.viola.webview.activity.BaseActivity
import tipz.viola.webview.pages.ExportedUrls

@SuppressLint("SetTextI18n")
class BuildInfoActivity : BaseActivity() {
    private lateinit var binding: ActivityBuildinfoBinding
    private val buildInfo = BuildInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuildinfoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Setup toolbar
        val toolbar = binding.materialToolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Set-up views
        val aboutText = binding.aboutText
        val changelogBtn = binding.changelogBtn
        val licenseBtn = binding.licenseBtn
        val websiteItem = binding.itemWebsite
        val feedbackItem = binding.itemFeedback
        val sourceCodeItem = binding.itemSourceCode
        val buildNumberItem = binding.itemBuildNumber

        // Set-up main text
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
        aboutText.text = textVersion + textCopyright + textLicense

        // Set-up buttons
        changelogBtn.visibility = if (BuildConfig.DEBUG) View.GONE else View.VISIBLE
        changelogBtn.setOnClickListener {
            needLoad(ExportedUrls.changelogUrl)
        }

        licenseBtn.setOnClickListener {
            needLoad(ExportedUrls.actualLicenseUrl)
        }

        // Set-up items
        websiteItem.apply {
            text1.setText(R.string.pref_website)
            text2.setText(R.string.pref_website_summary)
            viewParent.setOnClickListener {
                needLoad(ExportedUrls.websiteUrl)
            }
        }

        feedbackItem.apply {
            text1.setText(R.string.feedback)
            text2.setText(R.string.feedback_desp)
            viewParent.setOnClickListener {
                needLoad(ExportedUrls.feedbackUrl)
            }
        }

        sourceCodeItem.apply {
            text1.setText(R.string.source_code)
            text2.setText(R.string.source_code_desp)
            viewParent.setOnClickListener {
                needLoad(ExportedUrls.sourceUrl)
            }
        }

        buildNumberItem.apply {
            text1.setText(R.string.item_build_number)
            text2.text = BuildInfo().getProductBuildTag() ?: ""
        }
    }

    private fun needLoad(url: String) {
        val needLoad = Intent()
        needLoad.putExtra(SettingsKeys.needLoadUrl, url)
        setResult(0, needLoad)
        finish()
    }

    companion object {
        private var LOG_TAG = "BuildInfoActivity"
    }
}