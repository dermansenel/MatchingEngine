package com.staj.MatchingEngine;

import com.staj.MatchingEngine.engine.OrderMatch;
import com.staj.MatchingEngine.engine.OptimizedOrderMatch;
import com.staj.MatchingEngine.model.Order;
import com.staj.MatchingEngine.model.Side;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.Random;
import java.util.concurrent.*;

public class PerformanceTest {
    
    private OrderMatch normalEngine;
    private OptimizedOrderMatch optimizedEngine;
    private Random random;
    
    // Test parametreleri
    private static final int WARMUP_ORDERS = 1000;
    private static final int TEST_ORDERS = 5000;
    private static final int CONCURRENT_THREADS = 10;
    private static final int ORDERS_PER_THREAD = 500;
    
    @BeforeEach
    void setUp() {
        normalEngine = new OrderMatch();
        optimizedEngine = new OptimizedOrderMatch();
        random = new Random(42); // Deterministik sonuçlar için sabit seed
    }
    
    @Test
    @DisplayName("Temel Hız Karşılaştırması - Sequential Order Processing")
    void testBasicSpeedComparison() {
        System.out.println("\n=== TEMEİL HIZ KARŞILAŞTIRMASI ===");
        
        // Warmup
        warmupEngines();
        
        // Normal Engine Test
        long normalTime = measureEnginePerformance(normalEngine, TEST_ORDERS, "Normal Engine");
        
        // Reset for fair comparison
        normalEngine = new OrderMatch();
        
        // Optimized Engine Test  
        long optimizedTime = measureEnginePerformance(optimizedEngine, TEST_ORDERS, "Optimized Engine");
        
        // Sonuçları karşılaştır
        printComparisonResults(normalTime, optimizedTime, TEST_ORDERS);
    }
    
    @Test
    @DisplayName("Concurrent Processing Test - Multi-threaded Performance")
    void testConcurrentPerformance() throws InterruptedException {
        System.out.println("\n=== ÇOKLU İŞ PARÇACIĞI TESTİ ===");
        
        // Normal Engine Concurrent Test
        long normalConcurrentTime = measureConcurrentPerformance(normalEngine, "Normal Engine");
        
        // Reset engines
        normalEngine = new OrderMatch();
        optimizedEngine = new OptimizedOrderMatch();
        
        // Optimized Engine Concurrent Test
        long optimizedConcurrentTime = measureConcurrentPerformance(optimizedEngine, "Optimized Engine");
        
        // Sonuçları karşılaştır
        int totalOrders = CONCURRENT_THREADS * ORDERS_PER_THREAD;
        printComparisonResults(normalConcurrentTime, optimizedConcurrentTime, totalOrders);
    }
    
    @Test
    @DisplayName("Memory Usage and Trade Generation Efficiency")
    void testMemoryAndTradeEfficiency() {
        System.out.println("\n=== BELLEK VE TİCARET VERİMLİLİĞİ ===");
        
        // Memory usage before
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Garbage collection
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Generate many trades
        generateBalancedOrders(normalEngine, 2000);
        
        runtime.gc();
        long memoryAfterNormal = runtime.totalMemory() - runtime.freeMemory();
        
        // Reset and test optimized
        optimizedEngine = new OptimizedOrderMatch();
        runtime.gc();
        long memoryBeforeOptimized = runtime.totalMemory() - runtime.freeMemory();
        
        generateBalancedOrders(optimizedEngine, 2000);
        
        runtime.gc();
        long memoryAfterOptimized = runtime.totalMemory() - runtime.freeMemory();
        
        // Sonuçları yazdır
        System.out.println("Normal Engine Memory Usage: " + 
            formatBytes(memoryAfterNormal - memoryBefore));
        System.out.println("Optimized Engine Memory Usage: " + 
            formatBytes(memoryAfterOptimized - memoryBeforeOptimized));
        
        System.out.println("\nTrade Generation Results:");
        System.out.println("Normal Engine - Total Trades: " + normalEngine.getAllTrades().size());
        System.out.println("Optimized Engine - Total Trades: " + optimizedEngine.getAllTrades().size());
    }
    
    @Test
    @DisplayName("Scalability Test - Large Order Book Performance")
    void testScalabilityWithLargeOrderBook() {
        System.out.println("\n=== ÖLÇEKLENEBİLİRLİK TESTİ ===");
        
        int[] testSizes = {500,1000, 5000};
        
        for (int orderCount : testSizes) {
            System.out.println("\n--- " + orderCount + " Emir Testi ---");
            
            // Reset engines
            normalEngine = new OrderMatch();
            optimizedEngine = new OptimizedOrderMatch();
            
            long normalTime = measureEnginePerformance(normalEngine, orderCount, "Normal");
            long optimizedTime = measureEnginePerformance(optimizedEngine, orderCount, "Optimized");
            
            double improvement = ((double)(normalTime - optimizedTime) / normalTime) * 100;
            System.out.printf("Emir Sayısı: %d, İyileştirme: %.2f%%\n", orderCount, improvement);
        }
    }
    
