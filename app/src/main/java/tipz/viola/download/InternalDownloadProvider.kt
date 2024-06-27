package tipz.viola.download

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Base64
import android.webkit.MimeTypeMap
import tipz.viola.R
import tipz.viola.utils.CommonUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class InternalDownloadProvider(override val context: Context) : DownloadProvider {
    override val capabilities = listOf(
        // TODO: Enable for http/https downloads
        // DownloadCapabilities.SCHEMA_HTTP,
        // DownloadCapabilities.SCHEMA_HTTPS,
        DownloadCapabilities.PROTOCOL_DATA,
        DownloadCapabilities.PROTOCOL_BLOB)

    override fun startDownload(downloadObject: DownloadObject) = downloadObject.apply {
        when (DownloadCapabilities.fromString(downloadObject.getUriProtocol())) {
            DownloadCapabilities.PROTOCOL_DATA -> {
                val path =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val dataInfo = uriString.substring(uriString.indexOf(":") + 1, uriString.indexOf(","))
                val filename = (System.currentTimeMillis().toString() + "."
                        + MimeTypeMap.getSingleton().getExtensionFromMimeType(
                    dataInfo.substring(
                        0,
                        if (dataInfo.contains(";")) dataInfo.indexOf(";") else dataInfo.length
                    )
                ))
                val file = File(path, filename)
                try {
                    if (!path.exists()) path.mkdirs()
                    if (!file.exists()) file.createNewFile()
                    val dataString = uriString.substring(uriString.indexOf(",") + 1)
                    val writableBytes = if (dataInfo.contains(";base64")) Base64.decode(
                        dataString,
                        Base64.DEFAULT
                    ) else dataString.toByteArray()
                    val os: OutputStream = FileOutputStream(file)
                    os.write(writableBytes)
                    os.close()

                    // Tell the media scanner about the new file so that it is immediately available to the user.
                    MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
                    CommonUtils.showMessage(context, context.resources.getString(
                            R.string.notification_download_successful, filename))
                } catch (ignored: IOException) {
                }
            }
            DownloadCapabilities.PROTOCOL_BLOB -> {
                CommonUtils.showMessage(context, context.resources.getString(R.string.ver3_blob_no_support))
            }
            else -> return@apply // TODO: Implement all
        }
    }

}