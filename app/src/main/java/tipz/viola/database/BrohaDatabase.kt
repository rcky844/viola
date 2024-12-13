// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.database

import androidx.room.Database
import androidx.room.RoomDatabase
import tipz.viola.database.Broha

@Database(entities = [Broha::class], version = 1)
abstract class BrohaDatabase : RoomDatabase() {
    abstract fun brohaDao(): BrohaDao?
}