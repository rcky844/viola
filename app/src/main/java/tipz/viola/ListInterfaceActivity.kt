// Copyright (c) 2020-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import tipz.viola.database.Broha
import tipz.viola.database.FavClient
import tipz.viola.database.HistoryClient
import tipz.viola.database.IconHashClient
import tipz.viola.databinding.ActivityRecyclerDataListBinding
import tipz.viola.databinding.DialogFavEditBinding
import tipz.viola.databinding.TemplateEmptyBinding
import tipz.viola.databinding.TemplateIconTitleDescriptorTimeBinding
import tipz.viola.ext.copyClipboard
import tipz.viola.ext.doOnApplyWindowInsets
import tipz.viola.ext.dpToPx
import tipz.viola.ext.showMessage
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.TimeUtils
import tipz.viola.webview.activity.BaseActivity

class ListInterfaceActivity : BaseActivity() {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private lateinit var binding: ActivityRecyclerDataListBinding

    lateinit var favClient: FavClient
    lateinit var historyClient: HistoryClient
    lateinit var brohaList: RecyclerView
    lateinit var itemsAdapter: ItemsAdapter
    lateinit var fab: FloatingActionButton

    private var searchBarExpanded = false

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

    suspend fun getAllListData(): MutableList<Broha> {
        return (if (ActivityMode.isHistory()) historyClient.dao.getAll()
        else favClient.dao.getAll()).toMutableList()
    }

    suspend fun searchListData(query: String): MutableList<Broha> {
        return (if (ActivityMode.isHistory())
            historyClient.dao.search("%$query%", -1)
        else favClient.dao.search("%$query%", -1)).toMutableList()
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
                        if (ActivityMode.isHistory()) historyClient.dao.deleteAll()
                        else if (ActivityMode.isFavorites()) favClient.dao.deleteAll()
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
        brohaList = binding.recyclerView
        val layoutManager = brohaList.layoutManager as LinearLayoutManager
        layoutManager.reverseLayout = ActivityMode.isHistory()
        layoutManager.stackFromEnd = ActivityMode.isHistory()
        synchronized(this) {
            listData = runBlocking(Dispatchers.IO) {
                getAllListData()
            }
            itemsAdapter = ItemsAdapter(this@ListInterfaceActivity)
            brohaList.setAdapter(itemsAdapter) // Property access is causing lint issues
        }
        brohaList.doOnApplyWindowInsets { v, insets, _, _ ->
            insets.getInsets(WindowInsetsCompat.Type.systemBars()).apply {
                v.updatePadding(left = left, right = right, bottom = bottom)
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list_interface, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.run {
            queryHint = resources.getString(R.string.action_search_view_hint, title)
            maxWidth = Int.MAX_VALUE

            findViewById<View>(androidx.appcompat.R.id.search_plate).run {
                setBackgroundDrawable(
                    AppCompatResources.getDrawable(context, R.drawable.round_corner_elevated)
                )
                setPadding(context.dpToPx(8), 0, 0, 0)
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                private val mutex = Mutex()

                override fun onQueryTextSubmit(query: String?): Boolean {
                    synchronized(context) {
                        listData = runBlocking(Dispatchers.IO) {
                            searchListData(query ?: "")
                        }
                        // FIXME: Workaround to refresh list
                        brohaList.setAdapter(itemsAdapter) // Property access is causing lint issues
                    }
                    return true
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (!mutex.tryLock()) return false
                    ioScope.launch {
                        delay(1000L)
                        listData = query.toString().takeUnless { it.isEmpty() }?.let {
                            searchListData(it)
                        } ?: getAllListData()
                        withContext(Dispatchers.Main) {
                            // FIXME: Workaround to refresh list
                            brohaList.setAdapter(itemsAdapter) // Property access is causing lint issues
                        }
                        mutex.unlock()
                    }
                    return true
                }
            })
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                searchBarExpanded = true
                updateFabVisibility()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                searchBarExpanded = false
                updateFabVisibility()
                return true
            }
        })

        return true
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
                if (holder is EmptyViewHolder) holder.text.text =
                    activity.resources.getString(R.string.list_empty_hint, activity.title)
                return
            }
            if (holder !is ListViewHolder) return

            val iconHashClient = mIconHashClient
            val data = listData[position]
            val title = data.title
            val url = data.url
            var icon: Bitmap?

            if (data.iconHash != IconHashClient.INVALID_HASH) {
                activity.ioScope.launch {
                    icon = iconHashClient.read(data.iconHash)
                    if (icon != null)
                        MainScope().launch { holder.icon.setImageBitmap(icon) }
                }
            } else {
                holder.icon.setImageResource(R.drawable.default_favicon)
            }

            holder.url.text = url.toUri().host.takeUnless { it.isNullOrEmpty() } ?: url
            holder.title.text = title.takeUnless { it.isEmpty() } ?: holder.url.text
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
                                    activity.historyClient.dao.deleteById(data.id)
                                else if (ActivityMode.isFavorites())
                                    activity.favClient.dao.deleteById(data.id)
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

                                    activity.ioScope.launch {
                                        activity.favClient.dao.update(data.id, sTitle, sUrl)
                                        listData[position] = activity.favClient.dao.getById(data.id)
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
                                activity.favClient.dao.insert(title, url, data.iconHash)
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
            activity.updateFabVisibility()
            return if (listData.isEmpty()) 1 else listData.size
        }
    }

    fun updateFabVisibility() {
        fab.visibility = if (listData.isEmpty() || searchBarExpanded) View.GONE else View.VISIBLE
    }

    companion object {
        private var listData: MutableList<Broha> = mutableListOf()

        /* Activity mode */
        const val MODE_HISTORY = "HISTORY"
        const val MODE_FAVORITES = "FAVORITES"
    }
}
