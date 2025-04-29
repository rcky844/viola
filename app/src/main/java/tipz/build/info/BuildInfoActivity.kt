// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.build.info

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import tipz.viola.R
import tipz.viola.databinding.ActivityBuildinfoBinding
import tipz.viola.ext.doOnApplyWindowInsets
import tipz.viola.ext.dpToPx
import tipz.viola.webview.activity.BaseActivity

class BuildInfoActivity : BaseActivity() {
    private lateinit var binding: ActivityBuildinfoBinding

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

        // Set-up preference
        supportFragmentManager.beginTransaction()
            .replace(R.id.list_container, BuildInfoFragment()).commit()
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
}