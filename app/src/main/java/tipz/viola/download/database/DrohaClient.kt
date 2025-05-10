// Copyright (c) 2024-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room.databaseBuilder
import androidx.room.Update
import tipz.viola.settings.SettingsKeys
import tipz.viola.settings.SettingsSharedPreference

open class DrohaClient(context: Context) {
    private val appDatabase: DrohaDatabase =
        databaseBuilder(context, DrohaDatabase::class.java, "downloads").build()

    val dao: DrohaDao?
        get() = appDatabase.drohaDao()

    init {
        val settingsPreference = SettingsSharedPreference.instance
        val downloadApiVer = settingsPreference.getInt(SettingsKeys.downloadApi)
        if (downloadApiVer > LATEST_API || downloadApiVer <= -1) throw RuntimeException()
        settingsPreference.setInt(SettingsKeys.downloadApi, LATEST_API)
    }

    companion object {
        private const val LATEST_API = 0
    }

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