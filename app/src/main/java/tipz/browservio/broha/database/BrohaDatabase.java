package tipz.browservio.broha.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Broha.class}, version = 1)
public abstract class BrohaDatabase extends RoomDatabase {
    public abstract BrohaDao brohaDao();
}
