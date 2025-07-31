package com.staj.MatchingEngine;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    private OrderMatch testEngine;
    private List<String> testLogs;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private DateTimeFormatter logTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    @GetMapping("/run-scenarios")
    public Map<String, Object> runTestScenarios() {
        testEngine = new OrderMatch();
        testLogs = new ArrayList<>();
        
        LocalDateTime startTime = LocalDateTime.now();
        addLog("===== EMİR EŞLEŞTİRME SİSTEMİ TESTİ =====");
        addLog("Test başlangıç zamanı: " + startTime.format(timeFormatter));
        
        // Test 1: Basit eşleşme
        runScenario1();
        
        // Test 2: Kısmi eşleşme
        runScenario2();
        
        // Test 3: Fiyat önceliği
        runScenario3();
        
        // Test 4: Zaman önceliği (FIFO)
        runScenario4();
        
        // Test 5: Karışık emirler
        runScenario5();
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("testStartTime", startTime.format(timeFormatter));
        result.put("totalScenarios", 5);
        result.put("totalTrades", testEngine.getAllTrades().size());
        result.put("logs", testLogs);
        result.put("finalStats", getFinalStats());
        
        return result;
    }
    
    private void runScenario1() {
        addLog("TEST 1: Basit");
        
        // Alış emri
        Order buyOrder = new Order.Builder(Side.BUY)
            .withId("B001")
            .withPrice(100)
            .withAmount(10)
            .build();
        
        logOrderAdd(buyOrder, "Alış emri ekleniyor");
        testEngine.process(buyOrder);
        
        // Satış emri
        Order sellOrder = new Order.Builder(Side.SELL)
            .withId("S001")
            .withPrice(90)
            .withAmount(5)
            .build();
        
        logOrderProcess(sellOrder, "Satış emri işleniyor");
        List<Trade> trades = testEngine.process(sellOrder);
        
        logTrades(trades);
        addLog("");
    }
    
    private void runScenario2() {
        addLog("===== SENARYO 2: KISMİ EŞLEŞME =====");
        
        Order sellOrder = new Order.Builder(Side.SELL)
            .withId("S002")
            .withPrice(95)
            .withAmount(8)
            .build();
        
        logOrderProcess(sellOrder, "Satış emri işleniyor");
        List<Trade> trades = testEngine.process(sellOrder);
        
        logTrades(trades);
        addLog("");
    }
    
    private void runScenario3() {
        addLog("===== SENARYO 3: FİYAT ÖNCELİĞİ =====");
        
        // Düşük fiyatlı alış
        Order lowBuy = new Order.Builder(Side.BUY)
            .withId("B002")
            .withPrice(98)
            .withAmount(5)
            .build();
        
        logOrderAdd(lowBuy, "Düşük fiyatlı alış emri ekleniyor");
        testEngine.process(lowBuy);
        
        // Yüksek fiyatlı alış
        Order highBuy = new Order.Builder(Side.BUY)
            .withId("B003")
            .withPrice(102)
            .withAmount(5)
            .build();
        
        logOrderAdd(highBuy, "Yüksek fiyatlı alış emri ekleniyor");
        testEngine.process(highBuy);
        
        // Satış emri
        Order sellOrder = new Order.Builder(Side.SELL)
            .withId("S003")
            .withPrice(97)
            .withAmount(3)
            .build();
        
        logOrderProcess(sellOrder, "Satış emri işleniyor");
        List<Trade> trades = testEngine.process(sellOrder);
        
        logTrades(trades);
        addLog("");
    }
    
    private void runScenario4() {
        addLog("===== SENARYO 4: ZAMAN ÖNCELİĞİ (FIFO) =====");
        
        // İlk emir
        Order firstOrder = new Order.Builder(Side.BUY)
            .withId("B004")
            .withPrice(96)
            .withAmount(10)
            .build();
        
        logOrderAdd(firstOrder, "İlk emir ekleniyor");
        testEngine.process(firstOrder);
        
        // İkinci emir (aynı fiyat)
        Order secondOrder = new Order.Builder(Side.BUY)
            .withId("B005")
            .withPrice(96)
            .withAmount(10)
            .build();
        
        logOrderAdd(secondOrder, "İkinci emir (aynı fiyat) ekleniyor");
        testEngine.process(secondOrder);
        
        // Satış emri
        Order sellOrder = new Order.Builder(Side.SELL)
            .withId("S004")
            .withPrice(95)
            .withAmount(15)
            .build();
        
        logOrderProcess(sellOrder, "Satış emri işleniyor");
        List<Trade> trades = testEngine.process(sellOrder);
        
        logTrades(trades);
        addLog("");
    }
    
    private void runScenario5() {
        addLog("===== SENARYO 5: KARIŞIK EMİRLER VE İPTAL =====");
        addLog("Alış emirleri ekleniyor");
        
        // Birkaç alış emri ekle
        Order buyOrder1 = new Order.Builder(Side.BUY)
            .withId("B006")
            .withPrice(105)
            .withAmount(5)
            .build();
        
        testEngine.process(buyOrder1);
        
        Order buyOrder2 = new Order.Builder(Side.BUY)
            .withId("B007")
            .withPrice(103)
            .withAmount(3)
            .build();
        
        testEngine.process(buyOrder2);
        
        addLog("B006 ID'li emir iptal ediliyor");
        
        // Satış emri
        Order sellOrder = new Order.Builder(Side.SELL)
            .withId("S005")
            .withPrice(102)
            .withAmount(2)
            .build();
        
        logOrderProcess(sellOrder, "Satış emri işleniyor");
        List<Trade> trades = testEngine.process(sellOrder);
        
        logTrades(trades);
        addLog("");
    }
    
    private void logOrderAdd(Order order, String message) {
        String timestamp = LocalDateTime.now().format(logTimeFormatter);
        String log = String.format("[%s] %s: Emir[ID=%s, %s, Fiyat=%d, Miktar=%d, Zaman=%s]", 
            timestamp, message, order.getId(), order.getSide(), 
            order.getPrice(), order.getAmount(), timestamp);
        addLog(log);
    }
    
    private void logOrderProcess(Order order, String message) {
        String timestamp = LocalDateTime.now().format(logTimeFormatter);
        String log = String.format("[%s] %s: Emir[ID=%s, %s, Fiyat=%d, Miktar=%d, Zaman=%s]", 
            timestamp, message, order.getId(), order.getSide(), 
            order.getPrice(), order.getAmount(), timestamp);
        addLog(log);
    }
    
    private void logTrades(List<Trade> trades) {
        if (trades.isEmpty()) {
            addLog("Gerçekleşen İşlemler: Yok");
        } else {
            addLog("Gerçekleşen İşlemler:");
            for (Trade trade : trades) {
                String tradeLog = String.format("  - İşlem[Taker=%s, Maker=%s, Miktar=%d, Fiyat=%d]",
                    trade.getTakerOrderId(), trade.getMakerOrderId(), 
                    trade.getAmount(), trade.getPrice());
                addLog(tradeLog);
            }
        }
    }
    
    private void addLog(String message) {
        testLogs.add(message);
    }
    
    private Map<String, Object> getFinalStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("buyOrders", testEngine.getBuyOrders().size());
        stats.put("sellOrders", testEngine.getSellOrders().size());
        stats.put("totalTrades", testEngine.getAllTrades().size());
        
        List<Map<String, Object>> tradeDetails = new ArrayList<>();
        for (Trade trade : testEngine.getAllTrades()) {
            Map<String, Object> tradeInfo = new LinkedHashMap<>();
            tradeInfo.put("taker", trade.getTakerOrderId());
            tradeInfo.put("maker", trade.getMakerOrderId());
            tradeInfo.put("amount", trade.getAmount());
            tradeInfo.put("price", trade.getPrice());
            tradeInfo.put("takerPrice", trade.getTakerOrderPrice());
            tradeInfo.put("makerPrice", trade.getMakerOrderPrice());
            tradeDetails.add(tradeInfo);
        }
        stats.put("tradeDetails", tradeDetails);
        
        return stats;
    }
    
    @GetMapping("/scenario/{scenarioId}")
    public Map<String, Object> runSingleScenario(@PathVariable int scenarioId) {
        testEngine = new OrderMatch();
        testLogs = new ArrayList<>();
        
        LocalDateTime startTime = LocalDateTime.now();
        addLog("===== TEK SENARYO TESTİ =====");
        addLog("Test başlangıç zamanı: " + startTime.format(timeFormatter));
        
        switch (scenarioId) {
            case 1: runScenario1(); break;
            case 2: runScenario2(); break;
            case 3: runScenario3(); break;
            case 4: runScenario4(); break;
            case 5: runScenario5(); break;
            default: addLog("Geçersiz senaryo ID: " + scenarioId);
        }
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scenarioId", scenarioId);
        result.put("testStartTime", startTime.format(timeFormatter));
        result.put("logs", testLogs);
        result.put("stats", getFinalStats());
        
        return result;
    }
    
    @GetMapping("/custom")
    public Map<String, Object> runCustomTest(@RequestParam String orders) {
        testEngine = new OrderMatch();
        testLogs = new ArrayList<>();
        
        LocalDateTime startTime = LocalDateTime.now();
        addLog("===== ÖZEL TEST =====");
        addLog("Test başlangıç zamanı: " + startTime.format(timeFormatter));
        
        // orders parametresi: "BUY,100,10;SELL,90,5;BUY,95,3" formatında
        String[] orderStrings = orders.split(";");
        for (String orderStr : orderStrings) {
            String[] parts = orderStr.trim().split(",");
            if (parts.length == 3) {
                Side side = Side.valueOf(parts[0].trim());
                int price = Integer.parseInt(parts[1].trim());
                int amount = Integer.parseInt(parts[2].trim());
                
                String orderId = side == Side.BUY ? "B" + System.currentTimeMillis() : "S" + System.currentTimeMillis();
                
                Order order = new Order.Builder(side)
                    .withId(orderId)
                    .withPrice(price)
                    .withAmount(amount)
                    .build();
                
                logOrderProcess(order, "Emir işleniyor");
                List<Trade> trades = testEngine.process(order);
                logTrades(trades);
                addLog("");
            }
        }
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("testStartTime", startTime.format(timeFormatter));
        result.put("customOrders", orders);
        result.put("logs", testLogs);
        result.put("stats", getFinalStats());
        
        return result;
    }
}
