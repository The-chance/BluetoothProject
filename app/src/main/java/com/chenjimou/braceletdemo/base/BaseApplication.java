package com.chenjimou.braceletdemo.base;

import android.app.Application;
import android.util.Log;

import com.chenjimou.braceletdemo.service.BraceletServiceConnection;

public class BaseApplication extends Application
{
    public static Application sApplication;

    @Override
    public void onCreate()
    {
        super.onCreate();
        sApplication = this;
    }
}