// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class Broha {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo
    var iconHash: Int?

    @ColumnInfo
    var title: String?

    @ColumnInfo
    var url: String?

    @ColumnInfo
    var timestamp: Long
        private set

    constructor(id: Int, iconHash: Int?, title: String?, url: String, timestamp: Long) {
        this.id = id
        this.iconHash = iconHash
        this.title = title
        this.url = url
        this.timestamp = timestamp
    }

    @Ignore
    constructor(iconHash: Int?, title: String?, url: String) {
        this.iconHash = iconHash
        this.title = title
        this.url = url
        timestamp = System.currentTimeMillis() / 1000L
    }

    @Ignore
    constructor(title: String?, url: String) {
        iconHash = null
        this.title = title
        this.url = url
        timestamp = System.currentTimeMillis() / 1000L
    }

    @Ignore
    constructor(url: String) {
        iconHash = null
        title = null
        this.url = url
        timestamp = System.currentTimeMillis() / 1000L
    }

    @Ignore
    constructor() {
        iconHash = null
        title = null
        this.url = ""
        timestamp = System.currentTimeMillis() / 1000L
    }

    fun setTimestamp() {
        timestamp = System.currentTimeMillis() / 1000L
    }
}