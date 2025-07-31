package com.staj.MatchingEngine;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

public class StressTest {

    private static Random random = new Random();
    private static int buyOrderId = 1;
    private static int sellOrderId = 1;

    public static void main(String[] args) {
        System.out.println("===== YOĞUN EMİR BOMBARDIMANI TESTİ =====");
        System.out.println("Test başlangıç zamanı: " + getCurrentTimestamp());

        OrderMatch orderBook = new OrderMatch();

        // Test 1: Hızlı ardışık emirler
        System.out.println("\n[TEST 1] 100 hızlı ardışık emir gönderiliyor...");
        rapidOrderTest(orderBook, 100);

        // Test 2: Aynı fiyatta çok sayıda emir
        System.out.println("\n[TEST 2] Aynı fiyatta çok sayıda emir...");
        samePriceTest(orderBook, 50);

        // Test 3: Fiyat dalgalanma testi
        System.out.println("\n[TEST 3] Fiyat dalgalanma testi...");
        priceFluctuationTest(orderBook, 80);

        // Test 4: Büyük hacimli bombardıman
        System.out.println("\n[TEST 4] Büyük hacimli emir bombardımanı...");
        highVolumeTest(orderBook, 30);
    }

    // Test 1: Hızlı ardışık emirler
    private static void rapidOrderTest(OrderMatch orderBook, int orderCount) {
        int matches = 0;

        for (int i = 0; i < orderCount; i++) {
            // Rastgele alış/satış seç
            boolean isBuy = random.nextBoolean();
            int basePrice = 100;
            int priceVariation = random.nextInt(10) - 5; // -5 ile +5 arası
            int price = basePrice + priceVariation;
            int amount = 1 + random.nextInt(5); // 1-5 arası miktar

            Order order;
            if (isBuy) {
                order = createBuyOrder(price, amount);
            } else {
                order = createSellOrder(price, amount);
            }

            List<Trade> trades = orderBook.process(order);
            if (!trades.isEmpty()) {
                matches += trades.size();
                System.out.println("  ✓ " + formatOrder(order) + " → " + trades.size() + " eşleşme");

                // Eşleşen emirleri detaylı göster
                for (Trade trade : trades) {
                    System.out.println("      📝 " + trade.getTakerOrderId() + " ↔ " + trade.getMakerOrderId() + " | " + trade.getAmount() + " adet @ " + trade.getPrice() + " TL");
                }

                // Eğer kısmi eşleşme varsa kalan miktarı göster
                int totalTradeAmount = trades.stream().mapToInt(Trade::getAmount).sum();
                if (totalTradeAmount < order.getAmount()) {
                    System.out.println("      📋 Kalan miktar: " + (order.getAmount() - totalTradeAmount) + " adet beklemede");
                }
            } else {
                System.out.println("  - " + formatOrder(order) + " → beklemede");
            }

            // Çok kısa gecikme
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }

        System.out.println("  Toplam " + matches + " eşleşme gerçekleşti!");
    }

    // Test 2: Aynı fiyatta çok sayıda emir
    private static void samePriceTest(OrderMatch orderBook, int orderCount) {
        int buyPrice = 98;
        int sellPrice = 102;

        // Önce alış emirleri
        System.out.println("  Aynı fiyatta (" + buyPrice + ") alış emirleri:");
        for (int i = 0; i < orderCount / 2; i++) {
            Order buyOrder = createBuyOrder(buyPrice, 1 + random.nextInt(3));
            orderBook.process(buyOrder);
            System.out.println("    " + formatOrder(buyOrder));
        }

        // Sonra satış emirleri
        System.out.println("  Aynı fiyatta (" + sellPrice + ") satış emirleri:");
        for (int i = 0; i < orderCount / 2; i++) {
            Order sellOrder = createSellOrder(sellPrice, 1 + random.nextInt(3));
            orderBook.process(sellOrder);
            System.out.println("    " + formatOrder(sellOrder));
        }

        // Şimdi eşleşme yaratacak emirler gönder
        System.out.println("  Eşleşme yaratacak emirler:");
        Order triggerSell = createSellOrder(97, 10); // Düşük fiyatlı satış
        List<Trade> trades1 = orderBook.process(triggerSell);
        System.out.println("    " + formatOrder(triggerSell) + " → " + trades1.size() + " eşleşme");

        // Eşleşmeleri detaylı göster
        for (Trade trade : trades1) {
            System.out.println("        📝 " + trade.getTakerOrderId() + " ↔ " + trade.getMakerOrderId() + " | " + trade.getAmount() + " adet @ " + trade.getPrice() + " TL");
        }

        Order triggerBuy = createBuyOrder(103, 10); // Yüksek fiyatlı alış
        List<Trade> trades2 = orderBook.process(triggerBuy);
        System.out.println("    " + formatOrder(triggerBuy) + " → " + trades2.size() + " eşleşme");

        // Eşleşmeleri detaylı göster
        for (Trade trade : trades2) {
            System.out.println("        📝 " + trade.getTakerOrderId() + " ↔ " + trade.getMakerOrderId() + " | " + trade.getAmount() + " adet @ " + trade.getPrice() + " TL");
        }

        System.out.println("  FIFO test tamamlandı! Toplam " + (trades1.size() + trades2.size()) + " eşleşme");
    }

