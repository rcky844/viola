package tipz.browservio;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;

public class BrowservioActivity extends AppCompatActivity {
    public static SharedPreferences pref;
    public static WindowInsetsControllerCompat windowInsetsController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = ((Application) getApplicationContext()).pref;
        windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
    }

    @Override
    public void onStart() {
        super.onStart();
        doSettingsCheck();
    }

    @CallSuper
    public void doSettingsCheck() {
        // Dark Mode
        darkModeCheck(this);
    }

    public static void darkModeCheck(Context context) {
        // Dark mode
        if (SettingsUtils.getPrefNum(pref, SettingsKeys.themeId) == 0)
            AppCompatDelegate.setDefaultNightMode(Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1 ?
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        else
            AppCompatDelegate.setDefaultNightMode(SettingsUtils.getPrefNum(
                    pref, SettingsKeys.themeId) == 2 ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        windowInsetsController.setAppearanceLightStatusBars(!getDarkMode(context));
        windowInsetsController.setAppearanceLightNavigationBars(!getDarkMode(context));
    }

    public static boolean getDarkMode(Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES;
    }
}
