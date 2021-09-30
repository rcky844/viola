package tipz.browservio.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatCheckBox;

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
    public static void updateChkbox(final AppCompatCheckBox chk) {
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

    public static void RotateAlphaAnim(ObjectAnimator anim1, ObjectAnimator anim2, View view1, View view2) {
        anim1.setTarget(view1);
        anim2.setTarget(view2);
        anim1.setPropertyName("rotation");
        anim2.setPropertyName("alpha");
        anim1.setDuration(250);
        anim2.setDuration(250);
        if (view2.getVisibility() == View.VISIBLE) {
            anim1.setFloatValues(0, 180);
            anim2.setFloatValues(1, 0);
            view2.setVisibility(View.GONE);
        } else {
            view2.setVisibility(View.VISIBLE);
            anim1.setFloatValues(180, 0);
            anim2.setFloatValues(0, 1);
        }
        anim1.start();
        anim2.start();
    }

    public static boolean isIntStrOne(Object obj) {
        return obj.equals("1");
    }
}