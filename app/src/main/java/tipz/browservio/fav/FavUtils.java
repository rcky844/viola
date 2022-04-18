package tipz.browservio.fav;

import android.content.Context;

import tipz.browservio.broha.Broha;

public class FavUtils {
    /**
     * Favourites Saviour
     * <p>
     * Module to save favourites into a db.
     */
    public static void appendData(Context context, String title, String url) {
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