    // Helper Methods
    
    private void warmupEngines() {
        System.out.println("Warming up engines...");
        generateRandomOrders(normalEngine, WARMUP_ORDERS);
        generateRandomOrders(optimizedEngine, WARMUP_ORDERS);
    }
    
    private long measureEnginePerformance(Object engine, int orderCount, String engineName) {
        System.out.println(engineName + " testi başlıyor... (" + orderCount + " emir)");
        
        long startTime = System.nanoTime();
        
        if (engine instanceof OrderMatch) {
            generateRandomOrders((OrderMatch) engine, orderCount);
        } else if (engine instanceof OptimizedOrderMatch) {
            generateRandomOrders((OptimizedOrderMatch) engine, orderCount);
        }
        
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        System.out.printf("%s - Süre: %d ms, Ortalama: %.2f ms/emir\n", 
            engineName, duration, (double)duration / orderCount);
        
        return duration;
    }
    
    private long measureConcurrentPerformance(Object engine, String engineName) throws InterruptedException {
        System.out.println(engineName + " concurrent testi başlıyor...");
        
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    Random threadRandom = new Random(42 + threadId);
                    
                    for (int j = 0; j < ORDERS_PER_THREAD; j++) {
                        Order order = createRandomOrder(threadRandom);
                        
                        if (engine instanceof OrderMatch) {
                            ((OrderMatch) engine).process(order);
                        } else if (engine instanceof OptimizedOrderMatch) {
                            ((OptimizedOrderMatch) engine).process(order);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;
        
        executor.shutdown();
        
        int totalOrders = CONCURRENT_THREADS * ORDERS_PER_THREAD;
        System.out.printf("%s Concurrent - Süre: %d ms, Toplam Emir: %d, Ortalama: %.2f ms/emir\n", 
            engineName, duration, totalOrders, (double)duration / totalOrders);
        
        return duration;
    }
    
    private void generateRandomOrders(OrderMatch engine, int count) {
        for (int i = 0; i < count; i++) {
            Order order = createRandomOrder(random);
            engine.process(order);
        }
    }
    
    private void generateRandomOrders(OptimizedOrderMatch engine, int count) {
        for (int i = 0; i < count; i++) {
            Order order = createRandomOrder(random);
            engine.process(order);
        }
    }
    
    private void generateBalancedOrders(Object engine, int count) {
        // Balanced orders to generate more trades
        for (int i = 0; i < count; i++) {
            Side side = (i % 2 == 0) ? Side.BUY : Side.SELL;
            int basePrice = 100;
            int price = side == Side.BUY ? 
                basePrice + random.nextInt(10) : 
                basePrice - random.nextInt(10);
            
            Order order = new Order.Builder(side)
                .withAmount(random.nextInt(90) + 10)
                .withPrice(Math.max(price, 1))
                .withId("TEST_" + i)
                .withDate(new java.sql.Date(System.currentTimeMillis()))
                .build();
            
            if (engine instanceof OrderMatch) {
                ((OrderMatch) engine).process(order);
            } else if (engine instanceof OptimizedOrderMatch) {
                ((OptimizedOrderMatch) engine).process(order);
            }
        }
    }
    
    private Order createRandomOrder(Random rand) {
        Side side = rand.nextBoolean() ? Side.BUY : Side.SELL;
        return new Order.Builder(side)
            .withAmount(rand.nextInt(91) + 10) // 10-100 arası
            .withPrice(rand.nextInt(21) + 90) // 90-110 arası
            .withId("RAND_" + rand.nextInt(100000))
            .withDate(new java.sql.Date(System.currentTimeMillis()))
            .build();
    }
    
    private void printComparisonResults(long normalTime, long optimizedTime, int orderCount) {
        System.out.println("\n=== KARŞILAŞTIRMA SONUÇLARI ===");
        System.out.printf("Normal Engine: %d ms\n", normalTime);
        System.out.printf("Optimized Engine: %d ms\n", optimizedTime);
        
        if (optimizedTime < normalTime) {
            double improvement = ((double)(normalTime - optimizedTime) / normalTime) * 100;
            System.out.printf("✅ Optimized engine %.2f%% daha hızlı!\n", improvement);
            System.out.printf("Hız artışı: %.2fx\n", (double)normalTime / optimizedTime);
        } else {
            System.out.println("❌ Normal engine daha hızlı");
        }
        
        System.out.printf("İşlenen toplam emir: %d\n", orderCount);
        System.out.printf("Normal Engine - Ortalama: %.2f ms/emir\n", (double)normalTime / orderCount);
        System.out.printf("Optimized Engine - Ortalama: %.2f ms/emir\n", (double)optimizedTime / orderCount);
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        else return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
