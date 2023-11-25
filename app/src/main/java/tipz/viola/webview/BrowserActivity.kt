/*
 * Copyright (C) 2020-2023 Tipz Team
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
package tipz.viola.webview

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.JsonReader
import android.util.JsonToken
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.ValueCallback
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import tipz.viola.Application
import tipz.viola.R
import tipz.viola.broha.BrohaListInterfaceActivity
import tipz.viola.broha.api.FavUtils
import tipz.viola.broha.database.icons.IconHashClient
import tipz.viola.search.SearchEngineEntries
import tipz.viola.search.SuggestionAdapter
import tipz.viola.settings.SettingsActivity
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsUtils
import tipz.viola.utils.BrowservioURLs
import tipz.viola.utils.CommonUtils
import java.io.IOException
import java.io.StringReader
import java.lang.ref.WeakReference
import java.text.DateFormat
import java.util.Objects

@Suppress("DEPRECATION")
class BrowserActivity : VWebViewActivity() {
    private var UrlEdit: MaterialAutoCompleteTextView? = null
    private var fab: AppCompatImageView? = null
    private var currentPrebuiltUAState = false
    private var currentCustomUA: String? = null
    private var currentCustomUAWideView = false
    private var iconHashClient: IconHashClient? = null
    private var retractedRotation = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        initialize()
        initializeLogic()
    }

    // https://stackoverflow.com/a/57840629/10866268
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    fun itemSelected(view: AppCompatImageView?, item: Int) {
        if (item == R.drawable.arrow_back_alt && webview.canGoBack()) {
            webview.goBack()
        } else if (item == R.drawable.arrow_forward_alt && webview.canGoForward()) {
            webview.goForward()
        } else if (item == R.drawable.refresh) {
            webview.webViewReload()
        } else if (item == R.drawable.home) {
            if (CommonUtils.isIntStrOne(
                    SettingsUtils.getPrefNum(
                        pref,
                        SettingsKeys.useWebHomePage
                    )
                )
            ) {
                webview.loadUrl(
                    SearchEngineEntries.getHomePageUrl(
                        pref,
                        SettingsUtils.getPrefNum(pref, SettingsKeys.defaultHomePageId)
                    )
                )
            } else {
                UrlEdit!!.setText(CommonUtils.EMPTY_STRING)
                webview.loadUrl(BrowservioURLs.startUrl)
            }
        } else if (item == R.drawable.smartphone || item == R.drawable.desktop || item == R.drawable.custom) {
            currentPrebuiltUAState = !currentPrebuiltUAState
            webview.setPrebuiltUAMode(
                view,
                (if (currentPrebuiltUAState) 1 else 0).toDouble(),
                false
            )
        } else if (item == R.drawable.new_tab) {
            val i = Intent(this, BrowserActivity::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT) else i.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
            )
            startActivity(i)
        } else if (item == R.drawable.share) {
            CommonUtils.shareUrl(this, webview.url!!)
        } else if (item == R.drawable.app_shortcut) {
            if (webview.title != null && webview.title!!.isNotBlank()) ShortcutManagerCompat.requestPinShortcut(
                this, ShortcutInfoCompat.Builder(this, webview.title!!)
                    .setShortLabel(webview.title!!)
                    .setIcon(
                        IconCompat.createWithBitmap(
                            CommonUtils.drawableToBitmap(favicon.drawable)
                        )
                    )
                    .setIntent(
                        Intent(this, BrowserActivity::class.java)
                            .setData(Uri.parse(webview.url))
                            .setAction(Intent.ACTION_VIEW)
                    )
                    .build(), null
            )
        } else if (item == R.drawable.settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            mGetNeedLoad.launch(intent)
        } else if (item == R.drawable.history) {
            val intent = Intent(this@BrowserActivity, BrohaListInterfaceActivity::class.java)
            intent.putExtra(Intent.EXTRA_TEXT, BrohaListInterfaceActivity.mode_history)
            mGetNeedLoad.launch(intent)
        } else if (item == R.drawable.favorites) {
            val icon = favicon.drawable
            FavUtils.appendData(this, iconHashClient, webview.title, webview.url,
                if (icon is BitmapDrawable) icon.bitmap else null
            )
            CommonUtils.showMessage(this, resources.getString(R.string.save_successful)
            )
        } else if (item == R.drawable.close) {
            finish()
        }
    }

    fun itemLongSelected(view: AppCompatImageView?, item: Int) {
        if (item == R.drawable.smartphone || item == R.drawable.desktop || item == R.drawable.custom) {
            val layoutInflater = LayoutInflater.from(this)
            @SuppressLint("InflateParams") val root =
                layoutInflater.inflate(R.layout.dialog_ua_edit, null)
            val customUserAgent = root.findViewById<AppCompatEditText>(R.id.edittext)
            val deskMode = root.findViewById<AppCompatCheckBox>(R.id.deskMode)
            deskMode.isChecked = currentCustomUAWideView
            val dialog = MaterialAlertDialogBuilder(this)
            dialog.setTitle(resources.getString(R.string.customUA))
                .setView(root)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                    if (customUserAgent.length() != 0) webview.setUA(
                        view, deskMode.isChecked,
                        Objects.requireNonNull(customUserAgent.text).toString(),
                        R.drawable.custom, false
                    )
                    currentCustomUA = Objects.requireNonNull(customUserAgent.text).toString()
                    currentCustomUAWideView = deskMode.isChecked
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
            if (currentCustomUA != null) customUserAgent.setText(currentCustomUA)
        } else if (item == R.drawable.favorites) {
            val intent = Intent(this@BrowserActivity, BrohaListInterfaceActivity::class.java)
            intent.putExtra(Intent.EXTRA_TEXT, BrohaListInterfaceActivity.mode_favorites)
            mGetNeedLoad.launch(intent)
        }
    }

    /**
     * Initialize function
     */
    @SuppressLint("AddJavascriptInterface")
    private fun initialize() {
        fab = findViewById(R.id.fab)
        UrlEdit = findViewById(R.id.UrlEdit)
        progressBar = findViewById(R.id.webviewProgressBar)
        faviconProgressBar = findViewById(R.id.faviconProgressBar)
        swipeRefreshLayout = findViewById(R.id.layout_webview)
        webview = swipeRefreshLayout.findViewById(R.id.webview)
        val actionBar = findViewById<RecyclerView>(R.id.actionBar)
        favicon = findViewById(R.id.favicon)
        toolsContainer = findViewById(R.id.toolsContainer)
        startPageLayout = findViewById(R.id.layout_startpage)
        actionBar.layoutManager = LinearLayoutManager(
            this, RecyclerView.HORIZONTAL, false
        )
        actionBar.adapter = ItemsAdapter(this)
        favicon.setOnClickListener {
            val cert = webview.certificate
            val popupMenu = PopupMenu(this, favicon)
            val menu = popupMenu.menu
            menu.add(webview.title).isEnabled = false
            menu.add(resources.getString(R.string.copy_title))
            if (cert != null) menu.add(resources.getString(R.string.ssl_info))
            if (CommonUtils.isIntStrOne(
                    SettingsUtils.getPrefNum(
                        pref,
                        SettingsKeys.isJavaScriptEnabled
                    )
                )
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            ) menu.add(resources.getString(R.string.view_page_source))
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                if (item.title.toString() == resources.getString(R.string.copy_title)) {
                    CommonUtils.copyClipboard(this@BrowserActivity, webview.title)
                    return@setOnMenuItemClickListener true
                } else if (item.title.toString() == resources.getString(R.string.ssl_info)) {
                    val issuedTo = cert!!.issuedTo
                    val issuedBy = cert.issuedBy
                    val dialog = MaterialAlertDialogBuilder(this@BrowserActivity)
                    dialog.setTitle(Uri.parse(webview.url).host)
                        .setMessage(
                            resources.getString(
                                R.string.ssl_info_dialog_content,
                                issuedTo.cName, issuedTo.oName, issuedTo.uName,
                                issuedBy.cName, issuedBy.oName, issuedBy.uName,
                                DateFormat.getDateTimeInstance().format(cert.validNotBeforeDate),
                                DateFormat.getDateTimeInstance().format(cert.validNotAfterDate)
                            )
                        )
                        .setPositiveButton(resources.getString(android.R.string.ok), null)
                        .create().show()
                    return@setOnMenuItemClickListener true
                } else if (item.title.toString() == resources.getString(R.string.view_page_source)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webview.evaluateJavascript(
                            "document.documentElement.outerHTML",
                            ValueCallback { value: String? ->
                                val reader = JsonReader(StringReader(value))
                                reader.isLenient = true
                                try {
                                    if (reader.peek() == JsonToken.STRING) {
                                        val domStr = reader.nextString()
                                        reader.close()
                                        if (domStr == null) return@ValueCallback
                                        MaterialAlertDialogBuilder(this@BrowserActivity)
                                            .setTitle(resources.getString(R.string.view_page_source))
                                            .setMessage(domStr)
                                            .setPositiveButton(
                                                resources.getString(android.R.string.ok),
                                                null
                                            )
                                            .setNegativeButton(resources.getString(android.R.string.copy)) { _: DialogInterface?, _: Int ->
                                                CommonUtils.copyClipboard(
                                                    this@BrowserActivity,
                                                    domStr
                                                )
                                            }
                                            .create().show()
                                    }
                                } catch (ignored: IOException) {
                                }
                            })
                    }
                }
                false
            }
            popupMenu.show()
        }
        fab?.setOnClickListener {
            if (fab?.rotation != 0f || fab?.rotation != 180f) fab?.animate()?.cancel()
            if (toolsContainer.visibility == View.VISIBLE) {
                fab?.animate()?.rotation(retractedRotation.toFloat())?.setDuration(250)?.start()
                toolsContainer.animate()?.alpha(0f)?.setDuration(250)?.start()
                toolsContainer.visibility = View.GONE
            } else {
                fab?.animate()?.rotation((retractedRotation - 180).toFloat())?.setDuration(250)?.start()
                toolsContainer.animate()?.alpha(1f)?.setDuration(250)?.start()
                toolsContainer.visibility = View.VISIBLE
            }
            reachModeCheck()
        }

        /* Code for detecting return key presses */
        UrlEdit?.setOnEditorActionListener(
            OnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == KeyEvent.ACTION_DOWN) {
                    webview.loadUrl(UrlEdit?.text.toString())
                    UrlEdit?.clearFocus()
                    return@OnEditorActionListener true
                }
                false
            })
        UrlEdit?.setOnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                if (UrlEdit?.text
                        .toString() != webview.url
                ) UrlEdit?.setText(webview.url)
                UrlEdit?.setSelection(0)
                UrlEdit?.dropDownHeight = 0
                closeKeyboard()
            } else {
                UrlEdit?.dropDownHeight = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
        UrlEdit?.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, view: View, _: Int, _: Long ->
                webview.loadUrl((view.findViewById<View>(android.R.id.text1) as AppCompatTextView).text.toString())
                closeKeyboard()
            }
        UrlEdit?.setAdapter(SuggestionAdapter(this@BrowserActivity, R.layout.recycler_list_item_1))
    }

    private fun closeKeyboard() {
        WindowCompat.getInsetsController(window, UrlEdit!!).hide(WindowInsetsCompat.Type.ime())
    }

    /**
     * Welcome to the Browservio (The Shrek Browser)
     * This browser was originally designed with Sketchware
     * This project was started on Aug 13 2020
     *
     *
     * sur wen reel Sherk brower pls sand meme sum
     */
    private fun initializeLogic() {
        iconHashClient = (applicationContext as Application).iconHashClient

        /* Init VioWebView */webview.notifyViewSetup()
        val intent = intent
        val dataUri = intent.data
        if (CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.useWebHomePage))) {
            webview.loadUrl(
                dataUri?.toString()
                    ?: SearchEngineEntries.getHomePageUrl(
                        pref,
                        SettingsUtils.getPrefNum(pref, SettingsKeys.defaultHomePageId)
                    )
            )
        } else {
            webview.loadUrl(BrowservioURLs.startUrl)
        }
    }

    override fun onUrlUpdated(url: String?) {
        if (!UrlEdit!!.isFocused) UrlEdit!!.setText(url)
    }

    override fun onUrlUpdated(url: String?, position: Int) {
        UrlEdit!!.setText(url)
        UrlEdit!!.setSelection(position)
    }

    override fun onDropDownDismissed() {
        UrlEdit!!.dismissDropDown()
        UrlEdit!!.clearFocus()
    }

    override fun onStartPageEditTextPressed() {
        UrlEdit!!.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(UrlEdit, InputMethodManager.SHOW_FORCED)
    }

    override fun doSettingsCheck() {
        super.doSettingsCheck()

        // Settings check
        toolsContainer.gravity = if (CommonUtils.isIntStrOne(
                SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.centerActionBar
                )
            )
        ) Gravity.CENTER_HORIZONTAL else Gravity.NO_GRAVITY
        fab!!.rotation = (if (CommonUtils.isIntStrOne(
                SettingsUtils.getPrefNum(
                    pref,
                    SettingsKeys.reverseLayout
                )
            )
        ) 0 else 180).toFloat()
        retractedRotation = fab!!.rotation.toInt()
        if (toolsContainer.visibility == View.VISIBLE) fab!!.rotation = fab!!.rotation - 180
        val reverseOnlyActionBar =
            CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, SettingsKeys.reverseLayout)) &&
                    CommonUtils.isIntStrOne(
                        SettingsUtils.getPrefNum(
                            pref,
                            SettingsKeys.reverseOnlyActionBar
                        )
                    )
        fab!!.visibility = if (reverseOnlyActionBar) View.GONE else View.VISIBLE
        val dp8 = CommonUtils.getDisplayMetrics(this@BrowserActivity, 8).toInt()
        val dp48 = CommonUtils.getDisplayMetrics(this@BrowserActivity, 48).toInt()
        UrlEdit!!.setPadding(
            dp8, dp8,
            if (reverseOnlyActionBar) dp8 else dp48,
            dp8
        )
    }

    class ItemsAdapter(mainActivity: BrowserActivity) :
        RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
        private val mMainActivity: WeakReference<BrowserActivity>

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mImageView: AppCompatImageView

            init {
                mImageView = view.findViewById(R.id.imageView)
            }
        }

        init {
            mMainActivity = WeakReference(mainActivity)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_icon_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mImageView.setImageResource(actionBarItemList[position])
            holder.mImageView.setOnClickListener {
                mMainActivity.get()!!
                    .itemSelected(holder.mImageView, actionBarItemList[position])
            }
            holder.mImageView.setOnLongClickListener {
                mMainActivity.get()!!
                    .itemLongSelected(holder.mImageView, actionBarItemList[position])
                true
            }
        }

        override fun getItemCount(): Int {
            return actionBarItemList.size
        }
    }

    companion object {
        private val actionBarItemList = listOf(
            R.drawable.arrow_back_alt,
            R.drawable.arrow_forward_alt,
            R.drawable.refresh,
            R.drawable.home,
            R.drawable.smartphone,
            R.drawable.new_tab,
            R.drawable.share,
            R.drawable.app_shortcut,
            R.drawable.settings,
            R.drawable.history,
            R.drawable.favorites,
            R.drawable.close
        )
    }
}