package com.staj.MatchingEngine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.staj.MatchingEngine.engine.OptimizedOrderMatch;
import com.staj.MatchingEngine.model.Order;
import com.staj.MatchingEngine.model.Side;

@SpringBootApplication
public class MatchingEngineApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MatchingEngineApplication.class, args);
        
        // Run tests after application startup
        runMatchingEngineTests();
    }
    
    private static void runMatchingEngineTests() {
        testBasicMatching();
        testPartialMatching();
        testMultipleMatching();
        testFIFOOrdering();
        testNoMatching();
    }
    
    private static void testBasicMatching() {
        OptimizedOrderMatch engine = new OptimizedOrderMatch();
        
        Order sellOrder = new Order.Builder(Side.SELL)
            .withId("SELL_001")
            .withPrice(100)
            .withAmount(50)
            .build();
            
        Order buyOrder = new Order.Builder(Side.BUY)
            .withId("BUY_001")
            .withPrice(100)
            .withAmount(50)
            .build();
            
        engine.process(sellOrder);
        var trades = engine.process(buyOrder);
        
        assert trades.size() == 1 : "Basic matching failed";
        assert trades.get(0).getAmount() == 50 : "Trade quantity mismatch";
    }
    
    private static void testPartialMatching() {
        OptimizedOrderMatch engine = new OptimizedOrderMatch();
        
        Order sellOrder = new Order.Builder(Side.SELL)
            .withId("SELL_002")
            .withPrice(105)
            .withAmount(100)
            .build();
            
        Order buyOrder = new Order.Builder(Side.BUY)
            .withId("BUY_002")
            .withPrice(105)
            .withAmount(30)
            .build();
            
        engine.process(sellOrder);
        var trades = engine.process(buyOrder);
        
        assert trades.size() == 1 : "Partial matching failed";
        assert trades.get(0).getAmount() == 30 : "Partial trade quantity incorrect";
        assert engine.getSellOrders().size() == 1 : "Remaining sell order not found";
    }
    
    private static void testMultipleMatching() {
        OptimizedOrderMatch engine = new OptimizedOrderMatch();
        
        engine.process(new Order.Builder(Side.SELL)
            .withId("SELL_003")
            .withPrice(95)
            .withAmount(20)
            .build());
            
        engine.process(new Order.Builder(Side.SELL)
            .withId("SELL_004")
            .withPrice(96)
            .withAmount(30)
            .build());
            
        Order buyOrder = new Order.Builder(Side.BUY)
            .withId("BUY_003")
            .withPrice(100)
            .withAmount(40)
            .build();
            
        var trades = engine.process(buyOrder);
        
        assert trades.size() == 2 : "Multiple matching failed";
        assert trades.get(0).getPrice() == 95 : "Best price not matched first";
    }
    
    private static void testFIFOOrdering() {
        OptimizedOrderMatch engine = new OptimizedOrderMatch();
        
        engine.process(new Order.Builder(Side.SELL)
            .withId("SELL_FIRST")
            .withPrice(110)
            .withAmount(25)
            .build());
            
        engine.process(new Order.Builder(Side.SELL)
            .withId("SELL_SECOND")
            .withPrice(110)
            .withAmount(25)
            .build());
            
        Order buyOrder = new Order.Builder(Side.BUY)
            .withId("BUY_FIFO")
            .withPrice(110)
            .withAmount(20)
            .build();
            
        var trades = engine.process(buyOrder);
        
        assert trades.size() == 1 : "FIFO test failed";
        assert trades.get(0).getMakerOrderId().equals("SELL_FIRST") : "FIFO order not maintained";
    }
    
    private static void testNoMatching() {
        OptimizedOrderMatch engine = new OptimizedOrderMatch();
        
        Order sellOrder = new Order.Builder(Side.SELL)
            .withId("SELL_005")
            .withPrice(120)
            .withAmount(25)
            .build();
            
        Order buyOrder = new Order.Builder(Side.BUY)
            .withId("BUY_005")
            .withPrice(110)
            .withAmount(25)
            .build();
            
        engine.process(sellOrder);
        var trades = engine.process(buyOrder);
        
        assert trades.isEmpty() : "No matching test failed - trades should be empty";
        assert engine.getBuyOrders().size() == 1 : "Buy order should remain";
        assert engine.getSellOrders().size() == 1 : "Sell order should remain";
    }
}
