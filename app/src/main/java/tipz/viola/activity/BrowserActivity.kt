// Copyright (c) 2020-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.activity

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
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
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
import tipz.viola.LauncherActivity
import tipz.viola.R
import tipz.viola.activity.components.FullscreenFloatingActionButton
import tipz.viola.activity.components.SwipeController
import tipz.viola.broha.ListInterfaceActivity
import tipz.viola.broha.api.FavClient
import tipz.viola.broha.database.Broha
import tipz.viola.broha.database.IconHashClient
import tipz.viola.databinding.ActivityMainBinding
import tipz.viola.databinding.DialogHitTestTitleBinding
import tipz.viola.databinding.DialogUaEditBinding
import tipz.viola.databinding.TemplateIconDescriptionItemBinding
import tipz.viola.databinding.TemplateIconItemBinding
import tipz.viola.download.DownloadActivity
import tipz.viola.search.SuggestionAdapter
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.activity.SettingsActivity
import tipz.viola.utils.CommonUtils
import tipz.viola.utils.UpdateService
import tipz.viola.webview.VWebView
import tipz.viola.webview.VWebViewActivity
import tipz.viola.webview.pages.ExportedUrls
import tipz.viola.webview.pages.PrivilegedPages
import tipz.viola.widget.StringResAdapter
import java.lang.ref.WeakReference
import java.text.DateFormat


@Suppress("DEPRECATION")
class BrowserActivity : VWebViewActivity() {
    private lateinit var binding: ActivityMainBinding

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
    private lateinit var sslLock: AppCompatImageView
    private lateinit var fullscreenFab: FullscreenFloatingActionButton
    private var viewMode: Int = 0
    private var sslState: SslState = SslState.NONE
    private var sslErrorHost: String = ""
    private var setFabHiddenViews = false
    private lateinit var imm: InputMethodManager

    private var urlEditTextY1 = 0f
    private var urlEditTextY2 = 0f
    private var urlEditTextSwipeThreshold = 500f

    enum class SslState {
        NONE, SECURE, ERROR, SEARCH, FILES, INTERNAL
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize variables
        appbar = binding.appbar
        webviewContainer = binding.webviewContainer
        toolsContainer = binding.toolsContainer
        upRightFab = binding.upRightFab
        urlEditText = binding.urlEditText
        progressBar = binding.webviewProgressBar
        faviconProgressBar = binding.faviconProgressBar
        swipeRefreshLayout = binding.layoutWebview.swipe
        webview = binding.layoutWebview.webview
        favicon = binding.favicon
        sslLock = binding.sslLock
        fullscreenFab = binding.fullscreenFab

        // Broha Clients
        favClient = FavClient(this)
        iconHashClient = IconHashClient(this)

        // Miscellaneous
        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        // Start update service
        UpdateService(this, true)

