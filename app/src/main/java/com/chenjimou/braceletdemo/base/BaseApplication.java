package com.chenjimou.braceletdemo.base;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BaseApplication extends Application
{
    public static Application altContext; // APP程序上下文
    public static BluetoothSocket btSocket; // 当前蓝牙通信使用的socket
    public static BluetoothDevice btDevice; // 对端蓝牙设备对象
    public static Socket wifiSocket; // 当前WiFi通信使用的socket

    @Override
    public void onCreate()
    {
        super.onCreate();
        altContext = this;
    }

    public static void shutdown()
    {
        try
        {
            btSocket.close();
            btDevice = null;
            wifiSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}