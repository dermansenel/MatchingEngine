package com.staj.MatchingEngine;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    private OrderMatch engine = new OrderMatch();
    private int buycounter = 1;
    private int sellcounter = 1;

    @PostMapping("/order")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> req) {
        String type = (String) req.get("type");
        int price = Integer.parseInt(req.get("price").toString());
        int amount = Integer.parseInt(req.get("amount").toString());

        String orderId;
        if ("BUY".equals(type)) {
            orderId = "BUY" + buycounter++;
        } else {
            orderId = "SELL" + sellcounter++;
        }

        Order order = new Order.Builder(Side.valueOf(type))
                .withId(orderId)
                .withPrice(price)
                .withAmount(amount)
                .build();

        List<Trade> trades = engine.process(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("type", type);
        result.put("price", price);
        result.put("amount", amount);
        result.put("status", "created");
        if (!trades.isEmpty()) {
            result.put("matched", true);
            result.put("trades", trades.size());
        }

        return result;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("buyOrders", engine.getBuyOrders().size());
        stats.put("sellOrders", engine.getSellOrders().size());
        stats.put("totalTrades", engine.getAllTrades().size());
        stats.put("totalProcessedOrders", engine.getTotalProcessedOrders());

        // Eşleşen işlemler detayı
        List<Map<String, Object>> matchedTrades = new ArrayList<>();
        int tradeNumber = 1;

        for (Trade trade : engine.getAllTrades()) {
            Map<String, Object> tradeInfo = new LinkedHashMap<>();
            tradeInfo.put("takerOrderId", trade.getTakerOrderId());
            tradeInfo.put("makerOrderId", trade.getMakerOrderId());
            tradeInfo.put("amount", trade.getAmount());
            tradeInfo.put("tradePrice", trade.getPrice());
            tradeInfo.put("takerOriginalPrice", trade.getTakerOrderPrice());
            tradeInfo.put("makerOriginalPrice", trade.getMakerOrderPrice());
            tradeInfo.put("totalValue", trade.getPrice() * trade.getAmount());

            matchedTrades.add(tradeInfo);
        }

        stats.put("matchedTrades", matchedTrades);

        return stats;
    }

    @GetMapping("/trades")
    public List<Map<String, Object>> getTrades() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Trade trade : engine.getAllTrades()) {
            Map<String, Object> info = new LinkedHashMap<>();

            // İstediğin sırayla ekleme
            info.put("amount", trade.getAmount());
            info.put("trade-price", trade.getPrice());
            info.put("value", trade.getPrice() * trade.getAmount());
            info.put("makerOrderId", trade.getMakerOrderId());
            info.put("makerPrice", trade.getMakerOrderPrice());
            info.put("takerOrderId",trade.getTakerOrderId());
            info.put("takerOrderPrice", trade.getTakerOrderPrice());

            result.add(info);
        }

        return result;
    }

    @GetMapping("/buy-orders")
    public List<Map<String, Object>> getBuyOrders() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Order order : engine.getBuyOrders()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("orderId", order.getId());
            info.put("price", order.getPrice());
            info.put("amount", order.getAmount());
            info.put("type", "BUY");
            result.add(info);
        }

        return result;
    }

    @GetMapping("/sell-orders")
    public List<Map<String, Object>> getSellOrders() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Order order : engine.getSellOrders()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("orderId", order.getId());
            info.put("price", order.getPrice());
            info.put("amount", order.getAmount());
            info.put("type", "SELL");
            result.add(info);
        }

        return result;
    }

    @DeleteMapping("/reset")
    public Map<String, Object> reset() {
        engine = new OrderMatch();
        buycounter = 1;
        sellcounter = 1;

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Reset completed");
        return response;
    }
}
