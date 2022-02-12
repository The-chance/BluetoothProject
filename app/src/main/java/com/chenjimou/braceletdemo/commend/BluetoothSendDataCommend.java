package com.chenjimou.braceletdemo.commend;

import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import com.chenjimou.braceletdemo.ui.MainActivity;

import java.io.OutputStream;
import java.util.concurrent.Callable;

public class BluetoothSendDataCommend implements Callable<Exception>
{
    BluetoothSocket socket;
    String msg;

    public BluetoothSendDataCommend(BluetoothSocket socket, String msg)
    {
        this.socket = socket;
        this.msg = msg;
    }

    @Override
    public Exception call()
    {
        try
        {
            OutputStream outputStream = socket.getOutputStream();
            if (outputStream != null)
            {
                byte[] bytes = new byte[msg.length()];
                for (int i = 0; i < msg.length(); i++)
                {
                    char character = msg.charAt(i);
                    bytes[i] = (byte) character;
                }
                outputStream.write(bytes);
            }
        }
        catch (Exception e)
        {
            return e;
        }
        return null;
    }
}
