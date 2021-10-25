import java.util.*;
import java.lang.Math;

public class Market {
	private ArrayList<Order> buyBook;
	private ArrayList<Order> sellBook;
	private ArrayList<Trade> allTrades;

	public Market() {
		this.buyBook = new ArrayList<Order>();
		this.sellBook = new ArrayList<Order>();
		this.allTrades = new ArrayList<Trade>();
	}

	// will attempt to place a sell order in the market will update the changes of the sale inside the traders and in the market THE THING FOR SELLORDER 
	public List<Trade> placeSellOrder(Order sellOrder) {
		if (sellOrder == null || sellOrder.isBuy() == true ){
			return null;
		}

		//creates a temporary seller that will later be used to update the largest list of traders inside market
		Trader tempSeller = sellOrder.getTrader();
		
		// if they do not have the product or the product is over the amount of inventory the person actually has then then returns null
		if (tempSeller.getAmountStored(sellOrder.getProduct()) == 0.0 || tempSeller.getAmountStored(sellOrder.getProduct()) < sellOrder.getAmount() || sellOrder.getAmount() <= 0|| sellOrder.getPrice() <= 0){
			return null;
		}

		//---------------------------------------------------------GETTING VALID ORDERS---------------------------------------------------

		ArrayList<Trade> tradeList = new ArrayList<Trade>();
		ArrayList<Order> validOrders = new ArrayList<Order>();
		
		//goes through every request to buy and checks the conditions, if valid it adds it to the to valid buyOrders
		for (Order buyOrder : buyBook){
			//if it matches the product and is higher than the minimum sell price then its added to the valid orders
			if (buyOrder.getProduct().equals(sellOrder.getProduct()) && buyOrder.getPrice() >= sellOrder.getPrice()){
				validOrders.add(buyOrder);
			}
		}

		if (validOrders.isEmpty()){
			//still updates the information inside the market before adding the order to sell book
			tempSeller.exportProduct(sellOrder.getProduct(), sellOrder.getAmount());
			this.sellBook.add(sellOrder);
			return tradeList;
		}

		Order largestOrder = validOrders.get(0);
		double remainingAmount = sellOrder.getAmount();
		boolean tradeComplete = false;
		Double unitsSold;

		// ------------------------------------------------------SELLING THE STOCK WITH THE VALID ORDERS------------------------------------
		//only complete if the remaining selling units is 0 or there are no more items in valid order
		while (tradeComplete == false){
			// sets as -1.0 so the first order is always larger and resets the max every time
			double largestPrice = -1.0;

			for (Order buyOrder: validOrders){
				// follows the price-time algorithm the largest price is accessed first and if there are 2 then the first in the list of 2 is accessed first
				if (buyOrder.getPrice() > largestPrice){
					largestPrice = buyOrder.getPrice();
					largestOrder = buyOrder;
				}
			}
			
			// the amount is deducted by as many as the largest order possible
			unitsSold = largestOrder.getAmount();
			remainingAmount -= largestOrder.getAmount();

			//initially assumes that the largest order's units are completely used
			largestOrder.setAmount(0); 

			// if there are still more units to be bought
			if (remainingAmount > 0){
				// this also means that all of the largestOrder was bought up
				largestOrder.close();
				// remove cheapest order from valid orders (does not make tradeComplete true and will thus restart the loop) 
				for (int i = 0; i < validOrders.size(); i ++){
					Order validOrder = validOrders.get(i);
					if (validOrder.equals(largestOrder)){
						validOrders.remove(i);
					}
				}
			}

			//if there are more orders sold than the trader was willing to sell
			if (remainingAmount < 0){
				//the remaining amount is the amount of order should have left
				largestOrder.adjustAmount(Math.abs(remainingAmount));
				unitsSold -= largestOrder.getAmount();
				remainingAmount = 0;
			}

			// if completely sold out the trade is completed
			if (remainingAmount == 0.0){
				sellOrder.close();
				tradeComplete = true;
			}

			// if there are no more willing buyers in the buy book then the sell order is added
			if (validOrders.isEmpty()){
				sellOrder.setAmount(remainingAmount);
				tempSeller.exportProduct(sellOrder.getProduct(), remainingAmount);
				this.sellBook.add(sellOrder);
				tradeComplete = true;
			}

			if (largestOrder.getAmount() == 0){
				largestOrder.close();
			}

			// -----------------------------------------------UPDATING INFORMATION INSIDE THE MARKET------------------------------------

			// this area always occurs 
			// uses the temporary traders to accesses the methods inside 
			tempSeller.adjustBalance(unitsSold * largestOrder.getPrice());
			tempSeller.exportProduct(sellOrder.getProduct(), unitsSold);

			Trader tempBuyer = largestOrder.getTrader();
			tempBuyer.adjustBalance(-(unitsSold * largestOrder.getPrice()));
			tempBuyer.importProduct(largestOrder.getProduct(), unitsSold);

			// make trade objects
			Trade trade = new Trade(sellOrder.getProduct(), unitsSold, largestPrice, sellOrder, largestOrder);
			this.allTrades.add(trade);
			tradeList.add(trade);

			// then updates the information inside the larger market using the temporary traders
			// if the largest order is 0 it will remove it from the buy book, if there is more remaining it will update with the remaining
			this.updateBuyBook(largestOrder);
		}

		// returns the list of trades in the order they were added
		return tradeList;
	}

