package com.chenjimou.braceletdemo.thread;

import com.chenjimou.braceletdemo.base.BaseApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class HoldConnectionThread extends Thread
{
    Callback callback;
    boolean isWiFi;

    public HoldConnectionThread(boolean isWiFi, Callback callback)
    {
        this.isWiFi = isWiFi;
        this.callback = callback;
    }

    @Override
    public void run()
    {
        try
        {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            isWiFi ? BaseApplication.wifiSocket.getInputStream()
                                   : BaseApplication.btSocket.getInputStream()));
            // 检查线程中断标志位
            while (!Thread.currentThread().isInterrupted())
            {
                StringBuilder sb = new StringBuilder();
                int ch;
                while ((ch=reader.read()) != -1 && ch != '#')
                {
                    sb.append(reader.read());
                }
                String data = sb.toString();
                callback.onResponse(data);
                sb.setLength(0); // 重置stringBuilder
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public interface Callback
    {
        void onResponse(String msg);
    }
}
