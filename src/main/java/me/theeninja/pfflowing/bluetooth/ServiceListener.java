package me.theeninja.pfflowing.bluetooth;

import javax.bluetooth.*;
import java.util.List;

public class ServiceListener implements DiscoveryListener {
    private final List<String> serviceUrls;

    ServiceListener(List<String> serviceUrls) {
        this.serviceUrls = serviceUrls;
    }

    @Override
    public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
        // User specifies devices themselves
    }

    @Override
    public void servicesDiscovered(int id, ServiceRecord[] serviceRecords) {
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

    }

    @Override
    public void inquiryCompleted(int i) {

    }

    public List<String> getServiceUrls() {
        return serviceUrls;
    }
}
