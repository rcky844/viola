package tipz.browservio.fav;

import android.content.Context;
import android.graphics.Bitmap;

import tipz.browservio.broha.Broha;
import tipz.browservio.broha.icons.IconHashClient;

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
