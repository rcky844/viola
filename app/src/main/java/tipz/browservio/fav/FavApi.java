package tipz.browservio.fav;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import tipz.browservio.sharedprefs.AllPrefs;

public class FavApi {
    public static SharedPreferences bookmarks(Context context) {
        return context.getSharedPreferences(AllPrefs.bookmarks, Activity.MODE_PRIVATE);
    }
}
