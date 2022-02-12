package com.chenjimou.braceletdemo.commend;

import android.bluetooth.BluetoothSocket;

import java.io.InputStream;
import java.util.Calendar;

public class HoldBluetoothConnectionCommend implements Runnable
{
    BluetoothSocket socket;
    OnCallBackListener listener;

    public HoldBluetoothConnectionCommend(BluetoothSocket socket)
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
                        if (data.contains("#DATE"))
                        {
                            StringBuilder stringBuilder1 = new StringBuilder();
                            stringBuilder1.append("#TIME");
                            Calendar calendar = Calendar.getInstance();
                            stringBuilder1.append(calendar.get(Calendar.YEAR));
                            stringBuilder1.append(calendar.get(Calendar.MONTH + 1));
                            stringBuilder1.append(calendar.get(Calendar.DATE));
                            stringBuilder1.append(calendar.get(Calendar.HOUR_OF_DAY));
                            stringBuilder1.append(calendar.get(Calendar.MINUTE));
                            stringBuilder1.append(calendar.get(Calendar.SECOND));
                            if (listener != null)
                            {
                                listener.bluetoothCallBack(stringBuilder1.toString());
                            }
                        }
                        else if (data.contains("#TEMP"))
                        {
                            String[] strings = data.split("#TEMP");
                            if (listener != null)
                            {
                                listener.bluetoothCallBack(strings[1]);
                            }
                        }
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
        void bluetoothCallBack(String msg);
    }

    public void setOnCallBackListener(OnCallBackListener listener)
    {
        this.listener = listener;
    }
}
