package com.chenjimou.braceletdemo.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.chenjimou.braceletdemo.base.BaseApplication;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BraceletServiceConnection implements ServiceConnection
{
    private static volatile BraceletServiceConnection mInstance;
    private BraceletService mService;

    private BraceletServiceConnection() { }

    public static BraceletServiceConnection getInstance()
    {
        if (mInstance == null)
        {
            synchronized (BraceletServiceConnection.class)
            {
                if (mInstance == null)
                {
                    mInstance = new BraceletServiceConnection();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        BraceletService.BraceletBinder binder = (BraceletService.BraceletBinder) service;
        mService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        mService = null;
        tryConnection();
    }

    @Override
    public void onBindingDied(ComponentName name)
    {
        mService = null;
        tryConnection();
    }

    public void tryConnection()
    {
        Intent intent = new Intent(BaseApplication.sApplication, BraceletService.class);
        BaseApplication.sApplication.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void disConnection()
    {
        BaseApplication.sApplication.unbindService(this);
    }

    public void sendCommend(Runnable runnable)
    {
        if (mService != null)
        {
            mService.getThreadPool().execute(runnable);
        }
    }

    public <T> T sendCommend(Callable<T> callable)
    {
        T result = null;
        if (mService != null)
        {
            Future<T> future = mService.getThreadPool().submit(callable);
            try
            {
                // todo 这里会阻塞
                result = future.get();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return result;
    }
}
