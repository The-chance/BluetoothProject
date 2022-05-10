package com.chenjimou.braceletdemo.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
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
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mBinding;

    private static final int BLUETOOTH_REQUEST_CODE = 0;
    private static final int WIFI_REQUEST_CODE = 1;

    private static final int EXCEPTION = 2;
    private static final int FAN = 3;
    private static final int AIR_CONDITIONER = 4;
    private static final int WINDOW = 5;
    private static final int BRACELET = 6;
    private static final int FAILL = 7;

    private static final int BLUETOOTH = 0;
    private static final int WIFI = 1;



    /* 安全的Handler，避免Activity直接与后台线程绑定(内部类)导致内存泄漏 */

    final SafeHandler<MainActivity> handler = new SafeHandler<>(this, Looper.getMainLooper());

    /* 监听对端蓝牙设备的线程 */
    HoldConnectionThread wifiThread;
    /* 监听对端WiFi设备的线程 */
    HoldConnectionThread btThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();
    }


    /**
     * 初始化
     */
    private void init() {
        mBinding.appBarMain.toolbar.setTitle("");
        mBinding.appBarMain.tvTitle.setText("智慧家居");
        setSupportActionBar(mBinding.appBarMain.toolbar);
        mBinding.appBarMain.airConditionerControlLayout.setValueRange(16, 30);
        mBinding.appBarMain.airConditionerControlLayout.setCurrentValue(26);
        mBinding.appBarMain.airConditionerControlLayout.setOnSendDataListener(new ControlLayout.OnSendDataListener() {
            @Override
            public void onSendData(int type) {
                mBinding.appBarMain.autoAdjustSwitch.setChecked(false);
                int currentValue = mBinding.appBarMain.airConditionerControlLayout.getCurrentValue();
                boolean isOpen = mBinding.appBarMain.airConditionerControlLayout.isOpen();
                switch (type) {
                    case ControlLayout.Type.TYPE_UP:
                        if (!isOpen) {
                            Toast.makeText(MainActivity.this, "空调未开启", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (currentValue + 1 > 30) {
                            return;
                        }
                        sendDataToAirConditioner("#KT" + (currentValue + 1) + ".00COLD", true, true);
                        break;
                    case ControlLayout.Type.TYPE_DOWN:
                        if (!isOpen) {
                            Toast.makeText(MainActivity.this, "空调未开启", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (currentValue - 1 < 16) {
                            return;
                        }
                        sendDataToAirConditioner("#KT" + (currentValue - 1) + ".00COLD", false, true);
                        break;
                    case ControlLayout.Type.TYPE_SHUTDOWN:
                        //打开或关闭空调
                        if (isOpen) {
                            sendDataToAirConditioner("#KT00.00SHUTDOWN", false, false);
                        } else {
                            //恢复空调26度
                            if (currentValue > 26) {
                                for (int i = 0; i < currentValue - 26; i++)
                                    mBinding.appBarMain.airConditionerControlLayout.reduce();
                            } else {
                                for (int i = 0; i < 26 - currentValue; i++)
                                    mBinding.appBarMain.airConditionerControlLayout.increase();
                            }
                            sendDataToAirConditioner("#KT26.00OPEN", false, false);
                        }
                        break;
                }
            }
        });

        mBinding.appBarMain.fanControlLayout.setValueRange(0, 3);
        mBinding.appBarMain.fanControlLayout.setOnSendDataListener(new ControlLayout.OnSendDataListener() {
            @Override
            public void onSendData(int type) {
                mBinding.appBarMain.autoAdjustSwitch.setChecked(false);
                int currentValue = mBinding.appBarMain.fanControlLayout.getCurrentValue();
                switch (type) {
                    case ControlLayout.Type.TYPE_UP:
                        if (currentValue + 1 > 3) {
                            return;
                        }
                        sendDataToFan("#FS0" + (currentValue + 1), true, true);
                        break;
                    case ControlLayout.Type.TYPE_DOWN:
                        if (currentValue - 1 < 0) {
                            return;
                        }
                        sendDataToFan("#FS0" + (currentValue - 1), false, true);
                        break;
                    case ControlLayout.Type.TYPE_SHUTDOWN:
                        //关风扇
                        for (int i = 0; i < currentValue; i++)
                            mBinding.appBarMain.fanControlLayout.reduce();
                        sendDataToFan("#FS00", false, true);
                        break;
                }
            }
        });

        mBinding.appBarMain.windowControlLayout.setValueRange(0, 1);
        mBinding.appBarMain.windowControlLayout.setOnSendDataListener(new ControlLayout.OnSendDataListener() {
            @Override
            public void onSendData(int type) {
                mBinding.appBarMain.autoAdjustSwitch.setChecked(false);
                switch (type) {
                    case ControlLayout.Type.TYPE_UP:
                        sendDataToWindow("#MC" + "001", true);
                        break;
                    case ControlLayout.Type.TYPE_DOWN:
                    case ControlLayout.Type.TYPE_SHUTDOWN:
                        sendDataToWindow("#MC" + "000", false);
                        break;
                }
            }
        });
        mBinding.appBarMain.autoAdjustSwitch.setChecked(false);
        mBinding.appBarMain.autoAdjustSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                BaseApplication.isAutoAdjust = b;
                if (b) {
                    mBinding.appBarMain.controlMode.setText("自动模式");
                    Toast.makeText(MainActivity.this, "自动调节已打开", Toast.LENGTH_SHORT).show();
                    sendDataToChooseMode("#MODE1");//发自动模式指令
                } else {
                    mBinding.appBarMain.controlMode.setText("手动模式");
                    Toast.makeText(MainActivity.this, "自动调节已关闭", Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < 10; i++) {
                        if(BaseApplication.wifiSocket==null) return;
                        sendDataToChooseMode("#MODE0");//发手动模式指令
                    }
                }
            }
        });
    }

    /**
     * 初始化menu菜单
     *
     * @param menu 菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * menu菜单项的点击回调
     *
     * @param item 菜单项
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetoothConnection:
                startActivityForResult(new Intent(MainActivity.this, btConnectActivity.class), BLUETOOTH);
                return true;
            case R.id.wifiConnection:
                startActivityForResult(new Intent(MainActivity.this, WiFiConnectActivity.class), WIFI);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * <<<<<<< HEAD
     * 发送数据给手环
     *
     * @param order =======
     *              发送指令给对端手环
     * @param order 指令
     *              >>>>>>> 6389a6554f70b131857747513323071fda566c89
     */
    private void sendDataToBracelet(String order) {

        Log.d("MainActivity", "clicksend");
        Dispatcher.getInstance().sendOrder(new Order(OrderType.TYPE_BRACELET, order, new Order.Callback() {
            @Override
            public void onFailure(Exception e) {
                Log.d("MainActivity", "onFailure ");
                handler.sendEmptyMessage(EXCEPTION);
            }

            @Override
            public void onResponse() {
                Log.d("MainActivity", "onResponse: ");
                //最终手机蓝牙收到信号，将时间信息发送给
            }
        }));
    }

    /**
     * 发送指令给对端空调
     *
     * @param order 指令
     * @param isUp  true为升高温度，false为降低温度
     */
    private void sendDataToAirConditioner(String order, boolean isUp, boolean isChange) {
        Dispatcher.getInstance().sendOrder(new Order(OrderType.TYPE_AIR_CONDITIONER, order, new Order.Callback() {
            @Override
            public void onFailure(Exception e) {
                handler.sendEmptyMessage(EXCEPTION);
            }

            @Override
            public void onResponse() {
                Message message = Message.obtain();
                message.what = AIR_CONDITIONER;
                Bundle bundle = new Bundle();
                bundle.putString("order", order);
                bundle.putBoolean("isUp", isUp);
                bundle.putBoolean("isChange", isChange);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }));
    }

    /**
     * 发送指令给对端风扇
     *
     * @param order 指令
     * @param isUp  true为加大风速，false为降低风速
     */
    private void sendDataToFan(String order, boolean isUp, boolean isChange) {
        Dispatcher.getInstance().sendOrder(new Order(OrderType.TYPE_FAN, order, new Order.Callback() {
            @Override
            public void onFailure(Exception e) {
                handler.sendEmptyMessage(EXCEPTION);
            }

            @Override
            public void onResponse() {
                Message message = Message.obtain();
                message.what = FAN;
                Bundle bundle = new Bundle();
                bundle.putBoolean("isUp", isUp);
                bundle.putBoolean("isChange", isChange);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }));
    }

    /**
     * 发送指令切换调节模式
     * @param order 指令
     */
    private void sendDataToChooseMode(String order) {
        Dispatcher.getInstance().sendOrder(new Order(OrderType.TYPE_MODE, order, new Order.Callback() {
            @Override
            public void onFailure(Exception e) {
                handler.sendEmptyMessage(EXCEPTION);
            }

            @Override
            public void onResponse() {

            }
        }));
    }

    /**
     * 发送指令给对端窗户
     *
     * @param order 指令
     */
    private void sendDataToWindow(String order, boolean isUp) {
        Dispatcher.getInstance().sendOrder(new Order(OrderType.TYPE_WINDOW, order, new Order.Callback() {
            @Override
            public void onFailure(Exception e) {
                handler.sendEmptyMessage(EXCEPTION);
            }

            @Override
            public void onResponse() {
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
     * 开启蓝牙监听线程
     */
    void holdBluetoothConnection() {
        Log.d("TAG", "holdBluetoothConnection: ");

        btThread = new HoldConnectionThread(false, msg ->
        {
            Log.d("TAG", "holdBluetoothConnection: " + msg);
            //这里传进来依然是字节流的形式，转化为String形式

            if (msg.equals("faill")) {
                Message message = Message.obtain();
                message.what = FAILL;
                handler.sendMessage(message);
            } else {
                if (msg.contains("DATE")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("#TIME");
                    Calendar calendar = Calendar.getInstance();
                    sb.append(calendar.get(Calendar.YEAR));
                    sb.append(calendar.get(Calendar.MONTH) / 10 < 1 ? "0" : "");
                    // 更改get(Calendar.MONTH +1) 为 get(Calendar.MONTH )+1,同时整体进行了时间格式标准化处理(对于只有个位数的时间进行加 0处理)
                    sb.append(calendar.get(Calendar.MONTH) + 1);
                    sb.append(calendar.get(Calendar.DATE) / 10 < 1 ? "0" : "");
                    sb.append(calendar.get(Calendar.DATE));
                    sb.append(calendar.get(Calendar.HOUR_OF_DAY) / 10 < 1 ? "0" : "");
                    sb.append(calendar.get(Calendar.HOUR_OF_DAY));
                    sb.append(calendar.get(Calendar.MINUTE) / 10 < 1 ? "0" : "");
                    sb.append(calendar.get(Calendar.MINUTE));
                    sb.append(calendar.get(Calendar.SECOND) / 10 < 1 ? "0" : "");
                    sb.append(calendar.get(Calendar.SECOND));

                    Log.d("123456", "holdBluetoothConnection: ");
                    sendDataToBracelet(sb.toString());
                }

                if (msg.contains("TEMP")) {
                    String[] strings = msg.split("#TEMP");
                    Message message = Message.obtain();
                    message.what = BRACELET;
                    Bundle bundle = new Bundle();
                    bundle.putString("temp", strings[0]);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            }

        });

        btThread.start();
    }

    /**
     * <<<<<<< HEAD
     * 从连接后返回MainActivity时会被调用
     */

    private void holdWiFiConnection() {
        wifiThread = new HoldConnectionThread(true, msg ->
        {
            // do nothing
        });
        wifiThread.start();
    }

    /**
     * 安全Handler的消息回调
     *
     * @param msg 消息
     */
    @SuppressLint("SetTextI18n")
    public void handleMessage(Message msg) {
        Log.d("handler", "handleMessage: " + msg.what);
        switch (msg.what) {
            case EXCEPTION:
                Toast.makeText(MainActivity.this, "指令发送失败，请重试！", Toast.LENGTH_SHORT).show();
                break;
            case FAN:
                if (msg.getData().getBoolean("isChange")) {
                    if (msg.getData().getBoolean("isUp"))
                        mBinding.appBarMain.fanControlLayout.increase();
                    else mBinding.appBarMain.fanControlLayout.reduce();
                }
                mBinding.appBarMain.tvFanSpeed.setText(mBinding.appBarMain.fanControlLayout.getCurrentValue() + "档");
                break;
            case AIR_CONDITIONER:
                if (msg.getData().getBoolean("isChange")) {
                    if (msg.getData().getBoolean("isUp"))
                        mBinding.appBarMain.airConditionerControlLayout.increase();
                    else mBinding.appBarMain.airConditionerControlLayout.reduce();
                } else {
                    mBinding.appBarMain.airConditionerControlLayout.setOpen(msg.getData().getString("order").equals("#KT26.00OPEN"));
                }
                if (mBinding.appBarMain.airConditionerControlLayout.isOpen())
                    mBinding.appBarMain.tvAirConditionerState.setText("开");
                else
                    mBinding.appBarMain.tvAirConditionerState.setText("关");
                mBinding.appBarMain.tvAirConditionerTemperature.setText(mBinding.appBarMain.airConditionerControlLayout.getCurrentValue() + "");
                break;
            case WINDOW:
                if (msg.getData().getBoolean("isUp"))
                    mBinding.appBarMain.windowControlLayout.increase();
                else mBinding.appBarMain.windowControlLayout.reduce();
                if (mBinding.appBarMain.windowControlLayout.getCurrentValue() == 0)
                    mBinding.appBarMain.tvWindowState.setText("关");
                else
                    mBinding.appBarMain.tvWindowState.setText("开");
                break;
            case BRACELET:
                String temp = msg.getData().getString("temp").substring(4, msg.getData().getString("temp").length());
                String a = "当前皮肤温度: " + temp + "°C";
                a = a.replaceAll("\r|\n", "");
                mBinding.appBarMain.tvTemperature.setText(a);
                autoAdjust(BaseApplication.isAutoAdjust, temp);//自动调节
                break;
            case FAILL:
                mBinding.appBarMain.tvTemperature.setText("蓝牙指令有误");
                break;
        }
    }

    /**
     * 当打开自动调节时自动调节
     */
    private void autoAdjust(boolean isAuto, String temp) {
        //如果没有开户自动调节则return
        if (!isAuto) {
            return;
        }
        double tempNum = Double.parseDouble(temp);
        /**对门窗的按制**/
        if (tempNum <= 30.75) {
            //关窗
            sendDataToWindow("#MC000", false);//发送指令
        } else if (tempNum > 30.75 && tempNum <= 31.95) {
            //开窗
            sendDataToWindow("#MC001", true);//发送指令
        }
        /**对风扇的按制**/
        if (tempNum > 31.95 && tempNum <= 32.88) {
            //更新UI属性
            int currentValue = mBinding.appBarMain.fanControlLayout.getCurrentValue();
            if (mBinding.appBarMain.fanControlLayout.getCurrentValue() > 1) {
                for (int i = 0; i < currentValue - 1; i++)
                    mBinding.appBarMain.fanControlLayout.reduce();
            } else if (mBinding.appBarMain.fanControlLayout.getCurrentValue() < 1) {
                mBinding.appBarMain.fanControlLayout.increase();
            }
            //风扇1

            sendDataToFan("#FS01", false, false);//发送指令
        } else if (tempNum > 32.88 && tempNum <= 33.3) {
            //更新UI属性
            int currentValue = mBinding.appBarMain.fanControlLayout.getCurrentValue();
            if (mBinding.appBarMain.fanControlLayout.getCurrentValue() > 2) {
                mBinding.appBarMain.fanControlLayout.reduce();
            } else if (mBinding.appBarMain.fanControlLayout.getCurrentValue() < 2) {
                for (int i = 0; i < 2 - currentValue; i++)
                    mBinding.appBarMain.fanControlLayout.increase();
            }
            //风扇2
            sendDataToFan("#FS02", true, false);//发送指令
        } else if (tempNum > 33.3 && tempNum <= 34.03) {
            //更新UI属性
            for (int i = 0; i < 3; i++)
                mBinding.appBarMain.fanControlLayout.increase();
            //风扇3
            sendDataToFan("#FS03", true, false);//发送指令
        }
        /**对空调的按制**/
        if (tempNum > 34.03) {
            //更新UI属性
            int currentValue = mBinding.appBarMain.airConditionerControlLayout.getCurrentValue();
            if (currentValue > 26) {
                for (int i = 0; i < currentValue - 26; i++)
                    mBinding.appBarMain.airConditionerControlLayout.reduce();
            } else {
                for (int i = 0; i < 26 - currentValue; i++)
                    mBinding.appBarMain.airConditionerControlLayout.increase();
            }
            //恢复空调26度的UI
            sendDataToAirConditioner("#KT26.00OPEN", true, false);//发送指令 开空调26度
        }
    }

    /**
     * 上一个activity结束的回调
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    Log.d("TAG", "onActivityResult: ");
                    mBinding.appBarMain.tvBraceletState.setVisibility(View.GONE);
                    holdBluetoothConnection();
                }
                break;
            case WIFI:
                if (resultCode == RESULT_OK) {
                    holdWiFiConnection();
                    sendDataToWindow("#MC" + "000", false);
//                    if (BaseApplication.isAutoAdjust) {
//                        sendDataToFan("#MODE1",false,false);
//                        mBinding.appBarMain.controlMode.setText("自动模式");
//                    } else {
//                        sendDataToFan("#MODE0",false,false);
//                        mBinding.appBarMain.controlMode.setText("手动模式");
//                    }
                }
                break;
        }
    }

    /**
     * 活动被销毁回调，中断所有的监听线程，清理资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (btThread != null) btThread.interrupt();
        if (wifiThread != null) wifiThread.interrupt();
        BaseApplication.shutdown();
    }
}