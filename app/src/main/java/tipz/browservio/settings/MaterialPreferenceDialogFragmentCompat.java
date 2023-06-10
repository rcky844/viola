package tipz.browservio.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MaterialPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private final MaterialDialogPreferenceListener mMaterialPreferenceDialogListener;

    /** Which button was clicked. */
    private int mWhichButtonClicked;

    public static MaterialPreferenceDialogFragmentCompat newInstance(String key, MaterialDialogPreferenceListener materialPreferenceDialogListener) {
        final MaterialPreferenceDialogFragmentCompat
                fragment = new MaterialPreferenceDialogFragmentCompat(materialPreferenceDialogListener);
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    public MaterialPreferenceDialogFragmentCompat(MaterialDialogPreferenceListener materialPreferenceDialogListener) {
        mMaterialPreferenceDialogListener = materialPreferenceDialogListener;
    }

    @Override
    public @NonNull Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mWhichButtonClicked = DialogInterface.BUTTON_NEGATIVE;

        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getPreference().getDialogTitle())
                .setIcon(getPreference().getDialogIcon())
                .setPositiveButton(getPreference().getPositiveButtonText(), this)
                .setNegativeButton(getPreference().getNegativeButtonText(), this);

        View contentView = onCreateDialogView(requireContext());
        if (contentView != null) {
            onBindDialogView(contentView);
            builder.setView(contentView);
        } else {
            builder.setMessage(getPreference().getDialogMessage());
        }

        onPrepareDialogBuilder(builder);

        return builder.create();
    }

    @Override
    public void onClick(@NonNull DialogInterface dialog, int which) {
        mWhichButtonClicked = which;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        onDialogClosed(mWhichButtonClicked == DialogInterface.BUTTON_POSITIVE);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        mMaterialPreferenceDialogListener.onDialogClosed(positiveResult);
    }

    public interface MaterialDialogPreferenceListener {
        void onDialogClosed(boolean positiveResult);
    }
}
