package com.chenjimou.braceletdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;

public class BraceletService extends Service
{
    private final IBinder mBinder = new BraceletBinder();
    private ThreadPoolExecutor threadPool;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        threadPool = new ThreadPoolExecutor(3, 5,
                1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        threadPool.shutdownNow();
        if (!threadPool.isTerminated())
        {
            threadPool.shutdown();
        }
    }

    public ThreadPoolExecutor getThreadPool()
    {
        return threadPool;
    }

    public class BraceletBinder extends Binder
    {
        public BraceletService getService()
        {
            return BraceletService.this;
        }
    }
}
