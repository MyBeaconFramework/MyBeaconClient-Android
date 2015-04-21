package guilherme.krzisch.com.mybeaconclient.mybeaconframework.ObjectModule;

import java.util.ArrayList;
import java.util.List;

import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.BaseModule;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.BeaconObject;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.HandleBeaconsRangedInterface;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.MyBeaconManager;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.PersonModule.PersonTrackerManager;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.PersonModule.UserPassedByBeaconSectorInterface;

public class ObjectTrackerManager extends BaseModule implements HandleBeaconsRangedInterface, UserPassedByBeaconSectorInterface {
    private List<ObjectSeen> arrayObjectsSeen;
    private BeaconObject currentCloserSectorBeacon = null;

    // UserPassedByGateInterface
    private List<UserPassedByObjectInterface> userPassedByObjectInterfaceList;

    private static ObjectTrackerManager ourInstance = new ObjectTrackerManager();

    public static ObjectTrackerManager getInstance() {
        return ourInstance;
    }

    private ObjectTrackerManager() {
        arrayObjectsSeen = new ArrayList<>();
        userPassedByObjectInterfaceList = new ArrayList<>();
    }

    public void init(){
        super.init(true);
        MyBeaconManager.getInstance().addHandleBeaconsRangedInterface(this);
        PersonTrackerManager.getInstance().addUserPassedByBeaconSectorInterface(this);
    }

    public void addUserPassedByObjectInterface(UserPassedByObjectInterface userPassedByObjectInterface){
        userPassedByObjectInterfaceList.add(userPassedByObjectInterface);
    }

    public void removeUserPassedByObjectInterface(UserPassedByObjectInterface userPassedByObjectInterface){
        userPassedByObjectInterfaceList.remove(userPassedByObjectInterface);
    }

    @Override
    public void handleBeaconRanged(List<BeaconObject> arrayBeaconsRanged) {
        if(currentCloserSectorBeacon == null){
            return;
        }

        for(BeaconObject currentBeaconObject : arrayBeaconsRanged){
            if(currentBeaconObject.isObject()) {
                boolean beaconAlreadySeen = false;
                for (ObjectSeen currentObjectSeen : arrayObjectsSeen) {
                    if (currentObjectSeen.getBeaconLinkedObject().isEqual(currentBeaconObject)) {
                        beaconAlreadySeen = true;

                        if (currentCloserSectorBeacon.isEqual(currentObjectSeen.getCloserBeaconSector())) {
                        } else {
                            for(UserPassedByObjectInterface userPassedByObjectInterface : userPassedByObjectInterfaceList){
                                userPassedByObjectInterface.userPassedByObject(currentBeaconObject, currentCloserSectorBeacon);
                            }
                            int index = arrayObjectsSeen.indexOf(currentObjectSeen);
                            arrayObjectsSeen.get(index).setCloserBeaconSector(currentCloserSectorBeacon);
                        }
                        break;
                    }
                }

                if (!beaconAlreadySeen) {
                    for(UserPassedByObjectInterface userPassedByObjectInterface : userPassedByObjectInterfaceList){
                        userPassedByObjectInterface.userPassedByObject(currentBeaconObject, currentCloserSectorBeacon);
                    }
                    ObjectSeen objectSeen = new ObjectSeen(currentBeaconObject, currentCloserSectorBeacon);
                    arrayObjectsSeen.add(objectSeen);
                }
            }
        }
    }

    @Override
    public void userPassedByBeaconSector(BeaconObject beaconObject) {
        currentCloserSectorBeacon = beaconObject;
    }
}
