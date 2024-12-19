// Copyright (c) 2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.download.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Droha::class], version = 1)
abstract class DrohaDatabase : RoomDatabase() {
    abstract fun drohaDao(): DrohaDao?
}