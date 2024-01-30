package com.bytedance.clockapplication.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.bytedance.clockapplication.R;

import java.util.Calendar;
import java.util.Date;

public class Digit extends View {

    private int mSize, mWidth, mHeight, mCenterX, mCenterY;

    private int mDigitSize = 160;
    private int mDateSize = 100;

    private final static String TAG = Clock.class.getSimpleName();

    public Digit(Context context){
        super(context);
    }

    public Digit(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Digit(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(final Canvas canvas){
        super.onDraw(canvas);

        mWidth = getWidth();
        mHeight = getHeight();
        mSize = mWidth < mHeight ? mWidth : mHeight;

        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;

        drawDigit(canvas);
        postInvalidateDelayed(1000);
    }

    private void drawDigit(Canvas canvas){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(mDigitSize);
        paint.setColor(Color.WHITE);

        Typeface font = Typeface.create(Typeface.DEFAULT_BOLD , Typeface.BOLD);
        paint.setTypeface(font);

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        now = new Date(now.getTime() + 8 * 3600 * 1000);
        int nowHours = now.getHours();
        int nowMinutes = now.getMinutes();
        int nowSeconds = now.getSeconds();

        String timeStr = String.format("%02d",nowHours) + " : " +
                         String.format("%02d",nowMinutes) + " : " +
                         String.format("%02d",nowSeconds);
        float[] sz = new float[timeStr.length()];
        paint.getTextWidths(timeStr,sz);
        float wd = 0;
        for(float i : sz) wd += i;
        canvas.drawText(timeStr,mCenterX - wd / 2, mCenterY - mDigitSize / 3,paint);

        // draw date
        paint.setTextSize(mDateSize);
        String dateStr = String.format("%d",now.getYear() + 1900) + " / " +
                         String.format("%02d",now.getMonth() + 1) + " / " +
                         String.format("%02d",now.getDate());
        sz = new float[dateStr.length()];
        paint.getTextWidths(dateStr,sz);
        wd = 0;
        for(float i : sz) wd += i;
        canvas.drawText(dateStr,mCenterX - wd / 2, mCenterY + mDateSize,paint);


    }

}
