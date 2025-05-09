// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.Application
import tipz.viola.LauncherActivity
import tipz.viola.R
import tipz.viola.databinding.ActivityRecyclerDataListBinding
import tipz.viola.databinding.TemplateEmptyBinding
import tipz.viola.databinding.TemplateIconTitleDescriptorTimeBinding
import tipz.viola.download.database.Droha
import tipz.viola.download.database.DrohaClient
import tipz.viola.ext.copyClipboard
import tipz.viola.ext.doOnApplyWindowInsets
import tipz.viola.ext.showMessage
import tipz.viola.settings.activity.SettingsActivity
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

        // Set-up toolbar
        setTitle(R.string.toolbar_expandable_downloads)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.doOnApplyWindowInsets { v, insets, _, _ ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).apply {
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = left
                    topMargin = top
                    rightMargin = right
                }
            }
        }

        // Clear all button
        fab = binding.fab
        fab.setOnClickListener {
            val size = listData.size
            listData.clear()
            CoroutineScope(Dispatchers.IO).launch { drohaClient.deleteAll() }
            itemsAdapter.notifyItemRangeRemoved(0, size)
            showMessage(R.string.toast_cleared)
        }
        fab.doOnApplyWindowInsets { v, insets, _, margin ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).apply {
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = left + margin.left
                    bottomMargin = bottom + margin.bottom
                    rightMargin = right + margin.right
                }
            }
        }

        // Set-up RecyclerView
        val downloadList = binding.recyclerView
        itemsAdapter = ItemsAdapter(this)
        downloadList.setAdapter(itemsAdapter) // Property access is causing lint issues
        downloadList.doOnApplyWindowInsets { v, insets, _, _ ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).apply {
                v.updatePadding(left = left, right = right, bottom = bottom)
            }
        }

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_download, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(
                    Intent(this, SettingsActivity::class.java)
                        .putExtra(
                            SettingsActivity.EXTRA_INITIAL_PREF_SCREEN,
                            R.xml.preference_settings_downloads
                        )
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class ItemsAdapter(
        private val activity: DownloadActivity
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        class ListViewHolder(binding: TemplateIconTitleDescriptorTimeBinding)
            : RecyclerView.ViewHolder(binding.root) {
            val back: ConstraintLayout = binding.bg
            val icon: AppCompatImageView = binding.icon
            val title: AppCompatTextView = binding.title
            val url: AppCompatTextView = binding.url
            val time: AppCompatTextView = binding.time
        }

        class EmptyViewHolder(binding: TemplateEmptyBinding)
            : RecyclerView.ViewHolder(binding.root) {
            val text: AppCompatTextView = binding.text
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            if (listData.isEmpty()) {
                EmptyViewHolder(TemplateEmptyBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            } else {
                ListViewHolder(TemplateIconTitleDescriptorTimeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            }

        @SuppressLint("SimpleDateFormat")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (listData.isEmpty()) {
                if (holder is EmptyViewHolder) holder.text.setText(R.string.downloads_empty_message)
                return
            }

            if (holder is ListViewHolder) {
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
            activity.fab.visibility = if (listData.isEmpty()) View.GONE else View.VISIBLE
            return if (listData.isEmpty()) 1 else listData.size
        }
    }

    companion object {
        private var LOG_TAG = "DownloadActivity"
        private var listData: MutableList<Droha> = mutableListOf()
    }
}