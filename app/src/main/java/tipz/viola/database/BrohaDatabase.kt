// Copyright (c) 2022-2026 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Broha::class], version = 3)
abstract class BrohaDatabase : RoomDatabase() {
    abstract fun brohaDao(): BrohaDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Store timestamp as millis epoch
                db.execSQL("UPDATE Broha SET timestamp=timestamp*1000")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Convert null values to non-null alternatives
                db.execSQL("UPDATE Broha SET title='' WHERE title IS NULL")
                db.execSQL("UPDATE Broha SET url='' WHERE url IS NULL")
                db.execSQL("UPDATE Broha SET iconHash=0 WHERE iconHash IS NULL")
                // Recreate table with auto incrementing id column.
                db.execSQL("CREATE TABLE IF NOT EXISTS Broha_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, url TEXT NOT NULL, icon_hash INTEGER NOT NULL, timestamp INTEGER NOT NULL DEFAULT 1577836800000)")
                // Copy data
                db.execSQL("INSERT INTO Broha_new (id, title, url, icon_hash, timestamp) SELECT id, title, url, iconHash, timestamp FROM Broha")
                // Remove old table
                db.execSQL("DROP TABLE Broha")
                // Rename new table
                db.execSQL("ALTER TABLE Broha_new RENAME TO Broha")
            }
        }

        fun getDatabase(context: Context, dbName: String): BrohaDatabase {
            return synchronized(this) {
                databaseBuilder(context.applicationContext,
                    BrohaDatabase::class.java, dbName)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
            }
        }
    }
}