package me.theeninja.pfflowing.bluetooth;

import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.actions.Action;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EFlowSender {
    private static final UUID OBEX_OBJECT_PUSH_SERVICE = new UUID(0x1105);
    private static final int SERVICE_NAME_ATTRIBUTE = 0x0100;

    private static final UUID[] REQUESTED_SERVICES = {OBEX_OBJECT_PUSH_SERVICE};

    private static final int[] ATTRIBUTES = {SERVICE_NAME_ATTRIBUTE};

    private ClientSession clientSession;

    private static String parseDeviceAddress(final String deviceAddress) {
        return deviceAddress.replace(":", "");
    }

    public ClientSession getClientSession() {
        return clientSession;
    }

    private final Object inquiryLock = new Object();
    private final Object serviceLock = new Object();

    public Object getInquiryLock() {
        return this.inquiryLock;
    }

    public Object getServiceLock() {
        return this.serviceLock;
    }

    private void connect() throws IOException, InterruptedException {
        System.out.println("A");

        List<RemoteDevice> remoteDevices = new ArrayList<>();
        List<String> serviceUrls = new ArrayList<>();

        DiscoveryListener serviceListener = new EFlowDiscoveryListener(remoteDevices, serviceUrls, getInquiryLock(), getServiceLock());

        synchronized (getInquiryLock()) {
            boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, serviceListener);

            if (started) {
                System.out.println("wait for device inquiry to complete...");
                getInquiryLock().wait();
                System.out.println(remoteDevices.size() +  " device(s) found");
            }
        }
        // this.clientSession = (ClientSession) Connector.open(getOBEXURL());
        System.out.println("B");

        RemoteDevice remoteDevice = null;

        for (RemoteDevice remoteDevice1 : remoteDevices) {
            if (remoteDevice1.getFriendlyName(true).equals("MARIO-PC")) {
                remoteDevice = remoteDevice1;
                break;
            }
        }

        System.out.println("C");

        LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(
            ATTRIBUTES,
            REQUESTED_SERVICES,
            remoteDevice,
            serviceListener
        );

        System.out.println("D");

        System.out.println();
        serviceUrls.forEach(System.out::println);

        System.out.println(RemoteDevice.getRemoteDevice(getClientSession()).getFriendlyName(true));

        HeaderSet responseHeaderSet = getClientSession().connect(null);
        System.out.println("C");

        int responseCode = responseHeaderSet.getResponseCode();
        System.out.println("D");

        if (responseCode != ResponseCodes.OBEX_HTTP_OK) {
            throw new BluetoothConnectionException(responseCode);
        }
        System.out.println("E");
    }

    public void shareRound(Round round) throws IOException, InterruptedException {
        if (getClientSession() == null) {
            connect();
        }

        HeaderSet roundHeaderSet = getNewRoundHeaderSet(round);

        Operation putOperation = getClientSession().put(roundHeaderSet);
        HeaderSet receivedHeaders = putOperation.getReceivedHeaders();

        int responseCode = receivedHeaders.getResponseCode();

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
        System.out.println("creating");
        HeaderSet headerSet = getClientSession().createHeaderSet();
        System.out.println("finished creating");

        System.out.println("setting round name");
        headerSet.setHeader(EFlowHeader.ROUND_NAME, round.getRoundName());
        System.out.println("finished setting round name");
        headerSet.setHeader(EFlowHeader.SIDE, round.getSide().getRepresentation());

        System.out.println("done with method");

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
