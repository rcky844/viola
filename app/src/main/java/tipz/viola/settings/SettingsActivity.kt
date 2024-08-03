// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("DEPRECATION")

package tipz.viola.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import tipz.viola.R
import tipz.viola.webviewui.BaseActivity


class SettingsActivity : BaseActivity() {
    private val needLoad = Intent()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val toolbar = findViewById<Toolbar>(R.id._toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        onBackPressedDispatcher.addCallback(this) {
            needLoad.putExtra(SettingsKeys.needReload, SettingsMainFragment.needReload)
            setResult(0, needLoad)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        settingsMainFragment = SettingsMainFragment(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.list_container, settingsMainFragment!!).commit()
    }

    // TODO: Investigate why running at onSaveInstanceState doesn't work (API = 33)
    override fun onStop() {
        try {
            supportFragmentManager.beginTransaction().remove(settingsMainFragment!!).commit()
        } catch (ignored: IllegalStateException) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        }
        super.onStop()
    }

    // TODO: Investigate why running at onStop doesn't work (API = 23, 26)
    public override fun onSaveInstanceState(outState: Bundle) {
        try {
            supportFragmentManager.beginTransaction().remove(settingsMainFragment!!).commit()
        } catch (ignored: IllegalStateException) {
            // There's no way to avoid getting this if saveInstanceState has already been called.
        }
        super.onSaveInstanceState(outState)
    }

    companion object {
        var settingsMainFragment: SettingsMainFragment? = null
    }
}