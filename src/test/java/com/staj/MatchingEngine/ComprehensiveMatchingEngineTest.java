package com.staj.MatchingEngine;

import com.staj.MatchingEngine.engine.OptimizedOrderMatch;
import com.staj.MatchingEngine.model.Order;
import com.staj.MatchingEngine.model.Side;
import com.staj.MatchingEngine.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.util.List;

public class ComprehensiveMatchingEngineTest {

    private OptimizedOrderMatch engine;

    @BeforeEach
    void setUp() {
        engine = new OptimizedOrderMatch();
    }

    @Test
    @DisplayName("Test 1: Kapsamlı Eşleştirme Testi - Çoklu Emirler")
    void testComprehensiveMatching() {
        System.out.println("=== Test 1: Kapsamlı Eşleştirme Testi ===");

        // SELL Emirleri Ekleniyor
        Order sell1 = new Order.Builder(Side.SELL)
                .withId("SELL001")
                .withPrice(100)
                .withAmount(50)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        Order sell2 = new Order.Builder(Side.SELL)
                .withId("SELL002")
                .withPrice(100)
                .withAmount(30)
                .withDate(new Date(System.currentTimeMillis() + 1))
                .build();

        Order sell3 = new Order.Builder(Side.SELL)
                .withId("SELL003")
                .withPrice(101)
                .withAmount(40)
                .withDate(new Date(System.currentTimeMillis() + 2))
                .build();

        Order sell4 = new Order.Builder(Side.SELL)
                .withId("SELL004")
                .withPrice(99)
                .withAmount(25)
                .withDate(new Date(System.currentTimeMillis() + 3))
                .build();

        // SELL emirlerini ekle
        engine.process(sell1);
        engine.process(sell2);
        engine.process(sell3);
        engine.process(sell4);

        System.out.println("Toplam SELL orderları: " + engine.getSellOrdersSize());

        // BUY001: Fiyat=100, Miktar=20
        Order buy1 = new Order.Builder(Side.BUY)
                .withId("BUY001")
                .withPrice(100)
                .withAmount(20)
                .withDate(new Date(System.currentTimeMillis() + 4))
                .build();

        List<Trade> trades1 = engine.process(buy1);
        System.out.println("BUY001 işlendi - Trade sayısı: " + trades1.size());
        assertEquals(1, trades1.size(), "BUY001 1 trade oluşturmalı");

        if (!trades1.isEmpty()) {
            Trade trade = trades1.get(0);
            assertEquals(20, trade.getAmount(), "Trade miktarı 20 olmalı");
            assertEquals(99, trade.getPrice(), "Trade fiyatı 99 olmalı (en düşük SELL fiyatı)");
        }

        // BUY002: Fiyat=99, Miktar=25
        Order buy2 = new Order.Builder(Side.BUY)
                .withId("BUY002")
                .withPrice(99)
                .withAmount(25)
                .withDate(new Date(System.currentTimeMillis() + 5))
                .build();

        List<Trade> trades2 = engine.process(buy2);
        System.out.println("BUY002 işlendi - Trade sayısı: " + trades2.size());
        assertEquals(1, trades2.size(), "BUY002 1 trade oluşturmalı");

        // BUY003: Fiyat=101, Miktar=80 (Çoklu eşleşme)
        Order buy3 = new Order.Builder(Side.BUY)
                .withId("BUY003")
                .withPrice(101)
                .withAmount(80)
                .withDate(new Date(System.currentTimeMillis() + 6))
                .build();

        List<Trade> trades3 = engine.process(buy3);
        System.out.println("BUY003 işlendi - Trade sayısı: " + trades3.size());
        assertTrue(trades3.size() >= 1, "BUY003 en az 1 trade oluşturmalı");

        // BUY004: Fiyat=98, Miktar=15 (Eşleşme beklenmez)
        Order buy4 = new Order.Builder(Side.BUY)
                .withId("BUY004")
                .withPrice(98)
                .withAmount(15)
                .withDate(new Date(System.currentTimeMillis() + 7))
                .build();

        List<Trade> trades4 = engine.process(buy4);
        System.out.println("BUY004 işlendi - Trade sayısı: " + trades4.size());
        assertEquals(0, trades4.size(), "BUY004 trade oluşturmamalı");

        System.out.println("Final durum:");
        System.out.println("Kalan BUY orderları: " + engine.getBuyOrdersSize());
        System.out.println("Kalan SELL orderları: " + engine.getSellOrdersSize());
        System.out.println("Toplam trade: " + engine.getTradesSize());
    }

