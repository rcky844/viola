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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.databinding.ActivityRecyclerDataListBinding
import tipz.viola.databinding.TemplateEmptyBinding
import tipz.viola.databinding.TemplateIconTitleDescriptorTimeBinding
import tipz.viola.webview.activity.BaseActivity
import java.io.File
import java.lang.ref.WeakReference

class DownloadActivity : BaseActivity() {
    private lateinit var binding: ActivityRecyclerDataListBinding
    private lateinit var downloadClient: DownloadClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerDataListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        downloadClient = (applicationContext as Application).downloadClient

        setTitle(R.string.toolbar_expandable_downloads)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        val downloadList = binding.recyclerView

        CoroutineScope(Dispatchers.IO).launch {
            listData = downloadClient.downloadQueue.value
        }

        downloadList.adapter = ItemsAdapter(this)
    }

    class ItemsAdapter(downloadActivity: DownloadActivity)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val mDownloadActivity: WeakReference<DownloadActivity> =
            WeakReference(downloadActivity)

        class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val binding = Companion.binding as TemplateIconTitleDescriptorTimeBinding
            val back: ConstraintLayout = binding.bg
            val icon: AppCompatImageView = binding.icon
            val title: AppCompatTextView = binding.title
            val url: AppCompatTextView = binding.url
            val time: AppCompatTextView = binding.time
        }

        class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val binding = Companion.binding as TemplateEmptyBinding
            val text: AppCompatTextView = binding.text
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val isEmpty = listData!!.size == 0
            binding = if (isEmpty) {
                TemplateEmptyBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
            } else {
                TemplateIconTitleDescriptorTimeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
            }

            return if (isEmpty) EmptyViewHolder(binding.root)
            else ListViewHolder(binding.root)
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
        private lateinit var binding: ViewBinding
    }
}