package com.chenjimou.braceletdemo.widght;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.chenjimou.braceletdemo.ui.MainActivity;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

public class SafeHandler<T> extends Handler
{
    final WeakReference<T> mContext;

    public SafeHandler(@NonNull T context, @NonNull Looper looper)
    {
        super(looper);
        mContext = new WeakReference<>(context);
    }

    @Override
    public void handleMessage(@NonNull Message msg)
    {
        T thiz = mContext.get();
        if (thiz instanceof MainActivity)
        {
            ((MainActivity)thiz).handleMessage(msg);
        }
    }
}
