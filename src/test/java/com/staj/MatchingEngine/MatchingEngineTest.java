package com.staj.MatchingEngine;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class MatchingEngineTest {
    public static void main(String[] args) {
        System.out.println("===== EMİR EŞLEŞTİRME SİSTEMİ TESTİ =====");
        System.out.println("Test başlangıç zamanı: " + getCurrentTimestamp());

        OrderMatch orderBook = new OrderMatch();


        System.out.println("TEST 1: Basit");

        // İlk önce alış emirleri girelim (Builder pattern kullanarak)
        Order buy1 = new Order.Builder(Side.BUY)
                .withId("B001")
                .withPrice(100)
                .withAmount(10)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        System.out.println("[" + getCurrentTimestamp() + "] Alış emri ekleniyor: " + formatOrder(buy1));
        orderBook.process(buy1);

        // Satış emri gelsin ve eşleşme gerçekleşsin
        Order sell1 = new Order.Builder(Side.SELL)
                .withId("S001")
                .withPrice(90)
                .withAmount(5)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        System.out.println("[" + getCurrentTimestamp() + "] Satış emri işleniyor: " + formatOrder(sell1));
        List<Trade> trades1 = orderBook.process(sell1);

        // Sonuçları yazdır
        printTradeResults(trades1);

        // TEST SENARYO 2: Kısmi Eşleşme
        System.out.println("\n===== SENARYO 2: KISMİ EŞLEŞME =====");

        Order sell2 = new Order.Builder(Side.SELL)
                .withId("S002")
                .withPrice(95)
                .withAmount(8)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        System.out.println("[" + getCurrentTimestamp() + "] Satış emri işleniyor: " + formatOrder(sell2));
        List<Trade> trades2 = orderBook.process(sell2);

        printTradeResults(trades2);

        // TEST SENARYO 3: Fiyat Önceliği
        System.out.println("\n===== SENARYO 3: FİYAT ÖNCELİĞİ =====");

        // Farklı fiyatlı alış emirleri
        Order buy2 = new Order.Builder(Side.BUY)
                .withId("B002")
                .withPrice(98)
                .withAmount(5)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        Order buy3 = new Order.Builder(Side.BUY)
                .withId("B003")
                .withPrice(102)
                .withAmount(5)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        System.out.println("[" + getCurrentTimestamp() + "] Düşük fiyatlı alış emri ekleniyor: " + formatOrder(buy2));
        orderBook.process(buy2);

        System.out.println("[" + getCurrentTimestamp() + "] Yüksek fiyatlı alış emri ekleniyor: " + formatOrder(buy3));
        orderBook.process(buy3);

        // Satış emri gelsin - yüksek fiyatlı alış emri ile eşleşmeli
        Order sell3 = new Order.Builder(Side.SELL)
                .withId("S003")
                .withPrice(97)
                .withAmount(3)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        System.out.println("[" + getCurrentTimestamp() + "] Satış emri işleniyor: " + formatOrder(sell3));
        List<Trade> trades3 = orderBook.process(sell3);

        printTradeResults(trades3);

        // TEST SENARYO 4: Zaman Önceliği (FIFO)
        System.out.println("\n===== SENARYO 4: ZAMAN ÖNCELİĞİ (FIFO) =====");

        // Aynı fiyatla farklı zamanlarda gelen emirler
        Order buy4 = new Order.Builder(Side.BUY)
                .withId("B004")
                .withPrice(96)
                .withAmount(10)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        sleepForAMoment(50); // Zaman farkı oluştur

        Order buy5 = new Order.Builder(Side.BUY)
                .withId("B005")
                .withPrice(96)
                .withAmount(10)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        System.out.println("[" + getCurrentTimestamp() + "] İlk emir ekleniyor: " + formatOrder(buy4));
        orderBook.process(buy4);

        System.out.println("[" + getCurrentTimestamp() + "] İkinci emir (aynı fiyat) ekleniyor: " + formatOrder(buy5));
        orderBook.process(buy5);

        // Satış emirleri
        Order sell4 = new Order.Builder(Side.SELL)
                .withId("S004")
                .withPrice(95)
                .withAmount(15)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        System.out.println("[" + getCurrentTimestamp() + "] Satış emri işleniyor: " + formatOrder(sell4));
        List<Trade> trades4 = orderBook.process(sell4);

        printTradeResults(trades4);

        // TEST SENARYO 5: Karışık Emirler ve İptal
        System.out.println("\n===== SENARYO 5: KARIŞIK EMİRLER VE İPTAL =====");

        // Farklı fiyatlarla alış emirleri
        Order buy6 = new Order.Builder(Side.BUY)
                .withId("B006")
                .withPrice(105)
                .withAmount(5)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        Order buy7 = new Order.Builder(Side.BUY)
                .withId("B007")
                .withPrice(103)
                .withAmount(3)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        System.out.println("[" + getCurrentTimestamp() + "] Alış emirleri ekleniyor");
        orderBook.process(buy6);
        orderBook.process(buy7);

        // Emir iptal etme
        System.out.println("[" + getCurrentTimestamp() + "] B006 ID'li emir iptal ediliyor");

        // Yeni satış emri (B007 ile eşleşmeli)
        Order sell5 = new Order.Builder(Side.SELL)
                .withId("S005")
                .withPrice(102)
                .withAmount(2)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        System.out.println("[" + getCurrentTimestamp() + "] Satış emri işleniyor: " + formatOrder(sell5));
        List<Trade> trades5 = orderBook.process(sell5);

        printTradeResults(trades5);

        // DEBUG: Order Book Durumunu Kontrol Et
        System.out.println("\n===== ORDER BOOK DURUMU =====");
        System.out.println("Buy Orders: " + orderBook.getBuyOrders().size());
        System.out.println("Sell Orders: " + orderBook.getSellOrders().size());
        
        // Spread testi için emirler ekle
        Order testBuy = new Order.Builder(Side.BUY)
                .withId("TEST-BUY")
                .withPrice(90)
                .withAmount(5)
                .withDate(new Date(System.currentTimeMillis()))
                .build();
                
        Order testSell = new Order.Builder(Side.SELL)
                .withId("TEST-SELL")
                .withPrice(95)
                .withAmount(5)
                .withDate(new Date(System.currentTimeMillis()))
                .build();
                
        orderBook.process(testBuy);
        orderBook.process(testSell);
        
        System.out.println("Test order prices: Buy at 90, Sell at 95");

        // TÜM İŞLEMLERİ YAZDIR
        System.out.println("\n===== TÜM GERÇEKLEŞEN İŞLEMLER =====");
        printAllTrades(orderBook.getAllTrades());

        System.out.println("\n===== TEST SONUÇLANDI =====");
        System.out.println("Test bitiş zamanı: " + getCurrentTimestamp());
    }

    // Yardımcı metodlar
    private static void printTradeResults(List<Trade> trades) {
        if (trades.isEmpty()) {
            System.out.println("Eşleşme gerçekleşmedi!");
        } else {
            System.out.println("Gerçekleşen İşlemler:");
            for (Trade trade : trades) {
                System.out.println("  - " + trade);
            }
        }
    }

    // Tüm gerçekleşen işlemleri yazdıran metod
    private static void printAllTrades(List<Trade> allTrades) {
        if (allTrades.isEmpty()) {
            System.out.println("Hiç işlem gerçekleşmedi!");
        } else {
            System.out.println("Tüm gerçekleşen işlemler (" + allTrades.size() + " adet):");
            System.out.println("---------------------------------------------------");
            System.out.printf("%-10s %-10s %-8s %-8s\n", "TAKER ID", "MAKER ID", "MİKTAR", "FİYAT");
            System.out.println("---------------------------------------------------");
            for (Trade trade : allTrades) {
                System.out.printf("%-10s %-10s %-8d %-8d\n",
                        trade.getTakerOrderId(),
                        trade.getMakerOrderId(),
                        trade.getAmount(),
                        trade.getPrice());
            }
            System.out.println("---------------------------------------------------");

            // İşlemlerin toplam değeri
            int totalValue = 0;
            int totalAmount = 0;
            for (Trade trade : allTrades) {
                totalValue += trade.getAmount() * trade.getPrice();
                totalAmount += trade.getAmount();
            }

            System.out.println("Toplam İşlem Hacmi: " + totalAmount + " adet / " + totalValue + " TL");
        }
    }

    private static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new java.util.Date());
    }

    private static void sleepForAMoment(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String formatOrder(Order order) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        return String.format("Emir[ID=%s, %s, Fiyat=%d, Miktar=%d, Zaman=%s]",
                order.getId(), order.getSide(), order.getPrice(), order.getAmount(),
                sdf.format(order.getDateTimeOfOrder()));
    }
}