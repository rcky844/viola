package tipz.browservio.broha;

import android.content.Context;

import androidx.room.Room;

public class BrohaClient {
    private final BrohaDatabase appDatabase;
    private static String databaseName;


    public BrohaClient(Context context, String dbName) {
        databaseName = dbName;

        //appDatabase = Room.databaseBuilder(context, BrohaDatabase.class, dbName).build();
        /* FIXME: Don't run on main thread */
        appDatabase = Room.databaseBuilder(context, BrohaDatabase.class, dbName).allowMainThreadQueries().build();
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public BrohaDatabase getDatabase() {
        return appDatabase;
    }

    public BrohaDao getDao() {
        return appDatabase.brohaDao();
    }

    public Broha getBroha(int id) {
        return appDatabase.brohaDao().findById(id);
    }
}
