// Copyright (c) 2023-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.settings.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import tipz.viola.settings.ui.preference.MaterialPreferenceDialogFragmentCompat.MaterialDialogPreferenceListener

class MaterialDialogPreference : DialogPreference {
    lateinit var materialDialogPreferenceListener: MaterialDialogPreferenceListener

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
}