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

/**
 * Orijinal test senaryolarınızla karşılaştırmalı test sınıfı
 */
public class OriginalScenarioTest {

    private OptimizedOrderMatch engine;

    @BeforeEach
    void setUp() {
        engine = new OptimizedOrderMatch();
    }

    @Test
    @DisplayName("Test 1: Kapsamlı Eşleştirme Testi - Çoklu Emirler (Orijinal Senaryo)")
    void testOriginalComprehensiveMatching() {
        System.out.println("=== Test 1: Kapsamlı Eşleştirme Testi - Çoklu Emirler ===");
        
        // 📈 SELL Emirleri Ekleniyor
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
                .withDate(new Date(System.currentTimeMillis() + 100))
                .build();
        
        Order sell3 = new Order.Builder(Side.SELL)
                .withId("SELL003")
                .withPrice(101)
                .withAmount(40)
                .withDate(new Date(System.currentTimeMillis() + 200))
                .build();
        
        Order sell4 = new Order.Builder(Side.SELL)
                .withId("SELL004")
                .withPrice(99)
                .withAmount(25)
                .withDate(new Date(System.currentTimeMillis() + 300))
                .build();

        engine.process(sell1);
        engine.process(sell2);
        engine.process(sell3);
        engine.process(sell4);

        System.out.println("✓ SELL001: Fiyat=100, Miktar=50");
        System.out.println("✓ SELL002: Fiyat=100, Miktar=30");
        System.out.println("✓ SELL003: Fiyat=101, Miktar=40");
        System.out.println("✓ SELL004: Fiyat=99, Miktar=25");
        System.out.println("📊 İlk Durum:");
        System.out.println("  Toplam SELL orderları: " + engine.getSellOrdersSize() + " adet");

        assertEquals(4, engine.getSellOrdersSize());

        // 📉 BUY001: Fiyat=100, Miktar=20
        Order buy1 = new Order.Builder(Side.BUY)
                .withId("BUY001")
                .withPrice(100)
                .withAmount(20)
                .withDate(new Date(System.currentTimeMillis() + 400))
                .build();

        List<Trade> trades1 = engine.process(buy1);
        System.out.println("✓ BUY001: Fiyat=100, Miktar=20");
        System.out.println("  → Oluşan trade sayısı: " + trades1.size());
        
        for (int i = 0; i < trades1.size(); i++) {
            Trade trade = trades1.get(i);
            System.out.println("    Trade " + (i+1) + ": " + trade.getAmount() + " miktar, " + 
                             trade.getPrice() + " fiyat, Satıcı: " + trade.getMakerOrderId());
        }
        
        // Beklenen: En düşük fiyattan başlayarak eşleşme (SELL004: 99 fiyat)
        assertEquals(1, trades1.size(), "1 trade oluşmalı");
        assertEquals(20, trades1.get(0).getAmount(), "Trade miktarı 20 olmalı");
        assertEquals(99, trades1.get(0).getPrice(), "En düşük fiyat (99) ile eşleşmeli");
        assertEquals("SELL004", trades1.get(0).getMakerOrderId(), "SELL004 ile eşleşmeli");

        // 📉 BUY002: Fiyat=99, Miktar=25
        Order buy2 = new Order.Builder(Side.BUY)
                .withId("BUY002")
                .withPrice(99)
                .withAmount(25)
                .withDate(new Date(System.currentTimeMillis() + 500))
                .build();

        List<Trade> trades2 = engine.process(buy2);
        System.out.println("✓ BUY002: Fiyat=99, Miktar=25");
        System.out.println("  → Oluşan trade sayısı: " + trades2.size());
        
        // SELL004'dan kalan 5 miktar ile eşleşmeli
        assertEquals(1, trades2.size(), "1 trade oluşmalı");
        assertEquals(5, trades2.get(0).getAmount(), "Kalan 5 miktar ile eşleşmeli");

        // 📉 BUY003: Fiyat=101, Miktar=80 (Çoklu eşleşme bekleniyor)
        Order buy3 = new Order.Builder(Side.BUY)
                .withId("BUY003")
                .withPrice(101)
                .withAmount(80)
                .withDate(new Date(System.currentTimeMillis() + 600))
                .build();

        List<Trade> trades3 = engine.process(buy3);
        System.out.println("✓ BUY003: Fiyat=101, Miktar=80 (Çoklu eşleşme bekleniyor)");
        System.out.println("  → Oluşan trade sayısı: " + trades3.size());
        
        for (int i = 0; i < trades3.size(); i++) {
            Trade trade = trades3.get(i);
            System.out.println("    Trade " + (i+1) + ": " + trade.getAmount() + " miktar, " + 
                             trade.getPrice() + " fiyat, Satıcı: " + trade.getMakerOrderId());
        }

        // 📉 BUY004: Fiyat=98, Miktar=15 (Eşleşme beklenmez)
        Order buy4 = new Order.Builder(Side.BUY)
                .withId("BUY004")
                .withPrice(98)
                .withAmount(15)
                .withDate(new Date(System.currentTimeMillis() + 700))
                .build();

        List<Trade> trades4 = engine.process(buy4);
        System.out.println("✓ BUY004: Fiyat=98, Miktar=15 (Eşleşme beklenmez)");
        System.out.println("  → Oluşan trade sayısı: " + trades4.size());
        assertEquals(0, trades4.size(), "Eşleşme olmamalı");

        System.out.println("📊 Final Durum:");
        System.out.println("  Kalan BUY orderları: " + engine.getBuyOrdersSize() + " adet");
        System.out.println("  Kalan SELL orderları: " + engine.getSellOrdersSize() + " adet");
        System.out.println("  📋 Toplam oluşan trade: " + engine.getTradesSize());
    }

