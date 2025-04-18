package com.makinul.alphabet.learn.utils

import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toComposeRect
import java.lang.Math.abs

object AppConstants {

    fun isPathSimilarToChar(path: Path, char: Char, tolerance: Float = 0.2f): Boolean {
        if (char !in 'A'..'Z') {
            Log.w("PathSimilarity", "Unsupported character: $char. Only A-Z supported for now.")
            return false
        }

        //1. Convert Compose Path to Android Path and get its bounds
        val androidPath = path.asAndroidPath()
        val pathBounds = RectF().apply { androidPath.computeBounds(this, true) }

        //2. Define Reference Paths for Letters.  (Needs to be populated for all letters)
        val referencePath = getReferencePathForChar(char) ?: run {
            Log.e("PathSimilarity", "No reference path defined for character: $char")
            return false
        }
        val androidReferencePath = referencePath.asAndroidPath()
        val referenceBounds = RectF().apply { androidReferencePath.computeBounds(this, true) }

        //3. Normalize Paths by scaling and translating to fit within a unit square (0,0) - (1,1)
        val normalizedPath = normalizePath(androidPath, pathBounds)
        val normalizedReferencePath = normalizePath(androidReferencePath, referenceBounds)

        // 4. Compare Paths.  Simple approach: compare bounding boxes
        val normalizedPathBounds =
            RectF().apply { normalizedPath.computeBounds(this, true) }.toComposeRect()
        val normalizedReferenceBounds =
            RectF().apply { normalizedReferencePath.computeBounds(this, true) }.toComposeRect()

        val boundsDifference =
            calculateBoundsDifference(normalizedPathBounds, normalizedReferenceBounds)

        Log.d(
            "PathSimilarity",
            "Character: $char, Bounds Diff: $boundsDifference, Tolerance: $tolerance"
        )

        return boundsDifference <= tolerance
    }

    private fun calculateBoundsDifference(
        rect1: Rect,
        rect2: Rect
    ): Float {
        val widthDiff = abs(rect1.width - rect2.width)
        val heightDiff = abs(rect1.height - rect2.height)
        val xDiff = abs(rect1.left - rect2.left)
        val yDiff = abs(rect1.top - rect2.top)

        return (widthDiff + heightDiff + xDiff + yDiff) / 4 //Average difference
    }

    // Helper function to normalize a path to fit within a 1x1 unit square.
    private fun normalizePath(path: android.graphics.Path, bounds: RectF): android.graphics.Path {
        val normalizedPath = android.graphics.Path()
        if (bounds.isEmpty) {
            return normalizedPath  // Return empty path if bounds are empty
        }

        val scaleX = 1f / bounds.width()
        val scaleY = 1f / bounds.height()
        val matrix = Matrix().apply {
            postScale(scaleX, scaleY)
            postTranslate(-bounds.left * scaleX, -bounds.top * scaleY)
        }

        path.transform(matrix, normalizedPath)
        return normalizedPath
    }


    // Dummy function: Needs to be replaced with actual logic to provide reference paths for each character.
    private fun getReferencePathForChar(char: Char): Path? {
        return when (char) {
            'A' -> {
                // Example path for 'A' (Replace with a more accurate path)
                Path().apply {
                    moveTo(0.5f, 0f)
                    lineTo(0f, 1f)
                    moveTo(1f, 1f)
                }
            }
            // Add other character paths here...
            else -> null
        }
    }
}