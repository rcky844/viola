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
package tipz.viola.webviewui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.build.info.BuildInfo
import tipz.viola.LauncherActivity
import tipz.viola.R
import tipz.viola.broha.ListInterfaceActivity
import tipz.viola.broha.api.FavClient
import tipz.viola.broha.database.Broha
import tipz.viola.broha.database.IconHashClient
import tipz.viola.download.DownloadActivity
import tipz.viola.search.SuggestionAdapter
import tipz.viola.settings.SettingsActivity
import tipz.viola.settings.SettingsKeys
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.InternalUrls
import tipz.viola.webview.VWebView
import tipz.viola.webview.VWebViewActivity
import tipz.viola.webviewui.components.FullscreenFloatingActionButton
import java.lang.ref.WeakReference
import java.text.DateFormat


@Suppress("DEPRECATION")
class BrowserActivity : VWebViewActivity() {
    private lateinit var urlEditText: MaterialAutoCompleteTextView
    private lateinit var upRightFab: AppCompatImageView
    private var currentUserAgentState = VWebView.UserAgentMode.MOBILE
    private var currentCustomUserAgent: String? = null
    private var currentCustomUAWideView = false
    private lateinit var favClient: FavClient
    private lateinit var iconHashClient: IconHashClient
    private lateinit var toolBar: RecyclerView
    private lateinit var toolsBarExtendableRecycler: RecyclerView
    private lateinit var toolsBarExtendableBackground: ConstraintLayout
    private lateinit var toolsBarExtendableCloseHitBox: LinearLayoutCompat
    private lateinit var sslLock: AppCompatImageView
    private lateinit var homeButton: LinearLayoutCompat
    private lateinit var fullscreenFab: FullscreenFloatingActionButton
    private var viewMode: Int = 0
    private var sslState: SslState = SslState.NONE
    private var sslErrorHost: String = CommonUtils.EMPTY_STRING
    private var setFabHiddenViews = false

    enum class SslState {
        NONE, SECURE, ERROR, SEARCH, FILES, INTERNAL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize variables
        upRightFab = findViewById(R.id.upRightFab)
        urlEditText = findViewById(R.id.urlEditText)
        progressBar = findViewById(R.id.webviewProgressBar)
        faviconProgressBar = findViewById(R.id.faviconProgressBar)
        swipeRefreshLayout = findViewById(R.id.layout_webview)
        webview = swipeRefreshLayout.findViewById(R.id.webview)
        favicon = findViewById(R.id.favicon)
        startPageLayout = findViewById(R.id.layout_startpage)
        favClient = FavClient(this)
        iconHashClient = IconHashClient(this)
        sslLock = findViewById(R.id.ssl_lock)
        homeButton = findViewById(R.id.home_button)
        fullscreenFab = findViewById(R.id.fullscreen_fab)

