
package com.example.art.utils

import android.media.Image
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.ORB
import java.nio.ByteBuffer

class FeatureExtractor {

    private val orb = ORB.create(1000)

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    fun extractDescriptors(image: ImageProxy): Mat? {
        val mat = imageToMat(image.image ?: return null) ?: return null

        Preprocessor.enhanceImage(mat)

        // 🔽 Додай збереження обробленого зображення:
        DebugImageSaver.saveMatAsJpeg(AppContextProvider.getContext(), mat)

        val keypoints = MatOfKeyPoint()
        val descriptors = Mat()
        orb.detectAndCompute(mat, Mat(), keypoints, descriptors)

        Log.d("FeatureExtractor", "🔍 Found ${keypoints.toArray().size} keypoints, descriptors size: ${descriptors.rows()} x ${descriptors.cols()}")

        return if (descriptors.rows() > 0) descriptors else null
    }


    private fun imageToMat(image: Image): Mat? {
        return try {
            val yBuffer: ByteBuffer = image.planes[0].buffer
            val vuBuffer: ByteBuffer = image.planes[2].buffer

            val ySize = yBuffer.remaining()
            val vuSize = vuBuffer.remaining()

            val nv21 = ByteArray(ySize + vuSize)
            yBuffer.get(nv21, 0, ySize)
            vuBuffer.get(nv21, ySize, vuSize)

            val yuvMat = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
            yuvMat.put(0, 0, nv21)

            val rgbMat = Mat()
            org.opencv.imgproc.Imgproc.cvtColor(yuvMat, rgbMat, org.opencv.imgproc.Imgproc.COLOR_YUV2RGB_NV21)

            yuvMat.release()
            rgbMat
        } catch (e: Exception) {
            Log.e("FeatureExtractor", "❌ Failed to convert YUV to RGB: ${e.message}")
            null
        }
    }
}