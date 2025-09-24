package org.example.interview_round_2.multi_threaded;

import java.util.List;

public class TradingEngine {
    private final OrderBook orderBook = new OrderBook();

    /**
     * Processes an order by attempting to match it against the order book.
     * This operation is thread-safe.
     * @param newOrder The order to be processed.
     * @return A list of trades that occurred as a result of the order.
     */
    public List<Trade> processOrder(Order newOrder) {
        return orderBook.processOrder(newOrder);
    }
}

/* -----------------------------------------
    NOTES
--------------------------------------------

If/Else handling if there were more order types (e.g. Market-on-Close", "Fill-or-Kill", "All-or-None" etc.):

1. Polymorphism:
You could have different classes for different order types (e.g. BuyOrder and SellOrder that
inherit from a base Order class).
Each class would have its own match method. The engine would simply call newOrder.match(orderBook),
and polymorphism would ensure the correct logic is executed. This is a very "object-oriented"
solution.

2. Strategy Pattern:
You could define a MatchingStrategy interface and have different
implementations for BUY and SELL. The TradingEngine would hold a map (Map<Order.Side,
MatchingStrategy>) and, instead of an if statement, would look up the correct
strategy from the map and execute it. This is extremely flexible if you need to add
new order types in the future.

--------------------------------------------

A trade occurs when a buy order's price (bid) is at or above a sell order's price (ask)
or a sell order's price (ask) is at or below a buy order's price (bid).

Bid: The highest price a buyer is willing to pay.
Ask / Offer: The lowest price a seller is willing to accept.

--------------------------------------------*/
