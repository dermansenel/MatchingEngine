package com.staj.MatchingEngine.fix;

import java.io.FileInputStream;


import quickfix.*;

public class fixServis {
    public void startFixServer() throws Exception {

        fixApp application = new fixApp();
        SessionSettings settings = new SessionSettings(new FileInputStream("quickfix.cfg"));
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new FileLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();
        Acceptor acceptor = new SocketAcceptor(application, storeFactory, settings, logFactory, messageFactory);

        acceptor.start();
      

        acceptor.stop();
    }
}
