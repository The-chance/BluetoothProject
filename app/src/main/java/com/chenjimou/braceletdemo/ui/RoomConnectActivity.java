package com.chenjimou.braceletdemo.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.chenjimou.braceletdemo.base.BaseApplication;
import com.chenjimou.braceletdemo.databinding.ActivityRoomConnectBinding;

public class RoomConnectActivity extends AppCompatActivity
{
    private ActivityRoomConnectBinding mBinding;

    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivityRoomConnectBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();
    }

    private void init()
    {
        // 获取获取Application的WifiManager
        wifiManager = (WifiManager) BaseApplication.altContext.getSystemService(Context.WIFI_SERVICE);

        mBinding.toolbar.setTitle("");
        mBinding.tvTitle.setText("WiFi连接房间");
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!wifiManager.isWifiEnabled())
        {
            Toast.makeText(RoomConnectActivity.this, "请先连接模块WiFi热点进行初始化！",
                    Toast.LENGTH_SHORT).show();
//            finish();
        }
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
}