package com.staj.MatchingEngine;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class OrderMatch {

    private List<Order> buyOrders; // Alım emirleri listesi sıralanmış
    private List<Order> sellOrders; // Satım emirleri sıralanmış
    private List<Trade> allTrades; // trade sayılsı
    private int totalProcessedOrders = 0; // Toplam işlenen emir sayısı

    public OrderMatch() {
        this.buyOrders = new ArrayList<>();   // alıcılar
        this.sellOrders = new ArrayList<>();  // satıcılar
        this.allTrades = new ArrayList<>();   // tüm işlemler
        this.totalProcessedOrders = 0;
    }

    // çakışma engelleme çabaları.
    public synchronized List<Order> getBuyOrders() {
        return new ArrayList<>(buyOrders);
    }
    public synchronized List<Order> getSellOrders() {
        return new ArrayList<>(sellOrders);
    }
    public synchronized List<Trade> getAllTrades() {
        return new ArrayList<>(allTrades);
    }
    public synchronized int getTotalProcessedOrders() {
        return totalProcessedOrders;
    }

    public synchronized List<Trade> process(Order pOrder) {
        totalProcessedOrders++; // Her yeni emir için sayacı artır
        if (pOrder.getSide() == Side.BUY) {
            return this.processSideBuy(pOrder);
        } else {
            return this.processSideSell(pOrder);
        }
    }

    private List<Trade> processSideSell(Order order) {
        ArrayList<Trade> trades = new ArrayList<>();

        while (!buyOrders.isEmpty()) {
            Order buyOrder = this.buyOrders.get(0);

            if (buyOrder.getPrice() < order.getPrice()) {
                break;
            }

            if (buyOrder.getAmount() >= order.getAmount()) {
                // Trade oluştur
                Trade newTrade = new Trade(order.getId(), buyOrder.getId(),
                        order.getAmount(), buyOrder.getPrice(), order.getPrice(), buyOrder.getPrice());

                trades.add(newTrade);
                allTrades.add(newTrade);

                buyOrder.setAmount(buyOrder.getAmount() - order.getAmount());

                if (buyOrder.getAmount() == 0) {
                    this.removeBuyOrder(0);
                }

                //this.setLastSalePrice(buyOrder.getPrice());
                return trades;
            } else if (buyOrder.getAmount() < order.getAmount()) {
                Trade newTrade = new Trade(order.getId(), buyOrder.getId(),
                        buyOrder.getAmount(), buyOrder.getPrice(), order.getPrice(), buyOrder.getPrice());

                trades.add(newTrade);
                allTrades.add(newTrade); // Global listeye de ekle

                order.setAmount(order.getAmount() - buyOrder.getAmount());

                this.removeBuyOrder(0);
                // this.setLastSalePrice(buyOrder.getPrice());
                continue;
            }
        }

        this.sellOrders.add(order);
        Collections.sort(this.sellOrders);
        return trades;
    }

    private List<Trade> processSideBuy(Order order) {
        final ArrayList<Trade> trades = new ArrayList<>();
        //Döngü dışında kontrol ile. Döngüye girmeden kontrol. hız için.
        final int n = this.sellOrders.size();

        int currentPrice;
        if (n == 0) {
            currentPrice = -1;
        } else {
            currentPrice = this.sellOrders.get(0).getPrice();
        }

        if (n != 0 && currentPrice <= order.getPrice()) {

            while (!sellOrders.isEmpty()) {
                final Order sellOrder = this.sellOrders.get(0);

                if (sellOrder.getPrice() > order.getPrice()) {
                    break;
                }

                if (sellOrder.getAmount() >= order.getAmount()) {
                    // Trade oluştur
                    Trade newTrade = new Trade(order.getId(), sellOrder.getId(), order.getAmount(), sellOrder.getPrice(), order.getPrice(), sellOrder.getPrice());

                    trades.add(newTrade);
                    allTrades.add(newTrade); // global listte

                    sellOrder.setAmount(sellOrder.getAmount() - order.getAmount());

                    if (sellOrder.getAmount() == 0) {
                        this.removeSellOrder(0);
                    }

                    //this.setLastSalePrice(sellOrder.getPrice());
                    return trades;
                } else if (sellOrder.getAmount() < order.getAmount()) {
                    Trade newTrade = new Trade(order.getId(), sellOrder.getId(),
                            sellOrder.getAmount(), sellOrder.getPrice(), order.getPrice(), sellOrder.getPrice());

                    trades.add(newTrade);
                    allTrades.add(newTrade); // Global liste

                    order.setAmount(order.getAmount() - sellOrder.getAmount());

                    this.removeSellOrder(0);
                    //this.setLastSalePrice(sellOrder.getPrice());
                    continue;
                }
            }
        }

        this.buyOrders.add(order);
        Collections.sort(this.buyOrders);
        return trades;
    }

    private void removeSellOrder(int index) {
        this.sellOrders.remove(index);
    }

    private void removeBuyOrder(int index) {
        this.buyOrders.remove(index);
    }

}