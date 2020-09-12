package com.mancel.yann.poseanalyser.widgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.mancel.yann.poseanalyser.R
import com.mancel.yann.poseanalyser.models.KeyPointOfPose

/**
 * Created by Yann MANCEL on 04/09/2020.
 * Name of the project: PoseAnalyser
 * Name of the package: com.mancel.yann.poseanalyser.widgets
 *
 * A [View] subclass.
 */
class GraphicOverlay(
    context: Context,
    attrs: AttributeSet
) : View(context, attrs) {

    // FIELDS --------------------------------------------------------------------------------------

    private val _dotPaint = Paint()
    private val _linePaint = Paint().apply { style = Paint.Style.STROKE }
    private val _dotRadius: Float
    private val _pose = mutableListOf<KeyPointOfPose>()
    private var _scale: PointF? = null

    // CONSTRUCTORS --------------------------------------------------------------------------------

    init {
        // Attributes from xml file, called attrs.xml
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.GraphicOverlay)

        // Colors
        this._dotPaint.color = attributes.getColor(
            R.styleable.GraphicOverlay_dotColor,
            Color.WHITE
        )
        this._linePaint.color = attributes.getColor(
            R.styleable.GraphicOverlay_lineColor,
            Color.WHITE
        )

        // Dot radius
        this._dotRadius = attributes.getFloat(
            R.styleable.GraphicOverlay_dotRadius,
            1F
        )

        // Stroke width
        this._linePaint.strokeWidth = attributes.getFloat(
            R.styleable.GraphicOverlay_lineWidth,
            1F
        )

        attributes.recycle()
    }

    // METHODS -------------------------------------------------------------------------------------

    // -- View --

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // No canvas
        if (canvas == null) return

        // No scale
        if (this._scale == null) return

//        val imageWidth = this.right.toFloat() / this._scale!!.x
//        val imageHeight = this.bottom.toFloat() / this._scale!!.y
//        canvas.drawRect(0F, 0F, imageWidth, imageHeight, this._linePaint)

        val shoulderLine = mutableListOf<KeyPointOfPose>()
        val hipLine = mutableListOf<KeyPointOfPose>()

        // Draw all points
        if (this._pose.isNotEmpty()) {
            this._pose.forEach { keyPoint ->
                // Retrieve points for to draw lines
                if (keyPoint._type == KeyPointOfPose.Type.LEFT_SHOULDER) shoulderLine.add(keyPoint)
                if (keyPoint._type == KeyPointOfPose.Type.RIGHT_SHOULDER) shoulderLine.add(keyPoint)
                if (keyPoint._type == KeyPointOfPose.Type.LEFT_HIP) hipLine.add(keyPoint)
                if (keyPoint._type == KeyPointOfPose.Type.RIGHT_HIP) hipLine.add(keyPoint)

                this.drawPoint(
                    canvas,
                    keyPoint._position,
                    this._scale,
                    this._dotPaint,
                    this._dotRadius
                )
            }
        }

        // Draw shoulder line
        if (shoulderLine.size == 2) {
            this.drawLine(
                canvas,
                shoulderLine[0]._position,
                shoulderLine[1]._position,
                this._scale,
                this._linePaint
            )
        }

        // Draw hip line
        if (hipLine.size == 2) {
            this.drawLine(
                canvas,
                hipLine[0]._position,
                hipLine[1]._position,
                this._scale,
                this._linePaint
            )
        }
    }

    // -- Draw shape with Canvas --

    /**
     * Draws a point
     */
    private fun drawPoint(
        canvas: Canvas,
        point: PointF?,
        scale: PointF?,
        paint: Paint,
        dotRadius: Float
    ) {
        // No point
        if (point == null)  return

        canvas.drawCircle(
            point.x * (scale?.x ?: 1F),
            point.y * (scale?.y ?: 1F),
            dotRadius,
            paint
        )
    }

    /**
     * Draws a line
     */
    private fun drawLine(
        canvas: Canvas,
        start: PointF?,
        end: PointF?,
        scale: PointF?,
        paint: Paint
    ) {
        // No Line possible
        if (start == null || end == null) return

        canvas.drawLine(
            start.x * (scale?.x ?: 1F),
            start.y * (scale?.y ?: 1F),
            end.x * (scale?.x ?: 1F),
            end.y * (scale?.y ?: 1F),
            paint
        )
    }

    // -- Pose --

    /**
     * Updates the pose thanks to [List] of [KeyPointOfPose] in argument
     */
    fun updatePose(pose: List<KeyPointOfPose>) {
        with(this._pose) {
            clear()
            addAll(pose)
        }

        // Draw again in asynchronous way
        this.postInvalidate()
    }

    // -- Scale --

    /**
     * Updates scale
     */
    fun updateScale(imageWidth: Int, imageHeight: Int) {
        /*
               Image Size  | GraphicOverlay Size
            -------------- + --------------------
             Pose on image |         ?

         */

        val scaleX = this.right.toFloat() / imageWidth.toFloat()
        val scaleY = this.bottom.toFloat() / imageHeight.toFloat()
        this._scale = PointF(scaleX, scaleY)
    }
}