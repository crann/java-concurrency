package org.example.OrderBook.entities;

import org.example.OrderBook.enums.OrderSide;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

// Supporting classes
public record Order(String id,
                    OrderSide side,
                    BigDecimal price,
                    BigDecimal quantity,
                    AtomicReference<BigDecimal> remainingQuantity,
                    Instant timestamp) {

    public Order(String id, OrderSide side, BigDecimal price, BigDecimal quantity) {
        this(id, side, price, quantity, new AtomicReference<>(quantity), Instant.now());
    }

    public String getId() {
        return id;
    }

    public OrderSide getSide() {
        return side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity.get();
    }

    public void reduceQuantity(BigDecimal amount) {
        remainingQuantity.updateAndGet(current -> current.subtract(amount));
    }
}
