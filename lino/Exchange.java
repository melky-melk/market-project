import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Exchange {
  /** Represents the user interface through which people can interact with the market. */

  private Market market;
  private HashMap<String, Trader> traders;

  public Exchange() {
    this.market = new Market();
    this.traders = new HashMap<String, Trader>();
  }

  public void run() {
    /** Resets the Exchange and starts the main CLI loop. */
    Order.resetOrderNumbers();
    Scanner stdin = new Scanner(System.in);
    boolean running = true;

    // Main program loop
    while (running) {
      System.out.print("$ ");
      running = this.parseCommand(stdin);
    }

    // Closing message
    System.out.println("Have a nice day.");
  }

  private boolean parseCommand(Scanner stdin) {
    /** Parses a command and returns whether to exit. */
    String command = stdin.next();
    switch (command.toUpperCase()) {
      case "EXIT":
        return false;
      case "ADD": {
          String id = stdin.next();
          double balance = stdin.nextDouble();
          this.addTrader(id, balance);
          break;
        }
      case "BALANCE": {
          String id = stdin.next();
          this.printTraderBalance(id);
          break;
        }
      case "INVENTORY": {
          String id = stdin.next();
          this.printTraderInventory(id);
          break;
        }
      case "AMOUNT": {
          String id = stdin.next();
          String product = stdin.next();
          this.printTraderAmount(id, product);
          break;
        }
      case "SELL": {
          String id = stdin.next();
          String product = stdin.next();
          double amount = stdin.nextDouble();
          double price = stdin.nextDouble();
          this.makeOrder(false, id, product, amount, price);
          break;
        }
      case "BUY": {
          String id = stdin.next();
          String product = stdin.next();
          double amount = stdin.nextDouble();
          double price = stdin.nextDouble();
          this.makeOrder(true, id, product, amount, price);
          break;
        }
      case "IMPORT": {
          String id = stdin.next();
          String product = stdin.next();
          double amount = stdin.nextDouble();
          this.importProduct(id, product, amount);
          break;
        }
      case "EXPORT": {
          String id = stdin.next();
          String product = stdin.next();
          double amount = stdin.nextDouble();
          this.exportProduct(id, product, amount);
          break;
        }
      case "CANCEL": {
          String subCommand = stdin.next();
          String order = stdin.next();
          if (subCommand.equalsIgnoreCase("SELL")) {
            this.cancelOrder(false, order);
          } else {
            // subCommand must be "BUY", since we know it is a valid command.
            this.cancelOrder(true, order);
          }
          break;
        }
      case "ORDER": {
          String order = stdin.next();
          this.printOrder(order);
          break;
        }
      case "TRADERS": {
          this.printTraders();
          break;
        }
      case "TRADES": {
          String inputs = stdin.nextLine();
          if (inputs.isEmpty()) {
            this.printAllTrades();
          } else {
            String[] args = inputs.split(" ");
            // args[0] is an empty string,
            // because there's a space that isn't consumed by stdin.next();
            if (args[1].equalsIgnoreCase("TRADER")) {
              this.printTradesByTrader(args[2]);
            } else {
              // args[1] is "PRODUCT"
              this.printTradesByProduct(args[2]);
            }
          }
          break;
        }
      case "BOOK": {
          String subCommand = stdin.next();
          this.printBook(subCommand.equalsIgnoreCase("BUY"));
          break;
        }
      case "SAVE": {
          String traderPath = stdin.next();
          String tradesPath = stdin.next();
          this.save(traderPath, tradesPath);
          break;
        }
      case "BINARY": {
          String traderPath = stdin.next();
          String tradesPath = stdin.next();
          this.saveBinary(traderPath, tradesPath);
          break;
        }
      default: {
        // We got an invalid command, do nothing.
          break;
        }
    }
    return true;
  }

  private void addTrader(String traderID, double balance) {
    if (this.traders.containsKey(traderID)) {
      System.out.println("Trader with given ID already exists.");
    } else {
      if (balance >= 0) {
        this.traders.put(traderID, new Trader(traderID, balance));
        System.out.println("Success.");
      } else {
        System.out.println("Initial balance cannot be negative.");
      }
    }
  }

  private void printTraderBalance(String traderID) {
    if (this.traders.containsKey(traderID)) {
      Trader trader = this.traders.get(traderID);
      System.out.printf("$%.2f\n", trader.getBalance());
    } else {
      System.out.println("No such trader in the market.");
    }
  }

  private void printTraderInventory(String traderID) {
    if (this.traders.containsKey(traderID)) {
      Trader trader = this.traders.get(traderID);
      List<String> inventory = trader.getProductsInInventory();
      if (inventory.isEmpty()) {
        System.out.println("Trader has an empty inventory.");
      } else {
        for (String product : inventory) {
          System.out.println(product);
        }
      }
    } else {
      System.out.println("No such trader in the market.");
    }
  }

  private void printTraderAmount(String traderID, String product) {
    if (this.traders.containsKey(traderID)) {
      Trader trader = this.traders.get(traderID);
      if (trader.getProductsInInventory().contains(product)) {
        System.out.printf("%.2f\n", trader.getAmountStored(product));
      } else {
        System.out.println("Product not in inventory.");
      }
    } else {
      System.out.println("No such trader in the market.");
    }
  }

  private void makeOrder(boolean isBuy, String traderID, String product,
        double amount, double price) {
    if (this.traders.containsKey(traderID)) {
      if (amount > 0) {
        Trader trader = this.traders.get(traderID);
        String orderID = Order.nextID();
        Order order = new Order(product, isBuy, amount, price, trader, orderID);

        List<Trade> trades;
        if (isBuy) {
          trades = this.market.placeBuyOrder(order);
        } else {
          trades = this.market.placeSellOrder(order);
        }

        if (trades == null) {
          System.out.println("Order could not be placed onto the market.");
        } else if (trades.isEmpty()) {
          String orderType = isBuy ? "buy" : "sell";
          System.out.println("No trades could be made, order added to " + orderType + " book.");
        } else {
          String tradeType = isBuy ? "bought" : "sold";
          if (order.isClosed()) {
            System.out.println("Product " + tradeType + " in entirety, trades as follows:");
          } else {
            System.out.println("Product " + tradeType + " in part, trades as follows:");
          }
          Exchange.printList(trades);
        }
      } else {
        System.out.println("Order could not be placed onto the market.");
      }
    } else {
      System.out.println("No such trader in the market.");
    }
  }

  private void importProduct(String traderID, String product, double amount) {
    if (amount > 0) {
      if (this.traders.containsKey(traderID)) {
        Trader trader = this.traders.get(traderID);
        double newAmount = trader.importProduct(product, amount);
        System.out.printf("Trader now has %.2f units of %s.\n", newAmount, product);
      } else {
        System.out.println("No such trader in the market.");
      }
    } else {
      System.out.println("Could not import product into market.");
    }
  }

  private void exportProduct(String traderID, String product, double amount) {
    if (this.traders.containsKey(traderID)) {
      Trader trader = this.traders.get(traderID);
      double newAmount = trader.exportProduct(product, amount);
      if (newAmount > 0) {
        System.out.printf("Trader now has %.2f units of %s.\n", newAmount, product);
      } else if (newAmount == 0) {
        System.out.printf("Trader now has no units of %s.\n", product);
      } else {
        System.out.println("Could not export product out of market.");
      }
    } else {
      System.out.println("No such trader in the market.");
    }
  }

  private void cancelOrder(boolean isBuy, String orderID) {
    boolean successful;
    String orderType;
    if (isBuy) {
      successful = this.market.cancelBuyOrder(orderID);
      orderType = "buy";
    } else {
      successful = this.market.cancelSellOrder(orderID);
      orderType = "sell";
    }
    if (successful) {
      System.out.println("Order successfully cancelled.");
    } else {
      System.out.println("No such order in " + orderType + " book.");
    }
  }

  private void printOrder(String orderID) {
    if (this.market.getSellBook().size() == 0 && this.market.getBuyBook().size() == 0) {
      System.out.println("No orders in either book in the market.");
    } else {
      Order order = this.market.getOrder(orderID);
      if (order == null) {
        System.out.println("Order is not present in either order book.");
      } else {
        System.out.println(order);
      }
    }
  }

  private void printTraders() {
    if (this.traders.isEmpty()) {
      System.out.println("No traders in the market.");
    } else {
      ArrayList<String> traderIDs = new ArrayList<String>(this.traders.keySet());
      traderIDs.sort((x, y) -> x.compareTo(y)); // Alphabetic order
      for (String traderID : traderIDs) {
        System.out.println(traderID);
      }
    }
  }

  private void printAllTrades() {
    List<Trade> trades = this.market.getTrades();
    if (trades.isEmpty()) {
      System.out.println("No trades have been completed.");
    } else {
      Exchange.printList(trades);
    }
  }

  private void printTradesByTrader(String traderID) {
    if (this.traders.containsKey(traderID)) {
      List<Trade> trades = Market.filterTradesByTrader(
          this.market.getTrades(), this.traders.get(traderID));

      if (trades.isEmpty()) {
        System.out.println("No trades have been completed by trader.");
      } else {
        Exchange.printList(trades);
      }
    } else {
      System.out.println("No such trader in the market.");
    }
  }

  private void printTradesByProduct(String product) {
    List<Trade> trades = Market.filterTradesByProduct(this.market.getTrades(), product);

    if (trades.isEmpty()) {
      System.out.println("No trades have been completed with given product.");
    } else {
      Exchange.printList(trades);
    }
  }

  private void printBook(boolean isBuy) {
    List<Order> book;
    String bookType;
    if (isBuy) {
      book = this.market.getBuyBook();
      bookType = "buy";
    } else {
      book = this.market.getSellBook();
      bookType = "sell";
    }
    if (book.isEmpty()) {
      System.out.println("The " + bookType + " book is empty.");
    } else {
      Exchange.printList(book);
    }
  }

  private void save(String traderPath, String tradesPath) {
    ArrayList<String> traderIDs = new ArrayList<String>(this.traders.keySet());
    traderIDs.sort((x, y) -> x.compareTo(y)); // Alphabetic order
    List<Trader> traders = new ArrayList<Trader>(traderIDs.size());
    for (String id : traderIDs) {
      traders.add(this.traders.get(id));
    }

    Trader.writeTraders(traders, traderPath);
    Trade.writeTrades(this.market.getTrades(), tradesPath);
    System.out.println("Success.");
    // According to spec, there should be a different message when something goes wrong,
    // but according to Andrew this can't be tested and so isn't required:
    // https://edstem.org/au/courses/6538/discussion/584742?answer=1331594
  }

  private void saveBinary(String traderPath, String tradesPath) {
    ArrayList<String> traderIDs = new ArrayList<String>(this.traders.keySet());
    traderIDs.sort((x, y) -> x.compareTo(y)); // Alphabetic order
    List<Trader> traders = new ArrayList<Trader>(traderIDs.size());
    for (String id : traderIDs) {
      traders.add(this.traders.get(id));
    }

    Trader.writeTradersBinary(traders, traderPath);
    Trade.writeTradesBinary(this.market.getTrades(), tradesPath);
    System.out.println("Success.");
    // According to spec, there should be a different message when something goes wrong,
    // but according to Andrew this can't be tested and so isn't required:
    // https://edstem.org/au/courses/6538/discussion/584742?answer=1331594
  }

  public static void main(String[] args) {
    /** Runs the program. */
    Exchange exchange = new Exchange();
    exchange.run();
  }

  private static void printList(List list) {
    /** Print out each object in a List to a new line on stdout. */
    for (Object o : list) {
      System.out.println(o);
    }
  }
}
