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
package tipz.viola.broha.database.icons;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class IconHash {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo
    private int iconHash;

    public IconHash(int id, int iconHash) {
        this.id = id;
        this.iconHash = iconHash;
    }

    @Ignore
    public IconHash(int iconHash) {
        this.iconHash = iconHash;
    }

    public int getId() {
        return id;
    }

    public int getIconHash() {
        return iconHash;
    }
}
