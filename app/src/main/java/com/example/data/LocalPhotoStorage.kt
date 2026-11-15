package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Copies user-selected photos from Android's zero-permission Photo Picker into
 * private app storage (`filesDir/photos/`) so all imagery remains strictly offline
 * and persisted inside the app without needing broad storage permissions.
 */
object LocalPhotoStorage {
    private const val PHOTOS_DIR = "photos"
    private const val MAX_DIMENSION = 1600

    suspend fun savePickedImageLocally(context: Context, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val photosDir = File(context.filesDir, PHOTOS_DIR).apply {
                    if (!exists()) mkdirs()
                }
                val targetFile = File(photosDir, "photo_${UUID.randomUUID()}.jpg")
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val originalBitmap = inputStream.use { BitmapFactory.decodeStream(it) }
                    ?: return@withContext null
                val scaledBitmap = scaleBitmapIfNeeded(originalBitmap, MAX_DIMENSION)
                FileOutputStream(targetFile).use { out ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
                }
                if (scaledBitmap !== originalBitmap) {
                    scaledBitmap.recycle()
                }
                originalBitmap.recycle()
                targetFile.absolutePath
            } catch (_: Exception) {
                null
            }
        }

    fun deleteLocalPhotoIfOwned(context: Context, path: String) {
        if (path.isBlank()) return
        try {
            val file = File(path)
            val photosDir = File(context.filesDir, PHOTOS_DIR)
            if (file.exists() && file.canonicalPath.startsWith(photosDir.canonicalPath)) {
                file.delete()
            }
        } catch (_: Exception) {
        }
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDim && height <= maxDim) return bitmap
        val ratio = minOf(maxDim.toFloat() / width, maxDim.toFloat() / height)
        val newW = (width * ratio).toInt().coerceAtLeast(1)
        val newH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }
}
