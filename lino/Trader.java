import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Trader {
  /**
   * Represents a trader on the market who can buy/sell products.
   * Each trader has a unique string id, a balance (can be negative),
   * and an inventory of products with associated amounts.
   */

  private String id;
  private double balance;
  private HashMap<String, Double> inventory;

  public Trader(String id, double balance) {
    this.id = id;
    this.balance = balance;
    this.inventory = new HashMap<String, Double>();
  }

  public String getID() {
    return this.id;
  }

  public double getBalance() {
    return this.balance;
  }

  public double importProduct(String product, double amount) {
    if (product == null || amount <= 0) {
      return -1.0;
    } else {
      double newAmount = amount + this.inventory.getOrDefault(product, 0.0);
      this.inventory.put(product, newAmount);
      return newAmount;
    }
  }

  public double exportProduct(String product, double amount) {
    if (product == null || amount <= 0 || !this.inventory.containsKey(product)) {
      return -1.0;
    } else {
      double newAmount = this.inventory.get(product) - amount;
      if (newAmount < 0) {
        return -1.0;
      } else {
        if (newAmount == 0.0) {
          this.inventory.remove(product);
        } else {
          this.inventory.put(product, newAmount);
        }
        return newAmount;
      }
    }
  }

  public double getAmountStored(String product) {
    if (product == null) {
      return -1.0;
    } else {
      return this.inventory.getOrDefault(product, 0.0);
    }
  }

  public List<String> getProductsInInventory() {
    List<String> products = new ArrayList<String>(this.inventory.keySet());
    products.sort((x, y) -> x.compareTo(y));
    return products;
  }

  public double adjustBalance(double change) {
    this.balance += change;
    return this.balance;
  }

  @Override
  public String toString() {
    // Build a string representation of the inventory
    StringBuilder inventoryBuilder = new StringBuilder("{");
    for (String product : this.getProductsInInventory()) {
      inventoryBuilder.append(String.format("%s: %.2f, ", product, this.inventory.get(product)));
    }
    int inventoryLength = inventoryBuilder.length();

    if (inventoryLength > 1) {
      // Remove the extra ", " from the end
      inventoryBuilder.delete(inventoryLength - 2, inventoryLength); 
    }
    inventoryBuilder.append("}");

    return String.format("%s: $%.2f %s", this.id, this.balance, inventoryBuilder);
  }

  public static void writeTraders(List<Trader> traders, String path) {
    if (traders != null && path != null) {
      try {
        PrintWriter writer = new PrintWriter(new File(path));

        for (Trader trader : traders) {
          writer.println(trader);
        }

        writer.close();
      } catch (FileNotFoundException e) {
        // Spec says to do nothing if something goes wrong, but I want the stack trace for debugging.
        e.printStackTrace();
      }
    }
  }

  public static void writeTradersBinary(List<Trader> traders, String path) {
    if (traders != null && path != null) {
      try {
        DataOutputStream writer = new DataOutputStream(new FileOutputStream(path));
        for (Trader trader : traders) {
          writer.writeUTF(trader.toString());
          writer.writeUTF("\u001f");  // \u001f = Unit Separator
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
