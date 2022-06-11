package tipz.browservio.settings;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;
import static tipz.browservio.settings.SettingsUtils.doesNotHaveGoogle;
import static tipz.browservio.utils.urls.SearchEngineEntries.getHomepageUrl;
import static tipz.browservio.utils.urls.SearchEngineEntries.getSearchEngineUrl;

import android.content.Context;

import tipz.browservio.utils.urls.SearchEngineEntries;

public class SettingsInit {
    public SettingsInit(Context mContext) {
        // TODO: Switch to Brave entirely
        int searchSuggest = doesNotHaveGoogle ? 1 : 0;

        SettingsUtils.checkIfEmpty(browservio_saver(mContext),
                new String[]{SettingsKeys.defaultHomePage, SettingsKeys.defaultHomePageId, SettingsKeys.defaultSearch, SettingsKeys.defaultSearchId, SettingsKeys.defaultSuggestions, SettingsKeys.defaultSuggestionsId},
                new Object[]{getHomepageUrl(SearchEngineEntries.baseSearch[7]), 7, getSearchEngineUrl(SearchEngineEntries.baseSearch[7], SearchEngineEntries.searchSuffix[7]), 7, SearchEngineEntries.searchSuggestionsUrl[searchSuggest], searchSuggest});
        SettingsUtils.checkIfEmpty(browservio_saver(mContext),
                new String[]{SettingsKeys.isJavaScriptEnabled, SettingsKeys.enableAdBlock, SettingsKeys.sendDNT, SettingsKeys.showFavicon, SettingsKeys.themeId, SettingsKeys.centerActionBar, SettingsKeys.updateTesting},
                new Object[]{"1", 0, "0", "1", 0, 1, 0});
    }
}
