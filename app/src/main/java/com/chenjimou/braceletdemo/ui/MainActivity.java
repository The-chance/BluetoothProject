package com.chenjimou.braceletdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.chenjimou.braceletdemo.R;
import com.chenjimou.braceletdemo.base.BaseApplication;
import com.chenjimou.braceletdemo.order.Order;
import com.chenjimou.braceletdemo.thread.Dispatcher;
import com.chenjimou.braceletdemo.thread.HoldConnectionThread;
import com.chenjimou.braceletdemo.databinding.ActivityMainBinding;
import com.chenjimou.braceletdemo.order.OrderType;
import com.chenjimou.braceletdemo.widght.ControlLayout;
import com.chenjimou.braceletdemo.widght.SafeHandler;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
<<<<<<< HEAD
    private ActivityMainBinding mBinding;

    private static final int BLUETOOTH_REQUEST_CODE = 0;
    private static final int WIFI_REQUEST_CODE = 1;

    private static final int EXCEPTION = 2;
    private static final int FAN = 3;
    private static final int AIR_CONDITIONER = 4;
    private static final int WINDOW = 5;
    private static final int BRACELET = 6;
=======
    ActivityMainBinding mBinding;

    /* 安全的Handler，避免Activity直接与后台线程绑定(内部类)导致内存泄漏 */
