package tipz.browservio.settings;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;
import static tipz.browservio.search.SearchEngineEntries.getHomepageUrl;
import static tipz.browservio.search.SearchEngineEntries.getSearchEngineUrl;

import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.search.SearchEngineEntries;

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
            SettingsUtils.setPref(pref, SettingsKeys.defaultHomePage,
                    getHomepageUrl(SearchEngineEntries.baseSearch[7]));
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultHomePageId, 7);
            SettingsUtils.setPref(pref, SettingsKeys.defaultSearch,
                    getSearchEngineUrl(SearchEngineEntries.baseSearch[7],
                            SearchEngineEntries.searchSuffix[7]));
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSearchId, 7);
            SettingsUtils.setPref(pref, SettingsKeys.defaultSuggestions,
                    SearchEngineEntries.searchSuggestionsUrl[6]);
            SettingsUtils.setPrefNum(pref, SettingsKeys.defaultSuggestionsId, 6);
            SettingsUtils.setPrefNum(pref, SettingsKeys.isJavaScriptEnabled, 1);
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableAdBlock, 0);
            SettingsUtils.setPrefNum(pref, SettingsKeys.enableSwipeRefresh, 1);
            SettingsUtils.setPrefNum(pref, SettingsKeys.sendDNT, 0);
            SettingsUtils.setPrefNum(pref, SettingsKeys.showFavicon, 1);
            SettingsUtils.setPrefNum(pref, SettingsKeys.themeId, 0);
        } else {
            protoVer0To1();
        }
        SettingsUtils.setPrefNum(pref, SettingsKeys.protocolVersion, 1); /* CURRENT_PROTOCOL_VERSION */
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
}
