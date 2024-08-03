// Copyright (c) 2020-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.broha

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.R
import tipz.viola.broha.api.FavClient
import tipz.viola.broha.api.HistoryClient
import tipz.viola.broha.database.Broha
import tipz.viola.broha.database.IconHashClient
import tipz.viola.databinding.ActivityRecyclerDataListBinding
import tipz.viola.databinding.DialogFavEditBinding
import tipz.viola.databinding.TemplateEmptyBinding
import tipz.viola.databinding.TemplateIconTitleDescriptorTimeBinding
import tipz.viola.download.DownloadActivity.ItemsAdapter.EmptyViewHolder
import tipz.viola.download.DownloadActivity.ItemsAdapter.ListViewHolder
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.CommonUtils.copyClipboard
import tipz.viola.utils.CommonUtils.showMessage
import tipz.viola.webviewui.BaseActivity
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Objects

class ListInterfaceActivity : BaseActivity() {
    private lateinit var binding: ActivityRecyclerDataListBinding

    lateinit var favClient: FavClient
    lateinit var historyClient: HistoryClient
    lateinit var itemsAdapter: ItemsAdapter
    lateinit var fab: FloatingActionButton

    fun updateListData() {
        CoroutineScope(Dispatchers.IO).launch {
            listData =
                if (activityMode == mode_history) historyClient.getAll() as MutableList<Broha>?
                else favClient.getAll() as MutableList<Broha>?
        }
    }

    enum class PopupMenuMap(val itemId: Int, @StringRes val resId: Int) {
        DELETE(1, R.string.delete),
        EDIT(2, R.string.favMenuEdit),
        COPY_URL(3, R.string.copy_url),
        ADD_TO_FAVORITES(4, R.string.add_to_fav);

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

        // Setup some lateinit constants
        activityMode = intent.getStringExtra(Intent.EXTRA_TEXT)
        favClient = FavClient(this)
        historyClient = HistoryClient(this)

        // Setup UI
        setTitle(if (activityMode == mode_history) R.string.hist else R.string.fav)

