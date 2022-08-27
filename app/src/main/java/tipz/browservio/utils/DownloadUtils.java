package tipz.browservio.utils;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import tipz.browservio.Application;
import tipz.browservio.R;

public class DownloadUtils {
    public static void dmDownloadFile(Context context, String url,
                                      String contentDisposition,
                                      String mimeType, String requestUrl) {
        dmDownloadFile(context, url, contentDisposition, mimeType, null, null, requestUrl);
    }

    /* TODO: Rewrite into our own download manager */
    public static long dmDownloadFile(Context context, String url,
                                      String contentDisposition,
                                      String mimeType, String title,
                                      String customFilename, String requestUrl) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(UrlUtils.toSearchOrValidUrl(context, url)));

            // Let this downloaded file be scanned by MediaScanner - so that it can
            // show up in Gallery app, for example.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                request.allowScanningByMediaScanner();

            if (title != null)
                request.setTitle(title);

            // Referer header for some sites which use the same HTML link for the download link
            request.addRequestHeader("Referer", requestUrl == null ? url : requestUrl);

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // Notify client once download is completed!
            final String filename = customFilename == null ?
                    UrlUtils.guessFileName(url, contentDisposition, mimeType) : customFilename;

            try {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            } catch (IllegalStateException e) {
                CommonUtils.showMessage(context, context.getResources().getString(R.string.downloadFailed));
                return -1;
            }
            request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(url)));
            DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            try {
                return dm.enqueue(request);
            } catch (RuntimeException e) {
                CommonUtils.showMessage(context, context.getResources().getString(R.string.downloadFailed));
            }
        } else {
            if (url.startsWith("data:")) {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String dataInfo = url.substring(url.indexOf(":") + 1, url.indexOf(","));
                String filename = System.currentTimeMillis() + "."
                        + MimeTypeMap.getSingleton().getExtensionFromMimeType(
                                StringUtils.substringBefore(dataInfo,
                                        dataInfo.contains(";") ? dataInfo.indexOf(";") : dataInfo.length()));
                File file = new File(path, filename);
                try {
                    if (!path.exists())
                        path.mkdirs();
                    if (!file.exists())
                        file.createNewFile();

                    String dataString = url.substring(url.indexOf(",") + 1);
                    byte[] writableBytes = dataInfo.contains(";base64") ? Base64.decode(dataString, Base64.DEFAULT) : dataString.getBytes();
                    OutputStream os = new FileOutputStream(file);
                    os.write(writableBytes);
                    os.close();

                    // Tell the media scanner about the new file so that it is immediately available to the user.
                    MediaScannerConnection.scanFile(context,
                            new String[]{file.toString()}, null, null);

                    CommonUtils.showMessage(context, context.getResources().getString(R.string.notification_download_successful, filename));
                } catch (IOException ignored) {
                }
            } else if (url.startsWith("blob:")) { /* TODO: Make it actually handle blob: URLs */
                CommonUtils.showMessage(context, context.getResources().getString(R.string.ver3_blob_no_support));
            }
        }
        return -1;
    }
}