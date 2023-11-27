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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.Arrays

class IconHashUtils(context: Context) {
    private val fileDir: String

    init {
        fileDir = context.filesDir.path + "/favicon"
    }

    suspend fun save(icon: Bitmap): Int? {
        val buffer = ByteBuffer.allocate(icon.byteCount)
        icon.copyPixelsToBuffer(buffer)
        val hashInt = Arrays.hashCode(buffer.array())
        val dirFile = File(fileDir)
        if (dirFile.exists() || dirFile.mkdirs()) {
            var path = File(fileDir, "$hashInt.webp")
            if (path.exists()) return hashInt
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
        }
        return hashInt
    }

    suspend fun read(hash: Int?): Bitmap? {
        if (hash == null) return null
        val imgFile = File(fileDir, "$hash.webp")
        if (imgFile.exists()) return BitmapFactory.decodeFile(imgFile.absolutePath)
        return null
    }
}