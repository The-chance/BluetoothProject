package com.chenjimou.braceletdemo.order;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.chenjimou.braceletdemo.base.BaseApplication;
import com.chenjimou.braceletdemo.ui.MainActivity;

import java.io.IOException;

import androidx.annotation.NonNull;

public class Order implements Runnable
{
    String order;
    OrderType orderType;
    Callback callback;

    public Order(OrderType orderType, String order, Callback callback)
    {
        this.orderType = orderType;
        this.order = order;
        this.callback = callback;
    }

    @Override
    public void run()
    {
        try
        {
            switch (orderType)
            {
                case TYPE_BRACELET:
                    BaseApplication.btSocket.getOutputStream().write(order.getBytes());
                    break;
                case TYPE_AIR_CONDITIONER:
                case TYPE_WINDOW:
                case TYPE_FAN:
                    BaseApplication.wifiSocket.getOutputStream().write(order.getBytes());
                    break;
            }
            callback.onResponse();
        }
        catch (IOException e)
        {
            callback.onFailure(e);
        }
    }

    public interface Callback
    {
        void onFailure(Exception e);
        void onResponse();
    }
}
