package me.theeninja.pfflowing.bluetooth;

import me.theeninja.pfflowing.Action;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.tournament.Round;

import javax.bluetooth.BluetoothConnectionException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

;

public class EFlowSender {
    private static final UUID OBEX_OBJECT_PUSH_SERVICE = new UUID(0x1005);
    private static final int SERVICE_NAME_ATTRIBUTE = 0x0100;

    private static final UUID[] REQUESTED_SERVICES = {OBEX_OBJECT_PUSH_SERVICE};

    private static final int[] ATTRIBUTES = {SERVICE_NAME_ATTRIBUTE};

    private final ClientSession clientSession;
    private final Round round;

    public Round getRound() {
        return round;
    }

    EFlowSender(String deviceAddress, Round round) throws IOException {
        this.round = round;
        String obexURL = EFlowConnector.getOBEXURL(deviceAddress);

        System.out.println("a " + obexURL);
        this.clientSession = (ClientSession) Connector.open(obexURL);
        System.out.println("b");
    }

    public ClientSession getClientSession() {
        return clientSession;
    }

    public void connect() throws IOException {
        HeaderSet requestHeaderSet = getClientSession().createHeaderSet();

        requestHeaderSet.setHeader(HeaderSet.NAME, getRound().getName());

        System.out.println("c");
        //HeaderSet responseHeaderSet = getClientSession().connect(requestHeaderSet);
        System.out.println("d");

        /* if (responseHeaderSet.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
            throw new BluetoothConnectionException(ResponseCodes.OBEX_HTTP_INTERNAL_ERROR);
        } */

       putData(Map.of(
           HeaderSet.NAME, "myName"
       ), "Hello");
    }

    private void putAction(Action action) {

    }

    private void putData(Map<Integer, String> headerValues, String data) throws IOException {
        RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice(getClientSession());
        System.out.println("Friendly name " + remoteDevice.getFriendlyName(true));

        HeaderSet requestHeaderSet = getClientSession().createHeaderSet();

        for (Map.Entry<Integer, String> entry : headerValues.entrySet()) {
            int headerKey = entry.getKey();
            String headerValue = entry.getValue();

            requestHeaderSet.setHeader(headerKey, headerValue);
        }

        Operation operation = getClientSession().put(requestHeaderSet);

        // Sending the message
        byte[] actionJsonBytes = data.getBytes(StandardCharsets.UTF_8);
        OutputStream os = operation.openOutputStream();
        os.write(actionJsonBytes);
        os.close();

        operation.close();

        System.out.println("e");
    }

    private void disconnect() throws IOException {
        getClientSession().disconnect(null);
        getClientSession().close();
    }
}