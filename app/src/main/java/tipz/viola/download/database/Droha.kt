// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import tipz.viola.download.DownloadMode
import tipz.viola.download.DownloadProvider
import tipz.viola.webview.VWebView

@Entity
class Droha {
    /* DownloadClient specifics */
    @Ignore
    internal var taskId = 0

    @Ignore
    internal var downloadMode = DownloadMode.AUTO_DOWNLOAD_PROVIDER // TODO: Change when implemented in UI

    @Ignore
    internal var downloadStatus = false // TODO: Expand on this when needed

    @Ignore
    internal var vWebView: VWebView? = null

    @Ignore
    internal var dialogPositiveButtonClickListener: () -> Unit = {}

    /* Download task specifics / Database */
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo
    var uriString: String

    @ColumnInfo
    var filename: String?

    @ColumnInfo
    var downloadPath: String?

    @ColumnInfo
    var contentDisposition: String?

    @ColumnInfo
    var mimeType: String?

    @ColumnInfo
    var requestUrl: String?

    constructor(id: Int, uriString: String, filename: String?, downloadPath: String?,
                contentDisposition: String?, mimeType: String?, requestUrl: String?) {
        this.id = id
        this.uriString = uriString
        this.filename = filename
        this.downloadPath = downloadPath
        this.contentDisposition = contentDisposition
        this.mimeType = mimeType
        this.requestUrl = requestUrl
    }

    @Ignore
    constructor() {
        uriString = ""
        filename = null
        downloadPath = null
        contentDisposition = null
        mimeType = null
        requestUrl = null
    }

    /* DownloadProvider specifics */
    @Ignore
    var checkIsOnline = true

    @Ignore
    var showDialog = false

    @Ignore
    var statusListener: DownloadProvider.Companion.DownloadStatusListener? = null

    @Ignore
    var userAgent: String? = null

    /* Helper functions / features */
    internal fun getUriProtocol() = uriString.substringBefore(':')
    internal fun compareUriProtocol(value: String) : Boolean = getUriProtocol() == value
}
