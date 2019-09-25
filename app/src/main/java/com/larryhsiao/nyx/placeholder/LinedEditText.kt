package com.larryhsiao.nyx.placeholder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import com.silverhetch.aura.view.measures.DP
import com.larryhsiao.nyx.R

/**
 * EditText with notebook-like under line
 */
@Deprecated("Move this class to Aura")
class LinedEditText// we need this constructor for LayoutInflater
    (context: Context, attrs: AttributeSet) :
    AppCompatEditText(context, attrs), TextWatcher {
    private val lineWidth = DP(context, 1f).px()

    private val mRect: Rect = Rect()
    private val mPaint: Paint = Paint()

    init {
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = lineWidth
        mPaint.color = context.theme.obtainStyledAttributes(
            attrs,
            intArrayOf(R.styleable.LinedEditText_line_color),
            0,
            0
        ).getColor(0, Color.parseColor("#000000"))

        addTextChangedListener(this)
    }

    override fun onDraw(canvas: Canvas) {
        getLineBounds(0, mRect)
        val count = (height.toFloat() / lineHeight.toFloat()).toInt()

        if (lineCount < count - 1) {
            var append = ""
            for (i in 0..count - lineCount) {
                append += System.lineSeparator()
            }
            setText("$text$append")
        }

        for (i in 0 until count) {
            canvas.drawLine(
                mRect.left.toFloat(),
                (mRect.bottom).toFloat(),
                mRect.right.toFloat(),
                (mRect.bottom).toFloat(),
                mPaint
            )
            mRect.set(
                mRect.left,
                mRect.top + lineHeight,
                mRect.right,
                mRect.bottom + lineHeight
            )
        }

        super.onDraw(canvas)
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(
        s: CharSequence?,
        start: Int,
        count: Int,
        after: Int
    ) {
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }
}