package com.example.art.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream

object DebugImageSaver {
    fun saveMatAsJpeg(context: Context, mat: Mat, filename: String = "frame_debug.jpg") {
        try {
            val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(mat, bitmap)

            val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val debugDir = File(picturesDir, "ArtDebug")
            if (!debugDir.exists()) debugDir.mkdirs()

            val file = File(debugDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            Log.d("DebugImageSaver", "✅ Saved: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("DebugImageSaver", "❌ Failed to save image: ${e.message}", e)
        }
    }
}
