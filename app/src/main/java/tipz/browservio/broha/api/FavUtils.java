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
package tipz.browservio.broha.api;

import android.content.Context;
import android.graphics.Bitmap;

import tipz.browservio.broha.database.Broha;
import tipz.browservio.broha.database.icons.IconHashClient;

public class FavUtils {
    /**
     * Favourites Saviour
     * <p>
     * Module to save favourites into a db.
     */
    public static void appendData(Context context, IconHashClient iconHashClient, String title, String url, Bitmap icon) {
        if (iconHashClient != null && icon != null)
            FavApi.favBroha(context).insertAll(new Broha(iconHashClient.save(icon), title, url));
        else
            FavApi.favBroha(context).insertAll(new Broha(title, url));
    }

    public static void clear(Context context) {
        FavApi.favBroha(context).deleteAll();
    }

    public static void deleteById(Context context, int id) {
        FavApi.favBroha(context).deleteById(id);
    }

    public static boolean isEmptyCheck(Context context) {
        return FavApi.favBroha(context).isEmpty().size() == 0;
    }
}
