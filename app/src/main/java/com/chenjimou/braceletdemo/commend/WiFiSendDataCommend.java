package com.chenjimou.braceletdemo.commend;

import android.bluetooth.BluetoothSocket;

import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

public class WiFiSendDataCommend implements Callable<Exception>
{
    Socket socket;
    String msg;

    public WiFiSendDataCommend(Socket socket, String msg)
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
