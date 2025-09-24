package org.example.interview_round_2.single_threaded;

import java.math.BigDecimal;

public record Order(String id, String symbol, Order.Side side, int quantity, BigDecimal price) {
    public enum Side {
        BUY,
        SELL
    }

    public Order {
        if (quantity <= 0 || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity and price must be positive");
        }
    }
}