package com.chenjimou.braceletdemo.base;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

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
    private static ThreadPoolExecutor threadPool; // 用于发送指令的线程池（最大并发）

    @Override
    public void onCreate()
    {
        super.onCreate();
        altContext = this;
        threadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60,
                TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    public static void sendOrder(Runnable runnable)
    {
        threadPool.execute(runnable);
    }
}