        // Setup toolbar
        toolBar = binding.toolBar
        toolBar.adapter = ItemsAdapter(this, toolsBarItemList)
        (toolBar.layoutManager as FlexboxLayoutManager).apply {
            justifyContent = JustifyContent.SPACE_AROUND
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        // Setup toolbar expandable
        toolsBarExtendableRecycler = binding.toolsBarExtendableRecycler
        toolsBarExtendableRecycler.adapter =
            ToolbarItemsAdapter(this, toolsBarExpandableItemList, toolsBarExpandableDescriptionList)
        (toolsBarExtendableRecycler.layoutManager as FlexboxLayoutManager).apply {
            justifyContent = JustifyContent.SPACE_AROUND
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        toolsBarExtendableBackground = binding.toolsBarExtendableBackground
        toolsBarExtendableBackground.post {
            toolsBarExtendableBackground.visibility = View.GONE
        }

        // Layout HitBox
        webview.setOnTouchListener { _, _ ->
            if (toolsBarExtendableBackground.visibility == View.VISIBLE) expandToolBar()
            if (urlEditText.hasFocus() && imm.isAcceptingText) closeKeyboard()
            false
        }

        // Setup favicon
        favicon?.setOnClickListener {
            val popupMenu = PopupMenu(this, favicon!!)
            val menu = popupMenu.menu
            if (webview.visibility == View.GONE) menu.add(R.string.start_page).isEnabled = false
            else menu.add(webview.title).isEnabled = false

            menu.add(R.string.copy_title)
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
            val dialog = MaterialAlertDialogBuilder(this@BrowserActivity)
            val binding: DialogHitTestTitleBinding =
                DialogHitTestTitleBinding.inflate(LayoutInflater.from(this@BrowserActivity))

            // Custom Title
            val mView = binding.root

            binding.title.text = webview.title
            binding.url.text = Uri.parse(webview.url).host

            val icon = binding.icon
            val favicon = webview.currentFavicon
            if (favicon == null) icon.setImageResource(R.drawable.default_favicon)
            else icon.setImageBitmap(favicon)

            // SSL information
            val message = if (cert != null) {
                val issuedTo = cert.issuedTo
                val issuedBy = cert.issuedBy
                resources.getString(
                    R.string.ssl_info_dialog_content,
                    issuedTo.cName, issuedTo.oName, issuedTo.uName,
                    issuedBy.cName, issuedBy.oName, issuedBy.uName,
                    DateFormat.getDateTimeInstance()
                        .format(cert.validNotBeforeDate),
                    DateFormat.getDateTimeInstance().format(cert.validNotAfterDate)
                )
            } else if (sslState == SslState.SEARCH) {
                resources.getString(R.string.address_bar_hint)
            } else {
                resources.getString(R.string.ssl_info_dialog_content_nocert)
            }

            dialog.setCustomTitle(mView)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.copy_title) { _: DialogInterface?, _: Int ->
                    CommonUtils.copyClipboard(this@BrowserActivity, webview.title)
                }
                .create().show()
        }

        // Setup Url EditText box
        urlEditText.setOnEditorActionListener(
            OnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == KeyEvent.ACTION_DOWN) {
                    webview.loadUrl(urlEditText.text.toString())
                    closeKeyboard()
                    return@OnEditorActionListener true
                }
                false
            })
        urlEditText.setOnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                if (urlEditText.text.toString() != webview.url) urlEditText.setText(webview.url)
                urlEditText.dropDownHeight = 0
            } else {
                urlEditText.dropDownHeight = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
        urlEditText.setOnClickListener {
            if (toolsBarExtendableBackground.visibility == View.VISIBLE) expandToolBar()
        }
        urlEditText.setOnTouchListener(SwipeController(SwipeController.DIRECTION_SWIPE_DOWN) {
            sslLock.performClick()
        })
        urlEditText.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, mView: View, _: Int, _: Long ->
                webview.loadUrl((mView.findViewById<View>(android.R.id.text1) as AppCompatTextView)
                    .text.toString())
                closeKeyboard()
            }
        urlEditText.setAdapter(SuggestionAdapter(this))

        // Setup the up most fab (currently for reload)
        upRightFab.setOnClickListener {
            if (progressBar.progress > 0) webview.stopLoading()
            if (progressBar.progress == 0) webview.reload()
        }

        // For legacy compatibility
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
            progressBar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            sslLock.bringToFront()
            toolsBarExtendableBackground.bringToFront()
        }

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
                if (webview.title.isNullOrBlank() || webview.url.isBlank()) return false

                // Show dialog for selecting modes
                val dialog = MaterialAlertDialogBuilder(this)
                dialog.setTitle(R.string.toolbar_expandable_app_shortcut)

                // TODO: Export as proper list
                val arrayAdapter = StringResAdapter(this)
                arrayAdapter.add(R.string.toolbar_expandable_shortcuts_menu_browser)
                arrayAdapter.add(R.string.toolbar_expandable_shortcuts_menu_custom_tabs)
                arrayAdapter.add(R.string.toolbar_expandable_shortcuts_menu_webapp)
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
                if (url.isBlank() || PrivilegedPages.isPrivilegedPage(url)) return false

                val icon = favicon!!.drawable
                val title = webview.title

                CoroutineScope(Dispatchers.IO).launch {
                    val iconHash = if (icon is BitmapDrawable) iconHashClient.save(icon.bitmap) else null
                    favClient.insert(Broha(iconHash, title, url))
                }
                CommonUtils.showMessage(this, R.string.save_successful)
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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return false

                // Bail out for certain URLs
                if (webview.url.isBlank()) return false

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
                val binding: DialogUaEditBinding = DialogUaEditBinding.inflate(layoutInflater)
                val mView = binding.root
                val message = binding.message
                val customUserAgent = binding.edittext
                val deskMode = binding.deskMode

                message.text = resources.getString(R.string.current_user_agent,
                    webview.webSettings.userAgentString)
                deskMode.isChecked = currentCustomUAWideView
                val dialog = MaterialAlertDialogBuilder(this)
                dialog.setTitle(R.string.customUA)
                    .setView(mView)
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
            }

            R.drawable.share -> CommonUtils.copyClipboard(this, webview.url)

            R.drawable.code -> {
                if (webview.consoleLogging) {
                    MaterialAlertDialogBuilder(this).setTitle(R.string.dialog_console_title)
                        .setMessage(webview.consoleMessages) // TODO: Make it dynamically update
                        .setPositiveButton(android.R.string.ok, null)
                        .setNeutralButton(R.string.clear) { _: DialogInterface?, _: Int ->
                            webview.consoleMessages.clear()
                        }
                        .setNegativeButton(R.string.disable) { _: DialogInterface?, _: Int ->
                            webview.consoleLogging = false
                        }
                        .create().show()
                } else {
                    CommonUtils.showMessage(this, R.string.toast_console_enabled)
                    webview.consoleLogging = true
                }
            }
        }
    }

    fun expandToolBar() {
        val viewVisible: Boolean = toolsBarExtendableBackground.visibility == View.VISIBLE
        val transition: Transition = Slide(Gravity.BOTTOM)
        transition.duration = resources.getInteger(R.integer.anim_expandable_speed) *
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    Settings.Global.getFloat(contentResolver,
                        Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
                else 1.0f).toLong()
        transition.addTarget(R.id.toolsBarExtendableBackground)
        TransitionManager.beginDelayedTransition(toolsBarExtendableBackground, transition)
        toolsBarExtendableBackground.visibility = if (viewVisible) View.GONE else View.VISIBLE
    }

    override fun onPageLoadProgressChanged(progress: Int) {
        super.onPageLoadProgressChanged(progress)
        if (progress == -1) upRightFab.setImageResource(R.drawable.stop)
        if (progress == 0) upRightFab.setImageResource(R.drawable.refresh)
    }

    private fun closeKeyboard() {
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        urlEditText.clearFocus()
        webview.requestFocus()
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
    }

    override fun onSslCertificateUpdated() {
        // Startpage
        if (webview.getRealUrl() == ExportedUrls.actualStartUrl) {
            sslState = SslState.SEARCH
            sslLock.setImageResource(R.drawable.search)
            return
        }

        // All the other pages
        if (webview.certificate == null) {
            sslState = SslState.NONE
            sslLock.setImageResource(R.drawable.warning)
        } else if (sslState == SslState.ERROR) { // State error is set before SECURE
            sslErrorHost = Uri.parse(webview.url).host!!
            sslState = SslState.NONE
        } else {
            sslState = SslState.SECURE
            sslLock.setImageResource(R.drawable.lock)
        }
    }

    override fun onSslErrorProceed() {
        sslState = SslState.ERROR
        sslLock.setImageResource(R.drawable.warning)
        sslLock.isClickable = true
    }

    override fun onStartPageEditTextPressed() {
        urlEditText.requestFocus()
        imm.showSoftInput(urlEditText, InputMethodManager.SHOW_FORCED)
    }

    class ItemsAdapter(mainActivity: BrowserActivity, itemsList: List<Int>) :
        RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
        private lateinit var binding: TemplateIconItemBinding
        private val mBrowserActivity: WeakReference<BrowserActivity> = WeakReference(mainActivity)
        private val mItemsList: WeakReference<List<Int>> = WeakReference(itemsList)

        class ViewHolder(binding: TemplateIconItemBinding) : RecyclerView.ViewHolder(binding.root) {
            val mImageView: AppCompatImageView = binding.imageView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            binding = TemplateIconItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
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
        private lateinit var binding: TemplateIconDescriptionItemBinding
        private val mBrowserActivity: WeakReference<BrowserActivity> = WeakReference(mainActivity)
        private val mItemsList: WeakReference<List<Int>> = WeakReference(itemsList)
        private val mDescriptionList: WeakReference<List<Int>> = WeakReference(descriptionList)

        class ViewHolder(binding: TemplateIconDescriptionItemBinding)
            : RecyclerView.ViewHolder(binding.root) {
            val mItemBox: LinearLayoutCompat = binding.root
            val mImageView: AppCompatImageView = binding.imageView
            val mTextView: AppCompatTextView = binding.textView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            binding = TemplateIconDescriptionItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
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