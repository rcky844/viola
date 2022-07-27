package tipz.browservio.search;

import static tipz.browservio.settings.SettingsUtils.browservio_saver;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import tipz.browservio.settings.SettingsKeys;
import tipz.browservio.settings.SettingsUtils;

/*
    "Inspired" by LineageOS' Jelly
 */

public class SuggestionProvider {
    private static final long INTERVAL_DAY = TimeUnit.DAYS.toSeconds(1);
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private final Context mContext;
    @NonNull
    private final String mEncoding;
    @NonNull
    private final String mLanguage;

    public SuggestionProvider(Context context) {
        mEncoding = getEncoding();
        mLanguage = getLanguage();
        mContext = context;
    }

    @NonNull
    private static String getLanguage() {
        String language = Locale.getDefault().getLanguage();
        if (TextUtils.isEmpty(language))
            language = DEFAULT_LANGUAGE;
        return language;
    }

    @NonNull
    private static String getEncoding() {
        // TODO: Allow changing encoding
        return DEFAULT_ENCODING;
    }

    /**
     * Create a URL for the given query in the given language.
     *
     * @param query    the query that was made.
     * @param language the locale of the user.
     * @return should return a URL that can be fetched using a GET.
     */
    @NonNull
    protected String createQueryUrl(@NonNull String query,
                                    @NonNull String language) {
        SharedPreferences pref = browservio_saver(mContext);
        String suggestionString = SearchEngineEntries.getSuggestionsUrl(SettingsUtils.getPref(
                pref, SettingsKeys.defaultSuggestions), query);
        int suggestionId = SettingsUtils.getPrefNum(pref, SettingsKeys.defaultSuggestionsId);
        if (suggestionId == 0)
            return suggestionString + "&hl=" + language;
        else if (suggestionId == 2)
            return suggestionString + "&language=" + language;
        else
            return suggestionString;
    }

    /**
     * Parse the results of an input stream into a list of {@link String}.
     *
     * @param content  the raw input to parse.
     * @param callback the callback to invoke for each received suggestion
     * @throws Exception throw an exception if anything goes wrong.
     */
    void parseResults(@NonNull String content,
                      @NonNull ResultCallback callback) throws Exception {
        JSONArray respArray = new JSONArray(content);
        JSONArray jsonArray = respArray.getJSONArray(1);
        for (int n = 0, size = jsonArray.length(); n < size; n++) {
            String suggestion = jsonArray.getString(n);
            if (!callback.addResult(suggestion))
                break;
        }
    }

    /**
     * Retrieves the results for a query.
     *
     * @param rawQuery the raw query to retrieve the results for.
     * @return a list of history items for the query.
     */
    @NonNull
    final List<String> fetchResults(@NonNull final String rawQuery) {
        List<String> filter = new ArrayList<>(5);

        String query;
        try {
            query = URLEncoder.encode(rawQuery, mEncoding);
        } catch (UnsupportedEncodingException e) {
            return filter;
        }

        String content = downloadSuggestionsForQuery(query, mLanguage);
        if (content == null) {
            // There are no suggestions for this query, return an empty list.
            return filter;
        }
        try {
            parseResults(content, suggestion -> {
                filter.add(suggestion);
                return filter.size() < 5;
            });
        } catch (Exception ignored) {
        }

        return filter;
    }

    /**
     * This method downloads the search suggestions for the specific query.
     * NOTE: This is a blocking operation, do not fetchResults on the UI thread.
     *
     * @param query the query to get suggestions for
     * @return the cache file containing the suggestions
     */
    @Nullable
    private String downloadSuggestionsForQuery(@NonNull String query,
                                               @NonNull String language) {
        try {
            URL url = new URL(createQueryUrl(query, language));

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty("Cache-Control",
                    "max-age=" + INTERVAL_DAY + ", max-stale=" + INTERVAL_DAY);
            urlConnection.addRequestProperty("Accept-Charset", mEncoding);
            try (InputStream in = new BufferedInputStream(urlConnection.getInputStream())) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, getEncoding(urlConnection)));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    result.append(line);
                return result.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException ignored) {
        }

        return null;
    }

    private String getEncoding(HttpURLConnection connection) {
        String contentEncoding = connection.getContentEncoding();
        if (contentEncoding != null)
            return contentEncoding;

        String contentType = connection.getContentType();
        for (String value : contentType.split(";")) {
            value = value.trim();
            if (value.toLowerCase(Locale.US).startsWith("charset="))
                return value.substring(8);
        }

        return mEncoding;
    }

    interface ResultCallback {
        boolean addResult(String suggestion);
    }
}