package tipz.build.info

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import tipz.viola.BuildConfig
import tipz.viola.R
import tipz.viola.databinding.ActivityBuildinfoBinding
import tipz.viola.ext.doOnApplyWindowInsets
import tipz.viola.ext.dpToPx
import tipz.viola.settings.SettingsKeys
import tipz.viola.webview.activity.BaseActivity
import tipz.viola.webview.pages.ProjectUrls

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
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        toolbar.doOnApplyWindowInsets { v, insets, _, _ ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).apply {
                v.updatePadding(top = top)
                v.layoutParams.height = dpToPx(179) + top
            }
        }

        // Setup ScrollView
        binding.scrollView.doOnApplyWindowInsets { v, insets, _, _ ->
            insets.getInsets(WindowInsetsCompat.Type.navigationBars()).apply {
                v.updatePadding(left = left, right = right, bottom = bottom)
            }
        }

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
            R.string.buildinfo_version,
            buildInfo.productName, buildInfo.productVersionCodename,
            buildInfo.productVersion, buildId,
            productBuildExtra
        )
        val textCopyright = resources.getString(
            R.string.buildinfo_copyright,
            buildInfo.productCopyrightYear
        )
        val textLicense = resources.getString(
            R.string.buildinfo_license,
            buildInfo.productLicenseDocument
        )

        // We can do this because they are sections
        aboutText.text = textVersion + textCopyright + textLicense

        // Set-up buttons
        changelogBtn.visibility = if (BuildConfig.DEBUG) View.GONE else View.VISIBLE
        changelogBtn.setOnClickListener {
            needLoad(ProjectUrls.changelogUrl)
        }

        licenseBtn.setOnClickListener {
            needLoad(ProjectUrls.actualLicenseUrl)
        }

        // Set-up items
        websiteItem.apply {
            text1.setText(R.string.pref_website_title)
            text2.setText(R.string.pref_website_summary)
            viewParent.setOnClickListener {
                needLoad(ProjectUrls.websiteUrl)
            }
        }

        feedbackItem.apply {
            text1.setText(R.string.pref_feedback_title)
            text2.setText(R.string.pref_feedback_summary)
            viewParent.setOnClickListener {
                needLoad(ProjectUrls.feedbackUrl)
            }
        }

        sourceCodeItem.apply {
            text1.setText(R.string.pref_source_code_title)
            text2.setText(R.string.pref_source_code_summary)
            viewParent.setOnClickListener {
                needLoad(ProjectUrls.sourceUrl)
            }
        }

        buildNumberItem.apply {
            text1.setText(R.string.buildinfo_pref_build_number_title)
            text2.text = BuildInfo().getProductBuildTag() ?: ""
        }
    }

    override fun doSettingsCheck() {
        super.doSettingsCheck()

        // Set light status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!isDarkMode(this)) {
                // FIXME: Figure out why light status bar does not work on R+
                windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.run {
                // TODO: Investigate why setAppearanceLightStatusBars() does not work
                @Suppress("DEPRECATION")
                systemUiVisibility =
                    systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
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