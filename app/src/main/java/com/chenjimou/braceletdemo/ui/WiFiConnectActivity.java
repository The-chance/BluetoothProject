package com.chenjimou.braceletdemo.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.chenjimou.braceletdemo.R;
import com.chenjimou.braceletdemo.base.BaseApplication;
import com.chenjimou.braceletdemo.databinding.ActivityWifiConnectBinding;
import com.chenjimou.braceletdemo.widght.SafeHandler;
import com.google.android.material.textfield.TextInputEditText;

import java.net.Socket;
import java.util.List;

public class WiFiConnectActivity extends AppCompatActivity {
    private ActivityWifiConnectBinding mBinding;

    WifiManager wifiManager;
    WifiConfiguration config;
    ScanResult scanResult;

    // 目标 WiFi 是否找到
    boolean isFound = false;
    // 是否连上目标 WiFi
    boolean isConnection = false;

    /*
    wifi的各加密类型
     */
    private static final int WIFICIPHER_WPA = 0;
    private static final int WIFICIPHER_WEP = 1;
    private static final int WIFICIPHER_NOPASS = 2;

    /*
    WiFi的连接状态
     */
    private static final int WIFI_COMMUNICATION_EXCEPTION = 3;
    private static final int WIFI_COMMUNICATION_SUCCESS = 4;
    private static final int WIFI_SOCKET_EXCEPTION = 5;
    private static final int WIFI_CONNECT_SUCCESS = 6;
    private static final int WIFI_CONNECT_FAIL = 7;

    /*
    WiFi名称
     */
    String TARGET_WIFI_NAME = "Smarthome";
    /*
    WiFi密码
     */
    String TARGET_WIFI_PASSWORD = "";

