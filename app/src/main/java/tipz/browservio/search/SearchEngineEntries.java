package tipz.browservio.search;

import android.content.SharedPreferences;

import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;
import tipz.browservio.utils.UrlUtils;

public class SearchEngineEntries {
    private static final String queryPlaceholder = "{query}";
    private static final String languagePlaceholder = "{language}";

    private final static EngineObject[] engines = new EngineObject[] {
            new EngineObject().setHomePage("https://www.google.com")
                    .setSearch("https://www.google.com/search?q={query}")
                    .setSuggestion("http://suggestqueries.google.com/complete/search?client=android&oe=utf8&ie=utf8&q={query}&hl={language}"),
            new EngineObject().setHomePage("https://www.baidu.com")
                    .setSearch("https://www.baidu.com/s?wd={query}")
                    .setSuggestion("http://suggestion.baidu.com/su?ie=UTF-8&wd={query}&action=opensearch"),
            new EngineObject().setHomePage("https://www.duckduckgo.com")
                    .setSearch("https://www.duckduckgo.com/?q={query}")
                    .setSuggestion("https://duckduckgo.com/ac/?q={query}&type=list"),
            new EngineObject().setHomePage("https://www.bing.com")
                    .setSearch("https://www.bing.com/search?q={query}")
                    .setSuggestion("https://api.bing.com/osjson.aspx?query={query}&language={language}"),
            new EngineObject().setHomePage("https://search.yahoo.com")
                    .setSearch("https://search.yahoo.com/search?p={query}")
                    .setSuggestion("https://sugg.search.yahoo.net/sg/?output=fxjson&command={query}"),
            new EngineObject().setHomePage("https://www.ecosia.org")
                    .setSearch("https://www.ecosia.org/search?q={query}")
                    .setSuggestion("https://ac.ecosia.org/autocomplete?q={query}&type=list"),
            new EngineObject().setHomePage("https://yandex.com")
                    .setSearch("https://yandex.com/search/?text={query}")
                    .setSuggestion("https://yandex.com/suggest/suggest-ya.cgi?v=4&part={query}"),
            new EngineObject().setHomePage("https://search.brave.com")
                    .setSearch("https://search.brave.com/search?q={query}")
                    .setSuggestion("https://search.brave.com/api/suggest?q={query}"),
            new EngineObject() /* The object for custom URL */
    };

    public static String getHomePageUrl(SharedPreferences pref, int position) {
        String url = engines[position].getHomePage();
        if (url.isEmpty())
            url = SettingsUtils.getPref(pref, SettingsKeys.defaultHomePage);
        return UrlUtils.cve_2017_13274(url);
    }

    public static String getSearchUrl(SharedPreferences pref, int position, String query, String language) {
        String url = engines[position].getSearch();
        if (url.isEmpty())
            url = SettingsUtils.getPref(pref, SettingsKeys.defaultSearch);
        if (query != null)
            url = url.replace(queryPlaceholder, query).replace(languagePlaceholder, language);
        return UrlUtils.cve_2017_13274(url);
    }

    public static String getSuggestionsUrl(SharedPreferences pref, int position, String query, String language) {
        String url = engines[position].getSuggestion();
        if (url.isEmpty())
            url = SettingsUtils.getPref(pref, SettingsKeys.defaultSuggestions);
        if (query != null && language != null)
            url = url.replace(queryPlaceholder, query).replace(languagePlaceholder, language);
        return UrlUtils.cve_2017_13274(url);
    }
}
