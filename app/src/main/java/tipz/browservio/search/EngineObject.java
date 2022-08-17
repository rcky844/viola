package tipz.browservio.search;

import androidx.annotation.NonNull;

import tipz.browservio.utils.CommonUtils;

public class EngineObject {
    private String mHomePage = CommonUtils.EMPTY_STRING;
    private String mSearch = CommonUtils.EMPTY_STRING;
    private String mSuggestion = CommonUtils.EMPTY_STRING;

    public EngineObject setHomePage(@NonNull String homePage) {
        mHomePage = homePage;
        return this;
    }

    @NonNull
    public String getHomePage() {
        return mHomePage;
    }

    public EngineObject setSearch(@NonNull String search) {
        mSearch = search;
        return this;
    }

    @NonNull
    public String getSearch() {
        return mSearch;
    }

    public EngineObject setSuggestion(@NonNull String suggestion) {
        mSuggestion = suggestion;
        return this;
    }

    @NonNull
    public String getSuggestion() {
        return mSuggestion;
    }
}
