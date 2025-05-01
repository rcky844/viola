// Copyright (c) 2022-2025 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Broha::class], version = 2)
abstract class BrohaDatabase : RoomDatabase() {
    abstract fun brohaDao(): BrohaDao?

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE Broha SET timestamp=timestamp*1000")
            }
        }
    }
}