// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.databinding.ActivityRecyclerDataListBinding
import tipz.viola.databinding.TemplateEmptyBinding
import tipz.viola.databinding.TemplateIconTitleDescriptorTimeBinding
import tipz.viola.download.database.Droha
import tipz.viola.download.database.DrohaClient
import tipz.viola.ext.copyClipboard
import tipz.viola.ext.showMessage
import tipz.viola.webview.activity.BaseActivity
import java.io.File

class DownloadActivity : BaseActivity() {
    private lateinit var binding: ActivityRecyclerDataListBinding
    private lateinit var downloadClient: DownloadClient
    private lateinit var drohaClient: DrohaClient

    lateinit var itemsAdapter: ItemsAdapter
    lateinit var fab: FloatingActionButton

    enum class PopupMenuMap(val itemId: Int, @StringRes val resId: Int) {
        DELETE(1, R.string.delete),
        COPY_URL(2, R.string.menu_copy_link),
        RE_DOWNLOAD(3, R.string.downloads_menu_re_download);

        /* Helper functions */
        companion object {
            fun addMenu(menu: Menu, item: PopupMenuMap) {
                menu.add(0, item.itemId, 0, item.resId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerDataListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Set-up clients
        downloadClient = (applicationContext as Application).downloadClient
        drohaClient = downloadClient.drohaClient

        setTitle(R.string.toolbar_expandable_downloads)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Clear all button
        fab = binding.fab
        fab.setOnClickListener {
            val size = listData.size
            listData.clear()
            CoroutineScope(Dispatchers.IO).launch { drohaClient.deleteAll() }
            itemsAdapter.notifyItemRangeRemoved(0, size)
            showMessage(R.string.toast_cleared)
        }

        // Set-up RecyclerView
        val downloadList = binding.recyclerView
        itemsAdapter = ItemsAdapter(this)
        downloadList.setAdapter(itemsAdapter) // Property access is causing lint issues

        // Set-up layout manager
        val layoutManager = downloadList.layoutManager as LinearLayoutManager
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
    }

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {
            listData = mutableListOf() // Reset
            listData.addAll(drohaClient.getAll())
        }
    }

    class ItemsAdapter(
        private val activity: DownloadActivity
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
            val isEmpty = listData.size == 0
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
            if (holder is EmptyViewHolder) {
                holder.text.setText(R.string.downloads_empty_message)
            } else if (holder is ListViewHolder) {
                val data = listData[position]

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
                            activity, activity.applicationContext.packageName + ".provider",
                            file
                        )
                        Log.i(LOG_TAG, "onClickListener(): taskId=$taskId, openUri=$openUri")

                        val intent = Intent()
                            .setAction(Intent.ACTION_VIEW)
                            .setData(openUri)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        try {
                            activity.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                            activity.showMessage(R.string.toast_no_app_to_handle)
                        }
                    }
                }

                holder.back.setOnLongClickListener { view: View? ->
                    val popup = PopupMenu(
                        activity, view!!
                    )
                    val menu = popup.menu
                    PopupMenuMap.addMenu(menu, PopupMenuMap.DELETE)
                    PopupMenuMap.addMenu(menu, PopupMenuMap.COPY_URL)
                    PopupMenuMap.addMenu(menu, PopupMenuMap.RE_DOWNLOAD)
                    popup.setOnMenuItemClickListener { item: MenuItem ->
                        when (item.itemId) {
                            PopupMenuMap.DELETE.itemId -> {
                                CoroutineScope(Dispatchers.IO).launch {
                                    activity.drohaClient.deleteById(data.id)
                                }
                                listData.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeRemoved(position, itemCount - position)
                            }
                            PopupMenuMap.COPY_URL.itemId -> {
                                activity.copyClipboard(data.uriString)
                            }
                            PopupMenuMap.RE_DOWNLOAD.itemId -> {
                                activity.downloadClient.launchDownload(data)
                            }
                        }
                        true
                    }
                    popup.show()
                    true
                }
            }
        }

        override fun getItemCount(): Int {
            val isEmpty = listData.size == 0
            activity.fab.visibility = if (isEmpty) View.GONE else View.VISIBLE

            // Return 1 so that empty message is shown
            return if (isEmpty) 1
            else listData.size
        }
    }

    companion object {
        private var LOG_TAG = "DownloadActivity"
        private var listData: MutableList<Droha> = mutableListOf()
        private lateinit var binding: ViewBinding
    }
}