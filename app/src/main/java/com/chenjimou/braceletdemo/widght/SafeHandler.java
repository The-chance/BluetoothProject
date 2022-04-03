package com.chenjimou.braceletdemo.widght;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

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
        Class<?> thizClass = thiz.getClass();
        try
        {
            Method method = thizClass.getDeclaredMethod("handleMessage", Message.class);
            method.invoke(thiz, msg);
        }
        catch (Exception e)
        {
            Log.e("SafeHandler", "请在"+thizClass.getSimpleName()+"类中定义handleMessage函数", e);
        }
    }
}
