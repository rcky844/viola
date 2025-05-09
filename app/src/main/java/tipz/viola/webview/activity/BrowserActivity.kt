// Copyright (c) 2020-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.provider.MediaStore
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.ConsoleMessage
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tipz.viola.LauncherActivity
import tipz.viola.ListInterfaceActivity
import tipz.viola.R
import tipz.viola.database.Broha
import tipz.viola.database.instances.FavClient
import tipz.viola.database.instances.IconHashClient
import tipz.viola.databinding.ActivityMainBinding
import tipz.viola.databinding.DialogEditTextBinding
import tipz.viola.databinding.DialogHitTestTitleBinding
import tipz.viola.databinding.DialogTranslateBinding
import tipz.viola.download.DownloadActivity
import tipz.viola.ext.copyClipboard
import tipz.viola.ext.dpToPx
import tipz.viola.ext.getMinTouchTargetSize
import tipz.viola.ext.getOnSurfaceColor
import tipz.viola.ext.getSelectableItemBackground
import tipz.viola.ext.shareUrl
import tipz.viola.ext.showMessage
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.activity.SettingsActivity
import tipz.viola.utils.UpdateService
import tipz.viola.webview.VWebView
import tipz.viola.webview.VWebViewActivity
import tipz.viola.webview.activity.components.AddressBarView
import tipz.viola.webview.activity.components.ExpandableToolbarView
import tipz.viola.webview.activity.components.FavIconView
import tipz.viola.webview.activity.components.FindInPageView
import tipz.viola.webview.activity.components.FullscreenFloatingActionButton
import tipz.viola.webview.activity.components.LocalNtpPageView
import tipz.viola.webview.activity.components.PopupMaterialAlertDialogBuilder
import tipz.viola.webview.activity.components.ToolbarView
import tipz.viola.webview.pages.PrivilegedPages
import tipz.viola.webview.pages.ProjectUrls
import tipz.viola.widget.PropertyDisplayView
import tipz.viola.widget.StringResAdapter
import java.text.DateFormat


