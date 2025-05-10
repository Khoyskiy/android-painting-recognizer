package com.example.art.analyzer

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.art.model.PaintingDescriptor
import com.example.art.utils.DescriptorMatcherHelper
import com.example.art.utils.FeatureExtractor
import org.opencv.core.CvType
import org.opencv.core.Mat

class ImageAnalyzer(
    private val paintings: List<PaintingDescriptor>,
    private val onResult: (String) -> Unit,
    private val matchThreshold: Int = 10,
) : ImageAnalysis.Analyzer {

    private val featureExtractor = FeatureExtractor()
    private val matcherHelper = DescriptorMatcherHelper()
    private val matchTracker = MatchTracker(3)
    private var recognitionTriggered = false

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        if (recognitionTriggered) {
            image.close()
            return
        }

        // 🧠 Видобуток дескрипторів після попередньої обробки зображення
        val descriptors = featureExtractor.extractDescriptors(image)

        if (descriptors == null || descriptors.rows() <= 0) {
            Log.d("ImageAnalyzer", "❌ No descriptors extracted from preprocessed frame")
            image.close()
            return
        }

        var bestMatchName: String? = null
        var bestMatchScore = Double.MAX_VALUE
        var bestGoodMatchesCount = 0

        var secondBestGoodMatchesCount = 0
        var secondBestScore = Double.MAX_VALUE

        Log.d("ImageAnalyzer", "🔎 Comparing frame with paintings:")

        for (painting in paintings) {
            val paintingMat = byteArrayToMat(painting.descriptor)
            val goodMatches = matcherHelper.countGoodMatches(descriptors, paintingMat)
            val matchScore = matcherHelper.calculateMatchQuality(descriptors, paintingMat)

            Log.d("ImageAnalyzer", "➡ ${painting.name}: $goodMatches good matches, score = $matchScore")

            if (goodMatches >= matchThreshold && matchScore < bestMatchScore) {
                secondBestGoodMatchesCount = bestGoodMatchesCount
                secondBestScore = bestMatchScore

                bestMatchName = painting.name
                bestMatchScore = matchScore
                bestGoodMatchesCount = goodMatches
            } else if (goodMatches > secondBestGoodMatchesCount) {
                secondBestGoodMatchesCount = goodMatches
                secondBestScore = matchScore
            }
        }

        Log.d("ImageAnalyzer", "🔍 Best match: $bestMatchName ($bestGoodMatchesCount good matches, score = $bestMatchScore)")
        Log.d("ImageAnalyzer", "📊 Second best: $secondBestGoodMatchesCount good matches, score = $secondBestScore")

        if (bestMatchName != null && (bestGoodMatchesCount > 70 || bestMatchScore < 50.0)) {
            recognitionTriggered = true
            onResult(bestMatchName)
            image.close()
            return
        }

        val confirmed = matchTracker.track(bestMatchName)
        if (bestMatchName != null && bestGoodMatchesCount >= 15 && confirmed) {
            recognitionTriggered = true
            onResult(bestMatchName)
            image.close()
            return
        }

        val isClearlyBetter = (bestGoodMatchesCount - secondBestGoodMatchesCount >= 5) ||
                (bestMatchScore < secondBestScore * 0.75)

        if (bestMatchName != null && bestGoodMatchesCount >= 5 && bestMatchScore < 300.0 && isClearlyBetter) {
            recognitionTriggered = true
            onResult(bestMatchName)
            image.close()
            return
        }

        image.close()
    }

    private fun byteArrayToMat(byteArray: ByteArray): Mat {
        if (byteArray.isEmpty()) return Mat()
        val descriptorSize = 32
        val rows = byteArray.size / descriptorSize
        val mat = Mat(rows, descriptorSize, CvType.CV_8U)
        mat.put(0, 0, byteArray)
        return mat
    }

    fun resetRecognition() {
        recognitionTriggered = false
        matchTracker.reset()
    }

}
