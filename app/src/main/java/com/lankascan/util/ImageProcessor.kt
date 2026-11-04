package com.lankascan.util
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import android.graphics.Bitmap

object ImageProcessor {
    // Shadow removal + Magic filter - fixes blur in your screenshot 4
    fun applyMagicFilter(bitmap: Bitmap): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)
        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        val dilated = Mat()
        Imgproc.dilate(gray, dilated, Mat())
        Imgproc.medianBlur(dilated, dilated, 21)
        val diff = Mat()
        Core.absdiff(gray, dilated, diff)
        val result = Mat()
        Imgproc.adaptiveThreshold(diff, result, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 10.0)
        val outBmp = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888)
        Imgproc.cvtColor(result, src, Imgproc.COLOR_GRAY2BGR)
        Utils.matToBitmap(src, outBmp)
        return outBmp
    }
    fun perspectiveCrop(bitmap: Bitmap, tl: Point, tr: Point, br: Point, bl: Point): Bitmap {
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)
        val width = maxOf(tr.x - tl.x, br.x - bl.x).toInt()
        val height = maxOf(bl.y - tl.y, br.y - tr.y).toInt()
        val srcPoints = MatOfPoint2f(tl, tr, br, bl)
        val dstPoints = MatOfPoint2f(Point(0.0,0.0), Point(width.toDouble(),0.0), Point(width.toDouble(), height.toDouble()), Point(0.0, height.toDouble()))
        val transform = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)
        val dst = Mat()
        Imgproc.warpPerspective(src, dst, transform, Size(width.toDouble(), height.toDouble()))
        val outBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, outBmp)
        return outBmp
    }
}
