package org.example.Concurrency;

public class Counter {
    private int count = 0;

    public synchronized void increment(){
        count++;
        System.out.println("Count: " + count);
    }
}
