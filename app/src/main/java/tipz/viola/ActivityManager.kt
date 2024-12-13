// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola

import android.app.Activity
import java.lang.ref.WeakReference

class ActivityManager private constructor() {
    private var mCurrentActivityWeakRef: WeakReference<Activity>? = null

    var currentActivity: Activity?
        get() {
            if (mCurrentActivityWeakRef != null) {
                return mCurrentActivityWeakRef!!.get()
            }
            return null
        }
        set(activity) {
            mCurrentActivityWeakRef = WeakReference(activity)
        }

    companion object {
        val instance: ActivityManager = ActivityManager()
    }
}