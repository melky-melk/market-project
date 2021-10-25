import java.util.*;
import java.io.*;

public class Trade {

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

	// getters 
	public String getProduct() {return product;}
	public double getAmount() {return amount;}
	public Order getSellOrder() {return sellOrder;}
	public Order getBuyOrder() {return buyOrder;}
	public double getPrice() {return price;}

	public String toString() {
		return String.format("%s->%s: %.2fx%s for $%.2f.", sellOrder.getTrader().getID(), buyOrder.getTrader().getID(), amount, product, price);
	}

	public boolean involvesTrader(Trader trader) {
		if (trader == null){
			return false;
		}
		return (buyOrder.getTrader().equals(trader) || sellOrder.getTrader().equals(trader));
	}

	public static void writeTrades(List<Trade> trades, String path) {
		if (trades == null || path == null){return;}

		try {
			File file = new File(path);
			PrintWriter pw = new PrintWriter(file); 
			
			for (int i = 0; i < trades.size(); i ++){
				Trade trade = trades.get(i);
				if (i == trades.size() - 1){
					pw.print(trade.toString());
				} else {
					pw.println(trade.toString());
				}
			}

			pw.flush();
            pw.close();
		} catch (FileNotFoundException e){
		}
	}

	public static void writeTradesBinary(List<Trade> trades, String path) {
		if (trades == null || path == null){return;}

		try {
			FileOutputStream f = new FileOutputStream(path);
			DataOutputStream output = new DataOutputStream(f);
			
			for (Trade trade: trades){
				output.writeUTF(trade.toString());
				output.writeUTF("\u001F");
			}

			output.flush();
            output.close();
		} catch (FileNotFoundException e){
		} catch (IOException e){
		}
	}

	public boolean involvesProduct(String product) {
		if (product == null){
			return false;
		}
		return (product.equals(this.product));
	}
}