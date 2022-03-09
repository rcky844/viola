package tipz.browservio.settings;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;
import static tipz.browservio.utils.urls.SearchEngineEntries.getHomepageUrl;
import static tipz.browservio.utils.urls.SearchEngineEntries.getSearchEngineUrl;

import android.content.Context;

import tipz.browservio.utils.CommonUtils;
import tipz.browservio.utils.urls.SearchEngineEntries;

public class SettingsInit {
    public SettingsInit(Context mContext) {
        boolean doesNotHaveGoogle = mContext.getResources().getConfiguration().locale.getCountry().equals("CN");
        boolean isEqualToOneFirstLaunch = CommonUtils.isIntStrOne(SettingsUtils.getPref(browservio_saver(mContext), SettingsKeys.isFirstLaunch));
        SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.isJavaScriptEnabled, "1", isEqualToOneFirstLaunch);
        SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.enableAdBlock, 0, isEqualToOneFirstLaunch);
        SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.sendDNT, "0", isEqualToOneFirstLaunch);
        SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.showFavicon, "1", isEqualToOneFirstLaunch);
        SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.updateTesting, 0, isEqualToOneFirstLaunch);

        if (doesNotHaveGoogle) {
            SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.enableSuggestions, 0, isEqualToOneFirstLaunch);
            SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.defaultHomePage, getHomepageUrl(SearchEngineEntries.baidu), isEqualToOneFirstLaunch);
            SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.defaultHomePageId, 1, isEqualToOneFirstLaunch);
            SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.defaultSearch, getSearchEngineUrl(SearchEngineEntries.baidu, SearchEngineEntries.baiduSearchSuffix), isEqualToOneFirstLaunch);
            SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.defaultSearchId, 1, isEqualToOneFirstLaunch);
        } else {
            SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.enableSuggestions, 1, isEqualToOneFirstLaunch);
            SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.defaultHomePage, getHomepageUrl(SearchEngineEntries.google), isEqualToOneFirstLaunch);
            SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.defaultHomePageId, 0, isEqualToOneFirstLaunch);
            SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.defaultSearch, getSearchEngineUrl(SearchEngineEntries.google, SearchEngineEntries.googleSearchSuffix), isEqualToOneFirstLaunch);
            SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.defaultSearchId, 0, isEqualToOneFirstLaunch);
        }
        SettingsUtils.checkIfEmpty(browservio_saver(mContext), SettingsKeys.isFirstLaunch, "0", isEqualToOneFirstLaunch);
    }
}
