package com.judekwashie.balancemeter

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import java.text.NumberFormat

class BalanceMeterView @JvmOverloads
constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    View(context, attr, defStyleAttr, defStyleRes), ValueAnimator.AnimatorUpdateListener {

    private var remTextPosition: Float
    private var strokeWidth: Float
    private var balanceValue: Float = 0f
    private var maxBalanceValue: Float = 0f
    private var balanceMeterValue: Float = 0f
    private var currencySymbol: String?

    private var backgroundPaint: Paint = Paint()
    private var foregroundPaint: Paint = Paint()
    private var balanceTextPaint: Paint = Paint()
    private var remainingTextPaint: Paint = Paint()
    private var maxBalanceValueTextPaint: Paint = Paint()

    private var rectF: RectF = RectF()

    private lateinit var animator: ValueAnimator

    private var nf: NumberFormat

    init {
        context.theme.obtainStyledAttributes(attr, R.styleable.BalanceMeterView, 0, 0).apply {
            maxBalanceValue = getFloat(R.styleable.BalanceMeterView_max_balance, 3000.00f)
            currencySymbol =
                if (getString(R.styleable.BalanceMeterView_currency_symbol) == null) "₵" else getString(
                    R.styleable.BalanceMeterView_currency_symbol
                )
            this@BalanceMeterView.strokeWidth =
                getFloat(R.styleable.BalanceMeterView_meter_strokeWidth, 15f)

            setPaintProperties(backgroundPaint, Color.LTGRAY)
            setPaintProperties(
                foregroundPaint,
                getColor(R.styleable.BalanceMeterView_meter_color, Color.BLUE)
            )
            setPaintProperties(
                balanceTextPaint,
                Color.BLACK,
                Paint.Style.FILL,
                Paint.Align.CENTER,
                70f
            )
            setPaintProperties(
                remainingTextPaint,
                Color.LTGRAY,
                Paint.Style.FILL,
                Paint.Align.CENTER,
                50f
            )
            setPaintProperties(
                maxBalanceValueTextPaint,
                Color.BLACK,
                Paint.Style.FILL,
                Paint.Align.CENTER,
                50f
            )
            recycle()
        }

        remTextPosition = balanceTextPaint.descent() - balanceTextPaint.ascent()

        nf = NumberFormat.getInstance()
        nf.apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setUpRectF()

        canvas?.let {
            canvas.drawArc(rectF, 135f, 270f, false, backgroundPaint)
            canvas.drawArc(rectF, 135f, balanceMeterValue, false, foregroundPaint)
            canvas.drawText(
                "$currencySymbol ${nf.format(balanceValue)}",
                width / 2f,
                height / 2f,
                balanceTextPaint
            )
            canvas.drawText(
                "remaining",
                width / 2f,
                height / 2f + remTextPosition,
                remainingTextPaint
            )
            canvas.drawText(
                "${nf.format(maxBalanceValue)} GHS / day",
                width / 2f,
                rectF.bottom,
                maxBalanceValueTextPaint
            )
        }
    }

    private fun setPaintProperties(
        paint: Paint,
        paintColor: Int,
        paintStyle: Paint.Style = Paint.Style.STROKE,
        textAlign: Paint.Align = Paint.Align.CENTER,
        textSize: Float = 0f
    ) {
        paint.apply {
            style = paintStyle
            strokeCap = Paint.Cap.ROUND
            color = paintColor
            strokeWidth = this@BalanceMeterView.strokeWidth
            isAntiAlias = true
            this.textAlign = textAlign
            this.textSize = textSize
        }
    }

    private fun setUpRectF() {
        rectF.apply {
            set(strokeWidth, strokeWidth, width - strokeWidth, width - strokeWidth)
        }
    }

    fun startAnim() {
        animator = ValueAnimator.ofFloat(1f, maxBalanceValue)
        animator.interpolator = LinearInterpolator()
        animator.duration = 3000
        animator.repeatCount = 0
        animator.addUpdateListener(this)
        animator.start()
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        balanceValue = animation?.animatedValue as Float
        balanceMeterValue = (balanceValue / maxBalanceValue) * 270f
        invalidate()
    }
}
