package org.example.interview_round_2.multi_threaded;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        TradingEngine engine = new TradingEngine();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        System.out.println("--- Scenario: Multiple orders submitted concurrently ---");

        List<CompletableFuture<Void>> futures = Stream.of(
                new Order("B1", "AAPL", Order.Side.BUY, 100, BigDecimal.valueOf(151.0)),
                new Order("S1", "AAPL", Order.Side.SELL, 70, BigDecimal.valueOf(150.5)), // Should match B1
                new Order("B2", "AAPL", Order.Side.BUY, 200, BigDecimal.valueOf(150.0)),
                new Order("S2", "AAPL", Order.Side.SELL, 30, BigDecimal.valueOf(152.0)),
                new Order("B3", "AAPL", Order.Side.BUY, 80, BigDecimal.valueOf(151.5))  // Should NOT match S2 (price is too low)
        ).map(order -> CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + ": Submitting " + order.side() + " " + order.quantity() + " @ " + order.price());
            List<Trade> trades = engine.processOrder(order);
            if (!trades.isEmpty()) {
                trades.forEach(t -> System.out.println(">>> TRADE EXECUTED: " + t));
            }
        }, executor)).collect(Collectors.toList());

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Shutdown the executor
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("\nTrading simulation finished.");
    }
}
