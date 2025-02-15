package tipz.viola.download.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room.databaseBuilder
import androidx.room.Update

open class DrohaClient(context: Context?) {
    private val appDatabase: DrohaDatabase =
        databaseBuilder(context!!, DrohaDatabase::class.java, "downloads").build()

    val dao: DrohaDao?
        get() = appDatabase.drohaDao()

    suspend fun insert(vararg droha: Droha) = dao!!.insert(*droha)
    suspend fun update(vararg droha: Droha) = dao!!.update(*droha)
    suspend fun getAll(): List<Droha> = dao!!.getAll()
    suspend fun getWithFilename(filename: String): List<Droha> = dao!!.getWithFilename(filename)
    suspend fun deleteById(id: Int) = dao!!.deleteById(id)
    suspend fun deleteAll() = dao!!.deleteAll()
}

@Dao
interface DrohaDao {
    @Insert
    suspend fun insert(vararg droha: Droha)

    @Update
    suspend fun update(vararg droha: Droha)

    @Query("SELECT * FROM droha")
    suspend fun getAll(): List<Droha>

    @Query("SELECT * FROM droha WHERE filename = :filename")
    suspend fun getWithFilename(filename: String): List<Droha>

    @Query("DELETE FROM droha WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM droha")
    suspend fun deleteAll()
}