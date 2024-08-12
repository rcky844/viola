// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import tipz.viola.R
import tipz.viola.databinding.ActivitySettingsBinding
import tipz.viola.settings.SettingsKeys
import tipz.viola.activity.BaseActivity

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val needLoad = Intent()
    private lateinit var settingsMainFragment: SettingsMainFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Setup toolbar
        val toolbar = binding.materialToolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        onBackPressedDispatcher.addCallback(this) {
            needLoad.putExtra(SettingsKeys.needReload, SettingsMainFragment.needReload)
            setResult(0, needLoad)
            finish()
        }

        // Setup fragments
        settingsMainFragment = SettingsMainFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.list_container, settingsMainFragment)
            .addToBackStack("main").commit()
    }
}