    // Test 3: Fiyat dalgalanma testi
    private static void priceFluctuationTest(OrderMatch orderBook, int orderCount) {
        int currentPrice = 100;
        int matches = 0;

        for (int i = 0; i < orderCount; i++) {
            // Fiyat dalgalanması simüle et
            int priceChange = random.nextInt(6) - 3; // -3 ile +3 arası değişim
            currentPrice += priceChange;
            currentPrice = Math.max(80, Math.min(120, currentPrice)); // 80-120 arasında tut

            boolean isBuy = random.nextBoolean();
            int amount = 2 + random.nextInt(8); // 2-9 arası

            Order order;
            if (isBuy) {
                order = createBuyOrder(currentPrice, amount);
            } else {
                order = createSellOrder(currentPrice, amount);
            }

            List<Trade> trades = orderBook.process(order);
            if (!trades.isEmpty()) {
                matches += trades.size();
                System.out.println("  [Fiyat:" + currentPrice + "] " + formatOrder(order) + " → " + trades.size() + " eşleşme");

                // Eşleşen emirleri göster
                for (Trade trade : trades) {
                    System.out.println("      📝 " + trade.getTakerOrderId() + " ↔ " + trade.getMakerOrderId() + " | " + trade.getAmount() + " adet @ " + trade.getPrice() + " TL");
                }
            }

            if (i % 20 == 0) { // Her 20 emirde bir durum raporu
                System.out.println("    → Mevcut fiyat: " + currentPrice + ", Bekleyen alış: " + orderBook.getBuyOrders().size() + ", Bekleyen satış: " + orderBook.getSellOrders().size());
            }
        }

        System.out.println("  Dalgalanma testinde " + matches + " eşleşme!");
    }

    // Test 4: Büyük hacimli bombardıman
    private static void highVolumeTest(OrderMatch orderBook, int orderCount) {
        System.out.println("  Büyük hacimli emirler bombardımanı başlıyor...");

        for (int i = 0; i < orderCount; i++) {
            boolean isBuy = random.nextBoolean();
            int price = 95 + random.nextInt(11); // 95-105 arası
            int amount = 50 + random.nextInt(151); // 50-200 arası büyük miktarlar

            Order order;
            if (isBuy) {
                order = createBuyOrder(price, amount);
            } else {
                order = createSellOrder(price, amount);
            }

            long startTime = System.currentTimeMillis();
            List<Trade> trades = orderBook.process(order);
            long endTime = System.currentTimeMillis();

            if (!trades.isEmpty()) {
                System.out.println("  ⚡ " + formatOrder(order) + " → " + trades.size() + " eşleşme (" + (endTime - startTime) + "ms)");

                // Büyük hacimli eşleşmeleri göster
                for (Trade trade : trades) {
                    System.out.println("      📝 " + trade.getTakerOrderId() + " ↔ " + trade.getMakerOrderId() + " | " + trade.getAmount() + " adet @ " + trade.getPrice() + " TL");
                }

                // Kalan miktar varsa göster
                int totalTradeAmount = trades.stream().mapToInt(Trade::getAmount).sum();
                if (totalTradeAmount < order.getAmount()) {
                    System.out.println("      📋 Kalan miktar: " + (order.getAmount() - totalTradeAmount) + " adet beklemede");
                }
            } else {
                System.out.println("  📋 " + formatOrder(order) + " → beklemede");
            }

            // Kısa gecikme
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
        }
    }

    // Yardımcı metodlar
    private static Order createBuyOrder(int price, int amount) {
        return new Order.Builder(Side.BUY).withId("B" + String.format("%03d", buyOrderId++)).withPrice(Math.max(1, price)).withAmount(amount).withDate(new Date(System.currentTimeMillis())).build();
    }

    private static Order createSellOrder(int price, int amount) {
        return new Order.Builder(Side.SELL).withId("S" + String.format("%03d", sellOrderId++)).withPrice(Math.max(1, price)).withAmount(amount).withDate(new Date(System.currentTimeMillis())).build();
    }

    private static String formatOrder(Order order) {
        return String.format("%s[%s,%d@%d]", order.getId(), order.getSide() == Side.BUY ? "ALIŞ" : "SATIŞ", order.getAmount(), order.getPrice());
    }

    private static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new java.util.Date());
    }
}
