package com.chenjimou.braceletdemo.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.chenjimou.braceletdemo.base.BaseApplication;
import com.chenjimou.braceletdemo.databinding.ActivityBluetoothConnectBinding;
import com.chenjimou.braceletdemo.widght.SafeHandler;

import java.io.IOException;
import java.util.Set;

public class btConnectActivity extends AppCompatActivity
{
    ActivityBluetoothConnectBinding mBinding;

    BluetoothAdapter bluetoothAdapter;

    boolean isFound = false;

    /* 安全的Handler，避免Activity直接与后台线程绑定(内部类)导致内存泄漏 */
    final SafeHandler<btConnectActivity> handler = new SafeHandler<>(this, Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivityBluetoothConnectBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();
    }

    /**
     * 初始化
     */
    private void init()
    {
        // 获取设备自身的蓝牙适配器对象
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 当发现设备时注册 ACTION_FOUND 广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(BlueToothReceiver, filter);

        mBinding.toolbar.setTitle("");
        mBinding.tvTitle.setText("蓝牙连接模块");
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // 检查设备蓝牙功能是否正常
        if (bluetoothAdapter == null)
        {
            Toast.makeText(btConnectActivity.this,"该设备无法使用蓝牙功能！", Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            // 开启蓝牙
            if (!bluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,0);
            }
            // 进行蓝牙搜索
            else
            {
                trySearch();
            }
        }
    }

    private final BroadcastReceiver BlueToothReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action != null)
            {
                switch (action)
                {
                    case BluetoothDevice.ACTION_FOUND: // 蓝牙设备被发现
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device.getName().equals(BLUETOOTH_NAME))
                        {
                            Toast.makeText(btConnectActivity.this, "查找成功，正在尝试配对！",
                                    Toast.LENGTH_SHORT).show();
                            isFound = true;
                            BaseApplication.btDevice = device;
                            bluetoothAdapter.cancelDiscovery();
                            tryBond(device);
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: // 蓝牙搜索结束
                        if (!isFound)
                        {
                            Toast.makeText(btConnectActivity.this, "查找失败，未找到设备！", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED: // 蓝牙设备配对状态变化
                        switch (BaseApplication.btDevice.getBondState())
                        {
                            case BluetoothDevice.BOND_BONDED: // 配对成功
                                Toast.makeText(btConnectActivity.this, "配对成功，正在进行连接！",
                                        Toast.LENGTH_SHORT).show();
                                tryConnect(BaseApplication.btDevice);
                                break;
                            case BluetoothDevice.BOND_BONDING: // 配对中
                                Toast.makeText(btConnectActivity.this, "配对中，请稍后...",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case BluetoothDevice.BOND_NONE: // 配对失败
                                Toast.makeText(btConnectActivity.this, "配对失败，请换台设备进行操作！",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                        }
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED: // 蓝牙状态变化
                        int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                        if (blueState == BluetoothAdapter.STATE_OFF) finish();
                        break;
                }
            }
        }
    };

    /**
     * 进行蓝牙搜索
     */
    void trySearch()
    {
        // 获取已配对设备列表
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // 遍历列表搜索目标设备是否已配对
        for (BluetoothDevice device : pairedDevices)
        {
            if (device.getName() != null && device.getName().equals(BLUETOOTH_NAME))
            {
                Toast.makeText(btConnectActivity.this, "该蓝牙设备已配对，正在进行连接！",
                        Toast.LENGTH_SHORT).show();
                isFound = true;
                BaseApplication.btDevice = device;
                // 如果目标设备已配对就尝试连接
                tryConnect(device);
                bluetoothAdapter.cancelDiscovery();
                return;
            }
        }
        // 开启蓝牙搜索
        bluetoothAdapter.startDiscovery();
    }

    /**
     * 进行配对
     * @param device 蓝牙设备
     */
    private void tryBond(BluetoothDevice device)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) device.createBond();
        else Toast.makeText(btConnectActivity.this, "配对失败，Android版本不能低于4.4！", Toast.LENGTH_SHORT).show();
    }

    private void tryConnect(BluetoothDevice device)
    {
        new Thread(() -> {
            try
            {
                // 获取socket对象
                BaseApplication.btSocket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                // 通过socket连接到对端硬件设备。此调用将阻塞，直到成功或引发异常
                BaseApplication.btSocket.connect();
                handler.sendEmptyMessage(SUCCESS);
            }
            catch (IOException e)
            {
                try
                {
                    BaseApplication.btSocket.close();
                    BaseApplication.btSocket = null;
                    handler.sendEmptyMessage(EXCEPTION);
                }
                catch (IOException ignored) { }
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void handleMessage(Message msg)
    {
        switch (msg.what)
        {
            case EXCEPTION:
                Toast.makeText(btConnectActivity.this,
                        "蓝牙连接出现异常，请换台设备进行操作！", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case SUCCESS:
                Toast.makeText(btConnectActivity.this,
                        "设备连接成功，返回主界面进行操作！", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0)
        {
            if (resultCode == -1)
            {
                trySearch();
            }
            else
            {
                Toast.makeText(btConnectActivity.this,"该设备未开启蓝牙！", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(BlueToothReceiver);
    }

    private static final int EXCEPTION = 0;
    private static final int SUCCESS = 1;
    private static final String BLUETOOTH_NAME = "Bracelet";
}