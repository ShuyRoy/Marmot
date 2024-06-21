package me.jinheng.cityullm.models;

public class NativeMessageReceiver {
    private final Object lock = new Object();

    private String receivedString = null;

    private boolean start = false;

    public void receiveStringFromNative(String value) {
        synchronized (lock) {
            receivedString = value;
            lock.notifyAll();
        }
    }

    public void receiveStartFromNative(float toeknPerSecondPrefill, float toeknPerSecondDecode) {
        synchronized (lock) {
            receivedString = String.format("prefill: %.1f tok/s; decode: %.1f tok/s", toeknPerSecondPrefill, toeknPerSecondDecode);
            start = true;
            lock.notifyAll();
        }
    }

    public boolean isStart() {
        return start;
    }

    public String waitForString() {
        synchronized (lock) {
            while ((receivedString == null) && (!start)) {
                try {
                    lock.wait(); // 等待 native 代码发送字符串
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            return receivedString;
        }
    }

    public void reset() {
        receivedString = null;
        start = false;
    }

}