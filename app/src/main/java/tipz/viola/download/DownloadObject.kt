// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import tipz.viola.webview.VWebView

class DownloadObject {
    /* DownloadClient specifics */
    internal var taskId = 0
    internal var downloadMode = 0
    internal var downloadStatus = false // TODO: Expand on this when needed
    internal var vWebView: VWebView? = null

    /* Download task specifics */
    var uriString = ""
    var filename: String? = null
    var downloadPath: String? = null
    var contentDisposition: String? = null
    var mimeType: String? = null
    var requestUrl: String? = null

    /* DownloadProvider specifics */
    var checkIsOnline = true
    var statusListener: DownloadProvider.Companion.DownloadStatusListener? = null

    /* Helper functions / features */
    internal fun getUriProtocol() = uriString.substringBefore(':')
    internal fun compareUriProtocol(value: String) : Boolean = getUriProtocol() == value
}
