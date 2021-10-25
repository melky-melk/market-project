public class Order {
	private String id;
	private String product; 
	private boolean buy; //true if buy sell if false
	private Trader trader;
	private double amount;
	private double price;
	private boolean closed = false;

	public Order(String product, boolean buy, double amount, double price, Trader trader, String id) {
		this.id = id;
		this.product = product;
		this.buy = buy;
		this.trader = trader;
		this.amount = amount;
		this.price = price;
	}

	//getters
	public String getProduct() {return this.product;}
	public boolean isBuy() {return this.buy;}
	public double getAmount() {return this.amount;}
	public Trader getTrader() {return this.trader;} 
	public double getPrice() {return this.price;}
	public String getID() {return this.id;}
	public boolean isClosed() {return this.closed;}

	//setters
	public void close() {this.closed = true;}
	public void adjustAmount(double change) {
		if (this.amount + change > 0){
			this.amount += change;
		}
	}

	//-------------------------------------------------------OWN METHODS----------------------------------------------------
	public String toString() {
		String buyOrSell = (buy ? "BUY" : "SELL");	
		return String.format("%s: %s %.2fx%s @ $%.2f", id, buyOrSell, amount, product, price);
	}

	public boolean equals(Order otherOrder){
		return (this.getID().equals(otherOrder.getID()));
	}

	public void setAmount(double newAmount){this.amount = newAmount;}
}