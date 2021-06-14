package tipz.browservio.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
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

    /**
     * Module to check if app is installed
     *
     * Ref: https://stackoverflow.com/a/41693364/10866268
     *
     * @param uri should be supplied with app uri
     * @return if app is installed or not boolean
     */
    public static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}