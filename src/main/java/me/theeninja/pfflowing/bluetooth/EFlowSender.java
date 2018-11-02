package me.theeninja.pfflowing.bluetooth;

import me.theeninja.pfflowing.Action;
import me.theeninja.pfflowing.EFlow;

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

;

public class EFlowSender {
    private static final UUID OBEX_OBJECT_PUSH_SERVICE = new UUID(0x1005);
    private static final int SERVICE_NAME_ATTRIBUTE = 0x0100;

    private static final UUID[] REQUESTED_SERVICES = {OBEX_OBJECT_PUSH_SERVICE};

    private static final int[] ATTRIBUTES = {SERVICE_NAME_ATTRIBUTE};

    public void sendMessageToDevice(String deviceAddress, Action action) throws IOException {
        String obexURL = EFlowConnector.getOBEXURL(deviceAddress);

        System.out.println(obexURL);

        ClientSession clientSession = (ClientSession) Connector.open(obexURL);
        RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice(clientSession);
        System.out.println("Friendly name " + remoteDevice.getFriendlyName(true));

        HeaderSet requestHeaderSet = clientSession.createHeaderSet();
        requestHeaderSet.setHeader(HeaderSet.NAME, /*action.getClass().getName()*/ "Action");
        requestHeaderSet.setHeader(HeaderSet.TYPE, "text/json");

        HeaderSet responseHeaderSet = clientSession.connect(requestHeaderSet);

        if (responseHeaderSet.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {
            throw new BluetoothConnectionException(BluetoothConnectionException.UNACCEPTABLE_PARAMS);
        }

        //Create PUT Operation
        Operation operation = clientSession.put(requestHeaderSet);

        String actionJson = "Hello" /* EFlow.getInstance().getGSON().toJson(action, action.getClass()) */;

        // Sending the message
        byte[] actionJsonBytes = actionJson.getBytes(StandardCharsets.UTF_8);
        OutputStream os = operation.openOutputStream();
        os.write(actionJsonBytes);
        os.close();

        operation.close();
        clientSession.disconnect(null);
        clientSession.close();
    }
}