>>>>>>> 6389a6554f70b131857747513323071fda566c89
    final SafeHandler<MainActivity> handler = new SafeHandler<>(this, Looper.getMainLooper());

    /* 监听对端蓝牙设备的线程 */
    HoldConnectionThread wifiThread;
    /* 监听对端WiFi设备的线程 */
    HoldConnectionThread btThread;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();
    }

    /**
     * 初始化
     */
    private void init()
    {
        mBinding.appBarMain.toolbar.setTitle("");
        mBinding.appBarMain.tvTitle.setText("智慧家居");
        setSupportActionBar(mBinding.appBarMain.toolbar);

        mBinding.appBarMain.airConditionerControlLayout.setValueRange(0, 39);
        mBinding.appBarMain.airConditionerControlLayout.setOnSendDataListener(new ControlLayout.OnSendDataListener()
        {
            @Override
            public void onSendData(int type)
            {
                int currentValue = mBinding.appBarMain.airConditionerControlLayout.getCurrentValue();
                switch (type)
                {
                    case ControlLayout.Type.TYPE_UP:
                        sendDataToAirConditioner("#KT" + (currentValue + 1), true);
                        break;
                    case ControlLayout.Type.TYPE_DOWN:
                        sendDataToAirConditioner("#KT" + (currentValue - 1), false);
                        break;
                    case ControlLayout.Type.TYPE_SHUTDOWN:
                        break;
                }
            }
        });

        mBinding.appBarMain.fanControlLayout.setValueRange(0, 3);
        mBinding.appBarMain.fanControlLayout.setOnSendDataListener(new ControlLayout.OnSendDataListener()
        {
            @Override
            public void onSendData(int type)
            {
                int currentValue = mBinding.appBarMain.fanControlLayout.getCurrentValue();
                switch (type)
                {
                    case ControlLayout.Type.TYPE_UP:
                        sendDataToFan("#FS" + (currentValue + 1), true);
                        break;
                    case ControlLayout.Type.TYPE_DOWN:
                        sendDataToFan("#FS" + (currentValue - 1), false);
                        break;
                    case ControlLayout.Type.TYPE_SHUTDOWN:
                        break;
                }
            }
        });

        mBinding.appBarMain.windowControlLayout.setValueRange(0, 180);
        mBinding.appBarMain.windowControlLayout.setOnSendDataListener(new ControlLayout.OnSendDataListener()
        {
            @Override
            public void onSendData(int type)
            {
                int currentValue = mBinding.appBarMain.fanControlLayout.getCurrentValue();
                switch (type)
                {
                    case ControlLayout.Type.TYPE_UP:
                        sendDataToWindow("#MC" + (currentValue + 10), true);
                        break;
                    case ControlLayout.Type.TYPE_DOWN:
                        sendDataToWindow("#MC" + (currentValue - 10), false);
                        break;
                    case ControlLayout.Type.TYPE_SHUTDOWN:
                        break;
                }
            }
        });
    }

    /**
     * 初始化menu菜单
     * @param menu 菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * menu菜单项的点击回调
     * @param item 菜单项
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.bluetoothConnection:
                startActivityForResult(new Intent(MainActivity.this, btConnectActivity.class), BLUETOOTH);
                return true;
            case R.id.wifiConnection:
                startActivityForResult(new Intent(MainActivity.this, WiFiConnectActivity.class), WIFI);
                return true;
//            case R.id.roomConnection:
//                startActivity(new Intent(MainActivity.this, RoomConnectActivity.class));
//                return true;
//            case R.id.connectWindow:
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
<<<<<<< HEAD
     * 发送数据给手环
     * @param order
=======
     * 发送指令给对端手环
     * @param order 指令
>>>>>>> 6389a6554f70b131857747513323071fda566c89
     */
    private void sendDataToBracelet(String order)
    {
        Dispatcher.getInstance().sendOrder(new Order(OrderType.TYPE_BRACELET, order, new Order.Callback()
        {
            @Override
            public void onFailure(Exception e)
            {
                handler.sendEmptyMessage(EXCEPTION);
            }

            @Override
            public void onResponse()
            {
                // do nothing
            }
        }));
    }

    /**
     * 发送指令给对端空调
     * @param order 指令
     * @param isUp true为升高温度，false为降低温度
     */
    private void sendDataToAirConditioner(String order, boolean isUp)
    {
        Dispatcher.getInstance().sendOrder(new Order(OrderType.TYPE_AIR_CONDITIONER, order, new Order.Callback()
        {
            @Override
            public void onFailure(Exception e)
            {
                handler.sendEmptyMessage(EXCEPTION);
            }

            @Override
            public void onResponse()
            {
                Message message = Message.obtain();
                message.what = AIR_CONDITIONER;
                Bundle bundle = new Bundle();
                bundle.putBoolean("isUp", isUp);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }));
    }

    /**
     * 发送指令给对端风扇
     * @param order 指令
     * @param isUp true为加大风速，false为降低风速
     */
    private void sendDataToFan(String order, boolean isUp)
    {
        Dispatcher.getInstance().sendOrder(new Order(OrderType.TYPE_FAN, order, new Order.Callback()
        {
            @Override
            public void onFailure(Exception e)
            {
                handler.sendEmptyMessage(EXCEPTION);
            }

            @Override
            public void onResponse()
            {
                Message message = Message.obtain();
                message.what = FAN;
                Bundle bundle = new Bundle();
                bundle.putBoolean("isUp", isUp);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }));
    }

    /**
     * 发送指令给对端窗户
     * @param order 指令
     */
    private void sendDataToWindow(String order, boolean isUp)
    {
        Dispatcher.getInstance().sendOrder(new Order(OrderType.TYPE_WINDOW, order, new Order.Callback()
        {
            @Override
            public void onFailure(Exception e)
            {
                handler.sendEmptyMessage(EXCEPTION);
            }

            @Override
            public void onResponse()
            {
                Message message = Message.obtain();
                message.what = WINDOW;
                Bundle bundle = new Bundle();
                bundle.putBoolean("isUp", isUp);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }));
    }

    /**
     * 开启WiFi监听线程
     */
    void holdBluetoothConnection()
    {
        btThread = new HoldConnectionThread(false, msg ->
        {
            if (msg.contains("#DATE"))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("#TIME");
                Calendar calendar = Calendar.getInstance();
                sb.append(calendar.get(Calendar.YEAR));
                sb.append(calendar.get(Calendar.MONTH + 1));
                sb.append(calendar.get(Calendar.DATE));
                sb.append(calendar.get(Calendar.HOUR_OF_DAY));
                sb.append(calendar.get(Calendar.MINUTE));
                sb.append(calendar.get(Calendar.SECOND));
                sendDataToBracelet(sb.toString());
            }
            else if (msg.contains("#TEMP"))
            {
                String[] strings = msg.split("#TEMP");
                Message message = Message.obtain();
                message.what = BRACELET;
                Bundle bundle = new Bundle();
                bundle.putString("temp", strings[1]);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        });
        btThread.start();
    }

    /**
<<<<<<< HEAD
     * 从连接后返回MainActivity时会被调用
     */
    private void holdWiFiConnection()
=======
     * 开启蓝牙监听线程
     */
    void holdWiFiConnection()
>>>>>>> 6389a6554f70b131857747513323071fda566c89
    {
        wifiThread = new HoldConnectionThread(true, msg ->
        {
            // do nothing
        });
        wifiThread.start();
    }

    /**
     * 安全Handler的消息回调
     * @param msg 消息
     */
    public void handleMessage(Message msg)
    {
        switch (msg.what)
        {
            case EXCEPTION:
                Toast.makeText(MainActivity.this,"指令发送失败，请重试！",Toast.LENGTH_SHORT).show();
                break;
            case FAN:
                if (msg.getData().getBoolean("isUp")) mBinding.appBarMain.fanControlLayout.increase();
                else mBinding.appBarMain.fanControlLayout.reduce();
                break;
            case AIR_CONDITIONER:
                if (msg.getData().getBoolean("isUp")) mBinding.appBarMain.airConditionerControlLayout.increase();
                else mBinding.appBarMain.airConditionerControlLayout.reduce();
                break;
            case WINDOW:
                if (msg.getData().getBoolean("isUp")) mBinding.appBarMain.windowControlLayout.increase();
                else mBinding.appBarMain.windowControlLayout.reduce();
                break;
            case BRACELET:
                mBinding.appBarMain.tvTemperature.setText(msg.getData().getString("temp"));
                break;
        }
    }

    /**
     * 上一个activity结束的回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case BLUETOOTH:
                if (resultCode == RESULT_OK)
                {
                    mBinding.appBarMain.tvBraceletState.setVisibility(View.GONE);
                    holdBluetoothConnection();
                }
                break;
            case WIFI:
                if (resultCode == RESULT_OK)
                {
                    holdWiFiConnection();
                }
                break;
        }
    }

    /**
     * 活动被销毁回调，中断所有的监听线程，清理资源
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (btThread != null) btThread.interrupt();
        if (wifiThread != null) wifiThread.interrupt();
        BaseApplication.shutdown();
    }

    private static final int BLUETOOTH = 0;
    private static final int WIFI = 1;
    private static final int EXCEPTION = 2;
    private static final int FAN = 3;
    private static final int AIR_CONDITIONER = 4;
    private static final int WINDOW = 5;
    private static final int BRACELET = 6;
}