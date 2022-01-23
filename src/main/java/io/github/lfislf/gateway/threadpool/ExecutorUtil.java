package io.github.lfislf.gateway.threadpool;

import java.util.concurrent.*;

public class ExecutorUtil {

    public static ExecutorService createExecutorService(String namePrefix) {
        int cores = Runtime.getRuntime().availableProcessors();
        long keepAliveTime = 30;
        int queueSize = 2048;
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();//.DiscardPolicy();
        return new ThreadPoolExecutor(cores, cores,
                keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize),
                new NamedThreadFactory(namePrefix), handler);
    }
}
