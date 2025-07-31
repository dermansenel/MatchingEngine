package com.staj.MatchingEngine;

import java.sql.Date;

public class Order implements Comparable<Order> {
    private int amount;
    private int price;
    private String id;
    private Side side;
    private Date dateTimeOfOrder;

    // Builder pattern, okunaklı ve işlemi hatasız gerçekleştirmeyi sağlar.
    // Static nesne oluşturulmadan önce kullanabiliir.z
    public static class Builder {
        private int amount;
        private int price;
        private String id;
        private Side side;
        private Date dateTimeOfOrder;

        public Builder(Side side) {
            this.side = side;
        }

        public Builder withAmount(int pAmount) {
            this.amount = pAmount;
            return this;
        }

        public Builder withPrice(int pPrice) {
            this.price = pPrice;
            return this;
        }

        public Builder withId(String pId) {
            this.id = pId;
            return this;
        }

        public Builder withDate(Date pDateTimeOfOrder) {
            this.dateTimeOfOrder = pDateTimeOfOrder;
            return this;
        }

        public Order build() {
            return new Order(this.amount, this.price, this.id, this.side, this.dateTimeOfOrder);
        }
    }

    // Private Constructor
    private Order(int pAmount, int pPrice, String pId, Side pSide, Date pDateTimeOfOrder) {
        this.amount = pAmount;
        this.price = pPrice;
        this.id = pId;
        this.side = pSide;
        this.dateTimeOfOrder = (pDateTimeOfOrder != null) ? pDateTimeOfOrder : new Date(System.currentTimeMillis());
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int pAmount) {
        this.amount = pAmount;
    }

    public int getPrice() {
        return this.price;
    }

    public String getId() {
        return this.id;
    }

    public Side getSide() {
        return this.side;
    }

    public Date getDateTimeOfOrder() {
        return dateTimeOfOrder;
    }

    // Fiyat sıralaması.
    @Override
    public int compareTo(Order o) {
        if (this.side == Side.BUY) {
            int priceComparison = Integer.compare(o.price, this.price);
            if (priceComparison != 0)
                return priceComparison; //tamamlanır
        } else {
            int priceComparison = Integer.compare(this.price, o.price);
            if (priceComparison != 0)
                return priceComparison;  //tamamlanır
        }

        // aYNI Fiyat durumunda: FİFO Zaman önceliği
        return this.dateTimeOfOrder.compareTo(o.dateTimeOfOrder);
    }

}