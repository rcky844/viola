// Copyright (c) 2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package androidx.multidex

import android.content.Context
import android.os.Build
import android.util.Log

object MultiDex {
    private const val TAG = "MultiDex";

    fun install(context: Context?) {
        Log.i(TAG, "Skip installing application." +
                " SDK ${Build.VERSION.SDK_INT} already supports multidex.");
    }
}
