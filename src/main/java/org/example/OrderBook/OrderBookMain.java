package org.example.OrderBook;

import org.example.OrderBook.entities.MarketData;
import org.example.OrderBook.entities.Order;
import org.example.OrderBook.entities.OrderResult;
import org.example.OrderBook.enums.OrderSide;
import org.example.OrderBook.marketdata.MarketDataPublisher;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OrderBookMain {
    public static void main(String[] args) throws InterruptedException {
        // 1. Create a mock market data publisher
        MarketDataPublisher publisher = new MarketDataPublisher() {
            @Override
            public void publish(MarketData data) {
                System.out.println("--- Market Data Update ---");
                System.out.println("Bids: " + data.bids());
                System.out.println("Asks: " + data.asks());
                System.out.println("--------------------------");
            }
        };

        // 2. Create the ThreadSafeOrderBook
        ThreadSafeOrderBook orderBook = new ThreadSafeOrderBook("BTC/USD", publisher);

        // 3. Use an executor service to simulate concurrent users
        ExecutorService executor = Executors.newFixedThreadPool(10);

        System.out.println("Submitting orders...");

        // Add some initial orders
        CompletableFuture<OrderResult> buy1 = orderBook.addOrder(new Order(UUID.randomUUID().toString(), OrderSide.BUY, new BigDecimal("50000"), new BigDecimal("1.0")));
        CompletableFuture<OrderResult> sell1 = orderBook.addOrder(new Order(UUID.randomUUID().toString(), OrderSide.SELL, new BigDecimal("50002"), new BigDecimal("0.5")));

        // Simulate more activity
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            executor.submit(() -> {
                BigDecimal price = new BigDecimal("50000").add(new BigDecimal(finalI * 2));
                orderBook.addOrder(new Order(UUID.randomUUID().toString(), OrderSide.BUY, price, new BigDecimal("0.2")));
            });
            executor.submit(() -> {
                BigDecimal price = new BigDecimal("50002").add(new BigDecimal(finalI * 2));
                orderBook.addOrder(new Order(UUID.randomUUID().toString(), OrderSide.SELL, price, new BigDecimal("0.3")));
            });
        }

        // An order that will be cancelled
        String orderToCancelId = UUID.randomUUID().toString();
        CompletableFuture<OrderResult> orderToCancelFuture = orderBook.addOrder(new Order(orderToCancelId, OrderSide.BUY, new BigDecimal("49000"), new BigDecimal("10.0")));

        // A matching order - should create trades
        CompletableFuture<OrderResult> matchingSell = orderBook.addOrder(new Order(UUID.randomUUID().toString(), OrderSide.SELL, new BigDecimal("50000"), new BigDecimal("0.75")));

        // Wait for initial orders to be processed
        CompletableFuture.allOf(buy1, sell1, orderToCancelFuture, matchingSell).join();

        System.out.println("Initial orders processed. Now cancelling an order.");

        // Cancel an order
        CompletableFuture<OrderResult> cancelResult = orderBook.cancelOrder(orderToCancelId);

        // Wait for results and print them
        System.out.println("Matching Sell Result: " + matchingSell.join());
        System.out.println("Cancel Result: " + cancelResult.join());

        // Shutdown executor and wait for tasks to finish
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\nFinal Order Book State:");
        MarketData finalSnapshot = orderBook.createSnapshot();
        System.out.println("Bids: " + finalSnapshot.bids());
        System.out.println("Asks: " + finalSnapshot.asks());
    }
}
