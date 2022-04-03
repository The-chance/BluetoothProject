package com.chenjimou.braceletdemo.uitls;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
 * 收到数据广播Action:"WiFiModeUtil.Connect.ReceiveMessage"
 * 连接断开广播Action:"WiFiModeUtil.Disconnected"
 */
public class WiFiModeUtil {
    private static String mIp;//硬件的IP
    private static int mPort = 5000;//硬件的端口
    public static Socket mSocket = null;//连接成功可得到的Socket
    public static OutputStream outputStream = null;//定义输出流
    public static InputStream inputStream = null;//定义输入流
    public static StringBuffer DataRecivice = new StringBuffer();//数据
    public static List<String> DataList=new ArrayList<>();//数据
    public static boolean connectFlage = true;//连接成功或连接3s后变false
    //    private static int ShowPointSum = 0;//连接时显示 连接中.. 后面点的计数
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
                case 0://接收到数据
                    DataRecivice.append(msg.obj.toString());
                    DataRecivice.append("\n");
                    DataList.add(msg.obj.toString());//添加数据
                    Intent intent = new Intent("WiFiModeUtil.Connect.ReceiveMessage");
                    localBroadcastManager.sendBroadcast(intent);//发送收到数据广播
                    break;
                case 1://连接成功
                    Intent intent2 = new Intent("WiFiModeUtil.Connect.Succeed");
                    localBroadcastManager.sendBroadcast(intent2);//发送连接成功广播
                    readData();//开启接收线程
                    connectFlage = true;
                    break;
                case 2://连接断开
                    Intent intent3 = new Intent("WiFiModeUtil.Disconnected");
                    localBroadcastManager.sendBroadcast(intent3);//发送连接失败广播
                    connectFlage = true;
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
                closeSocketAndStream();
            }
            tcpClientCountDownTimer.cancel();//关掉定时器
            Intent intent = new Intent("WiFiModeUtil.Connect.Fail");
            localBroadcastManager.sendBroadcast(intent);
            Log.d(TAG,"连接失败");
        }
    };

    /**
     * 关掉Socket和输入输出流
     */
    @SuppressLint("LongLogTag")
    public static void closeSocketAndStream() {
        if (outputStream != null) {
            try {
                outputStream.close();
                Log.d(TAG,"关闭输出流");
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }
        if (inputStream != null) {
            try {
                inputStream.close();
                Log.d(TAG,"关闭输入流");
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
        }
        if (mSocket != null) {
            try {
                mSocket.close();
                Log.d(TAG,"关闭Socket");
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = null;
        }
    }

    /**
     * 连接服务器任务
     */
    static class ConnectSeverThread extends Thread {
        @SuppressLint("LongLogTag")
        @Override
        public void run() {
            Log.d(TAG,"连接线程开启");
            while (connectFlage) {
                try {
                    Log.d(TAG,"正在连接...");
                    mSocket = new Socket(mIp, mPort);//进行连接
                    connectFlage = false;//已连接
                    tcpClientCountDownTimer.cancel();//关掉计时器
                    /*连接成功更新显示连接状态的UI*/
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                    Log.d(TAG,"连接成功");
                    inputStream = mSocket.getInputStream();//获取输入流
                    Log.d(TAG,"获取输入流");
                    outputStream = mSocket.getOutputStream();////获取输出流
                    Log.d(TAG,"获取输出流");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d(TAG,"连接过程中出错");
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
    public static void connetByTCP(String IPAdress,int Port) {
        mIp = IPAdress;
        mPort=Port;
        ConnectSeverThread connectSeverThread = new ConnectSeverThread();
        connectSeverThread.start();
        tcpClientCountDownTimer.start();
    }

    /**
     * 向硬件发送数据
     */
    public static void sendData(String data) {
        if(mSocket!=null){
            byte[] sendByte = data.getBytes();
            new Thread() {
                @SuppressLint("LongLogTag")
                @Override
                public void run() {
                    try {
                        Log.d(TAG,"发送线程开启 正在发送中...");
                        DataOutputStream writer = new DataOutputStream(outputStream);
                        writer.write(sendByte, 0, sendByte.length);
                        Log.d(TAG,"已发送数据:"+data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,"发送线程结束");
                }
            }.start();
        }
    }

    /**
     * 连接成功后开启
     * 接收硬件发送的数据
     */
    public static void readData() {
        new Thread() {
            @SuppressLint("LongLogTag")
            @Override
            public void run() {
                Log.d(TAG,"接收线程开启");
                try {
                    while (true) {
                        Thread.sleep(200);
                        //如果连接断开 尝试重连
                        try {
                            /*
                                sendUrgentData()方法
                                它会往输出流发送一个字节的数据，
                                只要对方Socket的SO_OOBINLINE属性没有打开，
                                就会自动舍弃这个字节，
                                就会抛出异常，
                                而SO_OOBINLINE属性默认情况下就是关闭的
                             */
                            mSocket.sendUrgentData(0xFF);//发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
                        } catch (Exception ex) {
                            Log.d(TAG,"连接已断开，请重新进行连接");
                            Message msg=new Message();
                            msg.what=2;
                            msg.obj="连接已断开，请重新进行连接";
                            mHandler.sendMessage(msg);
                        }
                        DataInputStream reader = new DataInputStream(inputStream);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = reader.read(buffer)) != -1) {
                            String data = new String(buffer, 0, len);
                            Log.d(TAG,"接收到数据:"+data);
                            Message msg = new Message();
                            msg.what = 0;
                            msg.obj = data;
                            mHandler.sendMessage(msg);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG,"接收线程结束");
            }
        }.start();
    }
}
