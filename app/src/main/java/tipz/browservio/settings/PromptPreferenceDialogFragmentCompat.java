package tipz.browservio.settings;

import android.os.Bundle;

public class PromptPreferenceDialogFragmentCompat extends MaterialPreferenceDialogFragmentCompat {
    private final PromptPreferenceDialogListener mPromptPreferenceDialogListener;

    public static PromptPreferenceDialogFragmentCompat newInstance(String key, PromptPreferenceDialogListener promptPreferenceDialogListener) {
        final PromptPreferenceDialogFragmentCompat
                fragment = new PromptPreferenceDialogFragmentCompat(promptPreferenceDialogListener);
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    public PromptPreferenceDialogFragmentCompat(PromptPreferenceDialogListener promptPreferenceDialogListener) {
        mPromptPreferenceDialogListener = promptPreferenceDialogListener;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        mPromptPreferenceDialogListener.onDialogClosed(positiveResult);
    }

    public interface PromptPreferenceDialogListener {
        void onDialogClosed(boolean positiveResult);
    }
}
