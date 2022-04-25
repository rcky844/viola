package tipz.browservio.broha.icons;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {IconHash.class}, version = 1)
public abstract class IconHashDatabase extends RoomDatabase {
    public abstract IconHashDao iconHashDao();
}
