package tipz.browservio.broha.icons;

import android.content.Context;

import androidx.room.Room;

public class IconHashClient {
    private final IconHashDatabase appDatabase;

    public IconHashClient(Context context) {
        //appDatabase = Room.databaseBuilder(context, IconHashDatabase.class, "iconHash").build();
        /* FIXME: Don't run on main thread */
        appDatabase = Room.databaseBuilder(context, IconHashDatabase.class, "iconHash").allowMainThreadQueries().build();
    }

    public IconHashDatabase getDatabase() {
        return appDatabase;
    }

    public IconHashDao getDao() {
        return appDatabase.iconHashDao();
    }

    public IconHash getIconHashById(int id) {
        return appDatabase.iconHashDao().findById(id);
    }

    public IconHash getIconHashByHash(String hash) {
        return appDatabase.iconHashDao().findByHash(hash);
    }
}
