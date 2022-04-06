package com.chenjimou.braceletdemo.uitls;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.chenjimou.braceletdemo.base.BaseApplication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 正在连接广播Action:"WiFiModeUtil.Connecting"
 * 连接失败广播Action:"WiFiModeUtil.Connect.Fail"
 * 连接成功广播Action:"WiFiModeUtil.Connect.Succeed"
 */
public class WiFiModeUtil {
    private static String mIp;//硬件的IP
    private static int mPort = 5000;//硬件的端口
    public static boolean connectFlage = true;//连接成功或连接3s后变false
    private static final String TAG = "WifiDemoLogESP8266ClientActivity";

    /**
     * 本地广播管理器 从外面用:
     * WiFiModeUtil.localBroadcastManager=localBroadcastManager;
     * 进行赋值
     */
    public static LocalBroadcastManager localBroadcastManager;

    /**
     * 处理消息的Handler
     */
    @SuppressLint("HandlerLeak")
    public static Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1://连接成功
                    Intent intent2 = new Intent("WiFiModeUtil.Connect.Succeed");
                    localBroadcastManager.sendBroadcast(intent2);//发送连接成功广播
                    break;
                case 2://连接失败
                    Intent intent3 = new Intent("WiFiModeUtil.Fail");
                    localBroadcastManager.sendBroadcast(intent3);//发送连接失败广播
                    break;
            }
        }
    };

    /***
     * 延时3s的定时器
     * 在开始连接时计时3s
     * 3s未连接上视为连接失败
     */
    private final static CountDownTimer tcpClientCountDownTimer = new CountDownTimer(3000, 300) {
        @Override
        public void onTick(long millisUntilFinished) {//每隔300ms进入
            if (connectFlage) {
                Intent intent = new Intent("WiFiModeUtil.Connecting");
                localBroadcastManager.sendBroadcast(intent);
            }
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onFinish() {//3s后进入(没有取消定时器的情况下)
            if (connectFlage) {
                connectFlage = false;//连接失败
                try {
                    if (BaseApplication.wifiSocket != null) {
                        BaseApplication.wifiSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            tcpClientCountDownTimer.cancel();//关掉定时器
            Intent intent = new Intent("WiFiModeUtil.Connect.Fail");
            localBroadcastManager.sendBroadcast(intent);
            Log.d(TAG, "连接失败");
        }
    };

    /**
     * 连接服务器任务
     */
    static class ConnectSeverThread extends Thread {
        @SuppressLint("LongLogTag")
        @Override
        public void run() {
            Log.d(TAG, "连接线程开启");
            while (connectFlage) {
                try {
                    Log.d(TAG, "正在连接...");
                    BaseApplication.wifiSocket = new Socket(mIp, mPort);//进行连接
                    connectFlage = false;//已连接
                    tcpClientCountDownTimer.cancel();//关掉计时器
                    /*连接成功更新显示连接状态的UI*/
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                    Log.d(TAG, "连接成功");
                } catch (IOException e) {
                    Message msg = new Message();
                    msg.what = 2;
                    mHandler.sendMessage(msg);
                    connectFlage = false;//连接出错
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d(TAG, "连接过程中出错");
                }
            }
        }
    }

    /**
     * 传入硬件服务端IP建立TCP连接
     * 正在连接每200ms          发送一条‘正在连接’广播
     * 3秒后还未连接则连接失败    发送一条‘失败广播’
     * 连接成功会              发送一条‘成功广播’
     *
     * @param IPAdress
     */
    public static void connectByTCP(String IPAdress, int Port) {
        mIp = IPAdress;
        mPort = Port;
        connectFlage=true;
        ConnectSeverThread connectSeverThread = new ConnectSeverThread();
        connectSeverThread.start();//连接
        tcpClientCountDownTimer.start();//计时3s，3s后连接超时
    }
}
