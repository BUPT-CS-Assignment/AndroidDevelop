package com.bytedance.clockapplication.widget;

import android.content.Context;
import android.graphics.*;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Calendar;
import java.util.Date;

public class Clock extends View {

    private final static String TAG = Clock.class.getSimpleName();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.012f;

    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;

    private float PANEL_RADIUS = 200.0f;// 表盘半径

    private float HOUR_POINTER_LENGTH;// 指针长度
    private float MINUTE_POINTER_LENGTH;
    private float SECOND_POINTER_LENGTH;
    private float UNIT_DEGREE = (float) (6 * Math.PI / 180);// 一个小格的度数

    private int mSize,mWidth, mHeight, mCenterX, mCenterY, mRadius;

    private int degreesColor;

    private Paint mNeedlePaint;

    public Clock(Context context) {
        super(context);
        init(context, null);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
//        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
//        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();
//
//        if (widthWithoutPadding > heightWithoutPadding) {
//            size = heightWithoutPadding;
//        } else {
//            size = widthWithoutPadding;
//        }
//        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
          setMeasuredDimension(width,height);
    }

    private void init(Context context, AttributeSet attrs) {

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        mNeedlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNeedlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mNeedlePaint.setStrokeCap(Paint.Cap.ROUND);

    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getWidth();
        mHeight = getHeight();
        mSize = mWidth < mHeight ? mWidth : mHeight;
        mRadius =  mSize / 2;
        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;
        PANEL_RADIUS = mRadius;
        HOUR_POINTER_LENGTH = PANEL_RADIUS - 400;
        MINUTE_POINTER_LENGTH = PANEL_RADIUS - 250;
        SECOND_POINTER_LENGTH = PANEL_RADIUS - 150;

        drawDegrees(canvas);
        drawHoursValues(canvas);
        drawNeedles(canvas);
        drawDot(canvas);

        // todo 每一秒刷新一次，让指针动起来
        postInvalidateDelayed(50);

    }

    private void drawDot(Canvas canvas){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(mCenterX,mCenterY,15,paint);
    }

    private void drawDegrees(Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mSize * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = (int) (mRadius * 0.88f);
        int rEnd = (int) (mRadius * 0.95f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)
                paint.setAlpha(CUSTOM_ALPHA);
            else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startX = (int) (mCenterX + rPadded * Math.sin(Math.toRadians(i)));
            int startY = (int) (mCenterY - rPadded * Math.cos(Math.toRadians(i)));

            int stopX = (int) (mCenterX + rEnd * Math.sin(Math.toRadians(i)));
            int stopY = (int) (mCenterY - rEnd * Math.cos(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);

        }
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        // - hoursValuesColor
        Paint mHoursValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHoursValuePaint.setStyle(Paint.Style.FILL);
        mHoursValuePaint.setTextSize(80);
        mHoursValuePaint.setColor(Color.WHITE);
        Paint.FontMetrics fontMetrics = mHoursValuePaint.getFontMetrics();
        for(int i = 1; i <= 12; i++){
            float degree = i * 5 * UNIT_DEGREE;
            float offset = (fontMetrics.top - fontMetrics.bottom) / 4;
            float x = (float) (mCenterX + PANEL_RADIUS * 0.75 * Math.sin(degree) + offset);
            float y = (float) (mCenterY - PANEL_RADIUS * 0.75 * Math.cos(degree) - offset);
            canvas.drawText(String.valueOf(i),x,y,mHoursValuePaint);
        }
    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private void drawNeedles(final Canvas canvas) {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        now = new Date(now.getTime() + 8 * 3600 * 1000);
        // get milion seconds
        int nowMilionSeconds = (int)(now.getTime() % 1000);
        int nowHours = now.getHours();
        int nowMinutes = now.getMinutes();
        int nowSeconds = now.getSeconds();
        int nowDate = now.getDate();

        // draw date
        drawDate(canvas,nowDate);
        // 画秒针
        drawPointer(canvas, 2, nowSeconds + (float)nowMilionSeconds / 1000);
        // 画分针
        // todo 画分针
        drawPointer(canvas, 1, nowMinutes + (float)nowSeconds / 60);
        // 画时针
        drawPointer(canvas, 0, 5 * (nowHours + (float)nowMinutes / 60));


    }


    private void drawPointer(Canvas canvas, int pointerType, float value) {

        float degree;
        float[] pointerHeadXY = new float[2];

        mNeedlePaint.setStrokeWidth(mSize * DEFAULT_DEGREE_STROKE_WIDTH);
        switch (pointerType) {
            case 0:
                degree = value * UNIT_DEGREE;
                mNeedlePaint.setColor(Color.WHITE);
                pointerHeadXY = getPointerHeadXY(HOUR_POINTER_LENGTH, degree);
                break;
            case 1:
                // todo 画分针，设置分针的颜色
                degree = value * UNIT_DEGREE;
                mNeedlePaint.setColor(Color.BLUE);
                pointerHeadXY = getPointerHeadXY(MINUTE_POINTER_LENGTH, degree);
                break;
            case 2:
                degree = value * UNIT_DEGREE;
                mNeedlePaint.setColor(Color.GREEN);
                pointerHeadXY = getPointerHeadXY(SECOND_POINTER_LENGTH, degree);
                break;
        }


        canvas.drawLine(mCenterX, mCenterY, pointerHeadXY[0], pointerHeadXY[1], mNeedlePaint);
    }

    private float[] getPointerHeadXY(float pointerLength, float degree) {
        float[] xy = new float[2];
        xy[0] = (float) (mCenterX + pointerLength * Math.sin(degree));
        xy[1] = (float) (mCenterY - pointerLength * Math.cos(degree));
        return xy;
    }

    private void drawDate(Canvas canvas,int value){
        int mDateSize = 50;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.WHITE);

        RectF rect = new RectF((float) (mCenterX + mRadius * 0.5 - mDateSize), (float) (mCenterY - mDateSize), (float) (mCenterX + mRadius * 0.5 + mDateSize),mCenterY + mDateSize);
        canvas.drawRect(rect,paint);

        paint.setTextSize((int)(mDateSize));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.format("%02d",value),(float) (mCenterX + mRadius * 0.5),mCenterY + mDateSize/3,paint);

    }


}