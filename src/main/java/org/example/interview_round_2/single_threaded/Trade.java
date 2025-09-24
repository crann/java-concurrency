package org.example.interview_round_2.single_threaded;

import java.math.BigDecimal;
import java.time.Instant;

public record Trade(String buyOrderId, String sellOrderId, int quantity, BigDecimal price, Instant timestamp) {
    public Trade(String buyOrderId, String sellOrderId, int quantity, BigDecimal price) {
        this(buyOrderId, sellOrderId, quantity, price, Instant.now());
    }
}
