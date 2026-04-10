// Copyright (c) 2022-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.database

import androidx.room.Dao
import androidx.room.Query

@Dao
interface BrohaDao {
    @Query("SELECT * FROM Broha ORDER BY id DESC")
    suspend fun getAll(): List<Broha>

    @Query(
        "SELECT * FROM Broha" +
                "  WHERE title LIKE :keyword OR url LIKE :keyword" +
                "  ORDER BY timestamp DESC" +
                "  LIMIT :limit"
    )
    fun search(keyword: String, limit: Int): List<Broha>

    @Query("SELECT * FROM broha WHERE id LIKE :id LIMIT 1")
    suspend fun getById(id: Long): Broha

    @Query("INSERT INTO Broha (title, url, icon_hash, timestamp) " +
            "VALUES (:title, :url, :iconHash, :timestamp)")
    suspend fun insert(title: String, url: String, iconHash: Int,
                       timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE Broha SET title = :title, url = :url, " +
            "timestamp = :timestamp WHERE id = :id")
    suspend fun update(id: Long, title: String, url: String,
                       timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM Broha WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM Broha")
    suspend fun deleteAll()
}