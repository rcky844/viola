package tipz.viola.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.SwitchPreferenceCompat
import tipz.viola.Application
import tipz.viola.R

class MaterialSwitchPreference(context: Context, attrs: AttributeSet?) :
    SwitchPreferenceCompat(context, attrs) {
    private val settingsPreference: SettingsSharedPreference = (getContext().applicationContext as Application).settingsPreference!!
    private val mPreferenceTag: String?
    private val mNeedReload: Boolean

    init {
        // Get attrs
        val a = getContext().obtainStyledAttributes(attrs, R.styleable.MaterialSwitchPreference)
        mPreferenceTag = a.getString(R.styleable.MaterialSwitchPreference_preferenceTag)
        mNeedReload = a.getBoolean(R.styleable.MaterialSwitchPreference_needReload, false)
        a.recycle()

        // Handle checkbox
        isChecked = settingsPreference.getIntBool(mPreferenceTag!!)
        onPreferenceClickListener = OnPreferenceClickListener {
            settingsPreference.setIntBool(mPreferenceTag, isChecked)
            SettingsActivity.SettingsPrefHandler.needReload = mNeedReload
            true
        }

        // Use material switch
        widgetLayoutResource = R.layout.preference_material_switch
    }
}