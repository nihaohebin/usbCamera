package cn.usbfacedetect.util.threadpool;

import java.util.concurrent.ThreadFactory;

public class ServiceThreadFactory implements ThreadFactory {

    public ServiceThread newThread(Runnable r) {
        return new ServiceThread(r);
    }
}
