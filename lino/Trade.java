import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Trade {
  /**
   * Represents a completed trade made by the matching engine.
   * Each trade is for a given amount of a single product at an agreed price,
   * and has corresponding sell and buy orders.
   */

  private String product;
  private double amount;
  private double price;
  private Order sellOrder;
  private Order buyOrder;

  public Trade(String product, double amount, double price, Order sellOrder, Order buyOrder) {
    this.product = product;
    this.amount = amount;
    this.price = price;
    this.sellOrder = sellOrder;
    this.buyOrder = buyOrder;
  }

  public String getProduct() {
    return this.product;
  }

  public double getAmount() {
    return this.amount;
  }

  public Order getSellOrder() {
    return this.sellOrder;
  }

  public Order getBuyOrder() {
    return this.buyOrder;
  }

  public double getPrice() {
    return this.price;
  }

  @Override
  public String toString() {
    /**
     * Returns the string representation of the trade.
     * Format: SELLER->BUYER: AMOUNTxPRODUCT for PRICE
     */
    return String.format("%s->%s: %.2fx%s for $%.2f.",
        this.sellOrder.getTrader().getID(), this.buyOrder.getTrader().getID(),
        this.amount, this.product, this.price
    );
  }

  public boolean involvesTrader(Trader trader) {
    return (this.sellOrder.getTrader().equals(trader) || this.buyOrder.getTrader().equals(trader));
  }

  public static void writeTrades(List<Trade> trades, String path) {
    /**
     * Writes the list of trades to file in ASCII encoding.
     * For every trade in the provided list,
     * write the String representation on a new line in the given file.
     * If either argument is invalid, do nothing.
     */
    if (trades != null && path != null) {
      try {
        PrintWriter writer = new PrintWriter(new File(path));
        for (Trade trade : trades) {
          writer.println(trade);
        }
        writer.close();
      } catch (FileNotFoundException e) {
        // Spec says to do nothing if something goes wrong, but I want the stack trace for debugging.
        e.printStackTrace();
      }
    }
  }

  public static void writeTradesBinary(List<Trade> trades, String path) {
    /**
     * Writes the list of trades to file in binary encoding.
     * For every trade in the provided list, write the String representation,
     * followed by the Unit Separator byte (\u001f).
     * If either argument is invalid, do nothing.
     */
    if (trades != null && path != null) {
      try {
        DataOutputStream writer = new DataOutputStream(new FileOutputStream(path));
        for (Trade trade : trades) {
          writer.writeUTF(trade.toString());
          writer.writeUTF("\u001f");
        }
        writer.close();
      } catch (FileNotFoundException e) {
        // Spec says to do nothing if something goes wrong, but I want the stack trace for debugging.
        e.printStackTrace();
      } catch (IOException e) {
        // Spec says to do nothing if something goes wrong, but I want the stack trace for debugging.
        e.printStackTrace();
      }
    }
  }
}
