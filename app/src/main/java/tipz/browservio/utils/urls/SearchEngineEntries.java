package tipz.browservio.utils.urls;

import static tipz.browservio.utils.UrlUtils.composeSearchUrl;

import tipz.browservio.utils.CommonUtils;

public class SearchEngineEntries {
    public final static String google = "https://www.google.com/%s";
    public final static String googleSearchSuffix = "search?q=";
    public final static String googleSearchSuggestionsUrl = "http://suggestqueries.google.com/complete/search?client=android&oe=utf8&ie=utf8&q=%s";
    public final static String baidu = "https://www.baidu.com/%s";
    public final static String baiduSearchSuffix = "s?wd=";
    public final static String baiduSearchSuggestionsUrl = "http://suggestion.baidu.com/su?ie=UTF-8&wd=%s&action=opensearch";
    public final static String duck = "https://www.duckduckgo.com/%s";
    public final static String duckSearchSuffix = "?q=";
    public final static String bing = "https://www.bing.com/%s";
    public final static String bingSearchSuffix = googleSearchSuffix;
    public final static String bingSearchSuggestionsUrl = "https://api.bing.com/osjson.aspx?query=%s";
    public final static String yahoo = "https://search.yahoo.com/%s";
    public final static String yahooSearchSuffix = "search?p=";
    public final static String yahooSearchSuggestionsUrl = "https://sugg.search.yahoo.net/sg/?output=fxjson&command=%s";
    public final static String ecosia = "https://www.ecosia.org/%s";
    public final static String ecosiaSearchSuffix = googleSearchSuffix;
    public final static String yandex = "https://yandex.com/%s";
    public final static String yandexSearchSuffix = "search/?text=";

    public static String getSearchEngineUrl(String homeAdd, String searchSuffix) {
        return composeSearchUrl(searchSuffix, homeAdd, "%s");
    }

    public static String getHomepageUrl(String homeAdd) {
        return composeSearchUrl(CommonUtils.EMPTY_STRING, homeAdd, "%s");
    }

    public static String getSuggestionsUrl(String homeAdd, String suggestions) {
        return composeSearchUrl(suggestions, homeAdd, "%s");
    }
}
