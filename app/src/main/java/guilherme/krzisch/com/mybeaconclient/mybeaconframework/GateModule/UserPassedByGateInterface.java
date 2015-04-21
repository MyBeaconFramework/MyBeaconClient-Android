package guilherme.krzisch.com.mybeaconclient.mybeaconframework.GateModule;

import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.BeaconObject;

public interface UserPassedByGateInterface {
    void userPassedByGate(GateObject gate, BeaconObject closerBeacon);
}