    @Test
    @DisplayName("Test 2: Kapsamlı FIFO Önceliği Testi (Orijinal Senaryo)")
    void testOriginalFIFOPriority() {
        System.out.println("=== Test 2: Kapsamlı FIFO Önceliği Testi ===");
        
        // 📈 Aynı Fiyatta SELL Emirleri Ekleniyor (FIFO test için)
        long baseTime = System.currentTimeMillis();
        
        Order sellFirst = new Order.Builder(Side.SELL)
                .withId("SELL_FIRST")
                .withPrice(105)
                .withAmount(15)
                .withDate(new Date(baseTime))
                .build();
        
        Order sellSecond = new Order.Builder(Side.SELL)
                .withId("SELL_SECOND")
                .withPrice(105)
                .withAmount(25)
                .withDate(new Date(baseTime + 100))
                .build();
        
        Order sellThird = new Order.Builder(Side.SELL)
                .withId("SELL_THIRD")
                .withPrice(105)
                .withAmount(20)
                .withDate(new Date(baseTime + 200))
                .build();
        
        Order sellFourth = new Order.Builder(Side.SELL)
                .withId("SELL_FOURTH")
                .withPrice(105)
                .withAmount(30)
                .withDate(new Date(baseTime + 300))
                .build();
        
        Order sellFifth = new Order.Builder(Side.SELL)
                .withId("SELL_FIFTH")
                .withPrice(105)
                .withAmount(10)
                .withDate(new Date(baseTime + 400))
                .build();

        engine.process(sellFirst);
        engine.process(sellSecond);
        engine.process(sellThird);
        engine.process(sellFourth);
        engine.process(sellFifth);

        System.out.println("✓ 1. SELL_FIRST: Fiyat=105, Miktar=15 [İlk sırada]");
        System.out.println("✓ 2. SELL_SECOND: Fiyat=105, Miktar=25 [İkinci sırada]");
        System.out.println("✓ 3. SELL_THIRD: Fiyat=105, Miktar=20 [Üçüncü sırada]");
        System.out.println("✓ 4. SELL_FOURTH: Fiyat=105, Miktar=30 [Dördüncü sırada]");
        System.out.println("✓ 5. SELL_FIFTH: Fiyat=105, Miktar=10 [Beşinci sırada]");
        System.out.println("📊 SELL Emirleri Hazır:");
        System.out.println("  Beklenen FIFO sırası: FIRST(15) → SECOND(25) → THIRD(20) → FOURTH(30) → FIFTH(10)");

        // 📉 BUY_TEST1: Fiyat=105, Miktar=10
        Order buyTest1 = new Order.Builder(Side.BUY)
                .withId("BUY_TEST1")
                .withPrice(105)
                .withAmount(10)
                .withDate(new Date(baseTime + 500))
                .build();

        List<Trade> trades1 = engine.process(buyTest1);
        System.out.println("✓ BUY_TEST1: Fiyat=105, Miktar=10");
        if (!trades1.isEmpty()) {
            System.out.println("  → FIFO Test 1: Eşleşen=" + trades1.get(0).getMakerOrderId() + " (Beklenen: SELL_FIRST) " + 
                             (trades1.get(0).getMakerOrderId().equals("SELL_FIRST") ? "✅ DOĞRU" : "❌ YANLIŞ"));
            System.out.println("  → Trade miktarı: " + trades1.get(0).getAmount() + "/10");
        }
        
        assertEquals(1, trades1.size(), "1 trade oluşmalı");
        assertEquals("SELL_FIRST", trades1.get(0).getMakerOrderId(), "FIFO sırasına göre SELL_FIRST ile eşleşmeli");
        assertEquals(10, trades1.get(0).getAmount(), "10 miktar eşleşmeli");

        // 📉 BUY_TEST2: Fiyat=105, Miktar=20
        Order buyTest2 = new Order.Builder(Side.BUY)
                .withId("BUY_TEST2")
                .withPrice(105)
                .withAmount(20)
                .withDate(new Date(baseTime + 600))
                .build();

        List<Trade> trades2 = engine.process(buyTest2);
        System.out.println("✓ BUY_TEST2: Fiyat=105, Miktar=20");
        System.out.println("  → Oluşan trade sayısı: " + trades2.size());
        
        // SELL_FIRST'den kalan 5 + SELL_SECOND'dan 15 = 20 miktar
        assertEquals(2, trades2.size(), "2 trade oluşmalı");
        assertEquals("SELL_FIRST", trades2.get(0).getMakerOrderId(), "İlk trade SELL_FIRST ile olmalı");
        assertEquals(5, trades2.get(0).getAmount(), "SELL_FIRST'den kalan 5 miktar");
        assertEquals("SELL_SECOND", trades2.get(1).getMakerOrderId(), "İkinci trade SELL_SECOND ile olmalı");
        assertEquals(15, trades2.get(1).getAmount(), "SELL_SECOND'dan 15 miktar");

        System.out.println("📊 FIFO Test Sonucu:");
        System.out.println("  Kalan BUY orderları: " + engine.getBuyOrdersSize() + " adet");
        System.out.println("  Kalan SELL orderları: " + engine.getSellOrdersSize() + " adet");
        System.out.println("  📋 Toplam trade: " + engine.getTradesSize());
    }

