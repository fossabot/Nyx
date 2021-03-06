package com.larryhsiao.nyx.jot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.MonthView;

/**
 * Month View implementation for Jot.
 */
public class JotMonthView extends MonthView {
    private Paint mTextPaint = new Paint();
    private Paint mSchemeBasicPaint = new Paint();
    private float mRadio;
    private int mPadding;

    public JotMonthView(Context context) {
        super(context);
        mTextPaint.setTextSize(dipToPx(context, 8));
        mTextPaint.setColor(0xffFFFFFF);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFakeBoldText(true);

        mSchemeBasicPaint.setAntiAlias(true);
        mSchemeBasicPaint.setStyle(Paint.Style.FILL);
        mSchemeBasicPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeBasicPaint.setColor(0xffed5353);
        mSchemeBasicPaint.setFakeBoldText(true);
        mRadio = dipToPx(context, 7);
        mPadding = dipToPx(context, 4);
    }

    private static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme) {
        mSelectedPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x + mPadding, y + mPadding, x + mItemWidth - mPadding, y + mItemHeight - mPadding, mSelectedPaint);
        return true;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y) {
        mSchemeBasicPaint.setColor(calendar.getSchemeColor());

        canvas.drawText(calendar.getScheme(),
            x + (mItemWidth / 2f) - (mRadio / 2) - (mSchemeBasicPaint.measureText(calendar.getScheme()) / 2),
            y + (mItemHeight / 2f) + mPadding + mRadio,
            mTextPaint);
    }

    @Override
    protected void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected) {
        int cx = x + mItemWidth / 2;
        int top = y - mItemHeight / 6;

        if (isSelected) {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top,
                mSelectTextPaint);
        } else if (hasScheme) {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top,
                calendar.isCurrentDay() ? mCurDayTextPaint :
                    calendar.isCurrentMonth() ? mSchemeTextPaint : mOtherMonthTextPaint);
        } else {
            canvas.drawText(String.valueOf(calendar.getDay()), cx, mTextBaseLine + top,
                calendar.isCurrentDay() ? mCurDayTextPaint :
                    calendar.isCurrentMonth() ? mCurMonthTextPaint : mOtherMonthTextPaint);
        }
    }
}
