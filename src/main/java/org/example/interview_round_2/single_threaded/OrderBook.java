package org.example.interview_round_2.single_threaded;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.TreeMap;

public class OrderBook {
    // TreeMap for automatic sorting
    private final TreeMap<BigDecimal, Order> buyOrders = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<BigDecimal, Order> sellOrders = new TreeMap<>();

    public void addOrder(Order order) {
        if (order.side() == Side.BUY) {
            buyOrders.put(order.price(), order);
        } else {
            sellOrders.put(order.price(), order);
        }
    }

    public Order getBestBid() {
        return buyOrders.isEmpty() ? null : buyOrders.firstEntry().getValue();
    }

    public Order getBestAsk() {
        return sellOrders.isEmpty() ? null : sellOrders.firstEntry().getValue();
    }

    public boolean canMatch() {
        Order bestBid = getBestBid();
        Order bestAsk = getBestAsk();
        return bestBid != null && bestAsk != null &&
                bestBid.price().compareTo(bestAsk.price()) >= 0;
    }
}
