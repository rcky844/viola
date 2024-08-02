// Copyright (c) 2022-2024 Tipz Team
// SPDX-License-Identifier: Apache-2.0

package tipz.viola.broha.database

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

@Suppress("DEPRECATION")
class IconHashClient(context: Context) {
    private val fileDir: String = context.filesDir.path + "/favicon"

    fun save(icon: Bitmap): Int? {
        val buffer = ByteBuffer.allocate(icon.byteCount)
        icon.copyPixelsToBuffer(buffer)
        val hashInt = buffer.array().contentHashCode()
        val dirFile = File(fileDir)
        if (dirFile.exists() || dirFile.mkdirs()) {
            val path = File(fileDir, "$hashInt.webp")
            if (path.exists()) return hashInt
            try {
                val out = FileOutputStream(path)
                icon.compress(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY
                    else Bitmap.CompressFormat.WEBP, 75, out
                )
                out.flush()
                out.close()
            } catch (e: Exception) {
                return null
            }
        }
        return hashInt
    }

    fun read(hash: Int?): Bitmap? {
        if (hash == null) return null
        val imgFile = File(fileDir, "$hash.webp")
        if (imgFile.exists()) return BitmapFactory.decodeFile(imgFile.absolutePath)
        return null
    }
}