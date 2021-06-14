package tipz.browservio.Utils;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.Toast;

public class BrowservioBasicUtil {
    /**
     * Show Toast
     *
     * @param s is supplied for what to show
     */
    public static void showMessage(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    /**
     * Update CheckBox
     *
     * This module updates a Checkbox to another condition.
     *
     * @param chk is supplied as the CheckBox to update
     */
    public static void updateChkbox(final CheckBox chk) {
        chk.setChecked(!chk.isChecked());
    }
}