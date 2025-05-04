package com.example.art.utils

import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.features2d.BFMatcher
import org.opencv.features2d.DescriptorMatcher

class DescriptorMatcherHelper {

    private val matcher: DescriptorMatcher = BFMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING, false)

    /**
     * Рахує кількість хороших співпадінь між двома дескрипторами, використовуючи KNN та тест Lowe's ratio test
     */
    fun countGoodMatches(descriptors1: Mat, descriptors2: Mat, ratioThreshold: Float = 0.75f): Int {
        if (descriptors1.empty() || descriptors2.empty()) return 0

        val knnMatches = mutableListOf<MatOfDMatch>()
        matcher.knnMatch(descriptors1, descriptors2, knnMatches, 2)

        var goodMatchesCount = 0

        for (matchPair in knnMatches) {
            val matches = matchPair.toArray()
            if (matches.size >= 2) {
                val m1 = matches[0]
                val m2 = matches[1]
                if (m1.distance < ratioThreshold * m2.distance) {
                    goodMatchesCount++
                }
            }
        }

        return goodMatchesCount
    }

    /**
     * Рахує "якість" збігу як середню відстань між усіма відповідними парами дескрипторів
     */
    fun calculateMatchQuality(descriptors1: Mat, descriptors2: Mat): Double {
        if (descriptors1.empty() || descriptors2.empty()) return Double.MAX_VALUE

        val matches = mutableListOf<MatOfDMatch>()
        matcher.knnMatch(descriptors1, descriptors2, matches, 2)

        var totalDistance = 0.0
        var goodMatches = 0

        for (matchPair in matches) {
            val m = matchPair.toArray()
            if (m.size >= 2 && m[0].distance < 0.75f * m[1].distance) {
                totalDistance += m[0].distance
                goodMatches++
            }
        }

        return if (goodMatches > 0) totalDistance / goodMatches else Double.MAX_VALUE
    }
}