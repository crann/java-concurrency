package org.example.OrderBook.entities;

import org.example.OrderBook.enums.OrderEventType;

public record OrderEvent(OrderEventType type,
                         Order order,
                         long sequence) {
}