        // Setup toolbar
        toolBar = findViewById(R.id.toolBar)
        toolBar.adapter = ItemsAdapter(this, toolsBarItemList)
        (toolBar.layoutManager as FlexboxLayoutManager).apply {
            justifyContent = JustifyContent.SPACE_AROUND
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        // Setup toolbar expandable
        toolsBarExtendableRecycler = findViewById(R.id.toolsBarExtendableRecycler)
        toolsBarExtendableRecycler.adapter =
            ToolbarItemsAdapter(this, toolsBarExpandableItemList, toolsBarExpandableDescriptionList)
        (toolsBarExtendableRecycler.layoutManager as FlexboxLayoutManager).apply {
            justifyContent = JustifyContent.SPACE_AROUND
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        toolsBarExtendableBackground = this.findViewById(R.id.toolsBarExtendableBackground)
        toolsBarExtendableBackground.post {
            toolsBarExtendableBackground.visibility = View.GONE
        }
        toolsBarExtendableCloseHitBox = this.findViewById(R.id.toolsBarExtendableCloseHitBox)
        toolsBarExtendableCloseHitBox.setOnClickListener {
            expandToolBar()
        }

        // Setup favicon
        favicon?.setOnClickListener {
            val popupMenu = PopupMenu(this, favicon!!)
            val menu = popupMenu.menu
            menu.add(if (webview.visibility == View.GONE) resources.getString(R.string.start_page) else webview.title).isEnabled =
                false
            menu.add(resources.getString(R.string.copy_title))
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                if (item.title.toString() == resources.getString(R.string.copy_title)) {
                    CommonUtils.copyClipboard(this@BrowserActivity, webview.title)
                    return@setOnMenuItemClickListener true
                }
                false
            }
            popupMenu.show()
        }

        // Setup SSL Lock
        sslLock.setOnClickListener {
            val cert = webview.certificate
            val issuedTo = cert!!.issuedTo
            val issuedBy = cert.issuedBy
            val dialog = MaterialAlertDialogBuilder(this@BrowserActivity)
            dialog.setTitle(Uri.parse(webview.url).host)
                .setMessage(
                    resources.getString(
                        R.string.ssl_info_dialog_content,
                        issuedTo.cName, issuedTo.oName, issuedTo.uName,
                        issuedBy.cName, issuedBy.oName, issuedBy.uName,
                        DateFormat.getDateTimeInstance()
                            .format(cert.validNotBeforeDate),
                        DateFormat.getDateTimeInstance().format(cert.validNotAfterDate)
                    )
                )
                .setPositiveButton(resources.getString(android.R.string.ok), null)
                .create().show()
        }

        // Setup Url EditText box
        urlEditText.setOnEditorActionListener(
            OnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == KeyEvent.ACTION_DOWN) {
                    webview.loadUrl(urlEditText.text.toString())
                    urlEditText.clearFocus()
                    closeKeyboard()
                    return@OnEditorActionListener true
                }
                false
            })
        urlEditText.setOnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                if (urlEditText.text.toString() != webview.url) urlEditText.setText(webview.url)
                urlEditText.setSelection(0)
                urlEditText.dropDownHeight = 0
            } else {
                urlEditText.dropDownHeight = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
        urlEditText.setOnClickListener {
            if (toolsBarExtendableBackground.visibility == View.VISIBLE) expandToolBar()
        }
        urlEditText.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, view: View, _: Int, _: Long ->
                webview.loadUrl((view.findViewById<View>(android.R.id.text1) as AppCompatTextView).text.toString())
                closeKeyboard()
            }
        urlEditText.setAdapter(
            SuggestionAdapter(
                this@BrowserActivity,
                R.layout.template_item_text_single
            )
        )

        // Setup the up most fab (currently for reload)
        upRightFab.setOnClickListener {
            if (progressBar.progress > 0) webview.stopLoading()
            if (progressBar.progress == 0) webview.reload()
        }

        // Setup home button
        homeButton.findViewById<AppCompatImageView>(R.id.imageView)
            ?.setImageResource(R.drawable.home)
        homeButton.findViewById<AppCompatTextView>(R.id.textView)?.text =
            resources.getString(R.string.homepage_webpage_home)
        homeButton.setOnClickListener {
            webview.loadHomepage(false)
        }
        homeButton.visibility = View.GONE // FIXME: Unhide

        // Setup build tag
        val buildTag = findViewById<AppCompatTextView>(R.id.buildTag)
        buildTag.text = BuildInfo().getProductBuildTag()
        buildTag.visibility = View.VISIBLE // FIXME: Somewhere to set this

        // Finally, load homepage
        val dataUri = intent.data
        if (dataUri != null) {
            webview.loadUrl(dataUri.toString())
        } else {
            webview.loadHomepage(!settingsPreference.getIntBool(SettingsKeys.useWebHomePage))
        }
    }

    // https://stackoverflow.com/a/57840629/10866268
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val params =
            toolsBarExtendableBackground.layoutParams as ConstraintLayout.LayoutParams
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.height = resources.getDimension(R.dimen.toolbar_extendable_height).toInt()
            params.matchConstraintMaxWidth =
                resources.getDimension(R.dimen.toolbar_extendable_max_width).toInt()
        }
    }

    override fun doSettingsCheck() {
        super.doSettingsCheck()
        val reverseAddressBar = settingsPreference.getInt(SettingsKeys.reverseAddressBar)
        if (reverseAddressBar != viewMode) {
            appbar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = when (reverseAddressBar) {
                    1 -> R.id.toolsContainer
                    else -> ConstraintSet.UNSET
                }
                bottomToBottom = when (reverseAddressBar) {
                    1 -> ConstraintSet.PARENT_ID
                    else -> ConstraintSet.UNSET
                }
                bottomToTop = when (reverseAddressBar) {
                    0 -> R.id.webviewContainer
                    else -> ConstraintSet.UNSET
                }
                topToTop = when (reverseAddressBar) {
                    0 -> ConstraintSet.PARENT_ID
                    else -> ConstraintSet.UNSET
                }
            }
            webviewContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToTop = when (reverseAddressBar) {
                    1 -> ConstraintSet.PARENT_ID
                    else -> ConstraintSet.UNSET
                }
                topToBottom = when (reverseAddressBar) {
                    0 -> R.id.appbar
                    else -> ConstraintSet.UNSET
                }
            }
            toolsContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomToTop = when (reverseAddressBar) {
                    1 -> R.id.appbar
                    else -> ConstraintSet.UNSET
                }
                bottomToBottom = when (reverseAddressBar) {
                    0 -> ConstraintSet.PARENT_ID
                    else -> ConstraintSet.UNSET
                }
            }
            viewMode = reverseAddressBar
        }
    }

    // This function returns true to close ToolBar, and vice versa.
    fun itemSelected(view: AppCompatImageView?, item: Int): Boolean {
        when (item) {
            R.drawable.arrow_back_alt -> if (webview.canGoBack()) webview.goBack()
            R.drawable.arrow_forward_alt -> if (webview.canGoForward()) webview.goForward()
            R.drawable.refresh -> webview.reload()
            R.drawable.home -> {
                val reqVal: Boolean = !settingsPreference.getIntBool(SettingsKeys.useWebHomePage)
                webview.loadHomepage(reqVal)
                if (reqVal) urlEditText.setText(CommonUtils.EMPTY_STRING)
            }

            R.drawable.smartphone, R.drawable.desktop, R.drawable.custom -> {
                val userAgentMode = {
                    when (currentUserAgentState) {
                        VWebView.UserAgentMode.MOBILE -> VWebView.UserAgentMode.DESKTOP
                        VWebView.UserAgentMode.DESKTOP -> VWebView.UserAgentMode.MOBILE
                        VWebView.UserAgentMode.CUSTOM -> VWebView.UserAgentMode.MOBILE
                    }
                }

                val dataBundle = VWebView.UserAgentBundle()
                dataBundle.iconView = view
                webview.setUserAgent(userAgentMode(), dataBundle)
                currentUserAgentState = userAgentMode()
            }

            R.drawable.share -> CommonUtils.shareUrl(this, webview.url)
            R.drawable.app_shortcut -> { // FIXME: Shortcuts pointing to the same URL does not behave as expected
                // Bail out for certain URLs
                // FIXME: Block certain internal URLs
                if (webview.title.isNullOrBlank() || webview.url.isBlank()) return true

                // Show dialog for selecting modes
                val dialog = MaterialAlertDialogBuilder(this)
                dialog.setTitle(resources.getString(R.string.toolbar_expandable_app_shortcut))

                // TODO: Export as proper list
                val arrayAdapter = ArrayAdapter<String>(this, R.layout.template_item_text_single)
                arrayAdapter.add(resources.getString(R.string.toolbar_expandable_shortcuts_menu_browser))
                arrayAdapter.add(resources.getString(R.string.toolbar_expandable_shortcuts_menu_custom_tabs))
                arrayAdapter.add(resources.getString(R.string.toolbar_expandable_shortcuts_menu_webapp))
                dialog.setAdapter(arrayAdapter) { _: DialogInterface?, which: Int ->
                    val launchIntent = Intent(this, LauncherActivity::class.java)
                        .setData(Uri.parse(webview.url))
                        .setAction(Intent.ACTION_VIEW)
                        .putExtra(LauncherActivity.EXTRA_SHORTCUT_TYPE, which)

                    ShortcutManagerCompat.requestPinShortcut(
                        this, ShortcutInfoCompat.Builder(this, webview.title!!)
                            .setShortLabel(webview.title!!)
                            .setIcon(
                                IconCompat.createWithBitmap(
                                    CommonUtils.drawableToBitmap(favicon!!.drawable)
                                )
                            )
                            .setIntent(launchIntent)
                            .build(), null
                    )
                }
                dialog.create().show()
                return false
            }

            R.drawable.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                mGetNeedLoad.launch(intent)
            }

            R.drawable.history -> {
                val intent = Intent(this@BrowserActivity, ListInterfaceActivity::class.java)
                intent.putExtra(Intent.EXTRA_TEXT, ListInterfaceActivity.mode_history)
                mGetNeedLoad.launch(intent)
            }

            R.drawable.favorites -> {
                val intent = Intent(this@BrowserActivity, ListInterfaceActivity::class.java)
                intent.putExtra(Intent.EXTRA_TEXT, ListInterfaceActivity.mode_favorites)
                mGetNeedLoad.launch(intent)
            }

            R.drawable.favorites_add -> {
                val url = webview.url
                if (url.isBlank() || url == InternalUrls.aboutBlankUrl) return false

                val icon = favicon!!.drawable
                val title = webview.title

                CoroutineScope(Dispatchers.IO).launch {
                    val iconHash = if (icon is BitmapDrawable) iconHashClient.save(icon.bitmap) else null
                    favClient.insert(Broha(iconHash, title, url))
                }
                CommonUtils.showMessage(
                    this,
                    resources.getString(R.string.save_successful)
                )
            }

            R.drawable.close -> finish()
            R.drawable.view_stream -> expandToolBar()
            R.drawable.code -> return webview.loadViewSourcePage(null)
            R.drawable.new_tab -> {
                val i = Intent(this, BrowserActivity::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                else i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                startActivity(i)
            }

            R.drawable.print -> {
                val jobName = getString(R.string.app_name) + " Document"
                val printAdapter: PrintDocumentAdapter =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        webview.createPrintDocumentAdapter(jobName)
                    } else {
                        webview.createPrintDocumentAdapter()
                    }
                val printManager = getSystemService(PRINT_SERVICE) as PrintManager
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }

            R.drawable.download -> {
                val intent = Intent(this@BrowserActivity, DownloadActivity::class.java)
                mGetNeedLoad.launch(intent)
            }

            R.drawable.fullscreen -> {
                if (!setFabHiddenViews) {
                    fullscreenFab.hiddenViews = mutableListOf(appbar, toolsContainer)
                    fullscreenFab.activity = this
                    setFabHiddenViews = true
                }
                fullscreenFab.show()
            }
        }
        return true // Close ToolBar if not interrupted
    }

    fun itemLongSelected(view: AppCompatImageView?, item: Int) {
        when (item) {
            R.drawable.smartphone, R.drawable.desktop, R.drawable.custom -> {
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
                        val dataBundle = VWebView.UserAgentBundle()
                        dataBundle.userAgentString = customUserAgent.text.toString()
                        dataBundle.iconView = view
                        dataBundle.enableDesktop = deskMode.isChecked
                        webview.setUserAgent(VWebView.UserAgentMode.CUSTOM, dataBundle)

                        currentUserAgentState = VWebView.UserAgentMode.CUSTOM
                        currentCustomUserAgent = customUserAgent.text.toString()
                        currentCustomUAWideView = deskMode.isChecked
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show()
                if (currentCustomUserAgent != null) customUserAgent.setText(currentCustomUserAgent)
            }

            R.drawable.home -> {
                val reqVal: Boolean = settingsPreference.getIntBool(SettingsKeys.useWebHomePage)
                webview.loadHomepage(reqVal)
                if (reqVal) urlEditText.setText(CommonUtils.EMPTY_STRING)
            }
        }
    }

    fun expandToolBar() {
        val viewVisible: Boolean = toolsBarExtendableBackground.visibility == View.VISIBLE
        val transition: Transition = Slide(Gravity.BOTTOM)
        transition.duration =
            (resources.getInteger(R.integer.anim_expandable_speed) * Settings.Global.getFloat(
                contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )).toLong()
        transition.addTarget(R.id.toolsBarExtendableBackground)
        TransitionManager.beginDelayedTransition(toolsBarExtendableBackground, transition)
        toolsBarExtendableBackground.visibility = if (viewVisible) View.GONE else View.VISIBLE
        toolsBarExtendableCloseHitBox.visibility = if (viewVisible) View.GONE else View.VISIBLE
    }

    override fun onPageLoadProgressChanged(progress: Int) {
        super.onPageLoadProgressChanged(progress)
        if (progress == -1) upRightFab.setImageResource(R.drawable.stop)
        if (progress == 0) upRightFab.setImageResource(R.drawable.refresh)
    }

    private fun closeKeyboard() {
        WindowCompat.getInsetsController(window, urlEditText).hide(WindowInsetsCompat.Type.ime())
    }


    override fun onUrlUpdated(url: String?) {
        if (!urlEditText.isFocused) urlEditText.setText(url)
    }

    override fun onUrlUpdated(url: String?, position: Int) {
        urlEditText.setText(url)
        urlEditText.setSelection(position)
    }

    override fun onDropDownDismissed() {
        urlEditText.dismissDropDown()
        urlEditText.clearFocus()
    }

    override fun onSslCertificateUpdated() {
        if (startPageLayout?.visibility == View.VISIBLE) {
            sslState = SslState.SEARCH
            sslLock.setImageResource(R.drawable.search)
            sslLock.isClickable = false
            return
        }
        if (webview.certificate == null) {
            sslState = SslState.NONE
            sslLock.setImageResource(R.drawable.warning)
            sslLock.isClickable = false // Certificate cannot be null for the dialog
        } else if (sslState == SslState.ERROR) { // State error is set before SECURE
            sslErrorHost = Uri.parse(webview.url).host!!
            sslState = SslState.NONE
        } else {
            sslState = SslState.SECURE
            sslLock.setImageResource(R.drawable.lock)
            sslLock.isClickable = true
        }
    }

    override fun onSslErrorProceed() {
        sslState = SslState.ERROR
        sslLock.setImageResource(R.drawable.warning)
        sslLock.isClickable = true
    }

    override fun onStartPageEditTextPressed() {
        urlEditText.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(urlEditText, InputMethodManager.SHOW_FORCED)
    }

    class ItemsAdapter(mainActivity: BrowserActivity, itemsList: List<Int>) :
        RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
        private val mBrowserActivity: WeakReference<BrowserActivity>
        private val mItemsList: WeakReference<List<Int>>

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mImageView: AppCompatImageView

            init {
                mImageView = view.findViewById(R.id.imageView)
            }
        }

        init {
            mBrowserActivity = WeakReference(mainActivity)
            mItemsList = WeakReference(itemsList)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.template_icon_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mImageView.setImageResource(mItemsList.get()!![position])
            holder.mImageView.setOnClickListener {
                mBrowserActivity.get()!!
                    .itemSelected(holder.mImageView, mItemsList.get()!![position])
            }
            holder.mImageView.setOnLongClickListener {
                mBrowserActivity.get()!!
                    .itemLongSelected(holder.mImageView, mItemsList.get()!![position])
                true
            }
        }

        override fun getItemCount(): Int {
            return mItemsList.get()!!.size
        }
    }

    class ToolbarItemsAdapter(
        mainActivity: BrowserActivity,
        itemsList: List<Int>,
        descriptionList: List<Int>
    ) :
        RecyclerView.Adapter<ToolbarItemsAdapter.ViewHolder>() {
        private val mBrowserActivity: WeakReference<BrowserActivity>
        private val mItemsList: WeakReference<List<Int>>
        private val mDescriptionList: WeakReference<List<Int>>

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val mItemBox: LinearLayoutCompat
            val mImageView: AppCompatImageView
            val mTextView: AppCompatTextView

            init {
                mItemBox = view as LinearLayoutCompat
                mImageView = view.findViewById(R.id.imageView)
                mTextView = view.findViewById(R.id.textView)
            }
        }

        init {
            mBrowserActivity = WeakReference(mainActivity)
            mItemsList = WeakReference(itemsList)
            mDescriptionList = WeakReference(descriptionList)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.template_icon_description_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mItemBox.setOnClickListener {
                val closeToolBar = mBrowserActivity.get()!!
                    .itemSelected(holder.mImageView, mItemsList.get()!![position])
                if (closeToolBar) mBrowserActivity.get()!!.expandToolBar()
            }
            holder.mItemBox.setOnLongClickListener {
                mBrowserActivity.get()!!
                    .itemLongSelected(holder.mImageView, mItemsList.get()!![position])
                true
            }
            holder.mImageView.setImageResource(mItemsList.get()!![position])
            holder.mTextView.text =
                mBrowserActivity.get()!!.resources.getString(mDescriptionList.get()!![position])
        }

        override fun getItemCount(): Int {
            return mItemsList.get()!!.size
        }
    }

    companion object {
        // TODO: Add support for reverting to legacy layout
        private val legacyToolsBarItemList = listOf(
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
            R.drawable.download,
            R.drawable.close
        )

        private val toolsBarItemList = listOf(
            R.drawable.arrow_back_alt,
            R.drawable.arrow_forward_alt,
            R.drawable.home,
            R.drawable.share,
            R.drawable.view_stream
        )

        private val toolsBarExpandableItemList = listOf(
            R.drawable.new_tab,
            R.drawable.favorites,
            R.drawable.history,
            R.drawable.smartphone,
            R.drawable.favorites_add,
            R.drawable.download,
            R.drawable.fullscreen,
            R.drawable.app_shortcut,
            R.drawable.settings,
            R.drawable.code,
            R.drawable.print,
            R.drawable.close
        )

        private val toolsBarExpandableDescriptionList = listOf(
            R.string.toolbar_expandable_new_tab,
            R.string.toolbar_expandable_favorites,
            R.string.toolbar_expandable_history,
            R.string.toolbar_expandable_viewport,
            R.string.toolbar_expandable_favorites_add,
            R.string.toolbar_expandable_downloads,
            R.string.toolbar_expandable_fullscreen,
            R.string.toolbar_expandable_app_shortcut,
            R.string.toolbar_expandable_settings,
            R.string.toolbar_expandable_view_page_source,
            R.string.toolbar_expandable_print,
            R.string.toolbar_expandable_close
        )
    }
}