package com.staj.MatchingEngine;

import quickfix.fix44.NewOrderSingle;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;

public class QuickFixJTest {
    public static void main(String[] args) {
        NewOrderSingle order = new NewOrderSingle(
                new ClOrdID("12345"),
                new Side(Side.BUY),
                new TransactTime(java.time.LocalDateTime.now()),
                new OrdType(OrdType.LIMIT));
        order.set(new Symbol("AAPL"));
        order.set(new OrderQty(100));
        order.set(new Price(150.0));
        order.set(new HandlInst('1')); //
        System.out.println("FIX Message:\n" + order);
/*
 * çıktısı 8=FIX.4.49=7635=D11=1234521=138=10040=244=15054=155=AAPL60=20250805-15:46:12.52110=028
    8= FIX.4.4 porotok versiyonu -> otomatik gelir
    9= 76 mesaj uzunluğu?        -> otomatik gelir
    35=D mesaj tiği yani newordersingle ->newordersingle biz oluşturduğumuz için geldi
    11=12345 müşteri id'si  ->new ClOrdID("12345"),
    21=1 emir yönetim tipi ->order.set(new HandlInst('1')); //
    38=100 emir miktarı 
    40 =2 emir tipi
    44=150 emir fiyatı
    54=1 emir tarafı yani buy sell vs.
    55=AAPL id. 
    60: emir zamanı
    10 = mesaj kontrol toplamı  -ZChecksum, QuickFIX/J tarafından otomatik hesaplanır.
 
 */    
    }
}
