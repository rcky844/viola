package tipz.browservio.broha;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BrohaDao {
    @Query("SELECT * FROM broha")
    List<Broha> getAll();

    @Query("SELECT * FROM broha LIMIT 1")
    List<Broha> isEmpty();

    @Query("SELECT * FROM broha ORDER BY id DESC LIMIT 1")
    Broha lastUrl();

    @Query("SELECT * FROM broha WHERE id LIKE :id LIMIT 1")
    Broha findById(int id);

    @Insert
    void insertAll(Broha... broha);

    @Update
    void updateBroha(Broha... broha);

    @Query("DELETE FROM broha WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM broha")
    void deleteAll();
}
