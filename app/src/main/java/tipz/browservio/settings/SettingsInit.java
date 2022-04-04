package tipz.browservio.settings;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;
import static tipz.browservio.utils.urls.SearchEngineEntries.getHomepageUrl;
import static tipz.browservio.utils.urls.SearchEngineEntries.getSearchEngineUrl;

import android.content.Context;

import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.urls.SearchEngineEntries;

public class SettingsInit {
    public SettingsInit(Context mContext) {
        boolean isEqualToOneFirstLaunch = CommonUtils.isIntStrOne(SettingsUtils.getPref(browservio_saver(mContext), SettingsKeys.isFirstLaunch));
        SettingsUtils.checkIfEmpty(browservio_saver(mContext),
                new String[]{SettingsKeys.defaultHomePage, SettingsKeys.defaultHomePageId, SettingsKeys.defaultSearch, SettingsKeys.defaultSearchId, SettingsKeys.defaultSuggestions, SettingsKeys.defaultSuggestionsId},
                SettingsUtils.doesNotHaveGoogle ? new Object[]{getHomepageUrl(SearchEngineEntries.baidu), 1, getSearchEngineUrl(SearchEngineEntries.baidu, SearchEngineEntries.baiduSearchSuffix), 1, SearchEngineEntries.bingSearchSuggestionsUrl, 1}
                        : new Object[]{getHomepageUrl(SearchEngineEntries.google), 0, getSearchEngineUrl(SearchEngineEntries.google, SearchEngineEntries.googleSearchSuffix), 0, SearchEngineEntries.googleSearchSuggestionsUrl, 0}, isEqualToOneFirstLaunch);
        SettingsUtils.checkIfEmpty(browservio_saver(mContext),
                new String[]{SettingsKeys.isJavaScriptEnabled, SettingsKeys.enableAdBlock, SettingsKeys.sendDNT, SettingsKeys.showFavicon, SettingsKeys.themeId, SettingsKeys.updateTesting, SettingsKeys.isFirstLaunch},
                new Object[]{"1", 0, "0", "1", 0, 0, "0"}, isEqualToOneFirstLaunch);
    }
}
