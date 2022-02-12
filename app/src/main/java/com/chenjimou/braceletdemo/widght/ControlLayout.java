package com.chenjimou.braceletdemo.widght;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.chenjimou.braceletdemo.R;

import java.security.PublicKey;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ControlLayout extends FrameLayout implements View.OnClickListener
{
    private final Context mContext;
    private ProgressView progressView;
    private int startValue;
    private int endValue;
    private int currentValue;
    private int totalValue;
    private int multiple = 1;
    private OnSendDataListener listener;

    public ControlLayout(@NonNull Context context)
    {
        this(context, null);
    }

    public ControlLayout(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ControlLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mContext = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ControlLayout);
        multiple = typedArray.getInteger(R.styleable.ControlLayout_multiple, multiple);
        typedArray.recycle();

        initView();
    }

    private void initView()
    {
        View view = View.inflate(mContext, R.layout.widght_control, this);
        progressView = view.findViewById(R.id.progressView);
        ImageView iv_up = view.findViewById(R.id.iv_up);
        ImageView iv_down = view.findViewById(R.id.iv_down);
        ImageView iv_shutdown = view.findViewById(R.id.iv_shutdown);

        iv_up.setOnClickListener(this);
        iv_down.setOnClickListener(this);
        iv_shutdown.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.iv_up:
                if (null != listener)
                {
                    listener.onSendData(Type.TYPE_UP);
                }
                break;
            case R.id.iv_down:
                if (null != listener)
                {
                    listener.onSendData(Type.TYPE_DOWN);
                }
                break;
            case R.id.iv_shutdown:
                if (null != listener)
                {
                    listener.onSendData(Type.TYPE_SHUTDOWN);
                }
                break;
        }
    }

    public void setValueRange(int start, int end)
    {
        if (start > end)
        {
            startValue = 0;
            endValue = 0;
            totalValue = 0;
            return;
        }
        startValue = start;
        endValue = end;
        totalValue = endValue - startValue;
    }

    public void setCurrentValue(int current)
    {
        if (current < startValue || current > endValue)
        {
            currentValue = 0;
            return;
        }
        currentValue = current;
        progressView.setProportion((current - startValue) * 1f / totalValue);
    }

    @IntDef({Type.TYPE_UP, Type.TYPE_DOWN, Type.TYPE_SHUTDOWN})
    public @interface Type
    {
        int TYPE_UP = 0;
        int TYPE_DOWN = 1;
        int TYPE_SHUTDOWN = 2;
    }

    public interface OnSendDataListener
    {
        void onSendData(int type);
    }

    public void setOnSendDataListener(OnSendDataListener listener)
    {
        this.listener = listener;
    }

    public int getCurrentValue()
    {
        return currentValue;
    }

    public void increase()
    {
        if (currentValue < endValue)
        {
            currentValue += multiple;
            progressView.setProportion((currentValue - startValue) * 1f / totalValue);
        }
    }

    public void reduce()
    {
        if (currentValue > startValue)
        {
            currentValue -= multiple;
            progressView.setProportion((currentValue - startValue) * 1f / totalValue);
        }
    }
}