	// will attempt to place a buy order in the market will update the changes of the sale inside the traders and in the market
	public List<Trade> placeBuyOrder(Order buyOrder) {
		if (buyOrder == null || (buyOrder.isBuy() == false )){
			return null;
		}

		//---------------------------------------------------------GETTING VALID ORDERS---------------------------------------------------

		ArrayList<Trade> tradeList = new ArrayList<Trade>();
		ArrayList<Order> validOrders = new ArrayList<Order>();
		//goes through every request to buy and add it to valid buyOrders
		for (Order sellOrder : sellBook){
			//if it matches the product and is higher than the minimum sell price
			if (sellOrder.getProduct().equals(buyOrder.getProduct()) && (buyOrder.getPrice() >= sellOrder.getPrice())){
				validOrders.add(sellOrder);
			}
		}

		if (validOrders.isEmpty()){
			this.buyBook.add(buyOrder);
			return tradeList;
		}

		Order cheapestOrder = validOrders.get(0);
		double remainingAmount = buyOrder.getAmount();
		boolean tradeComplete = false;
		Double unitsSold;

		// ------------------------------------------------------SELLING THE STOCK WITH THE VALID ORDERS------------------------------------

		//would go through the number of units the trader is willing to buy until all is complete
		while (tradeComplete == false){
			// sets the smallest price as the largest possible double, so there will always be an order that has a lower value
			double smallestPrice = Double.POSITIVE_INFINITY;

			for (Order sellOrder: validOrders){
				// will only change the minimum sell price if its there
				// will also make sure the first one that is smaller is accessed first
				if (sellOrder.getPrice() < smallestPrice){
					smallestPrice = sellOrder.getPrice();
					cheapestOrder = sellOrder;
				}
			}

			// the amount is deducted by as many as the cheapest order possible
			unitsSold = cheapestOrder.getAmount();
			remainingAmount -= cheapestOrder.getAmount();
			cheapestOrder.setAmount(0);

			// if there are still more units to be bought
			if (remainingAmount > 0){
				// remove cheapest order from valid orders 
				cheapestOrder.close();
				for (int i = 0; i < validOrders.size(); i ++){
					Order validOrder = validOrders.get(i);
					if (validOrder.equals(cheapestOrder)){
						validOrders.remove(i);
					}
				}
			}

			//if there is more orders than there are remaining units the trader is willing to buy
			if (remainingAmount < 0){
				//the remaining amount is the amount of cheapest order should have left
				cheapestOrder.adjustAmount(Math.abs(remainingAmount));
				unitsSold -= cheapestOrder.getAmount();
				remainingAmount = 0.0;
			}

			if (remainingAmount == 0.0){
				buyOrder.close();
				tradeComplete = true;
			}

			if (cheapestOrder.getAmount() == 0){
				cheapestOrder.close();
			}

			if (validOrders.isEmpty()){
				buyOrder.setAmount(remainingAmount);
				this.buyBook.add(buyOrder);
				tradeComplete = true;
			}

			// -----------------------------------------------UPDATING INFORMATION INSIDE THE MARKET--------------------------------------

			// creates temporary traders and accesses the methods inside 
			Trader tempSeller = cheapestOrder.getTrader();			
			tempSeller.adjustBalance(unitsSold * cheapestOrder.getPrice());
			// does not need to export the product because if the product is on the market the amount of proudct has already been exported out of the seller 

			Trader tempBuyer = buyOrder.getTrader();
			tempBuyer.adjustBalance(-(unitsSold * cheapestOrder.getPrice()));
			tempBuyer.importProduct(buyOrder.getProduct(), unitsSold);

			// make trade objects
			Trade trade = new Trade(buyOrder.getProduct(), unitsSold, smallestPrice, cheapestOrder, buyOrder);
			tradeList.add(trade);
			this.allTrades.add(trade);

			// then updates the information inside the larger market
			this.updateSellBook(cheapestOrder);
		}

		return tradeList;
	}

