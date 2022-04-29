package com.chenjimou.braceletdemo.ui;

import static com.chenjimou.braceletdemo.uitls.WiFiModeUtil.localBroadcastManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chenjimou.braceletdemo.R;
import com.chenjimou.braceletdemo.base.BaseApplication;
import com.chenjimou.braceletdemo.databinding.ActivityWifiConnectBinding;
import com.chenjimou.braceletdemo.uitls.WiFiModeUtil;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WiFiConnectActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private ActivityWifiConnectBinding mBinding;
    private static final String TAG = "WiFiConnectActivity";
    private WiFiConnectBroadcastReceiver wiFiConnectBroadcastReceiver;//本地广播管理器
    private int connectingSum=0;//在连接时进行记数
    //保存上次输入的IP
    private SharedPreferences.Editor editor;
    private SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivityWifiConnectBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkWiFiIsOpen();
        if(BaseApplication.wifiSocket!=null){
            mBinding.connectStatus.setText("已连接");
        }else {
            mBinding.connectStatus.setText("未连接");
        }
    }

    /**
     * 为Toolbar载入相应的菜单
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wifi_connect_toolbar, menu);
        return true;
    }

    /**
     * 弹出对话框输入IP
     */
    private void connetDevice() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setCancelable(true);
            View view = View.inflate(WiFiConnectActivity.this, R.layout.dialog_input_ip, null);
            //设置对话框圆角样式
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_tosetting_wifi_dialog);
            dialog.setView(view);
            //初始化对话框view
            EditText inputIP=view.findViewById(R.id.ipInputET);
            Button connect=view.findViewById(R.id.connect_button);
            TextView errorTV=view.findViewById(R.id.errorMessage);
            //设置上次输入的ip
            String ip=pref.getString("ip","");
            inputIP.setText(ip);
            //连接按钮
            connect.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View view) {
                    String ip=inputIP.getText().toString();
                    if("".equals(ip)){
                        return;
                    }
                    //检查ip输入是否合法
                    Pattern pattern = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
                    Matcher matcher=pattern.matcher(ip);
                    if(matcher.matches()){
                        WiFiModeUtil.connectByTCP(ip,5000);//进行连接
                        dialog.dismiss();//关闭对话框
                        //记住本次输入的ip
                        editor.putString("ip",ip);
                        editor.apply();
                    }else{
                        errorTV.setText("IP格式不正确，请重新输入");
                    }
                }
            });
            dialog.show();
        }else{
            Toast.makeText(WiFiConnectActivity.this, "请先连接WiFi", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 检查WiFi是否打开
     * 如果没有则引导用户打开
     * 不去打开则finish()
     */
    private void checkWiFiIsOpen() {
        if (wifiManager.isWifiEnabled()) {
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setCancelable(false);
        View view = View.inflate(WiFiConnectActivity.this, R.layout.dialog_towifi_setting, null);
        //设置对话框圆角样式
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.shape_tosetting_wifi_dialog);
        dialog.setView(view);
        //初始化对话框view
        Button cancle = view.findViewById(R.id.cancle_button);
        Button toSetting = view.findViewById(R.id.tosetting_button);
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
                Toast.makeText(WiFiConnectActivity.this, "WiFi未打开", Toast.LENGTH_SHORT).show();
            }
        });
        toSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));//跳转到WiFi设置
            }
        });
        dialog.show();
    }

    /**
     * 初始化
     */
    private void init() {
        wifiManager = (WifiManager) BaseApplication.altContext.getSystemService(Context.WIFI_SERVICE);
        mBinding.toolbar.setTitle("");
        mBinding.tvTitle.setText("WiFi连接模块");
        setSupportActionBar(mBinding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        registerReceiver();//广播注册
        editor=getSharedPreferences("data",MODE_PRIVATE).edit();
        pref=getSharedPreferences("data",MODE_PRIVATE);
    }

    /**
     * 进行广播注册
     * 1.监听网络状态是否为WiFi
     * 2.监听与硬件连接状态
     */
    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        //addAction
        intentFilter.addAction("WiFiModeUtil.Connecting");//正在连接
        intentFilter.addAction("WiFiModeUtil.Connect.Succeed");//连接成功
        intentFilter.addAction("WiFiModeUtil.Connect.Fail");//连接失败
        wiFiConnectBroadcastReceiver = new WiFiConnectBroadcastReceiver();
        WiFiModeUtil.localBroadcastManager = LocalBroadcastManager.getInstance(this);//给WiFiModeUtil工具类中的本地广播管理器赋值
        WiFiModeUtil.localBroadcastManager.registerReceiver(wiFiConnectBroadcastReceiver, intentFilter);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.toWiFiSetting:
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));//跳转到WiFi设置
                break;
            case R.id.connectDevice:
                connetDevice();//连接硬件WiFi模块
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WiFiModeUtil.localBroadcastManager.unregisterReceiver(wiFiConnectBroadcastReceiver);//注销广播
    }

    /**
     * 广播接收器
     */
    class WiFiConnectBroadcastReceiver extends BroadcastReceiver {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "WiFiModeUtil.Connecting":
                    //嗯....这样也不错
                    connectingSum++;
                    switch (connectingSum % 4) {
                        case 0:
                            mBinding.connectStatus.setText("正在连接");
                            break;
                        case 1:
                            mBinding.connectStatus.setText("正在连接.");
                            break;
                        case 2:
                            mBinding.connectStatus.setText("正在连接..");
                            break;
                        case 3:
                            mBinding.connectStatus.setText("正在连接...");
                            break;
                    }
                    break;
                case "WiFiModeUtil.Connect.Succeed":
                    mBinding.connectStatus.setText("连接成功");
                    Toast.makeText(WiFiConnectActivity.this, "连接成功!", Toast.LENGTH_SHORT).show();
                    connectingSum=0;
                    finish();
                    break;
                case "WiFiModeUtil.Connect.Fail":
                    mBinding.connectStatus.setText("连接失败");
                    connectingSum=0;
                    break;
            }
        }
    }
}