    @Test
    @DisplayName("Test 2: FIFO Önceliği Testi")
    void testFIFOPriority() {
        System.out.println("=== Test 2: FIFO Önceliği Testi ===");

        // Aynı fiyatta SELL emirleri - FIFO test için
        Order sellFirst = new Order.Builder(Side.SELL)
                .withId("SELL_FIRST")
                .withPrice(105)
                .withAmount(15)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        Order sellSecond = new Order.Builder(Side.SELL)
                .withId("SELL_SECOND")
                .withPrice(105)
                .withAmount(25)
                .withDate(new Date(System.currentTimeMillis() + 100))
                .build();

        Order sellThird = new Order.Builder(Side.SELL)
                .withId("SELL_THIRD")
                .withPrice(105)
                .withAmount(20)
                .withDate(new Date(System.currentTimeMillis() + 200))
                .build();

        // SELL emirlerini ekle
        engine.process(sellFirst);
        engine.process(sellSecond);
        engine.process(sellThird);

        System.out.println("SELL emirleri eklendi - Toplam: " + engine.getSellOrdersSize());

        // BUY emri - FIFO testi
        Order buyTest = new Order.Builder(Side.BUY)
                .withId("BUY_TEST1")
                .withPrice(105)
                .withAmount(10)
                .withDate(new Date(System.currentTimeMillis() + 300))
                .build();

        List<Trade> trades = engine.process(buyTest);
        System.out.println("BUY_TEST1 işlendi - Trade sayısı: " + trades.size());

        if (!trades.isEmpty()) {
            Trade trade = trades.get(0);
            System.out.println("İlk trade maker ID: " + trade.getMakerOrderId());
            assertEquals("SELL_FIRST", trade.getMakerOrderId(), "FIFO sırasına göre SELL_FIRST ile eşleşmeli");
        }
    }

    @Test
    @DisplayName("Test 3: Fiyat Önceliği Testi")
    void testPricePriority() {
        System.out.println("=== Test 3: Fiyat Önceliği Testi ===");

        // Karışık fiyatlarda SELL emirleri
        Order sell120 = new Order.Builder(Side.SELL)
                .withId("SELL120")
                .withPrice(120)
                .withAmount(15)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        Order sell95 = new Order.Builder(Side.SELL)
                .withId("SELL95")
                .withPrice(95)
                .withAmount(20)
                .withDate(new Date(System.currentTimeMillis() + 100))
                .build();

        Order sell110 = new Order.Builder(Side.SELL)
                .withId("SELL110")
                .withPrice(110)
                .withAmount(25)
                .withDate(new Date(System.currentTimeMillis() + 200))
                .build();

        // SELL emirlerini ekle
        engine.process(sell120);
        engine.process(sell95);
        engine.process(sell110);

        System.out.println("SELL emirleri eklendi - Fiyat dağılımı: 95(20), 110(25), 120(15)");

        // Yüksek fiyatlı BUY emri - en düşük SELL fiyatıyla eşleşmeli
        Order buyTest = new Order.Builder(Side.BUY)
                .withId("BUY_TEST")
                .withPrice(125)
                .withAmount(15)
                .withDate(new Date(System.currentTimeMillis() + 300))
                .build();

        List<Trade> trades = engine.process(buyTest);
        System.out.println("BUY_TEST işlendi - Trade sayısı: " + trades.size());

        if (!trades.isEmpty()) {
            Trade trade = trades.get(0);
            System.out.println("İlk trade fiyatı: " + trade.getPrice());
            System.out.println("İlk trade maker ID: " + trade.getMakerOrderId());
            assertEquals(95, trade.getPrice(), "En düşük SELL fiyatı (95) ile eşleşmeli");
            assertEquals("SELL95", trade.getMakerOrderId(), "SELL95 ile eşleşmeli");
        }
    }

