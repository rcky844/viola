package tipz.browservio.urls;

import static tipz.browservio.utils.UrlUtils.composeSearchUrl;

import tipz.browservio.utils.BrowservioBasicUtil;

public class SearchEngineEntries {
    public final static String google = "https://www.google.com/%s";
    public final static String googleSearchSuffix = "search?q=";
    public final static String baidu = "https://www.baidu.com/%s";
    public final static String baiduSearchSuffix = "s?wd=";
    public final static String duck = "https://www.duckduckgo.com/%s";
    public final static String duckSearchSuffix = "?q=";
    public final static String bing = "https://www.bing.com/%s";
    public final static String bingSearchSuffix = googleSearchSuffix;
    public final static String yahoo = "https://search.yahoo.com/%s";
    public final static String yahooSearchSuffix = "search?p=";
    public final static String ecosia = "https://www.ecosia.org/%s";
    public final static String ecosiaSearchSuffix = googleSearchSuffix;
    public final static String yandex = "https://yandex.com/%s";
    public final static String yandexSearchSuffix = "search/?text=";

    public static String getSearchEngineUrl(String homeAdd, String searchSuffix) {
        return composeSearchUrl(searchSuffix, homeAdd, "%s");
    }

    public static String getHomepageUrl(String homeAdd) {
        return composeSearchUrl(BrowservioBasicUtil.EMPTY_STRING, homeAdd, "%s");
    }
}
