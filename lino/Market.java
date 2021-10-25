import java.util.ArrayList;
import java.util.List;

public class Market {
  /**
   * Represents a Matching Engine that processes orders and attempts to make trades between them.
   */

  private ArrayList<Order> buyBook;
  private ArrayList<Order> sellBook;
  private ArrayList<Trade> trades;

  public Market() {
    this.buyBook = new ArrayList<Order>();
    this.sellBook = new ArrayList<Order>();
    this.trades = new ArrayList<Trade>();
  }

  public List<Trade> placeSellOrder(Order order) {
    /**
     * Processes a sell order using Price-Time Priority.
     * If the order is null or is a buy order, do nothing and return null.
     * If the trader does not have sufficient amount of product, do nothing and return null.
     * For every trade:
     *  - close its finished orders,
     *  - remove finished orders from their respective book,
     *  - increase the balance of the seller (amount of product * price of unit),
     *  - decrease the balance of the buyer,
     *  - send the product to the buyer,
     *  - update the amount for any unfinished orders,
     *  - and add the trade to the returned list.
     */
    if (order == null || order.isBuy()) {
      return null;
    } else {
      Trader trader = order.getTrader();
      String product = order.getProduct();
      double amount = order.getAmount();
      if (trader.exportProduct(product, amount) < 0.0) {
        return null;
      } else {
        return this.placeOrder(order);
      }
    }
  }

  public List<Trade> placeBuyOrder(Order order) {
    /**
     * Processes a buy order using Price-Time Priority.
     * If the order is null or is a sell order, do nothing and return null.
     * For every trade:
     * - close its finished orders,
     * - remove finished orders from their respective book,
     * - increase the balance of the seller (amount of product * price of unit),
     * - decrease the balance of the buyer,
     * - send the product to the buyer,
     * - update the amount for any unfinished orders,
     * - and add the trade to the returned list.
     */
    if (order == null || !order.isBuy()) {
      return null;
    } else {
      return this.placeOrder(order);
    }
  }

  public boolean cancelBuyOrder(String orderID) {
    /**
     * Cancels and closes the given buy order and removes it from the buy book.
     * If the order is null, return false.
     * Returns whether the order could be closed.
     */
    if (orderID == null) {
      return false;
    } else {
      // Find the correct order in the book.
      for (int orderIndex = 0; orderIndex < this.buyBook.size(); orderIndex++) {
        Order order = this.buyBook.get(orderIndex);
        if (order.getID().equals(orderID)) {
          // Found the order, do the cancellation.
          this.buyBook.remove(orderIndex);
          order.close();

          return true;
        }
      }

      // The order isn't in this book.
      return false;
    }
  }

  public boolean cancelSellOrder(String orderID) {
    /**
     * Cancels and closes the given sell order and removes it from the buy book.
     * If the order is null, return false.
     * Returns whether the order could be closed.
     */
    if (orderID == null) {
      return false;
    } else {
      // Find the correct order in the book.
      for (int orderIndex = 0; orderIndex < this.sellBook.size(); orderIndex++) {
        Order order = this.sellBook.get(orderIndex);
        if (order.getID().equals(orderID)) {
          // Found the order, do the cancellation.
          this.sellBook.remove(orderIndex);
          order.close();
          order.getTrader().importProduct(order.getProduct(), order.getAmount());

          return true;
        }
      }

      // The order isn't in this book.
      return false;
    }
  }

  public List<Order> getSellBook() {
    return this.sellBook;
  }

  public List<Order> getBuyBook() {
    return this.buyBook;
  }

  public List<Trade> getTrades() {
    return this.trades;
  }

  public Order getOrder(String orderID) {
    // Check buyBook first (arbitrarily chosen)
    for (Order order : this.buyBook) {
      if (order.getID().equals(orderID)) {
        return order;
      }
    }
    // Wasn't in buyBook, check sellBook
    for (Order order : this.sellBook) {
      if (order.getID().equals(orderID)) {
        return order;
      }
    }
    // Wasn't in either book
    return null;
  }

