package tipz.browservio.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;

import tipz.browservio.Application;
import tipz.browservio.R;
import tipz.browservio.utils.CommonUtils;

public class MaterialSwitchPreference extends SwitchPreferenceCompat {
    private final SharedPreferences pref;
    private final String mPreferenceTag;
    private final boolean mNeedReload;

    public MaterialSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        pref = ((Application) getContext().getApplicationContext()).pref;

        // Get attrs
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MaterialSwitchPreference);
        mPreferenceTag = a.getString(R.styleable.MaterialSwitchPreference_preferenceTag);
        mNeedReload = a.getBoolean(R.styleable.MaterialSwitchPreference_needReload, false);
        a.recycle();

        // Handle checkbox
        setChecked(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, mPreferenceTag)));
        setOnPreferenceClickListener(preference -> {
            SettingsUtils.setPrefIntBoolAccBool(pref, mPreferenceTag, isChecked(), false);
            SettingsActivity.SettingsPrefHandler.needReload = mNeedReload;
            return true;
        });

        // Use material switch
        setWidgetLayoutResource(R.layout.preference_material_switch);
    }
}
