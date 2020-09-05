package com.mancel.yann.poseanalyser.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
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

    private val _leftPaint = Paint()
    private val _centerPaint = Paint()
    private val _rightPaint = Paint()
    private val _linePaint = Paint()

    private val _dotRadius: Float

    private val _pose = mutableListOf<KeyPointOfPose>()

    private var _scale: PointF? = null

    // CONSTRUCTORS --------------------------------------------------------------------------------

    init {
        // Attributes from xml file, called attrs.xml
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.GraphicOverlay)

        // Colors
        this._leftPaint.color = attributes.getColor(
            R.styleable.GraphicOverlay_leftColor,
            Color.WHITE
        )
        this._centerPaint.color = attributes.getColor(
            R.styleable.GraphicOverlay_centerColor,
            Color.WHITE
        )
        this._rightPaint.color = attributes.getColor(
            R.styleable.GraphicOverlay_rightColor,
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

        // Draw all points
        if (this._pose.isNotEmpty()) {
            this._pose.forEach { keyPoint ->
                this.drawPoint(
                    canvas,
                    keyPoint._position,
                    when {
                        keyPoint._type.name.contains("LEFT") -> this._leftPaint
                        keyPoint._type.name.contains("RIGHT") -> this._rightPaint
                        else -> this._centerPaint
                    },
                    this._scale
                )
            }
        }
    }

    // -- Canvas --

    private fun drawPoint(
        canvas: Canvas,
        point: PointF?,
        paint: Paint,
        scale: PointF?
    ) {
        // No point
        if (point == null)  return

        canvas.drawCircle(
            point.x * (scale?.x ?: 1F),
            point.y * (scale?.y ?: 1F),
            this._dotRadius,
            paint
        )
    }

    private fun drawLine(
        canvas: Canvas,
        start: PointF?,
        end: PointF?,
        paint: Paint
    ) {
        // No Line possible
        if (start == null || end == null) return

        canvas.drawLine(
            0F, //translateX(start.x),
            0F, //translateY(start.y),
            50F, //translateX(end.x),
            50F, //translateY(end.y),
            paint
        )
    }

    // -- Pose --

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
    fun updateScale(scaleX: Float, scaleY: Float) { this._scale = PointF(scaleX, scaleY) }
}