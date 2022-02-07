package tipz.browservio.sharedprefs;

import static tipz.browservio.searchengines.SearchEngineEntries.getHomepageUrl;
import static tipz.browservio.searchengines.SearchEngineEntries.getSearchEngineUrl;
import static tipz.browservio.sharedprefs.utils.BrowservioSaverUtils.browservio_saver;

import android.content.Context;

import tipz.browservio.searchengines.SearchEngineEntries;
import tipz.browservio.sharedprefs.utils.BrowservioSaverUtils;
import tipz.browservio.utils.BrowservioBasicUtil;

public class FirstTimeInit {
    public FirstTimeInit(Context mContext) {
        boolean isEqualToOneFirstLaunch = BrowservioBasicUtil.isIntStrOne(BrowservioSaverUtils.getPref(browservio_saver(mContext), AllPrefs.isFirstLaunch));
        BrowservioSaverUtils.checkIfEmpty(browservio_saver(mContext), AllPrefs.isJavaScriptEnabled, "1", isEqualToOneFirstLaunch);
        BrowservioSaverUtils.checkIfEmpty(browservio_saver(mContext), AllPrefs.defaultHomePage, getHomepageUrl(SearchEngineEntries.google), isEqualToOneFirstLaunch);
        BrowservioSaverUtils.checkIfEmpty(browservio_saver(mContext), AllPrefs.defaultHomePageId, 0, isEqualToOneFirstLaunch);
        BrowservioSaverUtils.checkIfEmpty(browservio_saver(mContext), AllPrefs.defaultSearch, getSearchEngineUrl(SearchEngineEntries.google, SearchEngineEntries.googleSearchSuffix), isEqualToOneFirstLaunch);
        BrowservioSaverUtils.checkIfEmpty(browservio_saver(mContext), AllPrefs.defaultSearchId, 0, isEqualToOneFirstLaunch);
        BrowservioSaverUtils.checkIfEmpty(browservio_saver(mContext), AllPrefs.showFavicon, "1", isEqualToOneFirstLaunch);
        BrowservioSaverUtils.checkIfEmpty(browservio_saver(mContext), AllPrefs.showZoomKeys, "0", isEqualToOneFirstLaunch);
        BrowservioSaverUtils.checkIfEmpty(browservio_saver(mContext), AllPrefs.showCustomError, "1", isEqualToOneFirstLaunch);
        BrowservioSaverUtils.checkIfEmpty(browservio_saver(mContext), AllPrefs.enableSuggestions, 1, isEqualToOneFirstLaunch);
        BrowservioSaverUtils.checkIfEmpty(browservio_saver(mContext), AllPrefs.isFirstLaunch, "0", isEqualToOneFirstLaunch);
    }
}
