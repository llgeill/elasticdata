package com.example.elasticdata.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h3>MSAGD</h3>
 * <p>创建自定义线程池</p>
 *
 * @author : liliguang
 * @date : 2020-04-16 13:21
 **/
public class ExecutorsUtil {

    public static ThreadPoolExecutor instance(Integer threadSize){
        return new ThreadPoolExecutor(
                1,
                threadSize,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(threadSize*2),
                new DefaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * The default thread factory
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "轨迹爬虫-" +
                    poolNumber.getAndIncrement() +
                    "-线程-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
