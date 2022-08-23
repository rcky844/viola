package tipz.browservio.settings;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.search.SearchEngineEntries;
import tipz.browservio.utils.CommonUtils;

public class SettingsInit {
    SharedPreferences pref;
    public SettingsInit(Context mContext) {
        pref = browservio_saver(mContext);

        /* A bloopers fix for migrating from old versions */
        if (SettingsUtils.isFirstLaunch(pref) && SettingsUtils.getPrefNum(pref, SettingsKeys.protocolVersion) == 0
                && !SettingsUtils.getPref(pref, SettingsKeys.isJavaScriptEnabled).isEmpty())
            SettingsUtils.setPref(pref, SettingsKeys.isFirstLaunch, "0");

        if (SettingsUtils.isFirstLaunch(pref)) {
            SettingsUtils.setPrefNum(pref, SettingsKeys.centerActionBar, 1);
            SettingsUtils.setPrefNum(pref, SettingsKeys.closeAppAfterDownload, 1);
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultHomePageId, 7);
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSearchId, 7);
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSuggestionsId, 6);
            SettingsUtils.setPrefNum(pref, SettingsKeys.isJavaScriptEnabled, 1);
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableAdBlock, 0);
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableSwipeRefresh, 1);
            SettingsUtils.setPrefNum(pref, SettingsKeys.enforceHttps, 1);
            SettingsUtils.setPrefNum(pref, SettingsKeys.sendDNT, 0);
            SettingsUtils.setPrefNum(pref, SettingsKeys.showFavicon, 1);
            SettingsUtils.setPrefNum(pref, SettingsKeys.themeId, 0);
            SettingsUtils.setPrefNum(pref, SettingsKeys.useCustomTabs, 1);
        } else {
            protoVer0To1();
            //protoVer1To2();
        }
        SettingsUtils.setPrefNum(pref, SettingsKeys.protocolVersion, 1 /* 2 */); /* CURRENT_PROTOCOL_VERSION */
        if (SettingsUtils.isFirstLaunch(pref))
            SettingsUtils.setPref(pref, SettingsKeys.isFirstLaunch, "0");
    }

    private void protoVer0To1() {
        if (SettingsUtils.getPrefNum(pref, SettingsKeys.protocolVersion) == 0) {
            /* 2c15e330: java: urls: Add Brave Search */
            if (SettingsUtils.getPrefNum(pref, SettingsKeys.defaultHomePageId) == 7)
                SettingsUtils.setPrefNum(pref, SettingsKeys.defaultHomePageId, 8);
            if (SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSearchId) == 7)
                SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSearchId, 8);
            /* 8cdfc371: java: settings: Move most settings to integer and add protocol version */
            SettingsUtils.setPrefNum(pref, SettingsKeys.isJavaScriptEnabled,
                    Integer.parseInt(SettingsUtils.getPref(pref, SettingsKeys.isJavaScriptEnabled)));
            SettingsUtils.setPrefNum(pref, SettingsKeys.sendDNT,
                    Integer.parseInt(SettingsUtils.getPref(pref, SettingsKeys.sendDNT)));
            SettingsUtils.setPrefNum(pref, SettingsKeys.showFavicon,
                    Integer.parseInt(SettingsUtils.getPref(pref, SettingsKeys.showFavicon)));
        }
    }

    private void protoVer1To2() {
        if (SettingsUtils.getPrefNum(pref, SettingsKeys.protocolVersion) == 1) {
            /* 4bb92167: java: settings: Allow enabling or disabling pull to refresh */
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableSwipeRefresh, 1);
            /* 1fd6ea58: java: main: Add experimental support for enforcing HTTPS */
            SettingsUtils.setPrefNum(pref, SettingsKeys.enforceHttps, 1);
            /* cc6cb8ea: java: search: Rewrite search engine code */
            if (SettingsUtils.getPrefNum(pref, SettingsKeys.defaultHomePageId) != 8)
                SettingsUtils.setPref(pref, SettingsKeys.defaultHomePage, CommonUtils.EMPTY_STRING);
            if (SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSearchId) != 8)
                SettingsUtils.setPref(pref, SettingsKeys.defaultSearch, CommonUtils.EMPTY_STRING);
            SettingsUtils.setPref(pref, SettingsKeys.defaultSuggestions, CommonUtils.EMPTY_STRING);
            /* 8f2ca067: java: settings: Allow the user to choose if they want Custom Tabs */
            SettingsUtils.setPrefNum(pref, SettingsKeys.useCustomTabs, 1);
            /* 2d6ce244: java: webview: Finish if launched page is a download */
            SettingsUtils.setPrefNum(pref, SettingsKeys.closeAppAfterDownload, 1);
        }
    }
}
