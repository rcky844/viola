// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room.databaseBuilder
import androidx.room.Update

open class BrohaClient(context: Context?, dbName: String?) {
    private val appDatabase: BrohaDatabase =
        databaseBuilder(context!!, BrohaDatabase::class.java, dbName).build()

    val dao: BrohaDao?
        get() = appDatabase.brohaDao()

    var brohaEnabled = true

    suspend fun insert(vararg broha: Broha) {
        if (brohaEnabled)
            dao!!.insert(*broha)
    }

    suspend fun update(vararg broha: Broha) {
        if (brohaEnabled)
            dao!!.update(*broha)
    }

    suspend fun getAll(): List<Broha> {
        if (brohaEnabled)
            return dao!!.getAll()
        return listOf() // Stub!
    }

    suspend fun deleteById(id: Int) {
        if (brohaEnabled)
            dao!!.deleteById(id)
    }
    suspend fun deleteAll() {
        if (brohaEnabled)
            dao!!.deleteAll()
    }
}

@Dao
interface BrohaDao {
    @Insert
    suspend fun insert(vararg broha: Broha)

    @Update
    suspend fun update(vararg broha: Broha)

    @Query("SELECT * FROM broha")
    suspend fun getAll(): List<Broha>

    @Query("DELETE FROM broha WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM broha")
    suspend fun deleteAll()
}