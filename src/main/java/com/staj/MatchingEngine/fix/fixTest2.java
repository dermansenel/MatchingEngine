package com.staj.MatchingEngine.fix;

import quickfix.Message;
import quickfix.SessionID;
import quickfix.fix44.NewOrderSingle;
import quickfix.field.*;
import com.staj.MatchingEngine.engine.OptimizedOrderMatch;

public class fixTest2 {

    public static void main(String[] args) throws Exception {
        
        // Test ortamını hazırla
        fixApp app = new fixApp();
        OptimizedOrderMatch engine = new OptimizedOrderMatch();
        app.setEngine(engine);
        SessionID sessionID = new SessionID("FIX.4.4", "SENDER", "TARGET");
               
        
        // Test 1: Düz Message ile test
        System.out.println("--- Test 1: Düz Message ile Eşleme ---");
        testWithMessage(app, engine, sessionID);
        
        // Engine'i temizle
        engine = new OptimizedOrderMatch();
        app.setEngine(engine);
        
        System.out.println("\n--- Test 2: NewOrderSingle ile Eşleme ---");
        testWithNewOrderSingle(app, engine, sessionID);
    }
    
    private static void testWithMessage(fixApp app, OptimizedOrderMatch engine, SessionID sessionID) throws Exception {
        System.out.println("Başlangıç durumu:");
        printEngineStatus(engine);
        
        // BUY emri oluştur
        Message buyMessage = new Message();
        buyMessage.setField(new ClOrdID("1111"));
        buyMessage.setField(new Symbol("FİXBUY"));
        buyMessage.setField(new Side(Side.BUY));
        buyMessage.setField(new Quantity(100));
        buyMessage.setField(new Price(150.0));
        buyMessage.getHeader().setField(new MsgType("D"));
        buyMessage.getHeader().setField(new BeginString("FIX.4.4"));
        buyMessage.getHeader().setField(new SenderCompID("SENDER"));
        buyMessage.getHeader().setField(new TargetCompID("TARGET"));
        buyMessage.getHeader().setField(new SendingTime());
        
        // SELL emri oluştur
        Message sellMessage = new Message();
        sellMessage.setField(new ClOrdID("1111"));
        sellMessage.setField(new Symbol("FİXSELL"));
        sellMessage.setField(new Side(Side.SELL));
        sellMessage.setField(new Quantity(100));
        sellMessage.setField(new Price(150.0));
        sellMessage.getHeader().setField(new MsgType("D"));
        sellMessage.getHeader().setField(new BeginString("FIX.4.4"));
        sellMessage.getHeader().setField(new SenderCompID("SENDER"));
        sellMessage.getHeader().setField(new TargetCompID("TARGET"));
        sellMessage.getHeader().setField(new SendingTime());
        
        // Emirleri işle
        SessionID sessionID2 = new SessionID("FIX", "send", "receive");
        System.out.println("BUY emri gönderiliyor...");
        app.fromApp(buyMessage, sessionID2);
        System.out.println("FIX Mesajı 1: " + buyMessage);
        printEngineStatus(engine);
        
        System.out.println("SELL emri gönderiliyor...");
        app.fromApp(sellMessage, sessionID2);
        System.out.println("FIX Mesajı 2: " + sellMessage);
        printEngineStatus(engine);
        
        System.out.println("Buy Orders Size: " + engine.getBuyOrders().size());
        System.out.println("Sell Orders Size: " + engine.getSellOrders().size());
        System.out.println("All Trades Size: " + engine.getAllTrades().size());
    }
    
    private static void testWithNewOrderSingle(fixApp app, OptimizedOrderMatch engine, SessionID sessionID) throws Exception {
        System.out.println("Başlangıç durumu:");
        printEngineStatus(engine);
        
        // BUY emri oluştur
        NewOrderSingle buyOrder = new NewOrderSingle();
        buyOrder.setField(new ClOrdID("BUY002"));
        buyOrder.setField(new Symbol("MSFT"));
        buyOrder.setField(new Side(Side.BUY));
        buyOrder.setField(new Quantity(200));
        buyOrder.setField(new Price(300.0));
        
        // SELL emri oluştur
        NewOrderSingle sellOrder = new NewOrderSingle();
        sellOrder.setField(new ClOrdID("SELL002"));
        sellOrder.setField(new Symbol("MSFT"));
        sellOrder.setField(new Side(Side.SELL));
        sellOrder.setField(new Quantity(200));
        sellOrder.setField(new Price(300.0));
        
        // Emirleri işle
        SessionID sessionID2 = new SessionID("FIX", "send", "receive");
        System.out.println("BUY emri gönderiliyor...");
        app.fromApp(buyOrder, sessionID2);
        System.out.println("FIX Mesajı 1: " + buyOrder);
        printEngineStatus(engine);
        
        System.out.println("SELL emri gönderiliyor...");
        app.fromApp(sellOrder, sessionID2);
        System.out.println("FIX Mesajı 2: " + sellOrder);
        printEngineStatus(engine);
        
        System.out.println("Test başarılı, mesaj işleme tamamlandı.");
        System.out.println("Buy Orders Size: " + engine.getBuyOrders().size());
        System.out.println("Sell Orders Size: " + engine.getSellOrders().size());
        System.out.println("All Trades Size: " + engine.getAllTrades().size());
    }
    
    private static void printEngineStatus(OptimizedOrderMatch engine) {
        System.out.println("  -> Buy Orders: " + engine.getBuyOrders().size());
        System.out.println("  -> Sell Orders: " + engine.getSellOrders().size());
        System.out.println("  -> Trades: " + engine.getAllTrades().size());
        if (engine.getAllTrades().size() > 0) {
            System.out.println("  -> EŞLEME BAŞARILI! ✓");
        }
        System.out.println();
    }
}
