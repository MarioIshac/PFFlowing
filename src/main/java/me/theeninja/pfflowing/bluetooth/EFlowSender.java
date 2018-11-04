package me.theeninja.pfflowing.bluetooth;

import com.google.common.base.Charsets;
import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.actions.Action;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;

import javax.bluetooth.BluetoothConnectionException;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

;

public class EFlowSender {
    private static final UUID OBEX_OBJECT_PUSH_SERVICE = new UUID(0x1005);
    private static final int SERVICE_NAME_ATTRIBUTE = 0x0100;

    private static final UUID[] REQUESTED_SERVICES = {OBEX_OBJECT_PUSH_SERVICE};

    private static final int[] ATTRIBUTES = {SERVICE_NAME_ATTRIBUTE};

    private final ClientSession clientSession;

    EFlowSender(String deviceAddress) throws IOException {
        String obexURL = EFlowConnector.getOBEXURL(deviceAddress);

        System.out.println("a " + obexURL);
        this.clientSession = (ClientSession) Connector.open(obexURL);
        System.out.println("b");
    }

    public ClientSession getClientSession() {
        return clientSession;
    }

    public void shareRound(Round round) throws IOException {
        System.out.println("round shares");

        HeaderSet roundHeaderSet = getNewRoundHeaderSet(round);

        System.out.println("c");
        HeaderSet responseHeaderSet = getClientSession().connect(roundHeaderSet);
        System.out.println("d");

        int responseCode = responseHeaderSet.getResponseCode();

        if (responseCode != ResponseCodes.OBEX_HTTP_OK) {
            throw new BluetoothConnectionException(responseCode);
        }

       putData(Map.of(
           HeaderSet.NAME, "myName"
       ), "Hello");
    }

    private void shareAction(Round round, Side side, Action action) throws IOException {
        HeaderSet actionHeaderSet = getNewActionHeaderSet(round, side, action);

        Operation putOperation = getClientSession().put(actionHeaderSet);
        HeaderSet receivedHeaders = putOperation.getReceivedHeaders();

        int responseCode = receivedHeaders.getResponseCode();

        if (responseCode != ResponseCodes.OBEX_HTTP_OK) {
            throw new BluetoothConnectionException(responseCode);
        }

        OutputStream outputStream = putOperation.openOutputStream();

        String actionJson = EFlow.getInstance().getGSON().toJson(action);
        byte[] actionJsonBytes = actionJson.getBytes(StandardCharsets.UTF_8);

        outputStream.write(actionJsonBytes);
        outputStream.close();

        putOperation.close();
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

    public HeaderSet getNewRoundHeaderSet(Round round) {
        HeaderSet headerSet = getClientSession().createHeaderSet();

        headerSet.setHeader(EFlowHeader.ROUND_NAME, round.getName());
        headerSet.setHeader(EFlowHeader.SIDE, round.getSide().getRepresentation());

        return headerSet;
    }

    public HeaderSet getNewActionHeaderSet(Round round, Side side, Action action) {
        HeaderSet headerSet = getClientSession().createHeaderSet();

        headerSet.setHeader(EFlowHeader.ROUND_NAME, round.getName());
        headerSet.setHeader(EFlowHeader.SIDE, side.getRepresentation());
        headerSet.setHeader(EFlowHeader.ACTION_CLASS, action.getClass());

        return headerSet;
    }
}