@Suppress("DEPRECATION")
class BrowserActivity : VWebViewActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var upRightFab: AppCompatImageView
    private var currentUserAgentState = VWebView.UserAgentMode.MOBILE
    private var currentCustomUserAgent: String? = null
    private var currentCustomUAWideView = false
    private lateinit var favClient: FavClient
    private lateinit var iconHashClient: IconHashClient
    private lateinit var localNtpPageView: LocalNtpPageView
    private lateinit var toolbarView: ToolbarView
    private lateinit var findInPageView: FindInPageView
    private lateinit var expandableToolbarView: ExpandableToolbarView
    private lateinit var favicon: FavIconView
    private lateinit var addressBar: AddressBarView
    private lateinit var urlEditText: MaterialAutoCompleteTextView
    private lateinit var sslLock: AppCompatImageView
    private lateinit var fullscreenFab: FullscreenFloatingActionButton
    private var consoleMessageTextView: TextView? = null
    var viewMode: Int = 0
    private var sslState: SslState = SslState.NONE
    private var sslErrorHost: String = ""
    private var setFabHiddenViews = false
    private lateinit var imm: InputMethodManager

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
        localNtpPageView = binding.localNtpPage
        toolbarView = binding.toolbarView
        findInPageView = binding.findInPageView
        upRightFab = binding.upRightFab
        addressBar = binding.addressBar
        favicon = binding.favicon
        urlEditText = addressBar.textView
        sslLock = addressBar.sslLock
        progressBar = binding.webviewProgressBar
        swipeRefreshLayout = binding.layoutWebview.swipe
        webview = binding.layoutWebview.webview
        fullscreenFab = binding.fullscreenFab

        // Broha Clients
        favClient = FavClient(this)
        iconHashClient = IconHashClient(this)

        // Miscellaneous
        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        // Start update service
        UpdateService(this, true)

        // Setup toolbar
        toolbarView = binding.toolbarView
        toolbarView.activity = this
        toolbarView.init()

        // Setup toolbar expandable
        expandableToolbarView = binding.expandableToolbarView

        // Layout HitBox
        webview.setOnTouchListener { _, _ ->
            if (expandableToolbarView.visibility == View.VISIBLE)
                expandableToolbarView.expandToolBar()
            if (urlEditText.hasFocus() && imm.isAcceptingText) closeKeyboard()
            false
        }

        // Setup favicon
        favicon.setOnClickListener {
            // Link up with SSL Lock dialog instead
            sslLock.performClick()
        }

        // Setup SSL Lock
        sslLock.setOnClickListener {
            val cert = webview.certificate
            val binding: DialogHitTestTitleBinding =
                DialogHitTestTitleBinding.inflate(LayoutInflater.from(this)).apply {
                    title.apply {
                        text = webview.title
                        setOnLongClickListener {
                            copyClipboard(webview.title)
                            true
                        }
                    }
                    url.text = Uri.parse(webview.url).host
                    this.icon.apply {
                        webview.faviconExt.takeUnless { it == null }?.let {
                            setImageBitmap(it)
                        } ?: setImageResource(R.drawable.default_favicon)
                    }
                }
            val titleView = binding.root

            // SSL information
            val messageView = if (cert != null) {
                val issuedTo = cert.issuedTo
                val issuedBy = cert.issuedBy

                val scrollView = NestedScrollView(this)
                scrollView.addView(PropertyDisplayView(this).apply {
                    property = arrayListOf(
                        arrayOf(R.string.ssl_info_dialog_issued_to),
                        arrayOf(R.string.ssl_info_dialog_common_name, issuedTo.cName),
                        arrayOf(R.string.ssl_info_dialog_organization, issuedTo.oName),
                        arrayOf(R.string.ssl_info_dialog_organization_unit, issuedTo.uName),
                        arrayOf(R.string.ssl_info_dialog_issued_by),
                        arrayOf(R.string.ssl_info_dialog_common_name, issuedBy.cName),
                        arrayOf(R.string.ssl_info_dialog_organization, issuedBy.oName),
                        arrayOf(R.string.ssl_info_dialog_organization_unit, issuedBy.uName),
                        arrayOf(R.string.ssl_info_dialog_validity_period),
                        arrayOf(R.string.ssl_info_dialog_issued_on,
                            DateFormat.getDateTimeInstance().format(cert.validNotBeforeDate)),
                        arrayOf(R.string.ssl_info_dialog_expires_on,
                            DateFormat.getDateTimeInstance().format(cert.validNotAfterDate)),
                    )
                })

                scrollView // Return
            } else if (sslState == SslState.SEARCH) {
                TextView(this).apply {
                    setText(R.string.address_bar_hint)
                }
            } else {
                TextView(this).apply {
                    setText(R.string.ssl_info_dialog_content_nocert)
                }
            }
            messageView.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(12))

            PopupMaterialAlertDialogBuilder(this, Gravity.TOP)
                .setCustomTitle(titleView)
                .setView(messageView)
                .create().show()
        }

        // Setup Url EditText box
        urlEditText.run {
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_GO, KeyEvent.ACTION_DOWN -> {
                        webview.loadUrl(urlEditText.text.toString())
                        closeKeyboard()
                        return@setOnEditorActionListener true
                    }
                }
                false
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    dropDownHeight = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    webview.url.takeIf { it != text.toString() }.let { setText(it) }
                    dropDownHeight = 0
                }
            }

            setOnClickListener {
                if (expandableToolbarView.visibility == View.VISIBLE)
                    expandableToolbarView.expandToolBar()
            }

            setOnItemClickListener { _, v, _, _ ->
                webview.loadUrl(v.findViewById<AppCompatTextView>(android.R.id.text1).text.toString())
                closeKeyboard()
            }
        }

        // Setup the up most fab (currently for reload)
        upRightFab.setOnClickListener {
            if (progressBar.progress > 0) webview.stopLoading()
            if (progressBar.progress == 0) webview.reload()
        }

        // Setup find in page
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            findInPageView.activity = this
            webview.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
                findInPageView.searchPositionInfo = Pair(activeMatchOrdinal, numberOfMatches)
            }
            findInPageView.onStartSearchCallback = { webview.findAllAsync(it) }
            findInPageView.onClearSearchCallback = { webview.clearMatches() }
            findInPageView.onSearchPositionChangeCallback = { webview.findNext(it) }
        }

        // For legacy compatibility
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2)
            progressBar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            expandableToolbarView.bringToFront()
        }

        // Set-up local new tab page
        localNtpPageView.setRealSearchBar(addressBar)
        localNtpPageView.involvedView = mutableListOf(
            ViewVisibility().apply {
                this.view = appbar
                isEnabledCallback = {
                    !fullscreenFab.isFullscreen
                }
            }
        )

        // Finally, load homepage
        intent.data.takeUnless { it == null }?.let {
            webview.loadUrl(it.toString())
        } ?: webview.loadHomepage()
    }

    // https://stackoverflow.com/a/57840629/10866268
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    override fun doSettingsCheck() {
        super.doSettingsCheck()
        favicon.updateIsDisplayed()
        addressBar.doSettingsCheck()

        val reverseAddressBar = settingsPreference.getInt(SettingsKeys.reverseAddressBar)
        if (reverseAddressBar != viewMode) {
            appbar.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = when (reverseAddressBar) {
                    1 -> R.id.toolbarView
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
            findInPageView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomToTop = when (reverseAddressBar) {
                    1 -> R.id.toolbarView
                    else -> ConstraintSet.UNSET
                }
                topToBottom = when (reverseAddressBar) {
                    0 -> R.id.appbar
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
            toolbarView.updateLayoutParams<ConstraintLayout.LayoutParams> {
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

        // Start Page Wallpaper
        settingsPreference.getString(SettingsKeys.startPageWallpaper).takeUnless { it.isEmpty() }?.let {
            try {
                localNtpPageView.setBackgroundDrawable(
                    BitmapDrawable(
                        resources,
                        MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(it))
                    )
                )
            } catch (_: SecurityException) {
                localNtpPageView.setBackgroundResource(0)
                settingsPreference.setString(SettingsKeys.startPageWallpaper, "")
            }
        } ?: run {
            localNtpPageView.setBackgroundResource(0)
        }

        // History access
        doExpandableToolbarStateCheck(R.drawable.history)
    }

    internal fun doExpandableToolbarStateCheck(@DrawableRes res: Int) {
        when (res) {
            R.drawable.history -> expandableToolbarView.setItemEnabled(res,
                settingsPreference.getIntBool(SettingsKeys.enableHistoryStorage))
            R.drawable.app_shortcut -> expandableToolbarView.setItemEnabled(res,
                !(webview.title.isNullOrBlank() || webview.url.isBlank()))
            R.drawable.favorites_add -> expandableToolbarView.setItemEnabled(res,
                !webview.url.let { it.isBlank() || PrivilegedPages.isPrivilegedPage(it) })
            R.drawable.translate -> expandableToolbarView.setItemEnabled(res,
                !(webview.url.isBlank() || PrivilegedPages.isPrivilegedPage(webview.url)))
        }
    }

    // This function returns true to close ToolBar, and vice versa.
    @SuppressLint("SetTextI18n")
    fun itemSelected(view: AppCompatImageView?, @DrawableRes item: Int): Boolean {
        when (item) {
            R.drawable.arrow_back_alt -> if (webview.canGoBack()) webview.goBack()
            R.drawable.arrow_forward_alt -> if (webview.canGoForward()) webview.goForward()
            R.drawable.refresh -> webview.reload()
            R.drawable.home -> webview.loadHomepage()

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

            R.drawable.share -> shareUrl(webview.url)
            R.drawable.app_shortcut -> { // FIXME: Shortcuts pointing to the same URL does not behave as expected
                // Show dialog for selecting modes
                val dialog = PopupMaterialAlertDialogBuilder(this, Gravity.BOTTOM)
                dialog.setTitle(R.string.toolbar_expandable_app_shortcut)

                // TODO: Export as proper list
                val arrayAdapter = StringResAdapter(this)
                arrayAdapter.add(R.string.shortcuts_menu_browser)
                arrayAdapter.add(R.string.shortcuts_menu_custom_tabs)
                arrayAdapter.add(R.string.shortcuts_menu_webapp)
                dialog.setAdapter(arrayAdapter) { _, which ->
                    val launchIntent = Intent(this, LauncherActivity::class.java)
                        .setData(Uri.parse(webview.url))
                        .setAction(Intent.ACTION_VIEW)
                        .putExtra(LauncherActivity.EXTRA_SHORTCUT_TYPE, which)

                    val drawable = favicon.imageView.drawable

                    val icon = Bitmap.createBitmap(
                        drawable.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )

                    val canvas = Canvas(icon)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)

                    ShortcutManagerCompat.requestPinShortcut(
                        this, ShortcutInfoCompat.Builder(this, webview.title!!)
                            .setShortLabel(webview.title!!)
                            .setIcon(IconCompat.createWithBitmap(icon))
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
                val icon = favicon.imageView.drawable
                val title = webview.title

                CoroutineScope(Dispatchers.IO).launch {
                    val iconHash = if (icon is BitmapDrawable) iconHashClient.save(icon.bitmap) else null
                    favClient.insert(Broha(iconHash, title, webview.url))
                }
                showMessage(R.string.save_successful)
            }

            R.drawable.close -> finish()
            R.drawable.view_stream -> expandableToolbarView.expandToolBar()
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

                val jobName = webview.title ?: (getString(R.string.app_name_display) + " Document")
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
                    fullscreenFab.hiddenViews = mutableListOf(
                        ViewVisibility().apply {
                            this.view = appbar
                            isEnabledCallback = {
                                localNtpPageView.visibility != View.VISIBLE
                            }
                        },
                        ViewVisibility().apply {
                            this.view = toolbarView
                        }
                    )
                    fullscreenFab.activity = this
                    setFabHiddenViews = true
                }
                fullscreenFab.show()
            }

            R.drawable.translate -> {
                val binding: DialogTranslateBinding =
                    DialogTranslateBinding.inflate(layoutInflater)
                val editView = binding.root

                val pageLangEditText = binding.pageLangEditText
                val targetLangEditText = binding.targetLangEditText
                pageLangEditText.setText("auto")

                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.toolbar_expandable_translate)
                    .setView(editView)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val targetLang = targetLangEditText.text.toString()
                        if (targetLang.isBlank()) return@setPositiveButton

                        var pageLang = pageLangEditText.text.toString()
                        if (pageLang.isBlank()) pageLang = "auto"
                        webview.loadUrl("https://translate.google.com/translate?js=n" +
                                "&sl=${pageLang}" +
                                "&tl=${targetLang}" +
                                "&u=${webview.url}")
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show()
            }

            R.drawable.search -> {
                findInPageView.expand(false)
            }
        }
        return true // Close ToolBar if not interrupted
    }

    @SuppressLint("RestrictedApi")
    fun itemLongSelected(view: AppCompatImageView?, @DrawableRes item: Int) {
        when (item) {
            R.drawable.smartphone, R.drawable.desktop, R.drawable.custom -> {
                // Layout
                val binding: DialogEditTextBinding = DialogEditTextBinding.inflate(layoutInflater)
                val uaEditView = binding.root

                // Views
                val customUserAgent = binding.edittext
                val message = PropertyDisplayView(this).apply {
                    setPadding(0, 0, 0, dpToPx(8))
                }
                val deskMode = AppCompatCheckBox(this).apply {
                    layoutParams = LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dpToPx(8)
                    }
                    isSingleLine = true
                    minHeight = dpToPx(48)
                    text = resources.getString(R.string.viewing_mode_wide_viewport_mode)
                }
                uaEditView.addView(message, 0)
                uaEditView.addView(deskMode)

                message.property = arrayListOf(
                    arrayOf(R.string.viewing_mode_current_user_agent, webview.webSettings.userAgentString)
                )
                deskMode.isChecked = currentCustomUAWideView
                val dialog = MaterialAlertDialogBuilder(this)
                dialog.setTitle(R.string.viewing_mode_custom_user_agent)
                    .setView(uaEditView)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
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
                webview.loadHomepage(settingsPreference.getIntBool(SettingsKeys.useWebHomePage))
            }

            R.drawable.share -> copyClipboard(webview.url)

            R.drawable.code -> {
                if (webview.consoleLogging) {
                    MaterialAlertDialogBuilder(this).setTitle(R.string.console_dialog_title)
                        .setView(RelativeLayout(this).apply {
                            val textView = AppCompatTextView(this@BrowserActivity).apply {
                                layoutParams = RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                hint = resources.getString(R.string.console_logging_none)
                                typeface = Typeface.MONOSPACE

                                // Generate console message
                                val builder = StringBuilder()
                                webview.consoleMessages.forEach {
                                    builder.append("${generateLogEntry(it)}\n")
                                }
                                text = builder
                            }
                            consoleMessageTextView = textView

                            val scrollView = ScrollView(context).apply {
                                id = ViewCompat.generateViewId()
                                layoutParams = RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    context.dpToPx(320)
                                )
                                addView(textView)
                            }

                            val editText = AppCompatEditText(this@BrowserActivity)
                            val sendButton = AppCompatImageView(this@BrowserActivity).apply {
                                id = ViewCompat.generateViewId()
                                getMinTouchTargetSize().let {
                                    layoutParams = RelativeLayout.LayoutParams(it, it).apply {
                                        setPadding(context.dpToPx(8))
                                        addRule(RelativeLayout.BELOW, scrollView.id)
                                        addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                    }
                                }
                                setImageResource(R.drawable.arrow_up)
                                setColorFilter(context.getOnSurfaceColor())
                                setOnClickListener {
                                    editText.text.toString().takeUnless { it.isBlank() }?.let {
                                        webview.evaluateJavascript(it)
                                    } ?: return@setOnClickListener
                                }
                                setBackgroundResource(getSelectableItemBackground())
                            }
                            editText.layoutParams = RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                getMinTouchTargetSize()
                            ).apply {
                                addRule(RelativeLayout.BELOW, scrollView.id)
                                addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                addRule(RelativeLayout.LEFT_OF, sendButton.id)
                            }

                            addView(scrollView)
                            addView(editText)
                            addView(sendButton)
                        }, dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(12))
                        .setPositiveButton(android.R.string.ok, null)
                        .setNeutralButton(R.string.clear) { _, _ -> webview.consoleMessages.clear() }
                        .setNegativeButton(R.string.console_logging_disable) { _, _ -> webview.consoleLogging = false }
                        .setOnDismissListener { consoleMessageTextView = null }
                        .create().show()
                } else {
                    showMessage(R.string.console_toast_enabled)
                    webview.consoleLogging = true
                }
            }
        }
    }

    // TODO: Replace with colour coding and proper log level display
    // TODO: Confirm log level mappings
    private fun generateLogEntry(consoleMessage: ConsoleMessage): String {
        val logLevel = when (consoleMessage.messageLevel()) {
            ConsoleMessage.MessageLevel.TIP -> "V"
            ConsoleMessage.MessageLevel.LOG -> "I"
            ConsoleMessage.MessageLevel.WARNING -> "W"
            ConsoleMessage.MessageLevel.ERROR -> "E"
            ConsoleMessage.MessageLevel.DEBUG -> "D"
            else -> "U" /* Unknown */
        }
        return "$logLevel: ${consoleMessage.message()}"
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage) {
        if (consoleMessageTextView != null) {
            val textView = consoleMessageTextView as TextView
            val newString = textView.text.toString() + generateLogEntry(consoleMessage) + "\n"
            textView.text = newString
        }
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
        if (fullscreenFab.isFullscreen) appbar.visibility = View.GONE
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
        if (webview.getRealUrl() == ProjectUrls.actualStartUrl) {
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

    override fun onFaviconUpdated(icon: Bitmap?) {
        super.onFaviconUpdated(icon)
        if (favicon.imageView.drawable is BitmapDrawable) return
        if (icon == null) favicon.setImageResource(R.drawable.default_favicon)
        else favicon.setImageBitmap(icon)
    }

    override fun onPageStateChanged(isLoading: Boolean) {
        favicon.isLoading = isLoading
        if (!isLoading) { /* PAGE_FINISHED */
            doExpandableToolbarStateCheck(R.drawable.app_shortcut)
            doExpandableToolbarStateCheck(R.drawable.favorites_add)
            doExpandableToolbarStateCheck(R.drawable.translate)
        }
    }

    fun checkHomePageVisibility() {
        val isHomePage = webview.getRealUrl() == ProjectUrls.actualStartUrl
        webview.visibility = if (isHomePage) View.GONE else View.VISIBLE
        localNtpPageView.visibility = if (isHomePage) View.VISIBLE else View.GONE
        localNtpPageView.updateVisibility(isHomePage)
    }

    class ViewVisibility {
        lateinit var view: View
        var isEnabledCallback: () -> Boolean = { true }
    }
}