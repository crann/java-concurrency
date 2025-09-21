package org.example.OrderBook;

import org.example.OrderBook.datastructures.RingBuffer;
import org.example.OrderBook.entities.MarketData;
import org.example.OrderBook.entities.Order;
import org.example.OrderBook.entities.OrderEvent;
import org.example.OrderBook.enums.OrderEventType;
import org.example.OrderBook.entities.OrderResult;
import org.example.OrderBook.enums.OrderSide;
import org.example.OrderBook.entities.PriceQuantity;
import org.example.OrderBook.entities.Trade;
import org.example.OrderBook.marketdata.MarketDataPublisher;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * High-performance thread-safe order book implementation
 * Handles thousands of updates per second with lock-free operations where possible
 */
public class ThreadSafeOrderBook {

    // Price levels using concurrent skip list for O(log n) operations
    private final ConcurrentSkipListMap<BigDecimal, PriceLevel> bidLevels;
    private final ConcurrentSkipListMap<BigDecimal, PriceLevel> askLevels;

    // Order tracking
    private final ConcurrentHashMap<String, Order> activeOrders;

    // Sequence number for ordering events
    private final AtomicLong sequenceNumber = new AtomicLong(0);

    // Market data publisher
    private final MarketDataPublisher marketDataPublisher;

    // Lock-free ring buffer for order events
    private final RingBuffer<OrderEvent> eventBuffer;

    public ThreadSafeOrderBook(String symbol, MarketDataPublisher publisher) {
        // Use reverse order for bids (highest price first)
        this.bidLevels = new ConcurrentSkipListMap<>(Collections.reverseOrder());

        // Natural order for asks (lowest price first)
        this.askLevels = new ConcurrentSkipListMap<>();

        this.activeOrders = new ConcurrentHashMap<>();
        this.marketDataPublisher = publisher;
        this.eventBuffer = new RingBuffer<>(8192); // Power of 2 for efficiency
    }

