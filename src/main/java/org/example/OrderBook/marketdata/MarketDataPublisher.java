package org.example.OrderBook.marketdata;

import org.example.OrderBook.entities.MarketData;

// Market data publisher interface
public interface MarketDataPublisher {
    void publish(MarketData data);
}
