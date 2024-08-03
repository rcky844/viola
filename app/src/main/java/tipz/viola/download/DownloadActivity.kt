// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.webviewui.BaseActivity
import java.io.File
import java.lang.ref.WeakReference

class DownloadActivity : BaseActivity() {
    private lateinit var downloadClient: DownloadClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        downloadClient = (applicationContext as Application).downloadClient

        setContentView(R.layout.activity_recycler_data_list)
        setTitle(R.string.toolbar_expandable_downloads)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        val downloadList = findViewById<RecyclerView>(R.id.recyclerView)

        CoroutineScope(Dispatchers.IO).launch {
            listData = downloadClient.downloadQueue.value
        }

        val layoutManager = downloadList.layoutManager as LinearLayoutManager
        downloadList.adapter = ItemsAdapter(this)
    }

    class ItemsAdapter(downloadActivity: DownloadActivity)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val mDownloadActivity: WeakReference<DownloadActivity> =
            WeakReference(downloadActivity)

        class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val back: ConstraintLayout = view.findViewById(R.id.bg)
            val icon: AppCompatImageView = view.findViewById(R.id.icon)
            val title: AppCompatTextView = view.findViewById(R.id.title)
            val url: AppCompatTextView = view.findViewById(R.id.url)
            val time: AppCompatTextView = view.findViewById(R.id.time)
        }

        class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: AppCompatTextView = view.findViewById(R.id.text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val layoutView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            return if (viewType == R.layout.template_empty) EmptyViewHolder(layoutView)
            else ListViewHolder(layoutView)
        }

        override fun getItemViewType(position: Int): Int {
            Log.i(LOG_TAG, "getItemViewType(): isEmpty=${listData!!.size == 0}")
            return if (listData!!.size == 0) R.layout.template_empty
            else R.layout.template_icon_title_descriptor_time
        }

        @SuppressLint("SimpleDateFormat")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val downloadActivity = mDownloadActivity.get()!!

            if (holder is EmptyViewHolder) {
                holder.text.setText(R.string.no_downloads)
            } else if (holder is ListViewHolder) {
                val data = listData!![position]

                holder.icon.setImageResource(FileFormat.getFileDrawableResId(data))
                holder.title.text = data.filename
                holder.url.text = data.uriString

                holder.back.setOnClickListener {
                    data.apply {
                        val file = File("$downloadPath$filename")
                        if (!file.exists()) {
                            Log.w(LOG_TAG, "onClickListener(): File does not exist, taskId=$taskId")
                            return@apply
                        }

                        val openUri = FileProvider.getUriForFile(
                            mDownloadActivity.get()!!,
                            mDownloadActivity.get()!!.applicationContext.packageName
                                    + ".provider",
                            file
                        )
                        Log.i(LOG_TAG, "onClickListener(): taskId=$taskId, openUri=$openUri")

                        val intent = Intent()
                            .setAction(Intent.ACTION_VIEW)
                            .setData(openUri)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        mDownloadActivity.get()!!.startActivity(intent)
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            // Return 1 so that empty message is shown
            return if (listData == null || listData!!.size == 0) 1
            else listData!!.size
        }
    }

    companion object {
        private var LOG_TAG = "DownloadActivity"
        private var listData: MutableList<DownloadObject>? = null
    }
}