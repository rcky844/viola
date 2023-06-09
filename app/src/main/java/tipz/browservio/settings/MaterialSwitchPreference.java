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
    private String mPreferenceTag;
    private boolean mNeedReload;

    public MaterialSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        pref = ((Application) getContext().getApplicationContext()).pref;

        // Handle checkbox, don't save preference
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MaterialSwitchPreference);
        mPreferenceTag = a.getString(R.styleable.MaterialSwitchPreference_preferenceTag);
        mNeedReload = a.getBoolean(R.styleable.MaterialSwitchPreference_needReload, false);
        setChecked(CommonUtils.isIntStrOne(SettingsUtils.getPrefNum(pref, a.getString(R.styleable.MaterialSwitchPreference_preferenceTag))));
        setOnPreferenceClickListener(preference -> {
            SettingsUtils.setPrefIntBoolAccBool(pref, mPreferenceTag, isChecked(), false);
            SettingsActivity.SettingsPrefHandler.needReload = mNeedReload;
            return true;
        });
        a.recycle();

        // Use material switch
        setWidgetLayoutResource(R.layout.preference_material_switch);
    }
}
