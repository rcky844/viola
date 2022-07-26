package tipz.browservio.search;

import static tipz.browservio.utils.UrlUtils.composeSearchUrl;

import tipz.browservio.utils.CommonUtils;

public class SearchEngineEntries {
    public final static String[] baseSearch = new String[]{
            "https://www.google.com/%s",
            "https://www.baidu.com/%s",
            "https://www.duckduckgo.com/%s",
            "https://www.bing.com/%s",
            "https://search.yahoo.com/%s",
            "https://www.ecosia.org/%s",
            "https://yandex.com/%s",
            "https://search.brave.com/%s",
    };

    public final static String[] searchSuffix = new String[]{
            "search?q=",
            "s?wd=",
            "?q=",
            "search?q=",
            "search?p=",
            "search?q=",
            "search/?text=",
            "search?q=",
    };

    public final static String[] searchSuggestionsUrl = new String[]{
            "http://suggestqueries.google.com/complete/search?client=android&oe=utf8&ie=utf8&q=%s",
            "http://suggestion.baidu.com/su?ie=UTF-8&wd=%s&action=opensearch",
            "https://api.bing.com/osjson.aspx?query=%s",
            "https://sugg.search.yahoo.net/sg/?output=fxjson&command=%s",
            "https://ac.ecosia.org/autocomplete?q=%s&type=list",
            "https://yandex.com/suggest/suggest-ya.cgi?v=4&part=%s",
            "https://search.brave.com/api/suggest?q=%s",
    };

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
