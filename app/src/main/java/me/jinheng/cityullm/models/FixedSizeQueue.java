package me.jinheng.cityullm.models;

import java.util.LinkedList;

public class FixedSizeQueue<T> {

    private int maxSize;
    private LinkedList<T> queue;

    public FixedSizeQueue(int maxSize) {
        this.maxSize = maxSize;
        queue = new LinkedList<>();
    }

    public void add(T e) {
        if (queue.size() >= maxSize) {
            queue.poll();
        }
        queue.offer(e);
    }

    public int getSize() {
        return queue.size();
    }

    public T remove() {
        return queue.poll();
    }
}
