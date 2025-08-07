package com.staj.MatchingEngine.engine;

import com.staj.MatchingEngine.model.Order;
import com.staj.MatchingEngine.model.Side;
import com.staj.MatchingEngine.model.Trade;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OptimizedOrderMatch {

    private Queue<Order> buyOrders; // buy emirleri
    private Queue<Order> sellOrders;// sel emirleri
    private List<Trade> allTrades; // tüm eşleşeneler.
    private int OrderCounter = 0; // sayaç
    // private ReentrantLock lock = new ReentrantLock();
    private TradePool tradePool; // Pool Objesi tanımı.
    /*
     * // Thread güvenliği için Lock //Lock bir kapı gibi görev yapar. tek Thread
     * içeri
     * // girer ardından kapıyı kapatır. sonra tekrar açar
     * public void increment() {
     * lock.lock();
     * try {
     * OrderCounter += 1;
     * } finally {
     * lock.unlock();
     * }
     * }
     */

    public OptimizedOrderMatch() {
        this.buyOrders = new LinkedList<>();
        this.sellOrders = new LinkedList<>();
        this.allTrades = new ArrayList<>();
        this.tradePool = new TradePool();
    }

    public Queue<Order> getBuyOrders() {
        return buyOrders;
    }

    public Queue<Order> getSellOrders() {
        return sellOrders;
    }

    public List<Trade> getAllTrades() {
        return allTrades;
    }

    public int getOrderCounter() {
        return OrderCounter;
    }

    public int getBuyOrdersSize() {
        return buyOrders.size();
    }

    public int getSellOrdersSize() {
        return sellOrders.size();
    }

    public int getTradesSize() {
        return allTrades.size();
    }

    public void reset() {
        buyOrders.clear();
        sellOrders.clear();
        allTrades.clear();
        OrderCounter = 0;
    }

    public int getBuyOrdersAmount() {
        return buyOrders.stream().mapToInt(Order::getAmount).sum();
    }

    public int getSellOrdersAmount() {
        return sellOrders.stream().mapToInt(Order::getAmount).sum();
    }

    public List<Trade> process(Order order) {

        List<Trade> matchedTrades = new ArrayList<>();
        // increment();

        if (order.getSide() == Side.BUY) {
            processBuyOrder(order, matchedTrades);
        } else {
            processSellOrder(order, matchedTrades);
        }

        return matchedTrades;
    }

    private void addSellOrderSorter(Order order) {
        // sellorders referansı linkedlist olarak cast edildi.
        LinkedList<Order> liste = (LinkedList<Order>) sellOrders;
        for (int i = 0; i < liste.size(); i++) {
            Order current = liste.get(i);
            if (order.getPrice() < current.getPrice()) {
                liste.add(i, order);
                return;
            } else if (order.getPrice() == current.getPrice()
                    && order.getDateTimeOfOrder().before(current.getDateTimeOfOrder())) {
                liste.add(i, order);
                return;
            }
        }
        liste.add(order);
    }

    private void addBuyOrderSorter(Order newOrder) {
        // sellorders referansı linkedlist olarak cast edildi.
        LinkedList<Order> liste = (LinkedList<Order>) buyOrders;
        for (int i = 0; i < liste.size(); i++) {
            Order curent = liste.get(i);
            if (newOrder.getPrice() > curent.getPrice()) {
                liste.add(i, newOrder);
                return;
            } else if (newOrder.getPrice() == curent.getPrice() &&
                    newOrder.getDateTimeOfOrder().before(curent.getDateTimeOfOrder())) {
                liste.add(i, newOrder);
                return;
            }
        }
        liste.add(newOrder);
    }

    private void processBuyOrder(Order buyOrder, List<Trade> matchedTrades) {
        while (!sellOrders.isEmpty() && buyOrder.getAmount() > 0) {
            // queue den al ama çıkarma
            Order bestSell = sellOrders.peek();

            if (bestSell.getPrice() > buyOrder.getPrice()) {
                break;
            }
            // queuden çıkart
            sellOrders.poll();

            int tradeAmount = Math.min(buyOrder.getAmount(), bestSell.getAmount());
            int tradePrice = bestSell.getPrice();

            Trade trade = tradePool.getTrade();
            trade.fillPool(buyOrder.getId(), bestSell.getId(), tradeAmount, tradePrice, buyOrder.getPrice(),
                    bestSell.getPrice());

            Trade cpy = new Trade(trade.getTakerOrderId(), trade.getMakerOrderId(), trade.getAmount(), trade.getPrice(),
                    trade.getMakerOrderPrice(), trade.getTakerOrderPrice());

            matchedTrades.add(cpy); // console test için
            allTrades.add(cpy); // api testi için

            buyOrder.setAmount(buyOrder.getAmount() - tradeAmount);
            bestSell.setAmount(bestSell.getAmount() - tradeAmount);

            tradePool.returnTrade(trade);

            if (bestSell.getAmount() > 0) {

                addSellOrderSorter(bestSell);

            }
        }

        if (buyOrder.getAmount() > 0) {

            addBuyOrderSorter(buyOrder);
        }
    }

    private void processSellOrder(Order sellOrder, List<Trade> matchedTrades) {
        while (!buyOrders.isEmpty() && sellOrder.getAmount() > 0) {
            Order bestBuy = buyOrders.peek();
            if (bestBuy.getPrice() < sellOrder.getPrice()) {
                break;
            }
            buyOrders.poll();

            int tradeAmount = Math.min(sellOrder.getAmount(), bestBuy.getAmount());
            int tradePrice = bestBuy.getPrice();

            Trade trade = tradePool.getTrade();
            trade.fillPool(bestBuy.getId(), sellOrder.getId(), tradeAmount, tradePrice, bestBuy.getPrice(),sellOrder.getPrice());
            Trade cpy = new Trade(trade.getTakerOrderId(), trade.getMakerOrderId(), trade.getAmount(), trade.getPrice(),trade.getMakerOrderPrice(), trade.getTakerOrderPrice());

            matchedTrades.add(cpy); // console tes için
            allTrades.add(cpy);// api test için

            sellOrder.setAmount(sellOrder.getAmount() - tradeAmount);
            bestBuy.setAmount(bestBuy.getAmount() - tradeAmount);

            tradePool.returnTrade(trade);

            if (bestBuy.getAmount() > 0) {
                addBuyOrderSorter(bestBuy);
            }
        }

        if (sellOrder.getAmount() > 0) {
            addSellOrderSorter(sellOrder);
        }
    }

    // Pool sınıfı, trade nesnelerini yeniden yaratmadan kullanıır.
    // maliyet azaltma, garbage collecter yükü azaltma, performans artışı, bellek
    // yönetimi
    private  class TradePool {
        private Queue<Trade> pool = new LinkedList<>();
        private int poolSize = 5000;
        private int poolSizeIncr = 2000;

        public TradePool() {
            for (int i = 0; i < poolSize; i++) {
                pool.offer(new Trade("", "", 0, 0, 0, 0));
            }

        }

        public Trade getTrade() {
            Trade trade = pool.poll();
            return trade;
        }

        public void returnTrade(Trade trade) {
            if (pool.size() < poolSize) {
                trade.fillPool("", "", 0, 0, 0, 0);
                pool.offer(trade);
            } else if (pool.size() >= poolSize) {
                for (int i = 0; i < poolSizeIncr; i++) {
                    pool.offer(new Trade("", "", 0, 0, 0, 0));
                }
            }
        }
    }
}
