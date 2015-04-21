package guilherme.krzisch.com.mybeaconclient.mybeaconframework.ObjectModule;

import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.BeaconObject;

public class ObjectSeen {
    private BeaconObject beaconLinkedObject;
    private BeaconObject closerBeaconSector;

    public ObjectSeen(BeaconObject beaconLinkedObject, BeaconObject closerBeaconSector) {
        this.beaconLinkedObject = beaconLinkedObject;
        this.closerBeaconSector = closerBeaconSector;
    }

    public BeaconObject getBeaconLinkedObject() {
        return beaconLinkedObject;
    }

    public BeaconObject getCloserBeaconSector() {
        return closerBeaconSector;
    }

    public void setCloserBeaconSector(BeaconObject closerBeaconSector) {
        this.closerBeaconSector = closerBeaconSector;
    }
}
