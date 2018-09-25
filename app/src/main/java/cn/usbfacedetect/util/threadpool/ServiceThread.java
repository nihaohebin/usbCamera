package cn.usbfacedetect.util.threadpool;

public class ServiceThread extends Thread {

    public ServiceThread(Runnable r) {
        super(r);

    }
}