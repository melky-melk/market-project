import java.util.*;
import java.io.*;

public class Exchange {
	private Market market;
	private int numOfOrders;
	private ArrayList<Trader> allTraders;

	public Exchange(){
		Market market = new Market();
		this.market = market; 
		allTraders = new ArrayList<>();
	}

	public boolean checkIfTraderInMarket(String id){
		if (this.checkValidTraderID(id) == false){
			System.out.println("No such trader in the market.");
			return false;
		}
		return true;
	}

	private String createOrderID(){
		// takes the number of orders total made, converts it to hexadecimal, makes it uppercase pads it to 4 length with 0's 
		String orderID = String.format("%4s", Integer.toHexString(numOfOrders)).toUpperCase().replace(' ', '0');
		this.numOfOrders++;
		return orderID;
	}

	public void run() {
		// initialising things before it runs
		Scanner input = new Scanner(System.in);
		String command;
		String id;
		String orderID;
		String product;
		String traderPath; 
		String tradesPath; 
		Order order;
		double amount;
		double price;
		double unitsSold;
		Trader tempTrader;
		List<Trade> trades;
		ArrayList<Trader> orderedTraders;

		while (true){
			System.out.print("$ ");
			
			String commandLine = input.nextLine(); 
			String[] commandArray = commandLine.split(" ");
			command = commandArray[0].toUpperCase();

			switch (command)
			{case "EXIT":
				input.close();
				System.out.println("Have a nice day.");
				return;
			
			// adds a new trader to the list of traders within exchange
			case "ADD":
				id = commandArray[1];
				double balance = Double.parseDouble(commandArray[2]);
				
				// checks if valid
				if (this.checkValidTraderID(id)){
					System.out.println("Trader with given ID already exists.");
				
				} else {
					if (balance < 0){
						System.out.println("Initial balance cannot be negative.");
				
					} else {
						// makes a new traders and adds it to the list inside exchange
						Trader newTrader = new Trader(id, balance);
						this.allTraders.add(newTrader);
						
						System.out.println("Success.");
					}
				} 
				break;
			
			// shows the balance of the trader BALANCE id
			case "BALANCE":
				id = commandArray[1];
				if (this.checkIfTraderInMarket(id)){
					// finds the trader based on id and gets the balance 
					System.out.printf("$%.2f\n", this.getTempTrader(id).getBalance());
				}
				break;
			
			//prints all the items in the trader's inverntory
			case "INVENTORY":
				id = commandArray[1];
				if (this.checkIfTraderInMarket(id)){
					
					List<String> tempTraderProducts = this.getTempTrader(id).getProductsInInventory();
					
					if (tempTraderProducts.isEmpty()){
						System.out.println("Trader has an empty inventory.");
					}

					// then prints all of the products, if there are no products then it doesnt print anything else
					for (String tempProduct: tempTraderProducts){
						System.out.println(tempProduct);
					}
				}
				break;
			
			// shows the amount of product the trader has AMOUNT id productName
			case "AMOUNT":
				id = commandArray[1];
				if (this.checkIfTraderInMarket(id)){
					tempTrader = this.getTempTrader(id);
					product = commandArray[2];
					
					// if the item is not true it prints the thing then breaks
					if (tempTrader.getInventory().containsKey(product) != true) {
						System.out.println("Product not in inventory.");
					} else {
						// gets the entire inventory of the corresponding 
						System.out.printf("%.2f\n", tempTrader.getInventory().get(product));
					}
				}
				break;
			
			// places a sell order SELL traderid productName amount price
			case "SELL":
				id = commandArray[1];
				if (this.checkIfTraderInMarket(id)){
					
					// saves the next inputs
					tempTrader = this.getTempTrader(id);
					product = commandArray[2];
					amount = Double.parseDouble(commandArray[3]);
					price = Double.parseDouble(commandArray[4]);

					// if the amounts given are incorrect then it cannot continue
					if (amount <= 0 || price <= 0 || product == null){
						System.out.println("No trades could be made, order added to sell book.");
					} else {
						//placing the order
						orderID = this.createOrderID();
						order = new Order(product, false, amount, price, tempTrader, orderID);
						trades = this.market.placeSellOrder(order);

						// if the trades are null then that means something went wrong with placing the order e.g the seller doesnt have enough product for it
						if (trades == null){
							System.out.println("Order could not be placed onto the market.");
						} else {
							
							if (trades.size() == 0){
								System.out.println("No trades could be made, order added to sell book.");
							} else {
								// it sums all of the trades together by the amount it was buy
								unitsSold = 0.0;
								for (Trade trade: trades){
									unitsSold += trade.getAmount();
								}
								
								// if the summed total of all the amounts in the trades equals to the order that means the order places was fully
								if (unitsSold == order.getAmount()){
									System.out.println("Product sold in entirety, trades as follows:");
								} else {
									System.out.println("Product sold in part, trades as follows:");
								}
								
								for (Trade trade: trades){
									System.out.println(trade.toString());
								}
							}
						}
					}
				}
				break;
			
			// places a buy order: buy traderid productName amount price
			case "BUY":
				id = commandArray[1];
				if (this.checkIfTraderInMarket(id)){
					tempTrader = this.getTempTrader(id);
					product = commandArray[2];
					amount = Double.parseDouble(commandArray[3]);
					price = Double.parseDouble(commandArray[4]);

					if (amount <= 0 || price <= 0 || product == null){
						System.out.println("No trades could be made, order added to buy book.");
					} else {						
						orderID = this.createOrderID();
						order = new Order(product, true, amount, price, tempTrader, orderID);
						trades = this.market.placeBuyOrder(order);

						if (trades == null){
							System.out.println("Order could not be placed onto the market.");
						} else {
							
							if (trades.size() == 0){
								System.out.println("No trades could be made, order added to buy book.");
							} else {
								unitsSold = 0.0;
								for (Trade trade: trades){
									unitsSold += trade.getAmount();
								}
								
								if (unitsSold == order.getAmount()){
									System.out.println("Product bought in entirety, trades as follows:");
								} else {
									System.out.println("Product bought in part, trades as follows:");
								}
								
								for (Trade trade: trades){
									System.out.println(trade.toString());
								}
							}
						}
					}
				}
				break;
			
			// imports the product and gives the trader the amount of the product: import traderID productName amount
			case "IMPORT":
				id = commandArray[1];
				if (this.checkIfTraderInMarket(id)){

					tempTrader = this.getTempTrader(id);
					product = commandArray[2];
					amount = Double.parseDouble(commandArray[3]);
					double importedAmount = tempTrader.importProduct(product, amount);

					if (importedAmount == -1.0){
						System.out.println("Could not import product into market.");
					} else {
						System.out.printf("Trader now has %.2f units of %s.\n", importedAmount, product);
					}
				}
				break;
			
			// exports the product out of the trader's inventory: export traderID productName amount
			case "EXPORT":
				id = commandArray[1];
				if (this.checkIfTraderInMarket(id)){

					tempTrader = this.getTempTrader(id);
					product = commandArray[2];
					amount = Double.parseDouble(commandArray[3]);
					double exportedAmount = tempTrader.exportProduct(product, amount);

					if (exportedAmount == -1.0){
						System.out.println("Could not export product out of market.");
					} else {
						if (tempTrader.getAmountStored(product) == 0.0){
							System.out.printf("Trader now has no units of %s.\n", product);
						} else {
							System.out.printf("Trader now has %.2f units of %s.\n", exportedAmount, product);
						}
					}
				}
				break;
			
			// finds the orderID within both buy books and removes the order from it: cancel sellOrBuy orderID
			case "CANCEL":
				String sellOrBuy = commandArray[1].toUpperCase();
				id = commandArray[2];
				boolean cancelSuccess;

				if (sellOrBuy.equals("SELL")){
					cancelSuccess = this.market.cancelSellOrder(id);
				
					if (cancelSuccess){
						System.out.println("Order successfully cancelled.");
					} else {
						System.out.println("No such order in sell book.");
					}
				} 
				
				else if (sellOrBuy.equals("BUY")) {
					cancelSuccess = this.market.cancelBuyOrder(id);
				
					if (cancelSuccess){
						System.out.println("Order successfully cancelled.");
					} else {
						System.out.println("No such order in buy book.");
					}
				}
				break;

			// prints the string representation of an order if is in buy or sell: order orderID
			case "ORDER":
				id = commandArray[1];
				boolean present = false;
				if (this.market.getBuyBook().isEmpty() && this.market.getSellBook().isEmpty()){
					System.out.println("No orders in either book in the market.");
					
				} else {
					for (Order buyOrder : this.market.getBuyBook()){
						if (buyOrder.getID().equals(id)){
							System.out.println(buyOrder.toString());
							present = true;
						}
					}
					for (Order sellOrder : this.market.getSellBook()){
						if (sellOrder.getID().equals(id)){
							System.out.println(sellOrder.toString());
							present = true;
						}
					}
					
					if (present == false){
						System.out.println("Order is not present in either order book.");
					}
				}
				break;

			// prints the string representation of traders: trades || trades trader traderID || trades product productName
			case "TRADES":
				if (commandArray.length == 1){
					if (this.market.getTrades().isEmpty()){
						System.out.println("No trades have been completed.");
					}
					
					for (Trade trade: this.market.getTrades()){
						System.out.println(trade.toString());
					}
				} else {
					
					if (commandArray[1].toUpperCase().equals("TRADER")){
						id = commandArray[2];
					
						if (this.checkIfTraderInMarket(id)){
							tempTrader = this.getTempTrader(id);
						
							if (this.market.filterTradesByTrader(this.market.getTrades(), tempTrader).isEmpty()){
								System.out.println("No trades have been completed by trader.");
							}

							for (Trade trade : this.market.filterTradesByTrader(this.market.getTrades(), tempTrader)){
								System.out.println(trade.toString());
							}
						}
					}
					else if (commandArray[1].toUpperCase().equals("PRODUCT")){
						product = commandArray[2];
						
						if (this.market.filterTradesByProduct(this.market.getTrades(), product).isEmpty()){
							System.out.println("No trades have been completed with given product.");
						}

						for (Trade trade : this.market.filterTradesByProduct(this.market.getTrades(), product)){
							System.out.println(trade.toString());
						}
					}
				}
				break;
			
			// prints everything inside the respective book: book buyOrSell
			case "BOOK":				
				String buyOrSell = commandArray[1].toUpperCase();
				
				if (buyOrSell.equals("SELL")){
					if (this.market.getSellBook().isEmpty()){
						System.out.println("The sell book is empty.");
					}
					
					for (Order sellOrder: this.market.getSellBook()) {
						System.out.println(sellOrder.toString());
					}
				} else if (buyOrSell.equals("BUY")){
				
					if (this.market.getBuyBook().isEmpty()){
						System.out.println("The buy book is empty.");
					}
				
					for (Order buyOrder: this.market.getBuyBook()) {
						System.out.println(buyOrder.toString());
					} 
				}
				break;

			// saves all the data by writing the data into 2 seperate files: save traderPath tradesPath
			case "SAVE": 
				traderPath = commandArray[1];
				tradesPath = commandArray[2];

				File f = new File(traderPath);
				File f2 = new File(traderPath);

				orderedTraders = Trader.orderTraders(this.allTraders);

				Trader.writeTraders(orderedTraders, traderPath);
				Trade.writeTrades(this.market.getTrades(), tradesPath);
				System.out.println("Success.");

				break;

			// saves all the data by writing the data into binary into 2 seperate files: save traderPath tradesPath
			case "BINARY": // [trader-path] [trades-path] 
				traderPath = commandArray[1];
				tradesPath = commandArray[2];

				f = new File(traderPath);
				f2 = new File(traderPath);
					
				orderedTraders = Trader.orderTraders(this.allTraders);

				Trader.writeTradersBinary(orderedTraders, traderPath);
				Trade.writeTradesBinary(this.market.getTrades(), tradesPath);

				System.out.println("Success.");
								
				break;

			// prints the id of all the traders in exchange: trader
			case "TRADERS":
				if (this.allTraders.isEmpty()){
					System.out.println("No traders in the market.");
				} else {
					orderedTraders = Trader.orderTraders(this.allTraders);
					
					for (Trader trader : orderedTraders){
						System.out.println(trader.getID());
					}
				}
				break; 
			}
		}
	}

	public boolean checkValidTraderID(String id){
		for (Trader trader: allTraders){
			if (trader.getID().equals(id)){
				return true;
			}
		}
		return false;
	}

	// finds the id given and matches it to the list of traders inside then returns the trader associated with it
	public Trader getTempTrader(String id){
		Trader trader;

		for (int i = 0; i < this.allTraders.size(); i++){
			trader = allTraders.get(i);
			if (trader.getID().equals(id)){
				return trader;
			}
		}
		return null;
	}

	public List<Trader> getTraders() {return this.allTraders;}

	public static void main(String[] args) {
		Exchange exchange = new Exchange();
		exchange.run();
	}
}