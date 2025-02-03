// Copyright (c) 2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.ext

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Activity.askForPermission(permission: Array<String>): Boolean {
    var shouldRequest = false
    permission.forEach {
        if (ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED)
            shouldRequest = true
    }

    if (shouldRequest)
        ActivityCompat.requestPermissions(this, permission, 0)
    return !shouldRequest
}
