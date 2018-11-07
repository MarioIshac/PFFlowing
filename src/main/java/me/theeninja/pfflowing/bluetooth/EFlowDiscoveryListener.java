package me.theeninja.pfflowing.bluetooth;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.List;

public class EFlowDiscoveryListener implements DiscoveryListener {
    private final List<RemoteDevice> remoteDevices;
    private final List<String> serviceUrls;
    private final Object serviceLock;
    private final Object inquiryLock;

    EFlowDiscoveryListener(List<RemoteDevice> remoteDevices, List<String> serviceUrls, Object inquiryLock, Object serviceLock) {
        this.remoteDevices = remoteDevices;
        this.serviceUrls = serviceUrls;
        this.serviceLock = serviceLock;
        this.inquiryLock = inquiryLock;
    }

    @Override
    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
        System.out.println("Device " + remoteDevice.getBluetoothAddress() + " found");
        getRemoteDevices().add(remoteDevice);
        try {
            System.out.println("     name " + remoteDevice.getFriendlyName(false));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void servicesDiscovered(int id, ServiceRecord[] serviceRecords) {
        System.out.println("servvicesDiscovered called");
        for (int serviceRecordIndex = 0; serviceRecordIndex < serviceRecords.length; serviceRecordIndex++) {
            ServiceRecord serviceRecord = serviceRecords[serviceRecordIndex];
            String url = serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);

            if (url == null) {
                continue;
            }

            getServiceUrls().add(url);
            DataElement serviceName = serviceRecord.getAttributeValue(0x0100);

            if (serviceName != null) {
                System.out.println("service " + serviceName.getValue() + " found " + url);
            } else {
                System.out.println("service found " + url);
            }
        }
    }

    @Override
    public void serviceSearchCompleted(int i, int i1) {
        System.out.println("service search completed!");

        synchronized (getServiceLock()){
            getServiceLock().notifyAll();
        }
    }

    @Override
    public void inquiryCompleted(int i) {
        System.out.println("Device Inquiry completed!");
        synchronized(getInquiryLock()){
            getInquiryLock().notifyAll();
        }
    }

    public List<String> getServiceUrls() {
        return serviceUrls;
    }

    public Object getServiceLock() {
        return serviceLock;
    }

    public Object getInquiryLock() {
        return inquiryLock;
    }

    public List<RemoteDevice> getRemoteDevices() {
        return remoteDevices;
    }
}
