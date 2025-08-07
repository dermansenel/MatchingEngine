package com.staj.MatchingEngine.fix;

import com.staj.MatchingEngine.engine.OptimizedOrderMatch;
import com.staj.MatchingEngine.model.Order;
import com.staj.MatchingEngine.model.Side;

import quickfix.*;
import quickfix.fix44.NewOrderSingle;

//quickfix app ını uygulamak için aplication adapter sınıfını extend ediyoruz
public class fixApp extends MessageCracker implements Application {

    private OptimizedOrderMatch engine;
    private int buyCount = 0;
    private int sellCount = 0;

    public void setEngine(OptimizedOrderMatch engine) {
        this.engine = engine;
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) {
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) {
        // her gelen uygulama mesajı için otomatik olarak çağrılır
        try {
            // MessageCracker ile mesajı çözme. Henüz yapılmadı.
            crack(message, sessionId);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void onCreate(SessionID sessionId) {
    }

    @Override
    public void onLogon(SessionID sessionId) {
    }

    @Override
    public void onLogout(SessionID sessionId) {
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
    }

    @Override
    public void toApp(Message message, SessionID sessionId) {
    }

    // New Orrder Single için method
    public void onMessage(quickfix.fix44.NewOrderSingle message, SessionID sessionID) {
        try {
            String symbol = message.getString(quickfix.field.Symbol.FIELD);
            char side = message.getChar(quickfix.field.Side.FIELD);
            int quantity = message.getInt(quickfix.field.Quantity.FIELD);
            int price = message.getInt(quickfix.field.Price.FIELD);

            Side orderside;
            if (side == quickfix.field.Side.BUY) {
                orderside = Side.BUY;
            } else {
                orderside = Side.SELL;
            }

            Order order = new Order.Builder(orderside)
                    .withId(symbol)
                    .withPrice(price)
                    .withAmount(quantity)
                    .build();

            engine.process(order);
        } catch (FieldNotFound e) {
            System.out.println(e);
        }
    }

    public void onMessage(Message message, SessionID sessionID) {
        try {
            String symbol = message.getString(quickfix.field.Symbol.FIELD);
            char side = message.getChar(quickfix.field.Side.FIELD);
            int quantity = message.getInt(quickfix.field.Quantity.FIELD);
            int price = message.getInt(quickfix.field.Price.FIELD);

            Side orderside;
            if (side == quickfix.field.Side.BUY) {
                orderside = Side.BUY;
            } else {
                orderside = Side.SELL;
            }

            Order order = new Order.Builder(orderside)
                    .withId(symbol)
                    .withPrice(price)
                    .withAmount(quantity)
                    .build();

            engine.process(order);
        } catch (FieldNotFound e) {
            System.out.println(e);
        }
    }
}