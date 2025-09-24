package org.example.interview_round_2.multi_threaded;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class OrderBook {
    private final ConcurrentSkipListMap<BigDecimal, Order> buyOrders = new ConcurrentSkipListMap<>(Collections.reverseOrder());
    private final ConcurrentSkipListMap<BigDecimal, Order> sellOrders = new ConcurrentSkipListMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();

    public List<Trade> processOrder(Order newOrder) {
        writeLock.lock();
        try {
            List<Trade> trades = (newOrder.side() == Order.Side.BUY)
                    ? matchBuyOrder(newOrder)
                    : matchSellOrder(newOrder);

            // If the new order is not fully filled, add it to the order book.
            if (newOrder.quantity() > 0) {
                addOrder(newOrder);
            }

            return trades;
        } finally {
            writeLock.unlock();
        }
    }

    // This is a private helper method and must be called from within a write lock.
    private void addOrder(Order order) {
        if (order.side() == Order.Side.BUY) {
            buyOrders.put(order.price(), order);
        } else {
            sellOrders.put(order.price(), order);
        }
    }

    // This is a private helper method and must be called from within a write lock.
    private List<Trade> matchBuyOrder(Order buyOrder) {
        List<Trade> trades = new ArrayList<>();
        while (buyOrder.quantity() > 0 && !sellOrders.isEmpty()) {
            Order bestAsk = sellOrders.firstEntry().getValue();
            if (buyOrder.price().compareTo(bestAsk.price()) < 0) {
                break; // No more matches possible
            }

            int tradeQuantity = Math.min(buyOrder.quantity(), bestAsk.quantity());
            trades.add(new Trade(buyOrder.id(), bestAsk.id(), tradeQuantity, bestAsk.price()));

            buyOrder.decreaseQuantity(tradeQuantity);
            bestAsk.decreaseQuantity(tradeQuantity);

            if (bestAsk.quantity() == 0) {
                sellOrders.remove(bestAsk.price());
            }
        }
        return trades;
    }

    // This is a private helper method and must be called from within a write lock.
    private List<Trade> matchSellOrder(Order sellOrder) {
        List<Trade> trades = new ArrayList<>();
        while (sellOrder.quantity() > 0 && !buyOrders.isEmpty()) {
            Order bestBid = buyOrders.firstEntry().getValue();
            if (sellOrder.price().compareTo(bestBid.price()) > 0) {
                break; // No more matches possible
            }

            int tradeQuantity = Math.min(sellOrder.quantity(), bestBid.quantity());
            trades.add(new Trade(bestBid.id(), sellOrder.id(), tradeQuantity, bestBid.price()));

            sellOrder.decreaseQuantity(tradeQuantity);
            bestBid.decreaseQuantity(tradeQuantity);

            if (bestBid.quantity() == 0) {
                buyOrders.remove(bestBid.price());
            }
        }
        return trades;
    }
}
