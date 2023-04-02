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

import android.content.Context;

import androidx.room.Room;

public class BrohaClient {
    private final BrohaDatabase appDatabase;

    public BrohaClient(Context context, String dbName) {
        //appDatabase = Room.databaseBuilder(context, BrohaDatabase.class, dbName).build();
        /* FIXME: Don't run on main thread */
        appDatabase = Room.databaseBuilder(context, BrohaDatabase.class, dbName).allowMainThreadQueries().build();
    }

    public BrohaDao getDao() {
        return appDatabase.brohaDao();
    }
}
