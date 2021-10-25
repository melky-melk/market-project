public class Order {
  /**
   * Represents an order to buy/sell a product on the market.
   *
   * Each order is for a positive amount of a single product, is either a buy order or a sell order,
   * has a limiting price (maximum for buy, minimum for sell), an invoking trader, and a unique
   * 4 digit hexadecimal id String. Orders remain open until they are finalised or cancelled.
   */

  private static int nextIDNumber;

  private String id;
  private String product;
  private boolean isBuy;
  private boolean open;
  private double amount;
  private double price;
  private Trader trader;

  public Order(String product, boolean isBuy, double amount, double price,
        Trader trader, String id) {
    this.product = product;
    this.isBuy = isBuy;
    this.open = true;
    this.amount = amount;
    this.price = price;
    this.trader = trader;
    this.id = id;
  }

  public String getProduct() {
    return this.product;
  }

  public boolean isBuy() {
    return this.isBuy;
  }

  public double getAmount() {
    return this.amount;
  }

  public Trader getTrader() {
    return this.trader;
  }

  public void close() {
    this.open = false;
  }

  public boolean isClosed() {
    return !this.open;
  }

  public double getPrice() {
    return this.price;
  }

  public String getID() {
    return this.id;
  }

  public void adjustAmount(double change) {
    /**
     * Adjusts the amount requested in the order by adding the change.
     * Only changes if the new amount is greater than zero.
     */
    this.amount += change;
    if (this.amount < 0.0) {
      this.amount -= change;
    } else if (this.amount == 0.0) {
      this.close();
    }
  }

  @Override
  public String toString() {
    /**
     * Returns the string representation of the order.
     * Format: ID: [BUY/SELL] AMOUNTxPRODUCT @ $PRICE
     */
    String orderType = this.isBuy ? "BUY" : "SELL";
    return String.format("%s: %s %.2fx%s @ $%.2f",
        this.id, orderType, this.amount, this.product, this.price);
  }

  public static String nextID() {
    /**
     * Returns the next valid Order ID.
     * Order IDs are 4 digit hexadecimal strings, and are assigned in numerical order.
     */
    return String.format("%04X", Order.nextIDNumber++);
  }

  public static void resetOrderNumbers() {
    Order.nextIDNumber = 0;
  }
}
