package com.chenjimou.braceletdemo.widght;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.chenjimou.braceletdemo.R;

import androidx.annotation.Nullable;

public class LoadingView extends View
{
    Paint textPaint;
    Paint cursorPaint;
    Paint ripplePaint;
    int totalWidth = 0;
    int totalHeight = 0;
    float middleX = 0;
    float middleY = 0;
    float ripple;
    int alpha;
    int textWidth;
    int textStartX;
    String middleText;
    int textSize = sp2px(20);
    final Rect textBound = new Rect();

    public LoadingView(Context context)
    {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs)
    {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        middleText = typedArray.getString(R.styleable.LoadingView_text);
        typedArray.recycle();

        textPaint = new Paint();
        textPaint.setColor(getResources().getColor(R.color.white));
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));

        cursorPaint = new Paint();
        cursorPaint.setColor(getResources().getColor(R.color.white));
        cursorPaint.setAntiAlias(true);
        cursorPaint.setDither(true);
        cursorPaint.setStyle(Paint.Style.STROKE);
        cursorPaint.setStrokeWidth(8);

        ripplePaint = new Paint();
        ripplePaint.setColor(getResources().getColor(R.color.white));
        ripplePaint.setAntiAlias(true);
        ripplePaint.setDither(true);
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setStrokeWidth(8);
        ripplePaint.setAlpha(100);

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "ripple", 1f, 2f);
        animator.setDuration(2000);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);

        textPaint.getTextBounds(middleText, 0, middleText.length(), textBound);
        textWidth = (int) (textPaint.measureText(middleText) + .5f);

        totalWidth = getMeasuredWidth();
        totalHeight = getMeasuredHeight();
        middleX = totalWidth / 2f;
        middleY = totalHeight / 2f;
        textStartX = totalWidth / 2 - textWidth / 2;
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
                result = size + getPaddingTop() + getPaddingBottom();
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
        canvas.drawText(middleText,
                textStartX,
                totalHeight / 2f - (textPaint.descent()/2 + textPaint.ascent()/2),
                textPaint);
        canvas.restore();
        canvas.save();
        canvas.drawCircle(middleX, middleY, totalWidth * 0.2f, cursorPaint);
        canvas.restore();
        ripplePaint.setAlpha(alpha);
        canvas.save();
        canvas.drawCircle(middleX, middleY, totalWidth * 0.2f * ripple, ripplePaint);
        canvas.restore();
    }

    public float getRipple()
    {
        return ripple;
    }

    public void setRipple(float ripple)
    {
        alpha = (int) (100 * (2 - ripple));
        this.ripple = ripple;
        invalidate();
    }

    static int sp2px(float dp)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, Resources.getSystem().getDisplayMetrics());
    }
}
