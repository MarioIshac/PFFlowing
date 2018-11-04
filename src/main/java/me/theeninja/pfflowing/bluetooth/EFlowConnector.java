package me.theeninja.pfflowing.bluetooth;

import me.theeninja.pfflowing.EFlow;
import me.theeninja.pfflowing.actions.Action;
import me.theeninja.pfflowing.gui.FlowController;
import me.theeninja.pfflowing.speech.Side;
import me.theeninja.pfflowing.tournament.Round;

import javax.bluetooth.LocalDevice;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import java.io.IOException;

public class EFlowConnector {
    private static final int EFLOW_IDENTIFIER = 2;

    private final EFlowSender eFlowSender;
    private final EFlowReceiver eFlowReceiver;

    private final LocalDevice localDevice = LocalDevice.getLocalDevice();

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

    public LocalDevice getLocalDevice() {
        return localDevice;
    }

    public static String getOBEXURL(String deviceAddress) {
        return "btgoep://" + deviceAddress + ":" + EFlowConnector.EFLOW_IDENTIFIER;
    }
}
