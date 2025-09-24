package org.example.interview_round_2.multi_threaded;

import java.math.BigDecimal;

// This class is intentionally mutable to allow for quantity updates during partial fills.
public class Order {
    private final String id;
    private final String symbol;
    private final Side side;
    private int quantity;
    private final BigDecimal price;

    public enum Side {
        BUY,
        SELL
    }

    public Order(String id, String symbol, Side side, int quantity, BigDecimal price) {
        if (quantity <= 0 || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity and price must be positive");
        }
        this.id = id;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
    }

    public String id() {
        return id;
    }

    public String symbol() {
        return symbol;
    }

    public Side side() {
        return side;
    }

    public int quantity() {
        return quantity;
    }

    public BigDecimal price() {
        return price;
    }

    public void decreaseQuantity(int amount) {
        if (amount > this.quantity) {
            throw new IllegalArgumentException("Cannot decrease quantity by more than the current quantity.");
        }
        this.quantity -= amount;
    }
}