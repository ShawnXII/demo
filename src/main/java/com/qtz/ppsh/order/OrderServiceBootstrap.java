package com.qtz.ppsh.order;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Dubbo服务启动类
 * 
 * @author Kevin Chang
 *
 */
public class OrderServiceBootstrap
{
    private static final Logger log = Logger.getLogger(OrderServiceBootstrap.class);
    private static volatile boolean running = true;
    static ClassPathXmlApplicationContext context;

    public static void main(String[] args) throws Exception
    {
        log.info("################orderService准备启动服务！################");
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        context.start();
        log.info("################orderService服务启动成功！################");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run()
            {
                if (context != null)
                {
                    context.stop();
                    context.close();
                    context = null;
                }
                log.info("################orderService服务已经停止!################");
                synchronized (OrderServiceBootstrap.class)
                {
                    running = false;
                    OrderServiceBootstrap.class.notify();
                }
            }
        });

        synchronized (OrderServiceBootstrap.class)
        {
            while (running)
            {
                try
                {
                    OrderServiceBootstrap.class.wait();
                }
                catch (Throwable e)
                {}
            }
        }
    }

}
