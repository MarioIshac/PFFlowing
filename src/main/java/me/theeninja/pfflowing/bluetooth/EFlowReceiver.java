package me.theeninja.pfflowing.bluetooth;

import javafx.concurrent.Task;

import javax.microedition.io.Connector;
import javax.obex.*;
import java.io.IOException;

public class EFlowReceiver extends ServerRequestHandler {
    private final EFlowRequestHandler eFlowRequestHandler = new EFlowRequestHandler();
    private final SessionNotifier streamConnectionNotifier;

    EFlowReceiver() throws IOException {
        String serverURL = EFlowConnector.getOBEXURL("localhost");
        this.streamConnectionNotifier = (SessionNotifier) Connector.open(serverURL);
    }

    private void start() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    getStreamConnectionNotifier().acceptAndOpen(getEFlowRequestHandler());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.run();
    }

    public EFlowRequestHandler getEFlowRequestHandler() {
        return eFlowRequestHandler;
    }

    public SessionNotifier getStreamConnectionNotifier() {
        return streamConnectionNotifier;
    }
}
