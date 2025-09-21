package org.example.OrderBook.entities;

import java.util.List;

public record OrderResult(String orderId, boolean success, String message, List<Trade> trades) {
    public static OrderResult success(String orderId, String message) {
        return new OrderResult(orderId, true, message, List.of());
    }

    public static OrderResult success(String orderId, String message, List<Trade> trades) {
        return new OrderResult(orderId, true, message, trades);
    }

    public static OrderResult error(String orderId, String message) {
        return new OrderResult(orderId, false, message, List.of());
    }
}
