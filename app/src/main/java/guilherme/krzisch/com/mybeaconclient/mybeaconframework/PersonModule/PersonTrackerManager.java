package guilherme.krzisch.com.mybeaconclient.mybeaconframework.PersonModule;

import java.util.ArrayList;
import java.util.List;

import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.BaseModule;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.BeaconObject;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.HandleBeaconsRangedInterface;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.MyBeaconManager;

public class PersonTrackerManager extends BaseModule implements HandleBeaconsRangedInterface {
    private BeaconObject closerBeacon = null;

    // UserPassedByBeaconSectorInterface
    private List<UserPassedByBeaconSectorInterface> userPassedByBeaconSectorInterfaceList;

    private static PersonTrackerManager ourInstance = new PersonTrackerManager();

    public static PersonTrackerManager getInstance() {
        return ourInstance;
    }

    private PersonTrackerManager() {
        userPassedByBeaconSectorInterfaceList = new ArrayList<>();
    }

    public void init(){
        super.init(true);
        MyBeaconManager.getInstance().addHandleBeaconsRangedInterface(this);
    }

    public void addUserPassedByBeaconSectorInterface(UserPassedByBeaconSectorInterface userPassedByBeaconSectorInterface){
        userPassedByBeaconSectorInterfaceList.add(userPassedByBeaconSectorInterface);
    }

    public void removeUserPassedByBeaconSectorInterface(UserPassedByBeaconSectorInterface userPassedByBeaconSectorInterface){
        userPassedByBeaconSectorInterfaceList.remove(userPassedByBeaconSectorInterface);
    }

    @Override
    public void handleBeaconRanged(List<BeaconObject> arrayBeaconsRanged) {
        BeaconObject currentCloserBeacon = null;
        for(BeaconObject currentBeacon : arrayBeaconsRanged){
            if(currentBeacon.isSector()) {
                if (currentCloserBeacon == null || (currentBeacon.getLastDistanceRegistered() < currentCloserBeacon.getLastDistanceRegistered())) {
                    currentCloserBeacon = currentBeacon;
                }
            }
        }

        if(currentCloserBeacon != null && (closerBeacon == null || (!closerBeacon.isEqual(currentCloserBeacon)))){
            closerBeacon = currentCloserBeacon;
            for(UserPassedByBeaconSectorInterface userPassedByBeaconSectorInterface : userPassedByBeaconSectorInterfaceList){
                userPassedByBeaconSectorInterface.userPassedByBeaconSector(closerBeacon);
            }
        }
    }
}
