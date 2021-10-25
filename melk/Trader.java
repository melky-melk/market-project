import java.util.*;
import java.io.*;

public class Trader {
	private String id;
	private double balance;
	private LinkedHashMap<String, Double> inventory = new LinkedHashMap<String, Double>(); // which has product as string and quantities of the product double. in order of when they were placed 

	public Trader(String id, double balance) {
		this.id = id;
		this.balance = balance;
	}

	public String getID() {return this.id;}
	public double getBalance() {return this.balance;}

	public double importProduct(String product, double amount) {
		if (product == null || amount <= 0.0){return -1.0;}

		//if the product doesnt exist yet then the product is added to the hashmap
		if (this.inventory.get(product) == null){ 
			this.inventory.put(product, amount);	
		} else {
			// otherwise the inventory is added and the old amount is override
			this.inventory.put(product, amount + this.inventory.get(product));	
		}

		return this.inventory.get(product);
	}

	public double exportProduct(String product, double amount) {
		if (product == null || amount <= 0.0 || (this.inventory.get(product) == null)) {return -1.0;}
		// if the amount is less than 0, it should not allow the product to be exported
		if (this.inventory.get(product) - amount < 0){
			return -1.0;
		}
		
		double newProductAmount = this.inventory.get(product) - amount;
		if (newProductAmount == 0.0){
			this.inventory.remove(product);
			return 0.0;
		}

		// otherwise the product, with the new amount is put into the inventory
		this.inventory.put(product, newProductAmount);
		return newProductAmount;
	}

	public double getAmountStored(String product) {
		if (product == null){
			return -1.0;
		}else if (this.inventory.get(product) == null){
			return 0.0;
		}

		return this.inventory.get(product);
	}

	public List<String> getProductsInInventory() {
		ArrayList<String> allKeys = new ArrayList<String>();
		
		for (Map.Entry<String, Double> item : inventory.entrySet()) {
			allKeys.add(item.getKey());
		}

		// sort anyways so it cant be in order
		Collections.sort(allKeys);
		return allKeys;
	}

	public double adjustBalance(double change) {
		this.balance += change;
		return this.balance;
	}


	// Returns a string representation of the trader
	public String toString() {
		// starts the string with the ID: balance and opening squiggly bracket 
		String str = String.format("%s: $%.2f {", this.id, this.balance);
		
		// goes through the inventory and formats the product with the product name
		for (String productName: this.getProductsInInventory()){
			// adds to the initial ID and balance
			str += String.format("%s: %.2f, ", productName, inventory.get(productName));
		}

		if (inventory.size() == 0){
			str += "}"; //just has a closing bracket so id: $balance {}
			return str;	
		}
		
		//Subtracts the last 2 characters (the extra comma and space) from the string. otherwise will go "PROD1: AMOUNT1, ..., PRODN: AMOUNTN, "
		str = str.substring(0, str.length() - 2);
		// then adds the last squiggly bracket
		str += "}";

		return str;
	}

	public static void writeTraders(List<Trader> traders, String path) {
		if (traders == null || path == null){return;}

		try {
			File file = new File(path);
			PrintWriter pw = new PrintWriter(file); 
			
			for (int i = 0; i < traders.size(); i ++){
				Trader trader = traders.get(i);
				if (i == traders.size() - 1){
					pw.print(trader.toString());
				} else {
					pw.println(trader.toString());
				}
			}

			pw.flush();
            pw.close();
		} catch (FileNotFoundException e){
		}
	}

	public static void writeTradersBinary(List<Trader> traders, String path) {
		if (traders == null || path == null){return;}

		try {
			FileOutputStream f = new FileOutputStream(path);
			DataOutputStream output = new DataOutputStream(f);
			
			for (Trader trader: traders){
				output.writeUTF(trader.toString());
				output.writeUTF("\u001F");
			}

			output.flush();
            output.close();
		} catch (FileNotFoundException e){
		} catch (IOException e){
		}
	}

	public static ArrayList<Trader> orderTraders(List<Trader> traders){
		ArrayList<String> orderTraderStrings = new ArrayList<String>();
		ArrayList<Trader> orderedTraders = new ArrayList<Trader>();
					
		for (Trader trader : traders){
			orderTraderStrings.add(trader.toString());
		}
		
		Collections.sort(orderTraderStrings);
		// goes through the list alphabetically
		for (String traderString : orderTraderStrings){

			// then goes through all the trader
			for (Trader trader : traders){
				// once the alphabetically ordered strings matches the trader then it adds the trader to the list 
				if (traderString.equals(trader.toString())){
					orderedTraders.add(trader);
				} 
			}
		}
		return orderedTraders;
	}

	// OWN METHODS
	public boolean equals(Trader otherTrader){
		return (this.getID().equals(otherTrader.getID()));
	}

	public LinkedHashMap<String, Double> getInventory() {return this.inventory;}
}