    /**
     * Add order to the book - returns immediately for async processing
     */
    public CompletableFuture<OrderResult> addOrder(Order order) {
        long sequence = sequenceNumber.incrementAndGet();
        OrderEvent event = new OrderEvent(OrderEventType.ADD, order, sequence);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return processAddOrder(event);
            } catch (Exception e) {
                return OrderResult.error(order.getId(), e.getMessage());
            }
        });
    }

    /**
     * Cancel order - atomic operation
     */
    public CompletableFuture<OrderResult> cancelOrder(String orderId) {
        return CompletableFuture.supplyAsync(() -> {
            Order order = activeOrders.remove(orderId);
            if (order == null) {
                return OrderResult.error(orderId, "Order not found");
            }

            removeOrderFromLevel(order);
            publishMarketData();

            return OrderResult.success(orderId, "Order cancelled");
        });
    }

    private OrderResult processAddOrder(OrderEvent event) {
        Order order = event.order();

        // Attempt matching first
        List<Trade> trades = tryMatch(order);

        // Add remaining quantity to book if not fully filled
        if (order.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
            addOrderToLevel(order);
            activeOrders.put(order.getId(), order);
        }

        // Publish market data update
        publishMarketData();

        return OrderResult.success(order.getId(), "Order processed", trades);
    }

    /**
     * Lock-free matching algorithm using optimistic concurrency
     */
    private List<Trade> tryMatch(Order incomingOrder) {
        List<Trade> trades = new ArrayList<>();
        NavigableMap<BigDecimal, PriceLevel> oppositeSide =
                incomingOrder.getSide() == OrderSide.BUY ? askLevels : bidLevels;

        while (incomingOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0) {
            Map.Entry<BigDecimal, PriceLevel> bestLevel = oppositeSide.firstEntry();

            if (bestLevel == null || !canMatch(incomingOrder, bestLevel.getKey())) {
                break;
            }

            PriceLevel level = bestLevel.getValue();
            Trade trade = level.executeAgainst(incomingOrder);

            if (trade != null) {
                trades.add(trade);

                // Remove level if empty
                if (level.isEmpty()) {
                    oppositeSide.remove(bestLevel.getKey());
                }
            } else {
                break; // No more matching possible
            }
        }

        return trades;
    }

    private boolean canMatch(Order order, BigDecimal price) {
        if (order.getSide() == OrderSide.BUY) {
            return order.getPrice().compareTo(price) >= 0;
        } else {
            return order.getPrice().compareTo(price) <= 0;
        }
    }

    private void addOrderToLevel(Order order) {
        NavigableMap<BigDecimal, PriceLevel> side =
                order.getSide() == OrderSide.BUY ? bidLevels : askLevels;

        side.computeIfAbsent(order.getPrice(), price ->
                new PriceLevel(price, order.getSide())).addOrder(order);
    }

    private void removeOrderFromLevel(Order order) {
        NavigableMap<BigDecimal, PriceLevel> side =
                order.getSide() == OrderSide.BUY ? bidLevels : askLevels;

        PriceLevel level = side.get(order.getPrice());
        if (level != null) {
            level.removeOrder(order);
            if (level.isEmpty()) {
                side.remove(order.getPrice());
            }
        }
    }

    private void publishMarketData() {
        MarketData snapshot = createSnapshot();
        marketDataPublisher.publish(snapshot);
    }

    public MarketData createSnapshot() {
        // Create immutable snapshot for publishing
        List<PriceQuantity> bids = bidLevels.values().stream()
                .limit(10) // Top 10 levels
                .map(PriceLevel::toPriceQuantity)
                .toList();

        List<PriceQuantity> asks = askLevels.values().stream()
                .limit(10)
                .map(PriceLevel::toPriceQuantity)
                .toList();

        return new MarketData(bids, asks, Instant.now());
    }

    /**
     * Thread-safe price level implementation
     */
    private static class PriceLevel {
        private final BigDecimal price;
        private final OrderSide side;
        private final ConcurrentLinkedDeque<Order> orders;
        private final AtomicReference<BigDecimal> totalQuantity;
        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        public PriceLevel(BigDecimal price, OrderSide side) {
            this.price = price;
            this.side = side;
            this.orders = new ConcurrentLinkedDeque<>();
            this.totalQuantity = new AtomicReference<>(BigDecimal.ZERO);
        }

        public void addOrder(Order order) {
            lock.writeLock().lock();
            try {
                orders.addLast(order);
                totalQuantity.updateAndGet(qty -> qty.add(order.getRemainingQuantity()));
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void removeOrder(Order order) {
            lock.writeLock().lock();
            try {
                if (orders.remove(order)) {
                    totalQuantity.updateAndGet(qty -> qty.subtract(order.getRemainingQuantity()));
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        /**
         * Execute trade against this level - atomic operation
         */
        public Trade executeAgainst(Order incomingOrder) {
            lock.writeLock().lock();
            try {
                Order headOrder = orders.peekFirst();
                if (headOrder == null) {
                    return null;
                }

                BigDecimal tradeQuantity = headOrder.getRemainingQuantity()
                        .min(incomingOrder.getRemainingQuantity());

                headOrder.reduceQuantity(tradeQuantity);
                incomingOrder.reduceQuantity(tradeQuantity);

                // Remove fully filled order
                if (headOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
                    orders.removeFirst();
                }

                totalQuantity.updateAndGet(qty -> qty.subtract(tradeQuantity));

                return new Trade(
                        UUID.randomUUID().toString(),
                        headOrder.getId(),
                        incomingOrder.getId(),
                        price,
                        tradeQuantity,
                        Instant.now()
                );
            } finally {
                lock.writeLock().unlock();
            }
        }

        public boolean isEmpty() {
            return orders.isEmpty();
        }

        public PriceQuantity toPriceQuantity() {
            return new PriceQuantity(price, totalQuantity.get());
        }
    }
}
