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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.chenjimou.braceletdemo.base.BaseApplication;
import com.chenjimou.braceletdemo.databinding.ActivityBluetoothConnectBinding;

import java.util.Set;

public class BluetoothConnectActivity extends AppCompatActivity
{
    private ActivityBluetoothConnectBinding mBinding;

    BluetoothAdapter bluetoothAdapter;

    boolean isFound = false;

    private static final int BLUETOOTH_CONNECT_EXCEPTION = 0;
    private static final int BLUETOOTH_CONNECT_SUCCESS = 1;

    private static final String BLUETOOTH_NAME = "Bracelet";

    private final Handler handler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(@NonNull Message msg)
        {
            switch (msg.what)
            {
                case BLUETOOTH_CONNECT_EXCEPTION:
                    Toast.makeText(BluetoothConnectActivity.this,
                            "蓝牙连接出现异常，请换台设备进行操作！", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case BLUETOOTH_CONNECT_SUCCESS:
                    Toast.makeText(BluetoothConnectActivity.this,
                            "设备连接成功，返回主界面进行操作！", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivityBluetoothConnectBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();
    }

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
        if (bluetoothAdapter == null)
        {
            Toast.makeText(BluetoothConnectActivity.this,"该设备无法使用蓝牙功能！", Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            if (!bluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,0);
            }
            else
            {
                tryFound();
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
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null && device.getName() != null)
                        {
                            if (device.getName().equals(BLUETOOTH_NAME))
                            {
                                Toast.makeText(BluetoothConnectActivity.this, "查找成功，正在尝试配对！",
                                        Toast.LENGTH_SHORT).show();
                                isFound = true;
                                BaseApplication.btDevice = device;
                                tryBond(device);
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        if (!isFound)
                        {
                            Toast.makeText(BluetoothConnectActivity.this, "查找失败，未找到设备！", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                        if (BaseApplication.btDevice != null)
                        {
                            switch (BaseApplication.btDevice.getBondState())
                            {
                                case BluetoothDevice.BOND_BONDED:// 配对成功
                                    Toast.makeText(BluetoothConnectActivity.this, "配对成功，正在进行连接！",
                                            Toast.LENGTH_SHORT).show();
                                    tryConnect(BaseApplication.btDevice);
                                    break;
                                case BluetoothDevice.BOND_BONDING:// 配对中
                                    Toast.makeText(BluetoothConnectActivity.this, "配对中，请稍后...",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case BluetoothDevice.BOND_NONE:// 配对失败
                                    Toast.makeText(BluetoothConnectActivity.this, "配对失败，请换台设备进行操作！",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                        if (blueState == BluetoothAdapter.STATE_OFF)
                        {
                            finish();
                        }
                        break;
                }
            }
        }
    };

    private void tryFound()
    {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices)
        {
            if (device.getName() != null && device.getName().equals(BLUETOOTH_NAME))
            {
                Toast.makeText(BluetoothConnectActivity.this, "该蓝牙设备已配对，正在进行连接！",
                        Toast.LENGTH_SHORT).show();
                isFound = true;
                BaseApplication.btDevice = device;
                tryConnect(device);
                return;
            }
        }
        bluetoothAdapter.startDiscovery();
    }

    private void tryBond(BluetoothDevice device)
    {
        bluetoothAdapter.cancelDiscovery();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            device.createBond();
        } else {
            Toast.makeText(BluetoothConnectActivity.this, "配对失败，Android版本不能低于4.4！", Toast.LENGTH_SHORT).show();
        }
    }

    private void tryConnect(BluetoothDevice device)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {

                    BaseApplication.btSocket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
                    // 通过socket连接到对端硬件设备。此调用将阻塞，直到成功或引发异常
                    BaseApplication.btSocket.connect();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                    try
                    {
                        BaseApplication.btSocket.close();
                        handler.sendEmptyMessage(BLUETOOTH_CONNECT_EXCEPTION);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }
                handler.sendEmptyMessage(BLUETOOTH_CONNECT_SUCCESS);
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

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0)
        {
            if (resultCode == -1)
            {
                tryFound();
            }
            else
            {
                Toast.makeText(BluetoothConnectActivity.this,"该设备未开启蓝牙！", Toast.LENGTH_SHORT).show();
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
}