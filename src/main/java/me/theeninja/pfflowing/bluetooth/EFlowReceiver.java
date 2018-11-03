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

    public void listen() {
        Task<Void> listeningTask = new Task<>() {
            @Override
            protected Void call() throws IOException {
                while (true) {
                    getStreamConnectionNotifier().acceptAndOpen(getEFlowRequestHandler());
                }
            }
        };

        Thread listeningThread = new Thread(listeningTask);

        // Listening terminates once EFlow application window is closed
        listeningThread.setDaemon(true);

        listeningThread.start();
    }

    public EFlowRequestHandler getEFlowRequestHandler() {
        return eFlowRequestHandler;
    }

    public SessionNotifier getStreamConnectionNotifier() {
        return streamConnectionNotifier;
    }
}
