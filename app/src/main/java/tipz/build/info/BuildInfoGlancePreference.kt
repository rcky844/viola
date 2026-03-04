// Copyright (c) 2024-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.build.info

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import tipz.viola.R
import tipz.viola.databinding.PreferenceBuildInfoGlanceBinding

class BuildInfoGlancePreference(
    context: Context, attrs: AttributeSet?
) : Preference(context, attrs) {
    private lateinit var binding: PreferenceBuildInfoGlanceBinding
    private val activity: BuildInfoActivity
    private val buildInfo = BuildInfo()

    init {
        layoutResource = R.layout.preference_build_info_glance
        activity = context as BuildInfoActivity
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        binding = PreferenceBuildInfoGlanceBinding.bind(holder.itemView)

        // Prevent clicking / ripple animation
        holder.itemView.isClickable = false

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
            R.string.buildinfo_version, buildInfo.productVersion,
            buildInfo.productVersionCodename, buildId, productBuildExtra)
        binding.versionNumber.text = textVersion
    }

    companion object {
        private var LOG_TAG = "BuildInfoGlancePreference"
    }
}