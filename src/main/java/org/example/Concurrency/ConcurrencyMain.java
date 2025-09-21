package org.example.Concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class ConcurrencyMain {
    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(2);
        Counter counter = new Counter();

        IntStream.range(0, 1000)
                .forEach(x -> service.submit(counter::increment));
    }
}
