package org.example.OrderBook.entities;

import java.time.Instant;
import java.util.List;

public record MarketData(List<PriceQuantity> bids, List<PriceQuantity> asks, Instant timestamp) {
}