        // Toolbar
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Clear all button
        fab = binding.fab
        fab.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_all_entries)
                .setMessage(
                    if (activityMode == mode_history) R.string.del_hist_message
                    else R.string.delete_fav_message
                )
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (activityMode == mode_history) historyClient.deleteAll()
                        else if (activityMode == mode_favorites) favClient.deleteAll()
                    }
                    val size = listData!!.size
                    listData!!.clear()
                    itemsAdapter.notifyItemRangeRemoved(0, size)
                    showMessage(this, R.string.wiped_success)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
        }

        // RecyclerView
        val brohaList = binding.recyclerView
        val layoutManager = brohaList.layoutManager as LinearLayoutManager
        layoutManager.reverseLayout = activityMode == mode_history
        layoutManager.stackFromEnd = activityMode == mode_history
        updateListData()
        itemsAdapter = ItemsAdapter(
            this@ListInterfaceActivity, IconHashClient(this)
        )
        brohaList.adapter = itemsAdapter
    }

    class ItemsAdapter(
        brohaListInterfaceActivity: ListInterfaceActivity,
        iconHashClient: IconHashClient?
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val LOG_TAG = "ListInterfaceAdapter"

        private lateinit var binding: ViewBinding
        private val mBrohaListInterfaceActivity: WeakReference<ListInterfaceActivity> =
            WeakReference(brohaListInterfaceActivity)
        private val mIconHashClient: WeakReference<IconHashClient?> = WeakReference(iconHashClient)

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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val isEmpty = listData!!.size == 0
            binding = if (isEmpty) {
                TemplateEmptyBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
            } else {
                TemplateIconTitleDescriptorTimeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
            }

            return if (isEmpty) EmptyViewHolder(binding as TemplateEmptyBinding)
            else ListViewHolder(binding as TemplateIconTitleDescriptorTimeBinding)
        }

        @SuppressLint("SimpleDateFormat")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val listInterfaceActivity = mBrohaListInterfaceActivity.get()!!

            if (holder is EmptyViewHolder) {
                holder.text.setText(
                    if (activityMode == mode_history) R.string.hist_empty
                    else R.string.fav_list_empty
                )
            } else if (holder is ListViewHolder) {
                val clientActivity = mBrohaListInterfaceActivity.get()!!
                val iconHashClient = mIconHashClient.get()
                val data = listData!![position]
                val title = data.title
                val url = data.url
                var icon: Bitmap?

                if (data.iconHash != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        icon = iconHashClient!!.read(data.iconHash)
                        if (icon != null)
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.icon.setImageBitmap(icon)
                            }
                    }
                } else {
                    holder.icon.setImageResource(R.drawable.default_favicon)
                }

                holder.title.text = title ?: url
                holder.url.text = Uri.parse(url ?: CommonUtils.EMPTY_STRING).host
                if (activityMode == mode_history) {
                    val date = Calendar.getInstance()
                    date.timeInMillis = data.timestamp * 1000L
                    holder.time.text = SimpleDateFormat("dd/MM\nHH:ss").format(date.time)
                }
                holder.back.setOnClickListener {
                    val needLoad = Intent()
                    needLoad.putExtra(SettingsKeys.needLoadUrl, url)
                    listInterfaceActivity.setResult(0, needLoad)
                    listInterfaceActivity.finish()
                }
                holder.back.setOnLongClickListener { view: View? ->
                    val popup = PopupMenu(
                        listInterfaceActivity, view!!
                    )
                    val menu = popup.menu
                    if (activityMode == mode_history) {
                        PopupMenuMap.addMenu(menu, PopupMenuMap.DELETE)
                        PopupMenuMap.addMenu(menu, PopupMenuMap.COPY_URL)
                        PopupMenuMap.addMenu(menu, PopupMenuMap.ADD_TO_FAVORITES)
                    } else if (activityMode == mode_favorites) {
                        PopupMenuMap.addMenu(menu, PopupMenuMap.EDIT)
                        PopupMenuMap.addMenu(menu, PopupMenuMap.COPY_URL)
                        PopupMenuMap.addMenu(menu, PopupMenuMap.DELETE)
                    }
                    popup.setOnMenuItemClickListener { item: MenuItem ->
                        when (item.itemId) {
                            PopupMenuMap.DELETE.itemId -> {
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (activityMode == mode_history)
                                        clientActivity.historyClient.deleteById(data.id)
                                    else if (activityMode == mode_favorites)
                                        clientActivity.favClient.deleteById(data.id)
                                }
                                listData!!.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeRemoved(position, itemCount - position)
                            }
                            PopupMenuMap.EDIT.itemId -> {
                                val binding: DialogFavEditBinding =
                                    DialogFavEditBinding.inflate(listInterfaceActivity.layoutInflater)
                                val editView = binding.root

                                val titleEditText = binding.titleEditText
                                val urlEditText = binding.favUrlEditText
                                titleEditText.setText(title)
                                urlEditText.setText(url)
                                MaterialAlertDialogBuilder(listInterfaceActivity)
                                    .setTitle(R.string.favMenuEdit)
                                    .setView(editView)
                                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                        if (Objects.requireNonNull(titleEditText.text).toString() != title
                                            || Objects.requireNonNull(urlEditText.text).toString() != url) {
                                            data.title =
                                                Objects.requireNonNull(titleEditText.text).toString()
                                            data.url = Objects.requireNonNull(urlEditText.text).toString()
                                            data.setTimestamp()
                                            CoroutineScope(Dispatchers.IO).launch {
                                                clientActivity.favClient.update(data)
                                                // FIXME: Update list dynamically to save system resources
                                                listData = clientActivity.favClient.getAll() as MutableList<Broha>?
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    notifyItemRangeRemoved(position, 1)
                                                }
                                            }
                                        }
                                    }
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .setIcon(holder.icon.drawable)
                                    .create().show()
                            }
                            PopupMenuMap.COPY_URL.itemId -> {
                                copyClipboard(listInterfaceActivity, url)
                            }
                            PopupMenuMap.ADD_TO_FAVORITES.itemId -> {
                                CoroutineScope(Dispatchers.IO).launch {
                                    clientActivity.favClient.insert(
                                        Broha(data.iconHash, title, url!!))
                                }
                                showMessage(listInterfaceActivity, R.string.save_successful)
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
            val isEmpty = listData == null || listData!!.size == 0
            mBrohaListInterfaceActivity.get()!!.fab.visibility =
                if (isEmpty) View.GONE else View.VISIBLE

            // Return 1 so that empty message is shown
            return if (isEmpty) 1
            else listData!!.size
        }
    }

    companion object {
        private var listData: MutableList<Broha>? = null

        /* Activity mode */
        var activityMode: String? = null
        const val mode_history = "HISTORY"
        const val mode_favorites = "FAVORITES"
    }
}
