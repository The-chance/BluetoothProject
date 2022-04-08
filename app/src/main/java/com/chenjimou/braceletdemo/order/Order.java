package com.chenjimou.braceletdemo.order;

import static androidx.constraintlayout.widget.Constraints.TAG;

import static com.chenjimou.braceletdemo.order.OrderType.TYPE_AIR_CONDITIONER;
import static com.chenjimou.braceletdemo.order.OrderType.TYPE_BRACELET;
import static com.chenjimou.braceletdemo.order.OrderType.TYPE_FAN;
import static com.chenjimou.braceletdemo.order.OrderType.TYPE_WINDOW;

import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
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
    //这里用到的就是下面的Callback接口，调用失败直接返回异常，成果则返回


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
            Log.d(TAG, "run: "+orderType);
            switch (orderType)
            {
                case TYPE_BRACELET:
                    BaseApplication.btSocket.getOutputStream().write(order.getBytes());
                    Log.d(TAG, "run: "+TYPE_BRACELET);
                    break;
                case TYPE_AIR_CONDITIONER:
                    Log.d(TAG, "run: "+TYPE_AIR_CONDITIONER);
                case TYPE_WINDOW:
                    Log.d(TAG, "run: "+TYPE_WINDOW);
                case TYPE_FAN:
                    Log.d(TAG, "run: "+ TYPE_FAN);
                    BaseApplication.wifiSocket.getOutputStream().write(order.getBytes());
                    break;
            }
            callback.onResponse();
            Log.d(TAG, "run: "+"成功获得了传输的字节流");
        }
        catch (Exception e)
        {
            Log.d(TAG, "run: "+"获得字节流失败");
            callback.onFailure(e);
        }


    }

    public interface Callback
    {
        void onFailure(Exception e);
        void onResponse();
    }
}
