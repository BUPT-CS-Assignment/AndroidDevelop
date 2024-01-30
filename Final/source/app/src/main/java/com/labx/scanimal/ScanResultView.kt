package com.labx.scanimal

import androidx.appcompat.widget.AppCompatImageView
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow


data class CropDetectionResult(
    val actualBoxRectF: RectF,
    val originalBoxRectF: Rect,
    val dotCenter: PointF,
    val searchResult: SearchResult?,
)

class ScanResultView : AppCompatImageView {

    companion object {
        const val TAG = "ScanResultView"
        private const val CLICKABLE_RADIUS = 40f
        private const val SHADOW_RADIUS = 10f
    }

    private val dotPaint = createDotPaint()
    private var cropResults: MutableList<CropDetectionResult> = mutableListOf()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun clearResults(){
        cropResults.clear()
    }

    fun addDetectionResult(result: SearchResult) {
        (drawable as? BitmapDrawable)?.bitmap?.let { srcImage ->
            Log.d(TAG,"add detection result")
            val scaleFactor =
                max(srcImage.width / width.toFloat(), srcImage.height / height.toFloat())
            val diffWidth = abs(width - srcImage.width / scaleFactor) / 2
            val diffHeight = abs(height - srcImage.height / scaleFactor) / 2

            result?.let { result ->
                val actualRectBoundingBox = RectF(
                    (result.detectedObject.boundingBox.left / scaleFactor) + diffWidth,
                    (result.detectedObject.boundingBox.top / scaleFactor) + diffHeight,
                    (result.detectedObject.boundingBox.right / scaleFactor) + diffWidth,
                    (result.detectedObject.boundingBox.bottom / scaleFactor) + diffHeight
                )
                val dotCenter = PointF(
                    (actualRectBoundingBox.right + actualRectBoundingBox.left) / 2,
                    (actualRectBoundingBox.bottom + actualRectBoundingBox.top) / 2,
                )
                CropDetectionResult(actualRectBoundingBox, result.detectedObject.boundingBox, dotCenter,result)
            }?.let { cropResults.add(it) }
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cropResults.forEach { result ->
            canvas.drawCircle(result.dotCenter.x, result.dotCenter.y, CLICKABLE_RADIUS, dotPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y
                val index =
                    cropResults.indexOfFirst {
                        val dx = (touchX - it.dotCenter.x).toDouble().pow(2.0)
                        val dy = (touchY - it.dotCenter.y).toDouble().pow(2.0)
                        (dx + dy) < CLICKABLE_RADIUS.toDouble().pow(2.0)
                    }
                if (index != -1) {
                    cropResults[index]?.searchResult.let {
                        it?.dialog?.show()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }


    private fun createDotPaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(SHADOW_RADIUS, 0F, 0F, Color.BLACK)
        setLayerType(LAYER_TYPE_SOFTWARE, this)
    }

}