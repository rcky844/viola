package tipz.browservio.settings;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

public class PromptPreference extends DialogPreference {
    private int mPreferenceType;
    private PromptPreferenceDialogFragmentCompat.PromptPreferenceDialogListener mPromptPreferenceDialogListener;

    public PromptPreference(@NonNull Context context) {
        super(context, null);
    }

    public PromptPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPromptPreferenceDialogListener(PromptPreferenceDialogFragmentCompat.PromptPreferenceDialogListener promptPreferenceDialogListener) {
        mPromptPreferenceDialogListener = promptPreferenceDialogListener;
    }

    public PromptPreferenceDialogFragmentCompat.PromptPreferenceDialogListener getPromptPreferenceDialogListener() {
        return mPromptPreferenceDialogListener;
    }
}
