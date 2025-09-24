package org.example.interview_round_2.single_threaded;

import java.math.BigDecimal;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        TradingEngine engine = new TradingEngine();

        Order buyOrder = new Order("B1", "AAPL", Order.Side.BUY, 100, BigDecimal.valueOf(150.0));
        Order sellOrder = new Order("S1", "AAPL", Order.Side.SELL, 50, BigDecimal.valueOf(149.0));

        Optional<Trade> trade1 = engine.processOrder(buyOrder);
        System.out.println("First order result: " + trade1); // No matching order, no trade.

        Optional<Trade> trade2 = engine.processOrder(sellOrder);
        System.out.println("Second order result: " + trade2); // Matching order, trade performed.
    }
}
