package com.chenjimou.braceletdemo.thread;

import com.chenjimou.braceletdemo.order.Order;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Dispatcher
{
    static volatile Dispatcher instance;

    public static Dispatcher getInstance()
    {
        if (instance == null)
        {
            synchronized (Dispatcher.class)
            {
                if (instance == null)
                    instance = new Dispatcher();
            }
        }
        return instance;
    }

    ThreadPoolExecutor threadPool; // 用于发送指令的线程池（最大并发）

    private Dispatcher()
    {
        threadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60,
                TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    public void sendOrder(Order order)
    {
        threadPool.execute(order);
    }

    public void shutdown()
    {
        threadPool.shutdown();
    }
}
