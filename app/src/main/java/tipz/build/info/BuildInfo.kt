// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.build.info

import android.util.Log
import androidx.annotation.IdRes
import androidx.annotation.Size
import tipz.viola.BuildConfig

open class BuildInfo {
    private val LOG_TAG = "BuildInfo"

    /* PRODUCT_NAME
     *
     */
    @Size(min=1)
    var productName: String? = null

    /* PRODUCT_VERSION
     *
     * Used to describes the current version of the product.
     * Expects a MAJOR.MINOR format.
     */
    @Size(min=1)
    var productVersion: String? = BuildConfig.VERSION_NAME

    /* PRODUCT_VERSION_CODENAME
     *
     * Used to describes the codename of the product.
     */
    @Size(min=1)
    var productVersionCodename: String? = BuildConfig.VERSION_CODENAME

    /* PRODUCT_BUILD_ID
     *
     * Describes the order in which builds were compiled.
     * It should be incremented for each build compiled.
     */
    @Size(min=1)
    var productBuildId: String? = BuildConfig.VERSION_BUILD_ID

    /* PRODUCT_BUILD_REVISION
     *
     * Describes the current revision number of the build ID.
     * Defaults to "0".
     */
    @Size(min=1)
    var productBuildRevision: String? = BuildConfig.VERSION_BUILD_REVISION

    /* PRODUCT_BUILD_GIT_REVISION
     *
     * Describes the current Git revision number of the build ID.
     */
    @Size(min=1)
    var productBuildGitRevision: String? = BuildConfig.VERSION_BUILD_GIT_REVISION

    /* PRODUCT_BUILD_BRANCH
     *
     * Describes the branch used to compile the build.
     */
    @Size(min=1)
    var productBuildBranch: String? = BuildConfig.VERSION_BUILD_BRANCH

    /* PRODUCT_BUILD_DATE
     *
     * Describes the date the build is compiled.
     */
    @Size(6)
    var productBuildDate: String? = BuildConfig.VERSION_BUILD_DATE_MINIMAL

    /* PRODUCT_BUILD_TIME
     *
     * Describes the time the build is compiled.
     */
    @Size(4)
    var productBuildTime: String? = BuildConfig.VERSION_BUILD_TIME_MINIMAL

    /* PRODUCT_COPYRIGHT_YEAR
     *
     */
    @Size(min=4)
    var productCopyrightYear: String? = BuildConfig.VERSION_COPYRIGHT_YEAR

    /* PRODUCT_LICENSE_DOCUMENT
     *
     */
    @Size(min=1)
    var productLicenseDocument: String? = BuildConfig.PRODUCT_LICENSE_DOCUMENT

    /* PRODUCT_BANNER
     *
     * The resource ID for the banner of the product to display in dialog.
     */
    @IdRes
    var productBanner: Int? = null

    fun getProductBuildTag() : String? {
        val buildTagBuffer = StringBuffer()

        // Product version
        if (productVersion == null) {
            Log.d(LOG_TAG, "Product version is missing, STOP!")
            return null
        }
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
        buildTagBuffer.append(productBuildDate)
        buildTagBuffer.append("-") // Separator
        buildTagBuffer.append(productBuildTime)

        return buildTagBuffer.toString()
    }
}