    @Test
    @DisplayName("Test 4: Kısmi Eşleşme Testi")
    void testPartialMatching() {
        System.out.println("=== Test 4: Kısmi Eşleşme Testi ===");

        // Küçük SELL emri
        Order sell = new Order.Builder(Side.SELL)
                .withId("SELL_SMALL")
                .withPrice(100)
                .withAmount(10)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        engine.process(sell);

        // Büyük BUY emri - kısmi eşleşme bekleniyor
        Order buy = new Order.Builder(Side.BUY)
                .withId("BUY_LARGE")
                .withPrice(100)
                .withAmount(30)
                .withDate(new Date(System.currentTimeMillis() + 100))
                .build();

        List<Trade> trades = engine.process(buy);
        System.out.println("Kısmi eşleşme testi - Trade sayısı: " + trades.size());

        assertEquals(1, trades.size(), "1 trade oluşmalı");
        if (!trades.isEmpty()) {
            Trade trade = trades.get(0);
            assertEquals(10, trade.getAmount(), "Trade miktarı 10 olmalı");
        }

        // Kalan BUY emri kuyruğa eklenmiş olmalı
        assertEquals(1, engine.getBuyOrdersSize(), "1 BUY emri kuyruğa eklenmiş olmalı");
    }

    @Test
    @DisplayName("Test 5: Eşleşme Olmayan Durum Testi")
    void testNoMatching() {
        System.out.println("=== Test 5: Eşleşme Olmayan Durum Testi ===");

        // Yüksek fiyatlı SELL emri
        Order sell = new Order.Builder(Side.SELL)
                .withId("SELL_HIGH")
                .withPrice(110)
                .withAmount(20)
                .withDate(new Date(System.currentTimeMillis()))
                .build();

        engine.process(sell);

        // Düşük fiyatlı BUY emri - eşleşme olmamalı
        Order buy = new Order.Builder(Side.BUY)
                .withId("BUY_LOW")
                .withPrice(105)
                .withAmount(15)
                .withDate(new Date(System.currentTimeMillis() + 100))
                .build();

        List<Trade> trades = engine.process(buy);
        System.out.println("Eşleşme olmayan test - Trade sayısı: " + trades.size());

        assertEquals(0, trades.size(), "Trade oluşmamalı");
        assertEquals(1, engine.getBuyOrdersSize(), "BUY emri kuyruğa eklenmiş olmalı");
        assertEquals(1, engine.getSellOrdersSize(), "SELL emri kuyruğa eklenmiş olmalı");
    }

    @Test
    @DisplayName("Test 6: Performans Testi - Küçük Ölçek")
    void testSmallScalePerformance() {
        System.out.println("=== Test 6: Performans Testi ===");

        long startTime = System.currentTimeMillis();

        // 20 SELL emri ekle
        for (int i = 1; i <= 20; i++) {
            Order sell = new Order.Builder(Side.SELL)
                    .withId("PERF_SELL_" + i)
                    .withPrice(100 + (i % 5))
                    .withAmount(10 + (i % 3))
                    .withDate(new Date(System.currentTimeMillis() + i))
                    .build();
            engine.process(sell);
        }

        System.out.println("20 SELL emri eklendi");

        // 15 BUY emri ekle
        for (int i = 1; i <= 15; i++) {
            Order buy = new Order.Builder(Side.BUY)
                    .withId("PERF_BUY_" + i)
                    .withPrice(102 + (i % 3))
                    .withAmount(8 + (i % 4))
                    .withDate(new Date(System.currentTimeMillis() + 1000 + i))
                    .build();
            List<Trade> trades = engine.process(buy);
            System.out.println("BUY " + i + " - Trade sayısı: " + trades.size());
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Performans test sonuçları:");
        System.out.println("Toplam işlenen emir: 35");
        System.out.println("Toplam trade: " + engine.getTradesSize());
        System.out.println("Süre: " + duration + " ms");
        System.out.println("Kalan BUY: " + engine.getBuyOrdersSize());
        System.out.println("Kalan SELL: " + engine.getSellOrdersSize());

        assertTrue(duration < 1000, "35 emir 1 saniyeden az sürede işlenmeli");
    }
}
