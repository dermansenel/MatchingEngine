package com.staj.MatchingEngine.fix;

import quickfix.Message;
import quickfix.SessionID;
import quickfix.fix44.NewOrderSingle;
import quickfix.field.ClOrdID;
import quickfix.field.Symbol;
import quickfix.field.Side;
import quickfix.field.Quantity;
import quickfix.field.Price;
import quickfix.field.MsgType;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;
import quickfix.field.SendingTime;
import com.staj.MatchingEngine.engine.OptimizedOrderMatch;

public class fixTest {

    public static void main(String[] args) throws Exception {

        fixApp app = new fixApp();
        OptimizedOrderMatch engine = new OptimizedOrderMatch();
        app.setEngine(engine);
        // Mesajları fixApp'in fromApp metoduna direkt yolluyoruz
        SessionID sessionID = new SessionID("FIX", "send", "recive");
        // İlk mesajı oluştur
        Message message = new Message();
        message.setField(new ClOrdID("12345"));
        message.setField(new Symbol("fixbuy"));
        message.setField(new Side(Side.BUY));
        message.setField(new Quantity(100));
        message.setField(new Price(50));
        message.getHeader().setField(new MsgType("D"));
        message.getHeader().setField(new BeginString("FIX"));
        message.getHeader().setField(new SenderCompID("Derman"));
        message.getHeader().setField(new TargetCompID("Engine"));
        message.getHeader().setField(new SendingTime());

        Message message2 = new Message();
        message2.setField(new ClOrdID("2131"));
        message2.setField(new Symbol("fixsell"));
        message2.setField(new Side(Side.SELL));
        message2.setField(new Quantity(150));
        message2.setField(new Price(50));
        message2.getHeader().setField(new MsgType("D"));
        message2.getHeader().setField(new BeginString("FIX"));
        message2.getHeader().setField(new SenderCompID("Derman"));
        message2.getHeader().setField(new TargetCompID("Engine"));
        message2.getHeader().setField(new SendingTime());

        

        // İlk mesajı işle (BUY)
        app.fromApp(message, sessionID);

        // İkinci mesajı işle (SELL)
        app.fromApp(message2, sessionID);

        // Mesajları ekrana yazdır
        System.out.println("FIX Mesajı 1: " + message);
        System.out.println("FIX Mesajı 2: " + message2);

        System.out.println("Test başarılı, mesaj işleme tamamlandı.");
        System.out.println("Buy Orders Size: " + engine.getBuyOrders().size());
        System.out.println("Sell Orders Size: " + engine.getSellOrders().size());
        System.out.println("All Trades Size: " + engine.getAllTrades().size());

        NewOrderSingle buyOrder = new NewOrderSingle();
        buyOrder.setField(new ClOrdID("12345"));
        buyOrder.setField(new Symbol("fixbuy"));
        buyOrder.setField(new Side(Side.BUY));
        buyOrder.setField(new Quantity(100));
        buyOrder.setField(new Price(50));

        NewOrderSingle sellOrder = new NewOrderSingle();
        sellOrder.setField(new ClOrdID("2131"));
        sellOrder.setField(new Symbol("fixsell"));
        sellOrder.setField(new Side(Side.SELL));
        sellOrder.setField(new Quantity(150));
        sellOrder.setField(new Price(50));
        
        // SessionID'yi tekrar tanımla
        SessionID sessionID2 = new SessionID("FIX", "send", "recive");
        app.fromApp(buyOrder, sessionID2);
        app.fromApp(sellOrder, sessionID2);
        System.out.println("FIX Mesajı 1: " + buyOrder);
        System.out.println("FIX Mesajı 2: " + sellOrder);

        System.out.println("Test başarılı, mesaj işleme tamamlandı.");
        System.out.println("Buy Orders Size: " + engine.getBuyOrders().size());
        System.out.println("Sell Orders Size: " + engine.getSellOrders().size());
        System.out.println("All Trades Size: " + engine.getAllTrades().size());

    }
}
