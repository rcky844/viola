/*
 * Copyright (c) 2020-2024 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.broha

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.CommonUtils.copyClipboard
import tipz.viola.utils.CommonUtils.showMessage
import tipz.viola.webviewui.BaseActivity
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Objects


class ListInterfaceActivity : BaseActivity() {
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

    @SuppressLint("NotifyDataSetChanged")
    fun notifyItemsAdapter() {
        CoroutineScope(Dispatchers.Main).launch {
            // FIXME: Update list dynamically to save system resources
            itemsAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Setup some lateinit constants */
        activityMode = intent.getStringExtra(Intent.EXTRA_TEXT)
        favClient = FavClient(this)
        historyClient = HistoryClient(this)

        /* Setup UI */
        setContentView(R.layout.activity_recycler_data_list)
        title = resources.getString(if (activityMode == mode_history) R.string.hist else R.string.fav)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Clear all button
        fab = findViewById(R.id._fab)
        fab.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.delete_all_entries))
                .setMessage(resources.getString(
                    if (activityMode == mode_history) R.string.del_hist_message
                    else R.string.delete_fav_message)
                )
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (activityMode == mode_history) historyClient.deleteAll()
                        else if (activityMode == mode_favorites) favClient.deleteAll()
                        updateListData()
                        notifyItemsAdapter()
                    }
                    showMessage(this, resources.getString(R.string.wiped_success))
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
        }

        // RecyclerView
        val brohaList = findViewById<RecyclerView>(R.id.recyclerView)
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

        private val mBrohaListInterfaceActivity: WeakReference<ListInterfaceActivity>
        private val mIconHashClient: WeakReference<IconHashClient?>

        class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val back: ConstraintLayout
            val icon: AppCompatImageView
            val title: AppCompatTextView
            val url: AppCompatTextView
            val time: AppCompatTextView

            init {
                back = view.findViewById(R.id.bg)
                icon = view.findViewById(R.id.icon)
                title = view.findViewById(R.id.title)
                url = view.findViewById(R.id.url)
                time = view.findViewById(R.id.time)
            }
        }

        class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: AppCompatTextView

            init {
                text = view.findViewById(R.id.text)
            }
        }

        init {
            mBrohaListInterfaceActivity = WeakReference(brohaListInterfaceActivity)
            mIconHashClient = WeakReference(iconHashClient)
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
            val listInterfaceActivity = mBrohaListInterfaceActivity.get()!!

            if (holder is EmptyViewHolder) {
                holder.text.text = listInterfaceActivity.resources.getString(
                    if (activityMode == mode_history) R.string.hist_empty
                    else R.string.fav_list_empty
                )
            } else if (holder is ListViewHolder) {
                val clientActivity = mBrohaListInterfaceActivity.get()!!
                val iconHashClient = mIconHashClient.get()
                val data = listData!![position]
                val title = data.title
                val url = data.url
                lateinit var icon: Bitmap

                CoroutineScope(Dispatchers.IO).launch {
                    if (data.iconHash != null) {
                        icon = iconHashClient!!.read(data.iconHash)!!
                        CoroutineScope(Dispatchers.Main).launch { holder.icon.setImageBitmap(icon) }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch { holder.icon.setImageResource(R.drawable.default_favicon) }
                    }
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
                    needLoad.putExtra("needLoadUrl", url)
                    listInterfaceActivity.setResult(0, needLoad)
                    listInterfaceActivity.finish()
                }
                holder.back.setOnLongClickListener { view: View? ->
                    val popup = PopupMenu(
                        listInterfaceActivity, view!!
                    )
                    val menu1 = popup.menu
                    if (activityMode == mode_history) {
                        menu1.add(listInterfaceActivity.resources.getString(R.string.delete))
                        menu1.add(listInterfaceActivity.resources.getString(R.string.copy_url))
                        menu1.add(listInterfaceActivity.resources.getString(R.string.add_to_fav))
                    } else if (activityMode == mode_favorites) {
                        menu1.add(listInterfaceActivity.resources.getString(R.string.favMenuEdit))
                        menu1.add(listInterfaceActivity.resources.getString(R.string.copy_url))
                        menu1.add(listInterfaceActivity.resources.getString(R.string.delete))
                    }
                    popup.setOnMenuItemClickListener { item: MenuItem ->
                        if (item.title.toString() == listInterfaceActivity.resources.getString(R.string.delete)) {
                            CoroutineScope(Dispatchers.IO).launch {
                                if (activityMode == mode_history)
                                    clientActivity.historyClient.deleteById(data.id)
                                else if (activityMode == mode_favorites)
                                    clientActivity.favClient.deleteById(data.id)
                            }
                            listData!!.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeRemoved(position, itemCount - position)
                        } else if (item.title.toString() == listInterfaceActivity.resources.getString(
                                R.string.copy_url)) {
                            copyClipboard(listInterfaceActivity, url)
                        } else if (item.title.toString() == listInterfaceActivity.resources.getString(
                                R.string.favMenuEdit)) {
                            val layoutInflater = LayoutInflater.from(listInterfaceActivity)
                            @SuppressLint("InflateParams") val root =
                                layoutInflater.inflate(R.layout.dialog_fav_edit, null)
                            val titleEditText = root.findViewById<AppCompatEditText>(R.id.titleEditText)
                            val urlEditText = root.findViewById<AppCompatEditText>(R.id.favUrlEditText)
                            titleEditText.setText(title)
                            urlEditText.setText(url)
                            MaterialAlertDialogBuilder(listInterfaceActivity)
                                .setTitle(listInterfaceActivity.resources.getString(R.string.favMenuEdit))
                                .setView(root)
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
                                                listInterfaceActivity.notifyItemsAdapter()
                                            }
                                        }
                                    }
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .setIcon(holder.icon.drawable)
                                .create().show()
                        } else if (item.title.toString() == listInterfaceActivity.resources.getString(
                                R.string.add_to_fav)) {
                            CoroutineScope(Dispatchers.IO).launch {
                                clientActivity.favClient.insert(Broha(data.iconHash, title, url!!))
                            }
                            showMessage(
                                listInterfaceActivity,
                                listInterfaceActivity.resources.getString(R.string.save_successful)
                            )
                        } else {
                            return@setOnMenuItemClickListener false
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
