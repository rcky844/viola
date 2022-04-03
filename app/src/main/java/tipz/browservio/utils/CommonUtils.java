package tipz.browservio.utils;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
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

    public static boolean isIntStrOne(Object obj) {
        return obj.equals(obj instanceof String ? "1" : 1);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}