package com.staj.MatchingEngine.fix;

import com.staj.MatchingEngine.engine.OptimizedOrderMatch;
import org.springframework.stereotype.Component;
import quickfix.*;
import java.io.InputStream;

@Component
public class FixServer {
    
    private Acceptor acceptor; //Aceptor suncusu
    
    public void start() throws Exception {
        // quickfix.cfg dosyasını classpath üzerinden yükle
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("quickfix.cfg");
        if (inputStream == null) {
            throw new RuntimeException("quickfix.cfg bulunamadı! resources klasöründe olduğundan emin olun.");
        }
        SessionSettings settings = new SessionSettings(inputStream);
        //configteki oturum ayarlarını yükledik settings ile
        Application application = new fixApp();
        //mesajları işleyen sınıf uygulaması
        ((fixApp) application).setEngine(new OptimizedOrderMatch());
        //Engine i fix appe bağladık
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();
        
        acceptor = new SocketAcceptor(application, storeFactory, settings, logFactory, messageFactory);
        acceptor.start();
        System.out.println("Server baslatildi.");
    }
    
    public void stop() {
        if (acceptor != null) {
            acceptor.stop();
        }
    }
}