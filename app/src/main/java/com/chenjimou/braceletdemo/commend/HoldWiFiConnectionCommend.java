package com.chenjimou.braceletdemo.commend;

import java.io.InputStream;
import java.net.Socket;
import java.util.Calendar;

public class HoldWiFiConnectionCommend implements Runnable
{
    Socket socket;
    OnCallBackListener listener;

    public HoldWiFiConnectionCommend(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        // todo 死循环的条件：线程没有被中断（isInterrupted返回false），线程池没有被shutdown，没有断开与Service的绑定，MainActivity仍存活
        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                InputStream inputStream = socket.getInputStream();
                if (inputStream != null)
                {
                    StringBuilder stringBuilder = new StringBuilder();
                    Thread.sleep(200);
                    while (inputStream.available() != 0)
                    {
                        stringBuilder.append((char)inputStream.read());
                    }
                    if (!stringBuilder.toString().equals(""))
                    {
                        String data = stringBuilder.toString();
                        listener.wifiCallBack(data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // todo 如果线程被中断，注销监听
        listener = null;
    }

    public interface OnCallBackListener
    {
        void wifiCallBack(String msg);
    }

    public void setOnCallBackListener(OnCallBackListener listener)
    {
        this.listener = listener;
    }
}
