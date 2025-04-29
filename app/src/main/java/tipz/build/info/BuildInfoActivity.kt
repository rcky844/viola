// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.build.info

import android.os.Bundle
import tipz.viola.R
import tipz.viola.databinding.ActivityBuildinfoBinding
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

        // Set-up preference
        supportFragmentManager.beginTransaction()
            .replace(R.id.list_container, BuildInfoFragment()).commit()
    }
}