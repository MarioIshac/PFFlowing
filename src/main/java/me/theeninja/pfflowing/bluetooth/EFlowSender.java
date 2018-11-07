package me.theeninja.pfflowing.bluetooth;

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
import java.nio.charset.StandardCharsets;
import java.util.Map;

;

public class EFlowSender {
    private static final UUID OBEX_OBJECT_PUSH_SERVICE = new UUID(0x1005);
    private static final int SERVICE_NAME_ATTRIBUTE = 0x0100;

    private static final UUID[] REQUESTED_SERVICES = {OBEX_OBJECT_PUSH_SERVICE};

    private static final int[] ATTRIBUTES = {SERVICE_NAME_ATTRIBUTE};

    private final ClientSession clientSession;

    private static String parseDeviceAddress(final String deviceAddress) {
        return deviceAddress.replace(":", "");
    }

    EFlowSender(String deviceAddress) throws IOException {
        deviceAddress = parseDeviceAddress(deviceAddress);

        final String obexURL = EFlowConnector.getOBEXURL(deviceAddress);

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
    }

    private void shareNewAction(Round round, Side side, Action action) throws IOException {
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

    private void shareActionModification(Round round, Side side, boolean isUndo) throws IOException {
        HeaderSet headerSet = getModifyActionHeaderSet(round, side, isUndo);

        Operation putOperation = getClientSession().put(headerSet);
        HeaderSet receivedHeaders = putOperation.getReceivedHeaders();

        int responseCode = receivedHeaders.getResponseCode();

        if (responseCode != ResponseCodes.OBEX_HTTP_OK) {
            throw new BluetoothConnectionException(responseCode);
        }

        putOperation.close();
    }

    private void disconnect() throws IOException {
        getClientSession().disconnect(null);
        getClientSession().close();
    }

    private HeaderSet getNewRoundHeaderSet(Round round) {
        HeaderSet headerSet = getClientSession().createHeaderSet();

        headerSet.setHeader(EFlowHeader.ROUND_NAME, round.getRoundName());
        headerSet.setHeader(EFlowHeader.SIDE, round.getSide().getRepresentation());

        return headerSet;
    }

    private HeaderSet getNewActionHeaderSet(Round round, Side side, Action action) {
        HeaderSet headerSet = getClientSession().createHeaderSet();

        headerSet.setHeader(EFlowHeader.ROUND_NAME, round.getRoundName());
        headerSet.setHeader(EFlowHeader.SIDE, side.getRepresentation());
        headerSet.setHeader(EFlowHeader.TYPE, PutOperationType.NEW_ACTION);
        headerSet.setHeader(EFlowHeader.ACTION_CLASS, action.getClass());

        return headerSet;
    }

    private HeaderSet getModifyActionHeaderSet(Round round, Side side, boolean isUndo) {
        byte actonType = isUndo ? PutOperationType.UNDO_ACTION : PutOperationType.REDO_ACTION;

        HeaderSet headerSet = getClientSession().createHeaderSet();

        headerSet.setHeader(EFlowHeader.ROUND_NAME, round.getRoundName());
        headerSet.setHeader(EFlowHeader.SIDE, side.getRepresentation());
        headerSet.setHeader(EFlowHeader.TYPE, actonType);

        return headerSet;
    }
}
