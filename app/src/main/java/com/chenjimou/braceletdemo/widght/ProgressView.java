package com.chenjimou.braceletdemo.widght;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.chenjimou.braceletdemo.R;
import com.chenjimou.braceletdemo.uitls.DisplayUtils;

import androidx.annotation.Nullable;

public class ProgressView extends View
{
    private Paint mIndicatorBackgroundPaint;
    private Paint mIndicatorPaint;
    private RectF mIndicatorBackground;
    private RectF mIndicator;
    private float proportion = 0f;
    private int totalWidth;
    private int totalHeight = 10;
    private float radius = 0f;
    private int indicatorBackgroundColor;
    private int indicatorColor;

    public ProgressView(Context context)
    {
        this(context, null);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressView);
        indicatorColor = typedArray.getColor(R.styleable.ProgressView_indicatorColor, indicatorColor);
        indicatorBackgroundColor = typedArray.getColor(R.styleable.ProgressView_indicatorBackgroundColor, indicatorBackgroundColor);
        typedArray.recycle();

        mIndicatorBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorBackgroundPaint.setStyle(Paint.Style.FILL);
        mIndicatorBackgroundPaint.setColor(indicatorBackgroundColor);

        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        mIndicatorPaint.setColor(indicatorColor);

        mIndicatorBackground = new RectF();
        mIndicator = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
        totalWidth = getMeasuredWidth();
        mIndicatorBackground.set(0f, 0f, getMeasuredWidth() * 1f, getMeasuredHeight() * 1f);
        radius = getMeasuredHeight() / 2f;
    }

    private int measureWidth(final int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                result = size + getPaddingLeft() + getPaddingRight();
                break;
        }
        result = (mode == MeasureSpec.AT_MOST) ? Math.min(result, size) : result;
        return result;
    }

    private int measureHeight(final int measureSpec)
    {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int result = 0;
        switch (mode)
        {
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                result = totalHeight + getPaddingTop() + getPaddingBottom();
                break;
        }
        result = (mode == MeasureSpec.AT_MOST) ? Math.min(result, size) : result;
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.save();
        canvas.drawRoundRect(mIndicatorBackground, radius, radius, mIndicatorBackgroundPaint);
        canvas.restore();
        float right = mIndicatorBackground.left + totalWidth * proportion;
        mIndicator.set(mIndicatorBackground.left, mIndicatorBackground.top, right, mIndicatorBackground.bottom);
        canvas.save();
        canvas.drawRoundRect(mIndicator, radius, radius, mIndicatorPaint);
        canvas.restore();
    }

    public void setProportion(float proportion) {
        this.proportion = proportion;
        invalidate();
    }

    public void setTotalHeight(int totalHeight) {
        this.totalHeight = totalHeight;
        requestLayout();
        invalidate();
    }
}
