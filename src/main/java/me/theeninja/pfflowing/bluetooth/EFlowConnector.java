package me.theeninja.pfflowing.bluetooth;

import me.theeninja.pfflowing.tournament.Round;

import javax.bluetooth.LocalDevice;
import java.io.IOException;

public class EFlowConnector {
    public static final int EFLOW_IDENTIFIER = 2;

    private final EFlowSender eFlowSender;
    private final EFlowReceiver eFlowReceiver;

    private final LocalDevice localDevice = LocalDevice.getLocalDevice();

    public EFlowConnector(String remoteDeviceAddress) throws IOException {
        this.eFlowSender = new EFlowSender(remoteDeviceAddress);
        this.eFlowReceiver = new EFlowReceiver();
    }

    public EFlowSender getFlowSender() {
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
