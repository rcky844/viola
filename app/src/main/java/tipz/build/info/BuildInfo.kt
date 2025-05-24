// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.build.info

import android.util.Log
import tipz.viola.BuildConfig
import tipz.viola.utils.TimeUtils

open class BuildInfo {
    private val LOG_TAG = "BuildInfo"

    /* PRODUCT_NAME
     *
     */
    var productName: String? = null

    /* PRODUCT_VERSION
     *
     * Used to describes the current version of the product.
     * Expects a MAJOR.MINOR format.
     */
    val productVersion = BuildConfig.VERSION_NAME

    /* PRODUCT_VERSION_CODENAME
     *
     * Used to describes the codename of the product.
     */
    val productVersionCodename = BuildConfig.VERSION_CODENAME

    /* PRODUCT_BUILD_EXTRA
     *
     * Describes the visible extra name given to the build
     */
    val productBuildExtra = BuildConfig.VERSION_BUILD_EXTRA

    /* PRODUCT_BUILD_ID
     *
     * Describes the order in which builds were compiled.
     * It should be incremented for each build compiled.
     */
    val productBuildId = BuildConfig.VERSION_BUILD_ID

    /* PRODUCT_BUILD_REVISION
     *
     * Describes the current revision number of the build ID.
     * Defaults to "0".
     */
    val productBuildRevision = BuildConfig.VERSION_BUILD_REVISION

    /* PRODUCT_BUILD_GIT_REVISION
     *
     * Describes the current Git revision number of the build ID.
     */
    val productBuildGitRevision = BuildConfig.VERSION_BUILD_GIT_REVISION

    /* PRODUCT_BUILD_BRANCH
     *
     * Describes the branch used to compile the build.
     */
    val productBuildBranch = BuildConfig.VERSION_BUILD_BRANCH

    /* PRODUCT_COPYRIGHT_YEAR
     *
     */
    val productCopyrightYear = BuildConfig.VERSION_COPYRIGHT_YEAR

    /* PRODUCT_LICENSE_DOCUMENT
     *
     */
    val productLicenseDocument = BuildConfig.PRODUCT_LICENSE_DOCUMENT

    /* VERSION_BUILD_TIMESTAMP
     *
     * Unix timestamp when the build was compiled.
     */
    val productBuildTimestamp: Long = BuildConfig.VERSION_BUILD_TIMESTAMP

    fun getProductBuildTag() : String {
        val buildTagBuffer = StringBuffer()

        // Product version
        buildTagBuffer.append(productVersion)
        buildTagBuffer.append(".") // Separator

        // Product build ID (or Git revision)
        val showGitRevision = productBuildId == null
        if (showGitRevision) {
            Log.d(LOG_TAG, "Build ID is missing, showing Git revision in build tag")
            buildTagBuffer.append(productBuildGitRevision)
        } else {
            buildTagBuffer.append(productBuildId)
        }
        buildTagBuffer.append(".") // Separator

        // Product build revision
        if (!showGitRevision) {
            buildTagBuffer.append(productBuildRevision)
            buildTagBuffer.append(".") // Separator
        }

        // Product build branch
        if (!showGitRevision) {
            buildTagBuffer.append(productBuildBranch)
            buildTagBuffer.append(".") // Separator
        }

        // Product build date & time
        buildTagBuffer.append(
            TimeUtils.formatEpochMillis(
                epochMillis = productBuildTimestamp,
                formatStyle = "yyMMdd-HHmm"
            )
        )

        return buildTagBuffer.toString()
    }
}