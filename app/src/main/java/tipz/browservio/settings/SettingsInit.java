package tipz.browservio.settings;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;
import static tipz.browservio.utils.urls.SearchEngineEntries.getHomepageUrl;
import static tipz.browservio.utils.urls.SearchEngineEntries.getSearchEngineUrl;

import android.content.Context;

import tipz.browservio.utils.urls.SearchEngineEntries;

public class SettingsInit {
    public SettingsInit(Context mContext) {
        SettingsUtils.checkIfEmpty(browservio_saver(mContext),
                new String[]{SettingsKeys.defaultHomePage, SettingsKeys.defaultHomePageId, SettingsKeys.defaultSearch, SettingsKeys.defaultSearchId, SettingsKeys.defaultSuggestions, SettingsKeys.defaultSuggestionsId},
                SettingsUtils.doesNotHaveGoogle ? new Object[]{getHomepageUrl(SearchEngineEntries.baseSearch[1]), 1, getSearchEngineUrl(SearchEngineEntries.baseSearch[1], SearchEngineEntries.searchSuffix[1]), 1, SearchEngineEntries.searchSuggestionsUrl[1], 1}
                        : new Object[]{getHomepageUrl(SearchEngineEntries.baseSearch[0]), 0, getSearchEngineUrl(SearchEngineEntries.baseSearch[0], SearchEngineEntries.searchSuffix[0]), 0, SearchEngineEntries.searchSuggestionsUrl[0], 0});
        SettingsUtils.checkIfEmpty(browservio_saver(mContext),
                new String[]{SettingsKeys.isJavaScriptEnabled, SettingsKeys.enableAdBlock, SettingsKeys.sendDNT, SettingsKeys.showFavicon, SettingsKeys.themeId, SettingsKeys.centerActionBar},
                new Object[]{"1", 0, "0", "1", 0, 1});
    }
}
