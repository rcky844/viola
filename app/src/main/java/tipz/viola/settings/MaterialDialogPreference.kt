package tipz.viola.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import tipz.viola.settings.MaterialPreferenceDialogFragmentCompat.MaterialDialogPreferenceListener

class MaterialDialogPreference : DialogPreference {
    var materialDialogPreferenceListener: MaterialDialogPreferenceListener? = null

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
}