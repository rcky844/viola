package tipz.browservio.broha.icons;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {IconHash.class}, version = 2, autoMigrations = {@AutoMigration(from = 1, to = 2)})
public abstract class IconHashDatabase extends RoomDatabase {
    public abstract IconHashDao iconHashDao();
}
