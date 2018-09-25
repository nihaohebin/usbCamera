package cn.usbfacedetect.util.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceThreadPool extends ThreadPoolExecutor {

    private boolean isPaused;
    private ReentrantLock pauseLock = new ReentrantLock();
    private Condition unpaused = pauseLock.newCondition();
    private AtomicLong counter = new AtomicLong();

    /**
     * @param corePoolSize    核心线程池大小，如果运行的线程少于 corePoolSize，则 Executor 始终首选添加新的线程，而不添加到workQueue等待执行。
     * @param maximumPoolSize 最大线程池大小
     * @param keepAliveTime   线程池中超过corePoolSize数目的空闲线程最大存活时间
     * @param workQueue
     */
    public ServiceThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, int workQueue) {
        super(corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(workQueue),
                new ServiceThreadFactory(),
                new AbortPolicy());

        this.prestartAllCoreThreads();
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        pauseLock.lock();
        try {
            while (isPaused) unpaused.await();
        } catch (InterruptedException e) {
        } finally {
            pauseLock.unlock();
            counter.incrementAndGet();
        }
    }

    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        counter.decrementAndGet();
    }


    public void pauseService() {
        pauseLock.lock();
        try {
            isPaused = true;
            while (counter.get() > 0) {
            }
        } finally {
            pauseLock.unlock();
        }
    }

    public void resumeService() {
        pauseLock.lock();
        try {
            isPaused = false;
            unpaused.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }
}
