package tipz.browservio.broha.database.icons;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface IconHashDao {
    @Query("SELECT * FROM iconHash WHERE id LIKE :id LIMIT 1")
    IconHash findById(int id);

    @Query("SELECT * FROM iconHash WHERE iconHash LIKE :hash LIMIT 1")
    IconHash findByHash(int hash);

    @Query("SELECT * FROM iconHash LIMIT 1")
    List<IconHash> isEmpty();

    @Query("SELECT * FROM iconHash ORDER BY id DESC LIMIT 1")
    IconHash lastIcon();

    @Insert
    void insertAll(IconHash... iconHash);
}
