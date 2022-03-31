package com.scy.netty.scheduler;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.*;

/**
 * @author : shichunyang
 * Date    : 2022/3/31
 * Time    : 8:13 下午
 * ---------------------------------------
 * Desc    : HashedWheelScheduler
 */
public class HashedWheelScheduler {

    private final ConcurrentMap<SchedulerKey, Timeout> scheduledFutures = new ConcurrentHashMap<>();

    private final HashedWheelTimer hashedWheelTimer;

    private final ThreadPoolExecutor threadPoolExecutor;

    public HashedWheelScheduler(ThreadPoolExecutor threadPoolExecutor) {
        hashedWheelTimer = new HashedWheelTimer();
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public HashedWheelScheduler(ThreadFactory threadFactory, ThreadPoolExecutor threadPoolExecutor) {
        hashedWheelTimer = new HashedWheelTimer(threadFactory);
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public void cancel(SchedulerKey key) {
        Timeout timeout = scheduledFutures.remove(key);
        if (timeout != null) {
            timeout.cancel();
        }
    }

    public void schedule(SchedulerKey key, Runnable runnable, long delay, TimeUnit unit) {
        Timeout result = hashedWheelTimer.newTimeout(timeout -> threadPoolExecutor.execute(() -> {
            try {
                runnable.run();
            } finally {
                scheduledFutures.remove(key);
            }
        }), delay, unit);

        replaceScheduledFuture(key, result);
    }

    public void shutdown() {
        hashedWheelTimer.stop();
    }

    private void replaceScheduledFuture(final SchedulerKey key, final Timeout newTimeout) {
        final Timeout oldTimeout;

        if (newTimeout.isExpired()) {
            // no need to put already expired timeout to scheduledFutures map.
            // simply remove old timeout
            oldTimeout = scheduledFutures.remove(key);
        } else {
            oldTimeout = scheduledFutures.put(key, newTimeout);
        }

        // if there was old timeout, cancel it
        if (oldTimeout != null) {
            oldTimeout.cancel();
        }
    }
}
