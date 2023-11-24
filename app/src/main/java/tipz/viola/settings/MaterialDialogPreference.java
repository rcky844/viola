package tipz.viola.settings;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

public class MaterialDialogPreference extends DialogPreference {
    private MaterialPreferenceDialogFragmentCompat.MaterialDialogPreferenceListener mMaterialDialogPreferenceListener;

    public MaterialDialogPreference(@NonNull Context context) {
        super(context, null);
    }

    public MaterialDialogPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMaterialDialogPreferenceListener(MaterialPreferenceDialogFragmentCompat.MaterialDialogPreferenceListener materialDialogPreferenceListener) {
        mMaterialDialogPreferenceListener = materialDialogPreferenceListener;
    }

    public MaterialPreferenceDialogFragmentCompat.MaterialDialogPreferenceListener getMaterialDialogPreferenceListener() {
        return mMaterialDialogPreferenceListener;
    }
}
