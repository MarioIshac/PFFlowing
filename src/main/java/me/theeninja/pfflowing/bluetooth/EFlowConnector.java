package me.theeninja.pfflowing.bluetooth;

import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.gui.FlowController;

import java.io.IOException;
import java.util.UUID;

public class EFlowConnector {
    private static final String EFLOW_IDENTIFIER = "2";

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
        return "btgoep://" + deviceAddress + ":" + EFlowConnector.EFLOW_IDENTIFIER + ";name=" + EFlow.class.getSimpleName();
    }
}
