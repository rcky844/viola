/*
 * Copyright (C) 2022-2023 Tipz Team
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

    @JvmField
    @ColumnInfo
    var title: String?

    @JvmField
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

    fun setTimestamp() {
        timestamp = System.currentTimeMillis() / 1000L
    }
}