package com.qtz.ppsh.order.service;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Dubbo服务启动类
 * 
 * @author Kevin Chang
 *
 */
public class PpshOrderServiceBootstrap{
    private static final Logger log = Logger.getLogger(PpshOrderServiceBootstrap.class);
    private static volatile boolean running = true;
    static ClassPathXmlApplicationContext context;

    public static void main(String[] args) throws Exception
    {
        log.info("################ppshOrderService准备启动服务！################");
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        context.start();
        log.info("################ppshOrderService服务启动成功！################");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run()
            {
                if (context != null)
                {
                    context.stop();
                    context.close();
                    context = null;
                }
                log.info("################ppshOrderService服务已经停止!################");
                synchronized (PpshOrderServiceBootstrap.class)
                {
                    running = false;
                    PpshOrderServiceBootstrap.class.notify();
                }
            }
        });

        synchronized (PpshOrderServiceBootstrap.class)
        {
            while (running)
            {
                try
                {
                	PpshOrderServiceBootstrap.class.wait();
                }
                catch (Throwable e)
                {}
            }
        }
    }

}
