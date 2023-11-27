package tipz.viola.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.R
import tipz.viola.ui.BrowserActivity
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.DownloadUtils
import tipz.viola.utils.UrlUtils

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

    open fun setupDialogForShowing(vWebView : VWebView, bundle : Bundle) : Boolean {
        val hr = vWebView.hitTestResult
        val type = hr.type
        var url = bundle.getString("url") ?: return false
        val title = bundle.getString("title")
        val src = bundle.getString("src")

        // Perform checks on the type of content we are dealing with
        if (type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.EDIT_TEXT_TYPE) return false

        // Truncate url string to make things load faster
        url = if (url.length > 75) url.substring(0, 74) + "…" else url

        if (title == null) {
            setTitle(url)
        } else {
            val layoutInflater = LayoutInflater.from(context)
            @SuppressLint("InflateParams") val root =
                    layoutInflater.inflate(R.layout.hit_test_dialog_title, null)

            root.findViewById<AppCompatTextView>(R.id.title).text = title
            root.findViewById<AppCompatTextView>(R.id.url).text = url

            val icon = root.findViewById<AppCompatImageView>(R.id.icon)
            if (src == null) icon.visibility = View.GONE
            else {
                CoroutineScope(Dispatchers.IO).launch {
                    val data = DownloadUtils.startFileDownload(src)
                    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    if (bitmap != null)
                        CoroutineScope(Dispatchers.Main).launch {
                            icon.setImageBitmap(bitmap)
                        }
                }
            }

            this.setCustomTitle(root);
        }

        arrayAdapter = ArrayAdapter<String>(context, R.layout.recycler_list_item_1)
        for (item in hitTestDialogItems) {
            // Insert Image items
            if (item == imageId) {
                if (src != null) {
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