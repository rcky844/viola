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

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BrohaDao {
    @Query("SELECT * FROM broha")
    List<Broha> getAll();

    @Query("SELECT * FROM broha LIMIT 1")
    List<Broha> isEmpty();

    @Query("SELECT * FROM broha ORDER BY id DESC LIMIT 1")
    Broha lastUrl();

    @Query("SELECT * FROM broha WHERE id LIKE :id LIMIT 1")
    Broha findById(int id);

    @Insert
    void insertAll(Broha... broha);

    @Update
    void updateBroha(Broha... broha);

    @Query("DELETE FROM broha WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM broha")
    void deleteAll();
}