    @Test
    @DisplayName("Test 3: Performans Testi - Yüksek Hacim Simülasyonu")
    void testHighVolumePerformance() {
        System.out.println("=== Test 5: Performans Testi - Yüksek Hacim ===");
        System.out.println("🚀 Yüksek hacimli emir işleme başlıyor...");
        
        long startTime = System.currentTimeMillis();
        
        // 📈 50 SELL Emri Ekleniyor
        for (int i = 1; i <= 50; i++) {
            Order sell = new Order.Builder(Side.SELL)
                    .withId("PERF_SELL_" + i)
                    .withPrice(95 + (i % 10)) // 95-104 arası fiyatlar
                    .withAmount(10 + (i % 20)) // 10-29 arası miktarlar
                    .withDate(new Date(System.currentTimeMillis() + i))
                    .build();
            engine.process(sell);
            
            if (i % 10 == 0) {
                System.out.println("  ✓ " + i + " SELL emri eklendi...");
            }
        }
        

        // 📉 30 BUY Emri Ekleniyor
        for (int i = 1; i <= 30; i++) {
            Order buy = new Order.Builder(Side.BUY)
                    .withId("PERF_BUY_" + i)
                    .withPrice(96 + (i % 8)) // 96-103 arası fiyatlar
                    .withAmount(15 + (i % 15)) // 15-29 arası miktarlar
                    .withDate(new Date(System.currentTimeMillis() + 1000 + i))
                    .build();
            List<Trade> trades = engine.process(buy);
            
            if (i % 5 == 0) {
                System.out.println("  ✓ " + i + " BUY emri eklendi... (Son emirde " + trades.size() + " trade)");
            }
        }
        
        // 🔥 20 Agresif BUY Emri
        System.out.println("🔥 20 Agresif BUY Emri:");
        for (int i = 1; i <= 20; i++) {
            Order buy = new Order.Builder(Side.BUY)
                    .withId("AGGRESSIVE_BUY_" + i)
                    .withPrice(110 + (i % 5)) // Yüksek fiyatlar
                    .withAmount(20 + (i % 10))
                    .withDate(new Date(System.currentTimeMillis() + 2000 + i))
                    .build();
            List<Trade> trades = engine.process(buy);
            
            if (i % 5 == 0) {
                System.out.println("  ✓ " + i + " Agresif BUY eklendi... (Son emirde " + trades.size() + " trade)");
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double ordersPerSecond = (100.0 / duration) * 1000;
        double tradesPerSecond = (engine.getTradesSize() / (double) duration) * 1000;
        double matchingRate = (engine.getTradesSize() / 100.0) * 100;
        
        System.out.println("⚡ Performans Test Sonuçları:");
        System.out.println("  📋 Toplam işlenen emir: 100");
        System.out.println("  🤝 Toplam gerçekleşen trade: " + engine.getTradesSize());
        System.out.println("  ⏱️  Toplam süre: " + duration + " ms");
        System.out.println("  🏃 Emir/saniye: " + String.format("%.2f", ordersPerSecond));
        System.out.println("  🔄 Trade/saniye: " + String.format("%.2f", tradesPerSecond));
        System.out.println("📊 Final Durum:");
        System.out.println("  📈 Eşleşme oranı: " + String.format("%.1f", matchingRate) + "%");
        
        assertTrue(duration < 5000, "100 emir 5 saniyeden az sürede işlenmeli");
        assertTrue(engine.getTradesSize() > 0, "En az bir trade oluşmalı");
        
        System.out.println("🎉 Performans testi başarıyla tamamlandı!");
    }
}
