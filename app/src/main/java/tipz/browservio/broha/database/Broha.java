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
package tipz.browservio.broha.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Broha {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo
    private String iconHash;

    @ColumnInfo
    private String title;

    @ColumnInfo
    private String url;

    @ColumnInfo
    private long timestamp;

    public Broha(int id, String iconHash, String title, String url, long timestamp) {
        this.id = id;
        this.iconHash = iconHash;
        this.title = title;
        this.url = url;
        this.timestamp = timestamp;
    }

    @Ignore
    public Broha(String iconHash, String title, String url) {
        this.iconHash = iconHash;
        this.title = title;
        this.url = url;
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    @Ignore
    public Broha(String title, String url) {
        this.iconHash = null;
        this.title = title;
        this.url = url;
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    @Ignore
    public Broha(String url) {
        this.iconHash = null;
        this.title = null;
        this.url = url;
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    public void setIconHash(String iconHash) {
        this.iconHash = iconHash;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTimestamp() {
        this.timestamp = System.currentTimeMillis() / 1000L;
    }

    public int getId() {
        return this.id;
    }

    public String getIconHash() {
        return this.iconHash;
    }

    public String getTitle() {
        return this.title;
    }

    public String getUrl() {
        return this.url;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
