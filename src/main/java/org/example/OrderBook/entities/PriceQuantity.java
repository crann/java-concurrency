package org.example.OrderBook.entities;

import java.math.BigDecimal;

public record PriceQuantity(BigDecimal price, BigDecimal quantity) {
}
