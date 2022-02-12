package com.chenjimou.braceletdemo.base;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.chenjimou.braceletdemo.service.BraceletServiceConnection;

import java.io.IOException;
import java.net.Socket;

import androidx.appcompat.app.AppCompatActivity;

public class ConnectionHelper
{
    public static BluetoothSocket bluetoothSocket;
    public static BluetoothDevice bluetoothDevice;
    public static Socket wifiSocket;
    public static boolean isConnectRoom = false;

    public static void reset()
    {
        if (bluetoothDevice != null)
        {
            bluetoothDevice = null;
        }
        if (bluetoothSocket != null)
        {
            try
            {
                bluetoothSocket.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if (wifiSocket != null)
        {
            try
            {
                wifiSocket.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        isConnectRoom = false;
    }
}
