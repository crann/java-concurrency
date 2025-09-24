package org.example.interview_round_2.single_threaded;

public class TradingEngine {
    private final OrderBook orderBook = new OrderBook();

    public Trade processOrder(Order newOrder) {
        Trade trade;
        if (newOrder.side() == Side.BUY) {
            trade = tryMatchBuyOrder(newOrder);
        } else {
            trade = tryMatchSellOrder(newOrder);
        }

        if (trade != null) {
            return trade;
        }

        orderBook.addOrder(newOrder);
        return null;
    }

    private Trade tryMatchBuyOrder(Order buyOrder) {
        Order bestAsk = orderBook.getBestAsk();
        if (bestAsk != null && buyOrder.price().compareTo(bestAsk.price()) >= 0) {
            // Execute trade at ask price
            return new Trade(buyOrder.id(),
                    bestAsk.id(),
                    Math.min(buyOrder.quantity(), bestAsk.quantity()),
                    bestAsk.price());
        }
        return null;
    }

    private Trade tryMatchSellOrder(Order sellOrder) {
        Order bestBid = orderBook.getBestBid();
        if (bestBid != null && sellOrder.price().compareTo(bestBid.price()) <= 0) {
            // Execute trade at bid price
            return new Trade(bestBid.id(), sellOrder.id(),
                    Math.min(sellOrder.quantity(), bestBid.quantity()),
                    bestBid.price());
        }
        return null;
    }
}


// A trade occurs when a buy order's price (bid) is at or above a sell order's price (ask)
// or a sell order's price (ask) is at or below a buy order's price (bid).
// Bid: The highest price a buyer is willing to pay.
// Ask / Offer: The lowest price a seller is willing to accept.