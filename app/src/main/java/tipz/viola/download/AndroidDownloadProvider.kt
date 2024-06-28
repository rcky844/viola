package tipz.viola.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import tipz.viola.R
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.UrlUtils

class AndroidDownloadProvider(override val context: Context) : DownloadProvider {
    private var downloadID: Long = 0
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) stopDownload()
        }
    }

    override val capabilities = listOf(
        DownloadCapabilities.PROTOCOL_HTTP,
        DownloadCapabilities.PROTOCOL_HTTPS)
    override var statusListener: DownloadProvider.Companion.DownloadStatusListener? = null

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun startDownload(downloadObject: DownloadObject) {
        super.startDownload(downloadObject)
        downloadObject.apply {
            val request = DownloadManager.Request(
                Uri.parse(UrlUtils.patchUrlForCVEMitigation(uriString))
            )

            Log.i(LOG_TAG, "startDownload(): uriString=${uriString}")

            // Let this downloaded file be scanned by MediaScanner - so that it can
            // show up in Gallery app, for example.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) request.allowScanningByMediaScanner()

            // Referer header for some sites which use the same HTML link for the download link
            request.addRequestHeader("Referer", requestUrl ?: uriString)

            // Notify client once download is completed
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            try {
                if (filename == null)
                    filename = UrlUtils.guessFileName(uriString, contentDisposition, mimeType)
                Log.i(LOG_TAG, "startDownload(): filename=${filename}")
                request.setDestinationUri(Uri.parse("file://$downloadPath$filename"))
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
                downloadID = dm.enqueue(request)
            } catch (e: RuntimeException) {
                CommonUtils.showMessage(context, context.resources.getString(R.string.downloadFailed))
                return@apply
            }

            // Setup download complete receiver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    onDownloadComplete,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(
                    onDownloadComplete,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }
        }
    }

    companion object {
        private val LOG_TAG = "AndroidDownloadProvider"
    }
}