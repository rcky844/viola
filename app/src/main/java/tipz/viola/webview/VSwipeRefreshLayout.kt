// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.webview

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

class VSwipeRefreshLayout(
    context: Context, attrs: AttributeSet?
) : SwipeRefreshLayout(context, attrs) {
    private val LOG_TAG = "VSwipeRefreshLayout"

    private lateinit var webview: VWebView
    private var layoutEnabled = true

    fun initialize(webview: VWebView) {
        this.webview = webview
        setOnRefreshListener { webview.reload() }
        doSettingsCheck()
    }

    private fun settingEnabled() = SettingsSharedPreference.instance
        .getIntBool(SettingsKeys.enableSwipeRefresh)

    private fun doSettingsCheck() {
        if (layoutEnabled)
            isEnabled = settingEnabled()
    }

    @Override
    override fun setRefreshing(refreshing: Boolean) {
        super.setRefreshing(refreshing)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return
        webview.evaluateJavascript("""
            function _viola_styleHelper(property, checkVal, expectVal) {
                var propVal = getComputedStyle(document.body).getPropertyValue(property);
                return propVal != "" && Boolean(propVal == checkVal & expectVal);
            }
            _viola_styleHelper('overflow-y', 'hidden', true) || _viola_styleHelper('overscroll-behavior-y', 'auto', false);
        """.trimIndent()
        ) { value: String ->
            val overscroll = value != "true"
            if (!overscroll) Log.d(LOG_TAG, "Webpage does not want to overscroll.")

            layoutEnabled = overscroll
            if (settingEnabled()) MainScope().launch { isEnabled = overscroll }
        }
    }
}