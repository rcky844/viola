package tipz.viola.webview

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.widget.ArrayAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tipz.viola.R
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.DownloadUtils
import tipz.viola.utils.UrlUtils
import tipz.viola.ui.BrowserActivity

open class HitTestAlertDialog(context: Context) : MaterialAlertDialogBuilder(context) {
    private var arrayAdapter : ArrayAdapter<String>? = null

    private val imageId = -1

    private val hitTestDialogItems = listOf(
            R.string.open_in_new_tab,
            imageId,
            R.string.copy_url,
            R.string.share_url
    )

    private val hitTestDialogImageItems = listOf(
            R.string.download_image,
            R.string.search_image
    )
    open fun setupDialogForShowing(vWebView : VWebView) : Boolean {
        val hr = vWebView.hitTestResult
        val type = hr.type
        val url = hr.extra ?: return false

        // Perform checks on the type of content we are dealing with
        if (type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.EDIT_TEXT_TYPE) return false

        setTitle(if (url.length > 75) url.substring(0, 74) + "…" else url)

        arrayAdapter = ArrayAdapter<String>(context, R.layout.recycler_list_item_1)
        for (item in hitTestDialogItems) {
            // Insert Image items
            if (item == imageId) {
                if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    for (imageItem in hitTestDialogImageItems) {
                        arrayAdapter?.add(context.resources.getString(imageItem))
                    }
                }
                continue
            }

            arrayAdapter?.add(context.resources.getString(item))
        }

        setAdapter(arrayAdapter) { _: DialogInterface?, which: Int ->
            when (which) {
                0 -> CommonUtils.copyClipboard(context, url)

                1 -> {
                    DownloadUtils.dmDownloadFile(
                            context, url,
                            null, null, url
                    )
                }

                2 -> vWebView.loadUrl("http://images.google.com/searchbyimage?image_url=$url")

                3 -> {
                    val intent = Intent(context, BrowserActivity::class.java)
                    intent.data = Uri.parse(UrlUtils.cve_2017_13274(url))
                    context.startActivity(intent)
                }

                4 -> CommonUtils.shareUrl(context, url)
            }
        }
        return true
    }
}