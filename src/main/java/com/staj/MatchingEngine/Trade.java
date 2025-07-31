package com.staj.MatchingEngine;

public class Trade {
    private String takerOrderId; // Alan emir
    private String makerOrderId; // Satan emir
    private int takerOrderPrice; // Alan emir fiyatı
    private int makerOrderPrice; // Satan emir fiyatı
    private int amount; // işlem miktarı
    private int price; // işlem fiyatı

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
}