    private static final String TAG = "WiFiConnectActivity";

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case WIFI_COMMUNICATION_EXCEPTION:
                    Toast.makeText(WiFiConnectActivity.this, "WiFi传输出现异常，请重试！",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case WIFI_COMMUNICATION_SUCCESS:
                    wifiManager.disconnect();
                    isFound = false;
                    isConnection = false;
                    trySearchWiFi();
                    break;
                case WIFI_SOCKET_EXCEPTION:
                    Toast.makeText(WiFiConnectActivity.this, "输入错误，请重新输入！",
                            Toast.LENGTH_SHORT).show();
                    break;
                case WIFI_CONNECT_SUCCESS:
                    Toast.makeText(WiFiConnectActivity.this, "已连接上目标WiFi！",
                            Toast.LENGTH_SHORT).show();
                    isConnection = true;
                    tryConnectRoom();
                    finish();
                    break;
                case WIFI_CONNECT_FAIL:
                    Toast.makeText(WiFiConnectActivity.this, "未能连接上目标wifi，请重试！",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivityWifiConnectBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        Log.d(TAG,"onCreate");
        init();
    }

    private void init() {
        Log.d(TAG,"init()");
        // 获取获取Application的WifiManager
        wifiManager = (WifiManager) BaseApplication.altContext.getSystemService(Context.WIFI_SERVICE);

        mBinding.toolbar.setTitle("");
        mBinding.tvTitle.setText("WiFi连接模块");
        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(WiFiReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        trySearchWiFi();
    }

    private void trySearchWiFi() {
        if (!wifiManager.isWifiEnabled()) {
            //开启wifi
            wifiManager.setWifiEnabled(true);
        }
        Log.d(TAG,"wifiManager.isWifiEnabled()--->"+wifiManager.isWifiEnabled());
    }

    private final BroadcastReceiver WiFiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION: // wifi扫描完成后发出的广播
                    // 防止重复进行连接
                    if (!isFound) {
                        // 扫描到的所有可用wifi
                        List<ScanResult> scanResults = wifiManager.getScanResults();
                        for (ScanResult scanResult : scanResults) {
                            // 找到了目标wifi
                            if (scanResult.SSID.equals(TARGET_WIFI_NAME)) {
                                Toast.makeText(WiFiConnectActivity.this, "找到了目标wifi：" + scanResult.SSID,
                                        Toast.LENGTH_SHORT).show();
                                WiFiConnectActivity.this.scanResult = scanResult;
                                isFound = true;
                                tryConnectWiFi(scanResult);
                                break;
                            }
                        }
                    }
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION: // wifi状态发生变化时发出的广播
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_ENABLED: // wifi开启发出的广播
                            Toast.makeText(WiFiConnectActivity.this, "检测到WiFi已开启，正在进行扫描！", Toast.LENGTH_SHORT).show();
                            if (!isTargetWifiConnected()) {
                                // 开始扫描
                                wifiManager.startScan();
                            } else {
                                Toast.makeText(WiFiConnectActivity.this, "已连接上目标WiFi！", Toast.LENGTH_SHORT).show();
                                isConnection = true;
                                tryConnectRoom();
                            }
                            break;
                        case WifiManager.WIFI_STATE_DISABLED: // wifi关闭发出的广播
                            Toast.makeText(WiFiConnectActivity.this, "检测到WiFi被关闭，正在尝试开启！", Toast.LENGTH_SHORT).show();
                            // 开启wifi
                            wifiManager.setWifiEnabled(true);
                            break;
                    }
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION: // 网络状态发生变化时发出的广播
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (info.getState().equals(NetworkInfo.State.CONNECTED)) // 已成功连接网络
                    {
                        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        if (!isConnection) {
                            if (wifiInfo.getSSID().equals("\"" + TARGET_WIFI_NAME + "\"")) {
                                Toast.makeText(WiFiConnectActivity.this, "已成功连接上目标WiFi！", Toast.LENGTH_SHORT).show();
                                isConnection = true;
                                tryConnectRoom();
                            } else if (wifiInfo.getSSID().equals("\"" + TARGET_WIFI_NAME + "\"")) {
                                Toast.makeText(WiFiConnectActivity.this, "已成功连接上目标WiFi！", Toast.LENGTH_SHORT).show();
                                isConnection = true;
                                buildRoomSocket();
                            } else {
                                wifiManager.disconnect();
                            }
                        }
                    } else // 其他状态
                    {
                        NetworkInfo.DetailedState state = info.getDetailedState();
                        if (state == state.CONNECTING) {
                            Toast.makeText(WiFiConnectActivity.this, "与目标WiFi连接中...", Toast.LENGTH_SHORT).show();
                        } else if (state == state.AUTHENTICATING) {
                            Toast.makeText(WiFiConnectActivity.this, "正在验证身份信息...", Toast.LENGTH_SHORT).show();
                        } else if (state == state.OBTAINING_IPADDR) {
                            Toast.makeText(WiFiConnectActivity.this, "正在获取IP地址...", Toast.LENGTH_SHORT).show();
                        } else if (state == state.FAILED) {
                            Toast.makeText(WiFiConnectActivity.this, "与目标WiFi连接失败，正在尝试重新连接！", Toast.LENGTH_SHORT).show();
                            tryConnectWiFi(scanResult);
                        }
                    }
                    break;
            }
        }
    };

    private void tryConnectRoom() {
        final TextInputEditText editText1;
        final TextInputEditText editText2;
        final TextInputEditText editText3;
        AlertDialog.Builder builder = new AlertDialog.Builder(WiFiConnectActivity.this);
        View view = View.inflate(WiFiConnectActivity.this, R.layout.dialog_input_connect_info, null);
        editText1 = view.findViewById(R.id.textInputEditText1);
        editText2 = view.findViewById(R.id.textInputEditText2);
        editText3 = view.findViewById(R.id.textInputEditText3);
        builder.setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BaseApplication.wifiSocket = new Socket(editText1.getText().toString(), 5000);
                        } catch (Exception e) {
                            handler.sendEmptyMessage(WIFI_SOCKET_EXCEPTION);
                            e.printStackTrace();
                        }
                        TARGET_WIFI_NAME = editText2.getText().toString();
                        TARGET_WIFI_PASSWORD = editText3.getText().toString();
                        String msg = "#WIFINAME" + TARGET_WIFI_NAME + "NEND" + "#WIFIPASSWORD" + TARGET_WIFI_PASSWORD + "PEND";
