package com.chenjimou.braceletdemo.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class RoomConnectActivity extends AppCompatActivity
{
//    private ActivityRoomConnectBinding mBinding;
//
//    WifiManager wifiManager;
    /**
     * =========================================
     *
     *
     *            这个Activity不用了
     *
     *
     * =========================================
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
//        super.onCreate(savedInstanceState);
//        mBinding = ActivityRoomConnectBinding.inflate(getLayoutInflater());
//        setContentView(mBinding.getRoot());
//        init();
    }
//
//    private void init()
//    {
//        // 获取获取Application的WifiManager
//        wifiManager = (WifiManager) BaseApplication.altContext.getSystemService(Context.WIFI_SERVICE);
//
//        mBinding.toolbar.setTitle("");
//        mBinding.tvTitle.setText("WiFi连接房间");
//        setSupportActionBar(mBinding.toolbar);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//    }
//
//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//        if (!wifiManager.isWifiEnabled())
//        {
//            Toast.makeText(RoomConnectActivity.this, "请先连接模块WiFi热点进行初始化！",
//                    Toast.LENGTH_SHORT).show();
////            finish();
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item)
//    {
//        if (item.getItemId() == android.R.id.home)
//        {
//            finish();
//        }
//        return super.onOptionsItemSelected(item);
//    }
}