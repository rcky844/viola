// Copyright (c) 2020-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola

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
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.database.Broha
import tipz.viola.database.instances.FavClient
import tipz.viola.database.instances.HistoryClient
import tipz.viola.database.instances.IconHashClient
import tipz.viola.databinding.ActivityRecyclerDataListBinding
import tipz.viola.databinding.DialogFavEditBinding
import tipz.viola.databinding.TemplateEmptyBinding
import tipz.viola.databinding.TemplateIconTitleDescriptorTimeBinding
import tipz.viola.ext.copyClipboard
import tipz.viola.ext.showMessage
import tipz.viola.settings.SettingsKeys
import tipz.viola.webview.activity.BaseActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Objects

class ListInterfaceActivity : BaseActivity() {
    private lateinit var binding: ActivityRecyclerDataListBinding

    lateinit var favClient: FavClient
    lateinit var historyClient: HistoryClient
    lateinit var itemsAdapter: ItemsAdapter
    lateinit var fab: FloatingActionButton

    fun updateListData(callback: () -> Any) {
        CoroutineScope(Dispatchers.IO).launch {
            listData =
                (if (activityMode == mode_history) historyClient.getAll()
                else favClient.getAll()) as MutableList<Broha>?
            CoroutineScope(Dispatchers.Main).launch { callback() }
        }
    }

    enum class PopupMenuMap(val itemId: Int, @StringRes val resId: Int) {
        DELETE(1, R.string.delete),
        EDIT(2, R.string.favorites_menu_edit),
        COPY_URL(3, R.string.menu_copy_link),
        ADD_TO_FAVORITES(4, R.string.menu_save_to_favorites);

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
        setTitle(if (activityMode == mode_history) R.string.history_title else R.string.favorites_title)

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
                .setTitle(R.string.dialog_delete_all_entries_title)
                .setMessage(
                    if (activityMode == mode_history) R.string.dialog_delete_all_entries_history_message
                    else R.string.dialog_delete_all_entries_favorites_message
                )
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (activityMode == mode_history) historyClient.deleteAll()
                        else if (activityMode == mode_favorites) favClient.deleteAll()
                    }
                    val size = listData!!.size
                    listData!!.clear()
                    itemsAdapter.notifyItemRangeRemoved(0, size)
                    showMessage(R.string.toast_cleared)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
        }

        // RecyclerView
        val brohaList = binding.recyclerView
        val layoutManager = brohaList.layoutManager as LinearLayoutManager
        layoutManager.reverseLayout = activityMode == mode_history
        layoutManager.stackFromEnd = activityMode == mode_history
        updateListData {
            itemsAdapter = ItemsAdapter(this)
            brohaList.setAdapter(itemsAdapter) // Property access is causing lint issues
        }
    }

    class ItemsAdapter(
        private val activity: ListInterfaceActivity
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val LOG_TAG = "ListInterfaceAdapter"

        private lateinit var binding: ViewBinding
        private val mIconHashClient: IconHashClient = IconHashClient(activity)

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
            val isEmpty = listData == null || listData!!.size == 0
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
            if (holder is EmptyViewHolder) {
                holder.text.setText(
                    if (activityMode == mode_history) R.string.history_empty_message
                    else R.string.favorites_empty_message
                )
            } else if (holder is ListViewHolder) {
                val iconHashClient = mIconHashClient
                val data = listData!![position]
                val title = data.title
                val url = data.url
                var icon: Bitmap?

                if (data.iconHash != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        icon = iconHashClient.read(data.iconHash)
                        if (icon != null)
                            CoroutineScope(Dispatchers.Main).launch {
                                holder.icon.setImageBitmap(icon)
                            }
                    }
                } else {
                    holder.icon.setImageResource(R.drawable.default_favicon)
                }

                holder.title.text = title ?: url
                holder.url.text = Uri.parse(url ?: "").host
                if (activityMode == mode_history) {
                    val date = Calendar.getInstance()
                    date.timeInMillis = data.timestamp * 1000L
                    holder.time.text = SimpleDateFormat("dd/MM\nHH:ss").format(date.time)
                }
                holder.back.setOnClickListener {
                    val needLoad = Intent()
                    needLoad.putExtra(SettingsKeys.needLoadUrl, url)
                    activity.setResult(0, needLoad)
                    activity.finish()
                }
                holder.back.setOnLongClickListener { view: View? ->
                    val popup = PopupMenu(
                        activity, view!!
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
                                        activity.historyClient.deleteById(data.id)
                                    else if (activityMode == mode_favorites)
                                        activity.favClient.deleteById(data.id)
                                }
                                listData!!.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeRemoved(position, itemCount - position)
                            }
                            PopupMenuMap.EDIT.itemId -> {
                                val binding: DialogFavEditBinding =
                                    DialogFavEditBinding.inflate(activity.layoutInflater)
                                val editView = binding.root

                                val titleEditText = binding.titleEditText
                                val urlEditText = binding.favUrlEditText
                                titleEditText.setText(title)
                                urlEditText.setText(url)
                                MaterialAlertDialogBuilder(activity)
                                    .setTitle(R.string.favorites_menu_edit)
                                    .setView(editView)
                                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                                        if (Objects.requireNonNull(titleEditText.text).toString() != title
                                            || Objects.requireNonNull(urlEditText.text).toString() != url) {
                                            data.title =
                                                Objects.requireNonNull(titleEditText.text).toString()
                                            data.url = Objects.requireNonNull(urlEditText.text).toString()
                                            data.setTimestamp()
                                            CoroutineScope(Dispatchers.IO).launch {
                                                activity.favClient.update(data)
                                                // FIXME: Update list dynamically to save system resources
                                                listData = activity.favClient.getAll() as MutableList<Broha>?
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
                                activity.copyClipboard(url)
                            }
                            PopupMenuMap.ADD_TO_FAVORITES.itemId -> {
                                CoroutineScope(Dispatchers.IO).launch {
                                    activity.favClient.insert(
                                        Broha(data.iconHash, title, url!!)
                                    )
                                }
                                activity.showMessage(R.string.save_successful)
                            }
                        }
                        true
                    }
                    popup.show()
                    true
                }

                // Set tint to none
                ImageViewCompat.setImageTintList(holder.icon, null)
            }
        }

        override fun getItemCount(): Int {
            val isEmpty = listData == null || listData!!.size == 0
            activity.fab.visibility = if (isEmpty) View.GONE else View.VISIBLE

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
