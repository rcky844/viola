/*
 * Copyright (C) 2022-2023 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
