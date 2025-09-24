package org.example.interview_round_2.single_threaded;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        TradingEngine engine = new TradingEngine();

        Order buyOrder = new Order("B1", "AAPL", Side.BUY, 100, BigDecimal.valueOf(150.0));
        Order sellOrder = new Order("S1", "AAPL", Side.SELL, 50, BigDecimal.valueOf(149.0));

        Trade trade1 = engine.processOrder(buyOrder);
        System.out.println("First order result: " + trade1); // No matching order, no trade.

        Trade trade2 = engine.processOrder(sellOrder);
        System.out.println("Second order result: " + trade2); // Matching order, trade performed.
    }
}
