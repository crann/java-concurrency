package org.example.OrderBook.datastructures;

import java.util.concurrent.atomic.AtomicLong;

// Lock-free ring buffer for high-throughput event processing
public class RingBuffer<T> {
    private final Object[] buffer;
    private final int mask;
    private final AtomicLong writeIndex = new AtomicLong(0);
    private final AtomicLong readIndex = new AtomicLong(0);

    public RingBuffer(int capacity) {
        if ((capacity & (capacity - 1)) != 0) {
            throw new IllegalArgumentException("Capacity must be power of 2");
        }
        this.buffer = new Object[capacity];
        this.mask = capacity - 1;
    }

    @SuppressWarnings("unchecked")
    public T poll() {
        long read = readIndex.get();
        if (read >= writeIndex.get()) {
            return null;
        }

        T item = (T) buffer[(int) read & mask];
        readIndex.compareAndSet(read, read + 1);
        return item;
    }

    public boolean offer(T item) {
        long write = writeIndex.get();
        if (write - readIndex.get() >= buffer.length) {
            return false; // Buffer full
        }

        buffer[(int) write & mask] = item;
        writeIndex.compareAndSet(write, write + 1);
        return true;
    }
}
