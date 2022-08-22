package tipz.browservio;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.webkit.CookieManager;

import androidx.appcompat.app.AppCompatActivity;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.tabbies.BrowserActivity;
import tipz.browservio.tabbies.CustomTabsActivity;
import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.UrlUtils;

public class MainActivity extends AppCompatActivity {
    public final Class<?>[] VioActivityMode = {
            null,                       /* ACTIVITY_DEFAULT */
            BrowserActivity.class,      /* ACTIVITY_BROWSER */
            CustomTabsActivity.class,   /* ACTIVITY_CUSTOM_TABS */
            null,                       /* ACTIVITY_WEB_APP */
    };

    public final String EXTRA_ACTIVITY_MODE = "extra_activity_mode";

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        if (!webViewEnabled()) {
            CommonUtils.showMessage(MainActivity.this, getResources().getString(R.string.no_webview));
            finish();
        }
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM)
                .enabled(true)
                .showErrorDetails(true)
                .showRestartButton(true)
                .logErrorOnRestart(true)
                .trackActivities(true)
                .minTimeBetweenCrashesMs(2000)
                .restartActivity(MainActivity.class)
                .errorActivity(null)
                .apply();

        /*
         * Getting information from intents, either from
         * sharing menu or default browser launch.
         */
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String scheme = intent.getScheme();

        int extraMode = intent.getIntExtra(EXTRA_ACTIVITY_MODE, 0);
        Class<?> classLaunch = VioActivityMode[extraMode];

        if (classLaunch == null) {
            if (intent.hasCategory("android.intent.category.LAUNCHER")
                    || SettingsUtils.getPrefNum(browservio_saver(MainActivity.this),
                        SettingsKeys.useCustomTabs) == 0)
                classLaunch = BrowserActivity.class;
            else
                classLaunch = CustomTabsActivity.class;
        }

        Intent openIntent = new Intent(this, classLaunch);
        Uri uri = null;

        if (Intent.ACTION_SEND.equals(action) /* From share menu */
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) { /* NFC sharing */
            if (type != null) {
                if ("text/plain".equals(type)) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    uri = Uri.parse(UrlUtils.cve_2017_13274(sharedText != null ? sharedText : CommonUtils.EMPTY_STRING));
                }
            }
        } else if (Intent.ACTION_VIEW.equals(action)) { /* From default browser */
            for (String match : UrlUtils.TypeSchemeMatch) {
                if (match.equals(type) || match.equals(scheme)) {
                    uri = getIntent().getData();
                }
            }
        }
        openIntent.setData(uri);
        startActivity(openIntent);
        finish();
    }

    public boolean webViewEnabled() {
        try {
            CookieManager.getInstance();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
