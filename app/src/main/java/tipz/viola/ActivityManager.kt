// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola

import android.app.Activity
import java.lang.ref.WeakReference

class ActivityManager private constructor() {
    private var currentActivityWeakRef: WeakReference<Activity>? = null

    var currentActivity: Activity?
        get() {
            if (currentActivityWeakRef != null) {
                return currentActivityWeakRef!!.get()
            }
            return null
        }
        set(activity) {
            currentActivityWeakRef = WeakReference(activity)
        }

    companion object {
        val instance: ActivityManager = ActivityManager()
    }
}