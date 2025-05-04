package com.example.art.utils

import android.util.Log
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object Preprocessor {

    /**
     * Головна функція покращення зображення перед подачею в ORB-детектор.
     * 1. Знаходить найбільший контур (можлива картина).
     * 2. Обрізає його.
     * 3. Перетворює в grayscale.
     * 4. Застосовує CLAHE.
     * 5. Масштабує до фіксованої висоти.
     */
    fun enhanceImage(input: Mat): Boolean {
        if (input.empty()) return false

        // 1. Конвертація в grayscale для пошуку контурів
        val gray = Mat()
        Imgproc.cvtColor(input, gray, Imgproc.COLOR_RGBA2GRAY)

        // 2. Бінаризація для пошуку контурів
        val blurred = Mat()
        Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
        val thresh = Mat()
        Imgproc.adaptiveThreshold(blurred, thresh, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY_INV, 11, 5.0)

        // 3. Пошук контурів
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        if (contours.isEmpty()) {
            Log.d("Preprocessor", "❌ No contours found")
            return false
        }

        // 4. Знаходження найбільшого прямокутника
        val largest = contours.maxByOrNull { Imgproc.contourArea(it) } ?: return false
        val rect = Imgproc.boundingRect(largest)

        if (rect.width < 100 || rect.height < 100) {
            Log.d("Preprocessor", "❌ Bounding box too small")
            return false
        }

        // 5. Обрізка
        val cropped = Mat(input, rect)

        // 6. Перетворення в grayscale + CLAHE
        val croppedGray = Mat()
        Imgproc.cvtColor(cropped, croppedGray, Imgproc.COLOR_RGBA2GRAY)
        val clahe = Imgproc.createCLAHE()
        clahe.clipLimit = 4.0
        val claheResult = Mat()
        clahe.apply(croppedGray, claheResult)

        // 7. Масштабування до стандартної висоти
        val targetHeight = 480.0
        val scale = targetHeight / claheResult.rows()
        val newSize = Size(claheResult.cols() * scale, targetHeight)
        Imgproc.resize(claheResult, input, newSize)

        Log.d("Preprocessor", "✅ Enhanced frame: resized to ${input.cols()}x${input.rows()}")
        return true
    }
}
