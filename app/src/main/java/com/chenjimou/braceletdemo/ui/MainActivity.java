package com.chenjimou.braceletdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.chenjimou.braceletdemo.R;
import com.chenjimou.braceletdemo.base.ConnectionHelper;
import com.chenjimou.braceletdemo.commend.HoldBluetoothConnectionCommend;
import com.chenjimou.braceletdemo.commend.BluetoothSendDataCommend;
import com.chenjimou.braceletdemo.commend.HoldWiFiConnectionCommend;
import com.chenjimou.braceletdemo.commend.WiFiSendDataCommend;
import com.chenjimou.braceletdemo.databinding.ActivityMainBinding;
import com.chenjimou.braceletdemo.service.BraceletServiceConnection;
import com.chenjimou.braceletdemo.widght.ControlLayout;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

public class MainActivity extends AppCompatActivity implements HoldBluetoothConnectionCommend.OnCallBackListener,
        HoldWiFiConnectionCommend.OnCallBackListener
{
    private ActivityMainBinding mBinding;

    ActionBarDrawerToggle mDrawerToggle;

    private static final int BLUETOOTH_REQUEST_CODE = 0;
    private static final int WIFI_REQUEST_CODE = 1;
    private static final int COMMUNICATION_EXCEPTION = 2;
    private static final int FAN_COMMUNICATION_SUCCESS = 3;
    private static final int AIR_CONDITIONER_COMMUNICATION_SUCCESS = 4;
    private static final int WINDOW_COMMUNICATION_SUCCESS = 5;

    private final Handler handler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(@NonNull Message msg)
        {
            switch (msg.what)
            {
                case COMMUNICATION_EXCEPTION:
                    Toast.makeText(MainActivity.this,"指令发送失败，请重试！",Toast.LENGTH_SHORT).show();
                    break;
                case FAN_COMMUNICATION_SUCCESS:
                    if (msg.getData().getBoolean("isUp"))
                    {
                        mBinding.appBarMain.fanControlLayout.increase();
                    }
                    else
                    {
                        mBinding.appBarMain.fanControlLayout.reduce();
                    }
                    break;
                case AIR_CONDITIONER_COMMUNICATION_SUCCESS:
                    if (msg.getData().getBoolean("isUp"))
                    {
                        mBinding.appBarMain.airConditionerControlLayout.increase();
                    }
                    else
                    {
                        mBinding.appBarMain.airConditionerControlLayout.reduce();
                    }
                    break;
                case WINDOW_COMMUNICATION_SUCCESS:
                    if (msg.getData().getBoolean("isUp"))
                    {
                        mBinding.appBarMain.windowControlLayout.increase();
                    }
                    else
                    {
                        mBinding.appBarMain.windowControlLayout.reduce();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();
    }

    private void init()
    {
        mBinding.appBarMain.toolbar.setTitle("");
        mBinding.appBarMain.tvTitle.setText("智慧家居");
        setSupportActionBar(mBinding.appBarMain.toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mBinding.drawerLayout, mBinding.appBarMain.toolbar,
                R.drawable.navigation, R.drawable.navigation) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerToggle.syncState();
        mBinding.drawerLayout.addDrawerListener(mDrawerToggle);

        mBinding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

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

        BraceletServiceConnection.getInstance().tryConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.bluetoothConnection:
                startActivityForResult(new Intent(MainActivity.this, BluetoothConnectActivity.class),
                        BLUETOOTH_REQUEST_CODE);
                return true;
            case R.id.wifiConnection:
                startActivityForResult(new Intent(MainActivity.this, WiFiConnectActivity.class),
                        WIFI_REQUEST_CODE);
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

    private void sendDataToBracelet(String msg)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Exception exception = BraceletServiceConnection.getInstance()
                        .sendCommend(new BluetoothSendDataCommend(ConnectionHelper.bluetoothSocket, msg));
                if (exception != null)
                {
                    handler.sendEmptyMessage(COMMUNICATION_EXCEPTION);
                }
            }
        }).start();
    }

    private void sendDataToAirConditioner(String msg, boolean isUp)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Exception exception = BraceletServiceConnection.getInstance()
                        .sendCommend(new WiFiSendDataCommend(ConnectionHelper.wifiSocket, msg));
                if (exception != null)
                {
                    handler.sendEmptyMessage(COMMUNICATION_EXCEPTION);
                }
                else
                {
                    Message msg = new Message();
                    msg.what = AIR_CONDITIONER_COMMUNICATION_SUCCESS;
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isUp", isUp);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void sendDataToFan(String msg, boolean isUp)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Exception exception = BraceletServiceConnection.getInstance()
                        .sendCommend(new WiFiSendDataCommend(ConnectionHelper.wifiSocket, msg));
                if (exception != null)
                {
                    handler.sendEmptyMessage(COMMUNICATION_EXCEPTION);
                }
                else
                {
                    Message msg = new Message();
                    msg.what = WINDOW_COMMUNICATION_SUCCESS;
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isUp", isUp);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void sendDataToWindow(String msg, boolean isUp)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Exception exception = BraceletServiceConnection.getInstance()
                        .sendCommend(new WiFiSendDataCommend(ConnectionHelper.wifiSocket, msg));
                if (exception != null)
                {
                    handler.sendEmptyMessage(COMMUNICATION_EXCEPTION);
                }
                else
                {
                    Message msg = new Message();
                    msg.what = FAN_COMMUNICATION_SUCCESS;
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isUp", isUp);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void holdBluetoothConnection()
    {
        HoldBluetoothConnectionCommend commend = new HoldBluetoothConnectionCommend(ConnectionHelper.bluetoothSocket);
        commend.setOnCallBackListener(this);
        BraceletServiceConnection.getInstance().sendCommend(commend);
    }

    private void holdWiFiConnection()
    {
        HoldWiFiConnectionCommend commend = new HoldWiFiConnectionCommend(ConnectionHelper.wifiSocket);
        commend.setOnCallBackListener(this);
        BraceletServiceConnection.getInstance().sendCommend(commend);
    }

    @Override
    public void bluetoothCallBack(String msg)
    {
        if (msg.contains("#TIME"))
        {
            sendDataToBracelet(msg);
        }
        else
        {
            mBinding.appBarMain.tvTemperature.setText(msg);
        }
    }

    @Override
    public void wifiCallBack(String msg)
    {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case BLUETOOTH_REQUEST_CODE:
                if (resultCode == RESULT_OK)
                {
                    mBinding.appBarMain.tvBraceletState.setVisibility(View.GONE);
                    if (ConnectionHelper.bluetoothSocket != null)
                    {
                        holdBluetoothConnection();
                    }
                }
                break;
            case WIFI_REQUEST_CODE:
                if (resultCode == RESULT_OK)
                {
                    if (ConnectionHelper.wifiSocket != null)
                    {
                        holdWiFiConnection();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        BraceletServiceConnection.getInstance().disConnection();
        try
        {
            ConnectionHelper.reset();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}