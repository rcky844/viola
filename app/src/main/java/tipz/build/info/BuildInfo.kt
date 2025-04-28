// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.build.info

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.Size
import tipz.viola.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date

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

    /* PRODUCT_BUILD_EXTRA
     *
     * Describes the visible extra name given to the build
     */
    @Size(min=0)
    var productBuildExtra: String? = BuildConfig.VERSION_BUILD_EXTRA

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
    var productBuildRevision = BuildConfig.VERSION_BUILD_REVISION

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

    /* VERSION_BUILD_TIMESTAMP
     *
     * Unix timestamp when the build was compiled.
     */
    var productBuildTimestamp: Long = BuildConfig.VERSION_BUILD_TIMESTAMP

    @SuppressLint("SimpleDateFormat")
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
        Date(productBuildTimestamp).let {
            buildTagBuffer.append(SimpleDateFormat("yyMMdd").format(it))
            buildTagBuffer.append("-") // Separator
            buildTagBuffer.append(SimpleDateFormat("HHmm").format(it))
        }

        return buildTagBuffer.toString()
    }
}