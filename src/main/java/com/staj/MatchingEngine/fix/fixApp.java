package com.staj.MatchingEngine.fix;

import com.staj.MatchingEngine.engine.OptimizedOrderMatch;
import com.staj.MatchingEngine.model.Order;
import com.staj.MatchingEngine.model.Side;

import quickfix.*;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Symbol;
import quickfix.field.MsgType;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;

public class fixApp extends MessageCracker implements Application {

    private OptimizedOrderMatch engine;

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
            System.out.println("📨 Gelen mesaj tipi: " + message.getClass().getSimpleName());
            
            // MsgType alanını kontrol ederek mesaj tipini belirleyelim
            String msgType = message.getHeader().getString(MsgType.FIELD);
            System.out.println("📋 MsgType: " + msgType);
            
            if ("D".equals(msgType)) {  // D = NewOrderSingle
                // Message'dan direkt field'ları çek
                processNewOrderSingle(message, sessionId);
            } else {
                System.out.println("⚠️ Desteklenmeyen MsgType: " + msgType);
            }
        } catch (Exception e) {
            System.out.println("❌ fromApp Hatası: " + e);
            e.printStackTrace();
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

    // NewOrderSingle mesajını direkt Message'dan işle
    private void processNewOrderSingle(Message message, SessionID sessionID) {
        try {
            String symbol = message.getString(Symbol.FIELD);
            char side = message.getChar(quickfix.field.Side.FIELD);
            int quantity = message.getInt(OrderQty.FIELD);
            int priceValue = (int) message.getDouble(Price.FIELD);
            String clOrdID = message.getString(ClOrdID.FIELD);

            Side orderside = (side == quickfix.field.Side.BUY) ? Side.BUY : Side.SELL;
            Order order = new Order.Builder(orderside)
                    .withId(clOrdID)
                    .withPrice(priceValue)
                    .withAmount(quantity)
                    .build();
            var trades = engine.process(order);
            System.out.println("📋 Emir işlendi: " + clOrdID + " (" + symbol + ")");
            if (!trades.isEmpty()) {
                System.out.println("✅ " + trades.size() + " trade gerçekleşti");
            }
            sendExecutionReport(order, sessionID, clOrdID, symbol);
        } catch (FieldNotFound e) {
            System.out.println("❌ NewOrderSingle işleme hatası: " + e);
        }
    }

    // Sadece NewOrderSingle mesajlarını işler
    public void onMessage(quickfix.fix44.NewOrderSingle message, SessionID sessionID) {
        try {
            String symbol = message.getString(Symbol.FIELD);
            char side = message.getChar(quickfix.field.Side.FIELD);
            int quantity = message.getInt(OrderQty.FIELD);
            int priceValue = (int) message.getDouble(Price.FIELD);
            String clOrdID = message.getString(ClOrdID.FIELD);

            Side orderside = (side == quickfix.field.Side.BUY) ? Side.BUY : Side.SELL;
            Order order = new Order.Builder(orderside)
                    .withId(clOrdID)
                    .withPrice(priceValue)
                    .withAmount(quantity)
                    .build();
            var trades = engine.process(order);
            System.out.println("📋 Emir işlendi: " + clOrdID + " (" + symbol + ")");
            if (!trades.isEmpty()) {
                System.out.println("✅ " + trades.size() + " trade gerçekleşti");
            }
            sendExecutionReport(order, sessionID, clOrdID, symbol);
        } catch (FieldNotFound e) {
            System.out.println("❌ Hata: " + e);
        }
    }

    // ExecutionReport gönderme metodu
    public void sendExecutionReport(Order order, SessionID sessionID, String clOrdID, String symbol) {
        try {
            ExecutionReport report = new ExecutionReport();
            report.set(new OrderID(order.getId()));
            report.set(new ExecID("EXEC_" + System.currentTimeMillis()));
            report.set(new ExecType(ExecType.FILL));
            report.set(new OrdStatus(OrdStatus.FILLED));
            report.set(new Symbol(symbol)); // gelen symbol
            report.set(new quickfix.field.Side(
                    order.getSide() == Side.BUY ? quickfix.field.Side.BUY : quickfix.field.Side.SELL));
            report.set(new LeavesQty(0));
            report.set(new CumQty(order.getAmount()));
            report.set(new AvgPx(order.getPrice()));
            report.set(new ClOrdID(clOrdID)); // OMS'den gelen order id
            report.set(new Price(order.getPrice()));
            report.set(new OrderQty(order.getAmount()));

            Session.sendToTarget(report, sessionID);
            System.out.println("✅ MatchingEngine: ExecutionReport gönderildi - " + clOrdID);
        } catch (Exception e) {
            System.out.println("❌ MatchingEngine: ExecutionReport gönderme hatası: " + e);
            e.printStackTrace();
        }
    }
}