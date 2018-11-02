package me.theeninja.pfflowing.bluetooth;

import me.theeninja.pfflowing.tournament.Round;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import java.io.IOException;

public class EFlowConnector {
    public static final int EFLOW_IDENTIFIER = 2;

    private final EFlowSender eFlowSender;
    private final EFlowReceiver eFlowReceiver;

    private final LocalDevice localDevice = LocalDevice.getLocalDevice();
    private final String otherDeviceURL;

    public EFlowConnector(String deviceAddress, Round round) throws IOException {
        this.otherDeviceURL = deviceAddress;
        this.eFlowSender = new EFlowSender(deviceAddress, round);
        this.eFlowReceiver = new EFlowReceiver();
    }

    public void start() throws IOException {
        getFlowSender().connect();
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

    public String getOtherDeviceURL() {
        return otherDeviceURL;
    }

    public static String getOBEXURL(String deviceAddress) {
        return "btgoep://" + deviceAddress + ":" + EFlowConnector.EFLOW_IDENTIFIER;
    }
}