	// if a buy order is in the buy book it will remove it
	public boolean cancelBuyOrder(String id) {
		if (buyBook.isEmpty()){return false;}

		if (this.checkValidOrderID(id) == false){
			return false;
		}

		for (int i = 0; i < buyBook.size(); i++){
			Order buyOrder = buyBook.get(i); 
			
			if (buyOrder.getID().equals(id)){
				buyBook.get(i).close();
				buyBook.remove(i);
				return true;
			}
		}
		return false;
	}

	// if a buy order is in the sell book it will remove it and return the product back to the trader
	public boolean cancelSellOrder(String id) {
		if (sellBook.size() == 0){return false;}

		if (this.checkValidOrderID(id) == false){
			return false;
		}

		for (int i = 0; i < sellBook.size(); i++){
			Order sellOrder = sellBook.get(i); 
			
			if (sellOrder.getID().equals(id)){
				sellBook.get(i).close();
				// gets the trader and imports the item into their inventory
				Trader tempSeller = sellOrder.getTrader();
				tempSeller.importProduct(sellOrder.getProduct(), sellOrder.getAmount());
				
				sellBook.remove(i);
				return true;
			}
		}

		return false;
	}

	//getter methods
	public List<Order> getSellBook() {return this.sellBook;}
	public List<Order> getBuyBook() {return this.buyBook;}
	public List<Trade> getTrades() {return this.allTrades;}

	// will return a list of trades that involved a certain trader either buying or selling the product
	public static List<Trade> filterTradesByTrader(List<Trade> trades, Trader trader) {
		if (trades == null || trader == null)
			return null;

		ArrayList<Trade> tradesWithTrader = new ArrayList<Trade>();
		for (Trade trade: trades){
			if (trade.involvesTrader(trader)){
				tradesWithTrader.add(trade);
			}
		}
		return tradesWithTrader;
	}
	
	// will return a list of trades that involved a certain product
	public static List<Trade> filterTradesByProduct(List<Trade> trades, String product) {
		if (trades == null || product == null)
			return null;

		ArrayList<Trade> tradesWithProduct = new ArrayList<Trade>();
		for (Trade trade: trades){
			if (trade.involvesProduct(product)){
				tradesWithProduct.add(trade);
			}
		}
		return tradesWithProduct;
	}

	// -------------------------------------------------------------OWN METHODS------------------------------------------------------------------------- 

	// given an order it will find the order inside the sell book and replace it with the updated version, will also remove closed orders from the sell book
	public void updateSellBook(Order closedOrder){
		//initially sets the existing so it can find the order matches the ID
		Order existingOrder;

		for (int i = 0; i < this.sellBook.size(); i++){
			existingOrder = sellBook.get(i);
			if (existingOrder.equals(closedOrder) && closedOrder.isClosed()){
				this.sellBook.remove(i);
			}
		}
	}
	
	// given an order it will find the order inside the sell book and replace it with the updated version, will also remove closed orders from the sell book
	public void updateBuyBook(Order closedOrder){
		Order existingOrder;

		for (int i = 0; i < this.buyBook.size(); i++){
			existingOrder = buyBook.get(i);
			if (existingOrder.equals(closedOrder) && closedOrder.isClosed()){
				this.buyBook.remove(i);
				return; 
			}
		}
	}

	public boolean checkValidOrderID(String id){
		for (Order order: buyBook){
			if (order.getID().equals(id)){
				return true;
			}
		}

		for (Order order: sellBook){
			if (order.getID().equals(id)){
				return true;
			}
		}
		return false;
	}
}