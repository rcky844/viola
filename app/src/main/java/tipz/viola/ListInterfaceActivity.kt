// Copyright (c) 2020-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola

import android.content.Intent
import android.graphics.Bitmap
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
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
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
import tipz.viola.ext.doOnApplyWindowInsets
import tipz.viola.ext.showMessage
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.TimeUtils
import tipz.viola.webview.activity.BaseActivity

class ListInterfaceActivity : BaseActivity() {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private lateinit var binding: ActivityRecyclerDataListBinding

    lateinit var favClient: FavClient
    lateinit var historyClient: HistoryClient
    lateinit var itemsAdapter: ItemsAdapter
    lateinit var fab: FloatingActionButton

    enum class ActivityMode {
        HISTORY, FAVORITES, UNKNOWN;

        companion object {
            private var activityMode = UNKNOWN
            fun setActivityMode(mode: String?): Boolean {
                activityMode = when (mode) {
                    MODE_HISTORY -> HISTORY
                    MODE_FAVORITES -> FAVORITES
                    else -> UNKNOWN
                }
                return activityMode != UNKNOWN
            }
            fun isHistory(): Boolean = activityMode == HISTORY
            fun isFavorites(): Boolean = activityMode == FAVORITES
        }
    }

    fun updateListData(callback: () -> Any) {
        ioScope.launch {
            listData =
                (if (ActivityMode.isHistory()) historyClient.getAll()
                else favClient.getAll()).toMutableList()
            MainScope().launch { callback() }
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
        if (!ActivityMode.setActivityMode(intent.getStringExtra(Intent.EXTRA_TEXT)))
            finish() // Exit if activity mode is unknown
        favClient = FavClient(this)
        historyClient = HistoryClient(this)

        // Setup UI
        setTitle(if (ActivityMode.isHistory()) R.string.history_title else R.string.favorites_title)

        // Toolbar
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
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_all_entries_title)
                .setMessage(
                    if (ActivityMode.isHistory()) R.string.dialog_delete_all_entries_history_message
                    else R.string.dialog_delete_all_entries_favorites_message
                )
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    ioScope.launch {
                        if (ActivityMode.isHistory()) historyClient.deleteAll()
                        else if (ActivityMode.isFavorites()) favClient.deleteAll()
                    }
                    val size = listData.size
                    listData.clear()
                    itemsAdapter.notifyItemRangeRemoved(0, size)
                    showMessage(R.string.toast_cleared)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
        }
        fab.doOnApplyWindowInsets { v, insets, _, margin ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).apply {
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = bottom + margin.bottom
                    rightMargin = right + margin.right
                }
            }
        }

        // RecyclerView
        val brohaList = binding.recyclerView
        val layoutManager = brohaList.layoutManager as LinearLayoutManager
        layoutManager.reverseLayout = ActivityMode.isHistory()
        layoutManager.stackFromEnd = ActivityMode.isHistory()
        updateListData {
            itemsAdapter = ItemsAdapter(this)
            brohaList.setAdapter(itemsAdapter) // Property access is causing lint issues
        }
        brohaList.doOnApplyWindowInsets { v, insets, _, _ ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).apply {
                v.updatePadding(left = left, right = right, bottom = bottom)
            }
        }
    }

    class ItemsAdapter(
        private val activity: ListInterfaceActivity
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val LOG_TAG = "ListInterfaceAdapter"

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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            if (listData.isEmpty()) {
                EmptyViewHolder(TemplateEmptyBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            } else {
                ListViewHolder(TemplateIconTitleDescriptorTimeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (listData.isEmpty()) {
                if (holder is EmptyViewHolder) holder.text.setText(
                    if (ActivityMode.isHistory()) R.string.history_empty_message
                    else R.string.favorites_empty_message
                )
                return
            }
            if (holder !is ListViewHolder) return

            val iconHashClient = mIconHashClient
            val data = listData[position]
            val title = data.title
            val url = data.url
            var icon: Bitmap?

            if (data.iconHash != null) {
                activity.ioScope.launch {
                    icon = iconHashClient.read(data.iconHash)
                    if (icon != null)
                        MainScope().launch { holder.icon.setImageBitmap(icon) }
                }
            } else {
                holder.icon.setImageResource(R.drawable.default_favicon)
            }

            holder.title.text = title ?: url
            holder.url.text = url.takeUnless { it == null }?.toUri()?.host ?: ""
            if (ActivityMode.isHistory()) {
                holder.time.text = TimeUtils.formatEpochMillis(
                    epochMillis = data.timestamp,
                    formatStyle = "dd/MM\nHH:ss"
                )
            }
            holder.back.setOnClickListener {
                val needLoad = Intent()
                needLoad.putExtra(SettingsKeys.needLoadUrl, url)
                activity.setResult(0, needLoad)
                activity.finish()
            }
            holder.back.setOnLongClickListener { view ->
                val popup = PopupMenu(
                    activity, view!!
                )
                val menu = popup.menu
                if (ActivityMode.isHistory()) {
                    PopupMenuMap.addMenu(menu, PopupMenuMap.DELETE)
                    PopupMenuMap.addMenu(menu, PopupMenuMap.COPY_URL)
                    PopupMenuMap.addMenu(menu, PopupMenuMap.ADD_TO_FAVORITES)
                } else if (ActivityMode.isFavorites()) {
                    PopupMenuMap.addMenu(menu, PopupMenuMap.EDIT)
                    PopupMenuMap.addMenu(menu, PopupMenuMap.COPY_URL)
                    PopupMenuMap.addMenu(menu, PopupMenuMap.DELETE)
                }
                popup.setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        PopupMenuMap.DELETE.itemId -> {
                            activity.ioScope.launch {
                                if (ActivityMode.isHistory())
                                    activity.historyClient.deleteById(data.id)
                                else if (ActivityMode.isFavorites())
                                    activity.favClient.deleteById(data.id)
                            }
                            listData.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeRemoved(position, itemCount - position)
                        }
                        PopupMenuMap.EDIT.itemId -> {
                            val binding: DialogFavEditBinding =
                                DialogFavEditBinding.inflate(activity.layoutInflater)
                            val editView = binding.root

                            val titleEditText = binding.titleEditText
                            val urlEditText = binding.favUrlEditText
                            val propertyDisplay = binding.propertyDisplay
                            titleEditText.setText(title)
                            urlEditText.setText(url)
                            propertyDisplay.property = arrayListOf(
                                arrayOf(R.string.favorites_dialog_added_on,
                                    TimeUtils.formatEpochMillis(data.timestamp)
                                )
                            )

                            MaterialAlertDialogBuilder(activity)
                                .setTitle(R.string.favorites_menu_edit)
                                .setView(editView)
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    val sTitle = titleEditText.text.toString()
                                    val sUrl = urlEditText.text.toString()
                                    if (sTitle == title && sUrl == url) return@setPositiveButton

                                    data.title = sTitle
                                    data.url = sUrl
                                    activity.ioScope.launch {
                                        activity.favClient.update(data)
                                        listData[position] = activity.favClient.getById(data.id)!!
                                        MainScope().launch {
                                            notifyItemRangeRemoved(position, 1)
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
                            activity.ioScope.launch {
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

        override fun getItemCount(): Int {
            activity.fab.visibility = if (listData.isEmpty()) View.GONE else View.VISIBLE
            return if (listData.isEmpty()) 1 else listData.size
        }
    }

    companion object {
        private var listData: MutableList<Broha> = mutableListOf()

        /* Activity mode */
        const val MODE_HISTORY = "HISTORY"
        const val MODE_FAVORITES = "FAVORITES"
    }
}
