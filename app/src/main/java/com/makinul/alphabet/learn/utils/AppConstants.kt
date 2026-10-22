package com.makinul.alphabet.learn.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath

object AppConstants {
    fun isPathSimilarToChar(path: Path, char: Char, tolerance: Float = 0.2f) =
        isPathsSimilarToChar(listOf(path), char, tolerance)

    fun isPathsSimilarToChar(paths: List<Path>, char: Char, tolerance: Float = 0.2f): Boolean {
        if (char !in 'A'..'Z' || paths.isEmpty()) return false
        val user = android.graphics.Path().apply { paths.forEach { addPath(it.asAndroidPath()) } }
        val userBounds = RectF().apply { user.computeBounds(this, true) }
        if (userBounds.isEmpty) return false
        val reference = android.graphics.Path()
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = 1f
            getTextPath(char.toString(), 0, 1, 0f, 1f, reference)
        }
        val referenceBounds = RectF().apply { reference.computeBounds(this, true) }
        val actual = rasterize(normalize(user, userBounds), 64)
        val expected = rasterize(normalize(reference, referenceBounds), 64)
        var intersection = 0
        var union = 0
        for (i in actual.indices) {
            if (actual[i] || expected[i]) union++
            if (actual[i] && expected[i]) intersection++
        }
        val score = intersection.toFloat() / union.coerceAtLeast(1)
        Log.d("PathSimilarity", "Character: $char, IoU: $score")
        // A loose overlap is not enough: unrelated strokes should not pass.
        return score >= 0.12f
    }

    private fun normalize(path: android.graphics.Path, bounds: RectF): android.graphics.Path {
        val result = android.graphics.Path()
        Matrix().apply {
            setScale(1f / bounds.width(), 1f / bounds.height())
            postTranslate(-bounds.left / bounds.width(), -bounds.top / bounds.height())
            path.transform(this, result)
        }
        return result
    }

    private fun rasterize(path: android.graphics.Path, size: Int): BooleanArray {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ALPHA_8)
        val scaledPath = android.graphics.Path()
        Matrix().apply {
            setScale(size - 4f, size - 4f)
            postTranslate(2f, 2f)
            path.transform(this, scaledPath)
        }
        Canvas(bitmap).drawPath(scaledPath, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        })
        return BooleanArray(size * size) { i -> bitmap.getPixel(i % size, i / size) != 0 }
    }
}
