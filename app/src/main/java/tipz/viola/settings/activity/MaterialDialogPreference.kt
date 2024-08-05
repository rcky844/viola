// Copyright (c) 2023-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.activity

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import tipz.viola.settings.activity.MaterialPreferenceDialogFragmentCompat.MaterialDialogPreferenceListener

class MaterialDialogPreference : DialogPreference {
    var materialDialogPreferenceListener: MaterialDialogPreferenceListener? = null

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
}