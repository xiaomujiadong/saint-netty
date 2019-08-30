package com.saint.netty.init;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartServer implements ApplicationListener<ContextRefreshedEvent> {

    private TcpServerInit tcpServerInit = null;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //只加载一次
        if (this.tcpServerInit == null) {
            //启动netty 服务
            this.tcpServerInit = (TcpServerInit) contextRefreshedEvent.getApplicationContext().getBean("tcpServerInit");
            ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor(
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            return new Thread(r, "tcpServerInit");
                        }
                    });
            singleThreadExecutor.execute(() -> {
               this.tcpServerInit.run();
            });
        }
        //检测tcp server和kcp server是否都启动成功，等启动成功再注册zookeeper
        while (!tcpServerInit.isInited()) {
            try {
                TimeUnit.MILLISECONDS.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
