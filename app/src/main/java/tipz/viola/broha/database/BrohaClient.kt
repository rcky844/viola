/*
 * Copyright (c) 2022-2024 Tipz Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tipz.viola.broha.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room.databaseBuilder
import androidx.room.Update

open class BrohaClient(context: Context?, dbName: String?) {
    private val appDatabase: BrohaDatabase

    init {
        appDatabase = databaseBuilder(context!!, BrohaDatabase::class.java, dbName).build()
    }

    val dao: BrohaDao?
        get() = appDatabase.brohaDao()

    suspend fun insert(vararg broha: Broha) = dao!!.insert(*broha)
    suspend fun update(vararg broha: Broha) = dao!!.update(*broha)
    suspend fun getAll(): List<Broha> = dao!!.getAll()
    suspend fun isEmpty(): Boolean = dao!!.isEmpty().isEmpty()
    suspend fun deleteById(id: Int) = dao!!.deleteById(id)
    suspend fun deleteAll() = dao!!.deleteAll()
}

@Dao
interface BrohaDao {
    @Insert
    suspend fun insert(vararg broha: Broha)

    @Update
    suspend fun update(vararg broha: Broha)

    @Query("SELECT * FROM broha")
    suspend fun getAll(): List<Broha>

    @Query("SELECT * FROM broha LIMIT 1")
    suspend fun isEmpty(): List<Broha>

    @Query("DELETE FROM broha WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM broha")
    suspend fun deleteAll()
}