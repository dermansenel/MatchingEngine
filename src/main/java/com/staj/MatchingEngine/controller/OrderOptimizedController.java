package com.staj.MatchingEngine.controller;

import com.staj.MatchingEngine.engine.OptimizedOrderMatch;
import com.staj.MatchingEngine.model.Order;
import com.staj.MatchingEngine.model.Side;
import com.staj.MatchingEngine.model.Trade;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/optimized")
public class OrderOptimizedController {

    private OptimizedOrderMatch engine = new OptimizedOrderMatch();
    private AtomicInteger buycounter = new AtomicInteger(1);
    private AtomicInteger sellcounter = new AtomicInteger(1);

    @PostMapping("/order")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> req) {
        String type = (String) req.get("type");
        int price = ((Number) req.get("price")).intValue(); 
        int amount = ((Number) req.get("amount")).intValue();

        String orderId = type.equals("BUY") ? 
            "BUY" + buycounter.getAndIncrement() : 
            "SELL" + sellcounter.getAndIncrement();

        Order order = new Order.Builder(Side.valueOf(type))
                .withId(orderId)
                .withPrice(price)
                .withAmount(amount)
                .build();

        List<Trade> trades = engine.process(order);

        // Minimal response
        Map<String, Object> result = new HashMap<>(3);
        result.put("orderId", orderId);
        result.put("matched", !trades.isEmpty());
        result.put("trades", trades.size());

        return result;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        // Ultra hızlı stats - direkt size çağrıları
        Map<String, Object> result = new HashMap<>(4);
        result.put("buyOrders", engine.getBuyOrdersSize());
        result.put("sellOrders", engine.getSellOrdersSize());
        result.put("totalTrades", engine.getTradesSize());
        result.put("totalProcessedOrders", engine.getOrderCounter());
        return result;
    }

    @GetMapping("/orders/buy")
    public List<Order> getBuyOrders() {
        return new ArrayList<>(engine.getBuyOrders());
    }

    @GetMapping("/orders/sell") 
    public List<Order> getSellOrders() {
        return new ArrayList<>(engine.getSellOrders());
    }

    @GetMapping("/trades")
    public List<Trade> getTrades() {
        return engine.getAllTrades();
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        engine.reset();
        buycounter.set(1);
        sellcounter.set(1);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Engine reset successfully");
        result.put("status", "success");
        return result;
    }
}
