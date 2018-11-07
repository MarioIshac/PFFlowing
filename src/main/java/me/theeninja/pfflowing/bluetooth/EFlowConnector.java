package me.theeninja.pfflowing.bluetooth;

import me.theeninja.pfflowing.gui.FlowController;

import java.io.IOException;

public class EFlowConnector {
    private static final int EFLOW_IDENTIFIER = 2;

    private final EFlowSender eFlowSender;
    private final EFlowReceiver eFlowReceiver;

    public EFlowConnector(String remoteDeviceAddress, FlowController flowController) throws IOException {
        this.eFlowSender = new EFlowSender(remoteDeviceAddress);
        this.eFlowReceiver = new EFlowReceiver(flowController);
    }

    public EFlowSender getSender() {
        return eFlowSender;
    }

    public EFlowReceiver getFlowReceiver() {
        return eFlowReceiver;
    }

    static String getOBEXURL(String deviceAddress) {
        return "btgoep://" + deviceAddress + ":" + EFlowConnector.EFLOW_IDENTIFIER;
    }
}
