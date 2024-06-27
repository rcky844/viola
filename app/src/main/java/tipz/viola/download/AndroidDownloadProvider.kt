package tipz.viola.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.MimeTypeMap
import tipz.viola.R
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.UrlUtils

class AndroidDownloadProvider(override val context: Context) : DownloadProvider {
    override val capabilities = listOf(
        DownloadCapabilities.PROTOCOL_HTTP,
        DownloadCapabilities.PROTOCOL_HTTPS)

    override fun startDownload(downloadObject: DownloadObject) = downloadObject.apply {
        val request = DownloadManager.Request(
            Uri.parse(UrlUtils.patchUrlForCVEMitigation(uriString))
        )

        // Let this downloaded file be scanned by MediaScanner - so that it can
        // show up in Gallery app, for example.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) request.allowScanningByMediaScanner()

        // Referer header for some sites which use the same HTML link for the download link
        request.addRequestHeader("Referer", requestUrl ?: uriString)

        // Notify client once download is completed
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val filename = UrlUtils.guessFileName(uriString, contentDisposition, mimeType)
        try {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        } catch (e: IllegalStateException) {
            CommonUtils.showMessage(context, context.resources.getString(R.string.downloadFailed))
        }
        request.setMimeType(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(uriString)
            )
        )
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        try {
            dm.enqueue(request)
        } catch (e: RuntimeException) {
            CommonUtils.showMessage(context, context.resources.getString(R.string.downloadFailed))
        }
    }
}