package tipz.browservio.utils;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import tipz.browservio.R;

public class CommonUtils {
    public static final String EMPTY_STRING = "";

    public static String LINE_SEPARATOR() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return System.getProperty("line.separator");
        } else {
            return System.lineSeparator();
        }
    }

    /**
     * Show Toast
     *
     * @param s is supplied for what to show
     */
    public static void showMessage(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    /**
     * Copy to Clipboard
     *
     * @param s string to copy
     */
    public static void copyClipboard(Context context, String s) {
        ((ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", s));
        showMessage(context, context.getResources().getString(R.string.copied_clipboard));
    }

    /**
     * Module to check if app is installed
     * <p>
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
        if (obj instanceof String)
            return obj.equals("1");
        else
            return obj.equals(1);
    }
}