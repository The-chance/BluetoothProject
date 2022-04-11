package com.chenjimou.braceletdemo.thread;

import android.util.Log;
import android.widget.Toast;

import com.chenjimou.braceletdemo.base.BaseApplication;
import com.chenjimou.braceletdemo.ui.MainActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class HoldConnectionThread extends Thread
{
    Callback callback;
    boolean isWiFi;

    public HoldConnectionThread(boolean isWiFi, Callback callback)
    {
        this.isWiFi = isWiFi;
        this.callback = callback;
    }

    @Override
    public void run()
    {
        Log.d("HoldThread", "run: ");
        try
        {
            InputStream tmpin=null;
            OutputStream tmpout=null;

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            isWiFi ? BaseApplication.wifiSocket.getInputStream()
                                   : BaseApplication.btSocket.getInputStream()));

            Log.d("TAG", "run: "+reader);
            // 检查线程中断标志位
            while (!Thread.currentThread().isInterrupted())
            {

                StringBuilder sb = new StringBuilder();

                int ch;
                while ((ch=reader.read()) != -1 && ch != '#')
                {
                    sb.append((char)ch);
                    Log.d("TAG", "run: "+sb);
                }

                Log.d("TAG", "run: "+"循环结束");
                String data = sb.toString();
                Log.d("TAG", "run: "+sb.toString().length()+sb.toString());

                if ((sb.toString().length()!=9 &&sb.toString().contains("TEMP"))){
                    Log.d("TAG", "进入else");
                    callback.onResponse("fail");
                    sb.setLength(0);
                }else{
                    //用九位字符限制输入必须符合格式要求
                    Log.d("TAG", "进入if");
                    callback.onResponse(sb.toString());
                    sb.setLength(0); // 重置stringBuilder
                }





//                String resultData = null;      //需要返回的结果
//                byte[] data=new byte[1024];
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                int len=0;
//                StringBuffer a=new StringBuffer();
//                try{
//                    Log.d("TAG", "run: while前");
//                    while((len = tmpin.read()) != -1&&len!='#') {
//                        byteArrayOutputStream.write(data, 0, len);
//                        char c = (char) len;
//                        a.append(c);
//                        Log.d("TAG", "run: "+c);
//                    }
//                    Log.d("TAG", "run: while后");
//                }catch(IOException e){
//                    e.printStackTrace();
//                }
//                String str = a.toString();
//                Log.d("TAG", "字符串："+str);
//                callback.onResponse(str);
            }


        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public interface Callback
    {
        void onResponse(String msg);
    }
}
