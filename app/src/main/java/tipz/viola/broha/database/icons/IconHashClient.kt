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
package tipz.viola.broha.database.icons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.room.Room.databaseBuilder
import java.io.File
import java.io.FileOutputStream
import java.lang.Integer.toString
import java.nio.ByteBuffer
import java.util.Arrays

class IconHashClient(context: Context) {
    private val appDatabase: IconHashDatabase
    private val fileDir: String

    init {
        //appDatabase = Room.databaseBuilder(context, IconHashDatabase.class, "iconHash").build();
        /* FIXME: Don't run on main thread */
        appDatabase = databaseBuilder(
            context,
            IconHashDatabase::class.java,
            "iconHash"
        ).allowMainThreadQueries().build()
        fileDir = context.filesDir.path + "/favicon"
    }

    private val dao: IconHashDao?
        get() = appDatabase.iconHashDao()

    private fun getIconHashById(id: Int): IconHash? {
        return appDatabase.iconHashDao()?.findById(id)
    }

    private fun getIconHashByHash(hash: Int): IconHash? {
        return appDatabase.iconHashDao()?.findByHash(hash)
    }

    fun save(icon: Bitmap): String? {
        val buffer = ByteBuffer.allocate(icon.byteCount)
        icon.copyPixelsToBuffer(buffer)
        val hashInt = Arrays.hashCode(buffer.array())
        val hash = hashInt.toString()
        val dirFile = File(fileDir)
        if (dirFile.exists() || dirFile.mkdirs()) {
            var wasJpg = false
            var path = File(fileDir, "$hash.jpg")
            if (path.exists()) {
                path.delete() /* Delete old JPEG files */
                wasJpg = true
            }
            path = File(fileDir, "$hash.webp")
            if (path.exists()) return getIconHashByHash(hashInt)?.id.toString()
            try {
                val out = FileOutputStream(path)
                icon.compress(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP,
                    75,
                    out
                )
                out.flush()
                out.close()
            } catch (e: Exception) {
                return null
            }
            return if (wasJpg) {
                getIconHashByHash(hashInt)?.id.toString()
            } else {
                dao?.insertAll(IconHash(hashInt))
                dao?.lastIcon()?.id.toString()
            }
        }
        return null
    }

    fun read(iconId: String?): Bitmap? {
        if (iconId == null) return null
        val data = getIconHashById(iconId.toInt())
        val hash = data?.iconHash.toString()
        var imgFile = File(fileDir, "$hash.webp")
        if (imgFile.exists()) return BitmapFactory.decodeFile(imgFile.absolutePath)
        imgFile = File(fileDir, "$hash.jpg")
        return if (imgFile.exists()) BitmapFactory.decodeFile(imgFile.absolutePath) else null
    }
}