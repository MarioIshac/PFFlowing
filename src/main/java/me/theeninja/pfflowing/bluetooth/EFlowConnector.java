package me.theeninja.pfflowing.bluetooth;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;

public class EFlowConnector {
    public static final int EFLOW_IDENTIFIER = 2;

    private final EFlowSender eFlowSender;
    private final EFlowReceiver eFlowReceiver;

    private final LocalDevice localDevice = LocalDevice.getLocalDevice();
    private final String otherDeviceURL;

    public EFlowConnector(String otherDeviceURL) throws BluetoothStateException {
        this.otherDeviceURL = otherDeviceURL;
        this.eFlowSender = new EFlowSender();
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

    public String getOtherDeviceURL() {
        return otherDeviceURL;
    }

    public static String getOBEXURL(String deviceAddress) {
        return "btgoep://" + deviceAddress + ":" + EFlowConnector.EFLOW_IDENTIFIER;
    }
}
