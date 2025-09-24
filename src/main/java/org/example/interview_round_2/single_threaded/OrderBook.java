package org.example.interview_round_2.single_threaded;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.TreeMap;

public class OrderBook {
    // TreeMap for automatic sorting
    private final TreeMap<BigDecimal, Order> buyOrders = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<BigDecimal, Order> sellOrders = new TreeMap<>();

    public void addOrder(Order order) {
        if (order.side() == Order.Side.BUY) {
            buyOrders.put(order.price(), order);
        } else {
            sellOrders.put(order.price(), order);
        }
    }

    public Order getBestBid() {
        return buyOrders.isEmpty() ? null : buyOrders.firstEntry().getValue();
    }

    public Order getBestAsk() {
        return sellOrders.isEmpty() ? null : sellOrders.firstEntry().getValue();
    }
}

/* -----------------------------------------
    NOTES
--------------------------------------------
No need to manually override hashCode and equals for the Order record used in the TreeMaps:

1. record Does It for us:
One of the biggest advantages of using a record is that the Java compiler automatically generates
equals(), hashCode(), and toString() methods for you based on all the fields in the record.

2. Correct implementation:
The Order records will be considered equal if and only if all their corresponding fields have the
same value. The hashCode is also generated consistently based on the values of all fields.

3. Context in TreeMap:
In the OrderBook the Order object is the value in the TreeMap, not the key. The TreeMap uses the
equals() and hashCode() methods of its keys (the BigDecimal prices) to manage its structure, but
it does not use these methods on its values.
 --------------------------------------------*/
