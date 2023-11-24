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
package tipz.viola.utils;

public class StringUtils {
    public static final int INDEX_NOT_FOUND = -1;

    public static String substringBefore(final String str, final int separator) {
        if (str.isEmpty()) {
            return str;
        }
        final int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    public static String substringAfterLast(final String str, final String separator) {
        if (str.isEmpty()) {
            return str;
        }
        if (separator.isEmpty()) {
            return CommonUtils.EMPTY_STRING;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND || pos == str.length() - separator.length()) {
            return CommonUtils.EMPTY_STRING;
        }
        return str.substring(pos + separator.length());
    }
}