//                        Exception exception = BraceletServiceConnection.getInstance()
//                                .sendCommend(new WiFiSendDataCommend(BaseApplication.wifiSocket, msg));
//                        if (exception != null)
//                        {
//                            handler.sendEmptyMessage(WIFI_COMMUNICATION_EXCEPTION);
//                        }
//                        else
//                        {
//                            handler.sendEmptyMessage(WIFI_COMMUNICATION_SUCCESS);
//                        }
                    }
                }).start();
            }
        });
        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void tryConnectWiFi(ScanResult scanResult) {
        String capabilities = scanResult.capabilities;
        int type = WIFICIPHER_WPA;
        if (!TextUtils.isEmpty(capabilities)) {
            if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                type = WIFICIPHER_WPA;
            } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                type = WIFICIPHER_WEP;
            } else {
                type = WIFICIPHER_NOPASS;
            }
        }
        config = isExsits(scanResult.SSID);
        if (config == null) {
            if (type != WIFICIPHER_NOPASS) {
                // 需要输入密码
                enterWiFiPassword(type, TARGET_WIFI_PASSWORD);
            } else {
                config = createWifiInfo(scanResult.SSID, TARGET_WIFI_PASSWORD, type);
                dispatchConnectWiFi(config);
            }
        } else {
            dispatchConnectWiFi(config);
        }
    }

    private void enterWiFiPassword(int type, String password) {
        if (password.equals("")) {
            final TextInputEditText editText;
            AlertDialog.Builder builder = new AlertDialog.Builder(WiFiConnectActivity.this);
            View view = View.inflate(WiFiConnectActivity.this, R.layout.dialog_input_password, null);
            editText = view.findViewById(R.id.textInputEditText);
            builder.setView(view).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TARGET_WIFI_PASSWORD = editText.getText().toString();
                    config = createWifiInfo(scanResult.SSID, TARGET_WIFI_PASSWORD, type);
                    dispatchConnectWiFi(config);
                }
            });
            Dialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            config = createWifiInfo(scanResult.SSID, password, type);
            dispatchConnectWiFi(config);
        }
    }

    private void buildRoomSocket() {
        final TextInputEditText editText;
        AlertDialog.Builder builder = new AlertDialog.Builder(WiFiConnectActivity.this);
        View view = View.inflate(WiFiConnectActivity.this, R.layout.dialog_input_socket_info, null);
        editText = view.findViewById(R.id.textInputEditText);
        builder.setView(view).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    BaseApplication.wifiSocket = new Socket(editText.getText().toString(), 5000);
                    WiFiConnectActivity.this.setResult(RESULT_OK);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private WifiConfiguration isExsits(String SSID) {
        // 权限检查
        if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)) {
            // 如果没有权限就添加
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE},
                    9999);
        }
        List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
        if (existingConfigs != null) {
            for (WifiConfiguration existingConfig : existingConfigs) {
                // 找到了已有的 WiFi 设置信息
                if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    // 设置最高优先级，防止自动连接上别的wifi
                    existingConfig.priority = 100000;
                    // 更新已有的 WiFi 设置信息
                    wifiManager.updateNetwork(existingConfig);
                    // 保存
                    wifiManager.saveConfiguration();
                    return existingConfig;
                }
            }
        }
        return null;
    }

    private void dispatchConnectWiFi(WifiConfiguration config) {
        // 将新的网络设置添加到 wifiManager 中
        int identityID = wifiManager.addNetwork(config);
        if (identityID != -1) {
            // 开始连接wifi
            wifiManager.enableNetwork(identityID, true);
            // 权限检查
            if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)) {
                // 如果没有权限就添加
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE},
                        9999);
            }
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 3; i++) {
                            Thread.sleep(5000);
                            boolean success = configuredNetworks.get(identityID).status == 0;
                            if (success) {
                                handler.sendEmptyMessage(WIFI_CONNECT_SUCCESS);
                            } else if (i == 2) {
                                handler.sendEmptyMessage(WIFI_CONNECT_FAIL);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * 为 WiFi 创建一个新的设置信息
     */
    private WifiConfiguration createWifiInfo(String SSID, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // 设置最高优先级，防止自动连接上别的wifi
        config.priority = 100000;
        if (type == WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "\"" + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

    public boolean isTargetWifiConnected() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            Toast.makeText(this, "当前连接的wifi名称是：" + wifiInfo.getSSID(),
                    Toast.LENGTH_SHORT).show();
            isFound = wifiInfo.getSSID().equals("\"" + TARGET_WIFI_NAME + "\"");
        }
        return wifiInfo != null && wifiInfo.getSSID().equals("\"" + TARGET_WIFI_NAME + "\"");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isConnection) {
            if (wifiManager.isWifiEnabled()) {
                // 关闭wifi
                wifiManager.setWifiEnabled(false);
            }
        }
        // 注销广播接收器
        unregisterReceiver(WiFiReceiver);
    }
}