  private List<Trade> placeOrder(Order order) {
    /** Perform the Price-Time Priority Algorithm to place the given order */

    boolean buyMode = order.isBuy();
    ArrayList<Order> fromBook, toBook;
    if (buyMode) {
      fromBook = this.buyBook;
      toBook = this.sellBook;
    } else {
      fromBook = this.sellBook;
      toBook = this.buyBook;
    }

    fromBook.add(order);

    List<Trade> trades = new ArrayList<Trade>();
    if (!toBook.isEmpty()) {
      // Filter to just orders with an allowed price range.
      ArrayList<Order> filteredToBook = new ArrayList<Order>(toBook.size());
      for (Order o : toBook) {
        if (o.getProduct().equals(order.getProduct())  // Ensure the orders are for the same product
            && ((buyMode && o.getPrice() <= order.getPrice())      // buy => less than max price
                || (!buyMode && o.getPrice() >= order.getPrice())) // sell => greater than min price
        ) {
          filteredToBook.add(o);
        }
      }

      // While there are still orders with a valid price, and our order is not fulfilled,
      // find the next order to use and trade with it.
      while (!filteredToBook.isEmpty() && !order.isClosed()) {
        // Get next order to trade with.
        Order nextTradeOrder = filteredToBook.get(0);
        for (Order o : filteredToBook) {
          // By using < / > instead of <= / >=, we get the first item in the list with the desired
          // price (max / min depending on buyMode).
          // Since the books are in temporal order, this automatically handles the time priority.
          if ((buyMode && o.getPrice() < nextTradeOrder.getPrice())     // buy => lowest price
              || (!buyMode && o.getPrice() > nextTradeOrder.getPrice()) // sell => highest price
          ) {
            nextTradeOrder = o;
          }
        }

        // Perform Trade
        double tradeAmount = Double.min(order.getAmount(), nextTradeOrder.getAmount());
        order.adjustAmount(-tradeAmount);
        nextTradeOrder.adjustAmount(-tradeAmount);
        if (order.isClosed()) {
          fromBook.remove(order);
        }
        if (nextTradeOrder.isClosed()) {
          toBook.remove(nextTradeOrder);
          filteredToBook.remove(nextTradeOrder);
        }

        Trade trade;
        Trader buyer, seller;
        String product = order.getProduct();
        if (buyMode) {
          seller = nextTradeOrder.getTrader();
          buyer = order.getTrader();

          buyer.importProduct(product, tradeAmount);
          trade = new Trade(product, tradeAmount, nextTradeOrder.getPrice(),
              nextTradeOrder, order);
        } else {
          buyer = nextTradeOrder.getTrader();
          seller = order.getTrader();

          buyer.importProduct(product, tradeAmount);
          trade = new Trade(product, tradeAmount, nextTradeOrder.getPrice(),
              order, nextTradeOrder);
        }
        double totalPrice = nextTradeOrder.getPrice() * tradeAmount;
        buyer.adjustBalance(-totalPrice);
        seller.adjustBalance(totalPrice);

        trades.add(trade);
        this.trades.add(trade);
      }
    }

    return trades;
  }

  public static List<Trade> filterTradesByTrader(List<Trade> trades, Trader trader) {
    /**
     * Filters the list of trades based on if the given trader was involved.
     * A new list is created, with the original list untouched.
     * Returns null if either parameter is null.
     */
    if (trades == null || trader == null) {
      return null;
    } else {
      ArrayList<Trade> filtered = new ArrayList<Trade>();
      for (Trade trade : trades) {
        if (trade.involvesTrader(trader)) {
          filtered.add(trade);
        }
      }
      return filtered;
    }
  }

  public static List<Trade> filterTradesByProduct(List<Trade> trades, String product) {
    /**
     * Filters the list of trades based on if the given product was traded.
     * A new list is created, with the original list untouched.
     * Returns null if either parameter is null.
     */
    if (trades == null || product == null) {
      return null;
    } else {
      ArrayList<Trade> filtered = new ArrayList<Trade>();
      for (Trade trade : trades) {
        if (product.equals(trade.getProduct())) {
          filtered.add(trade);
        }
      }
      return filtered;
    }
  }
}
