package tipz.browservio.settings;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;
import static tipz.browservio.settings.SettingsUtils.doesNotHaveGoogle;
import static tipz.browservio.utils.urls.SearchEngineEntries.getHomepageUrl;
import static tipz.browservio.utils.urls.SearchEngineEntries.getSearchEngineUrl;

import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.utils.urls.SearchEngineEntries;

public class SettingsInit {
    public SettingsInit(Context mContext) {
        SharedPreferences pref = browservio_saver(mContext);

        if (!SettingsUtils.isFirstLaunch(pref)) {
            switch (SettingsUtils.getPrefNum(pref, SettingsKeys.protocolVersion)) {
                case 0:
                    // TODO: finalize
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
                case 1:
                    break;
            }
        }
        SettingsUtils.setPrefNum(pref, SettingsKeys.protocolVersion, 1); /* CURRENT_PROTOCOL_VERSION */

        // TODO: Switch to Brave entirely
        int searchSuggest = doesNotHaveGoogle ? 1 : 0;

        SettingsUtils.checkIfEmpty(pref,
                new String[]{SettingsKeys.defaultHomePage,
                        SettingsKeys.defaultHomePageId,
                        SettingsKeys.defaultSearch,
                        SettingsKeys.defaultSearchId,
                        SettingsKeys.defaultSuggestions,
                        SettingsKeys.defaultSuggestionsId,
                        SettingsKeys.isJavaScriptEnabled,
                        SettingsKeys.enableAdBlock,
                        SettingsKeys.sendDNT,
                        SettingsKeys.showFavicon,
                        SettingsKeys.themeId,
                        SettingsKeys.centerActionBar},
                new Object[]{getHomepageUrl(SearchEngineEntries.baseSearch[7]), 7,
                        getSearchEngineUrl(SearchEngineEntries.baseSearch[7], SearchEngineEntries.searchSuffix[7]), 7,
                        SearchEngineEntries.searchSuggestionsUrl[searchSuggest], searchSuggest,
                        1, 0, 0, 1, 0, 1});
    }
}
