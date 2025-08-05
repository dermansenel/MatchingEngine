package com.staj.MatchingEngine.model;

public class Trade {
    private String takerOrderId; //  alan
    private String makerOrderId; // satan
    private int takerOrderPrice; // alan fiyat
    private int makerOrderPrice; // satan fiyat
    private int amount; // miktar
    private int price; // fiyat

    // Constructor metodu 
    public Trade(String takerOrderId, String makerOrderId, int amount, int price, int takerOrderPrice, int makerOrderPrice) {
        this.takerOrderId = takerOrderId;
        this.makerOrderId = makerOrderId;
        this.amount = amount;
        this.price = price;
        this.makerOrderPrice = makerOrderPrice;
        this.takerOrderPrice = takerOrderPrice;
    }

    public String getTakerOrderId() {
    	return takerOrderId; 
    }
    public String getMakerOrderId() { 
    	return makerOrderId; 
    }
    public int getAmount() { 
    	return amount; 
    }
    public int getPrice() {
    	return price; 
    }
    public int getTakerOrderPrice() {
        return takerOrderPrice;
    }
    public int getMakerOrderPrice() {
        return makerOrderPrice;
    }

    // Beslenme yeri
    public Trade fillPool(String takerOrderId, String makerOrderId, int amount,int price, int takerOrderPrice, int makerOrderPrice) {
        this.takerOrderId = takerOrderId;
        this.makerOrderId = makerOrderId;
        this.amount = amount;
        this.price = price;
        this.takerOrderPrice = takerOrderPrice;
        this.makerOrderPrice = makerOrderPrice;
        return this;
    }
}
