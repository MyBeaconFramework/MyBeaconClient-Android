package guilherme.krzisch.com.mybeaconclient.mybeaconframework.GateModule;

import android.util.Log;

import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.BeaconObject;

public class GateObject {
    private static final boolean DEBUG = true;
    private static final String TAG = "GateObject";
    private static final double THRESHHOLD_DISTANCE = 3;
    private static final double THRESHHOLD_DISTANCE_MEDIUM = 2;

    public static final String REMOTE_ID = "remoteId";
    public static final String DESCRIPTION = "description";
    public static final String BEACON_A_ID = "beaconAId";
    public static final String BEACON_B_ID = "beaconBId";

    public static final int NON_INIT_STATE = 0;
    public static final int BEACON_A_STATE = 1;
    public static final int MEDIUM_STATE = 2;
    public static final int BEACON_B_STATE = 3;

    private String remoteId;
    private BeaconObject beaconA;
    private BeaconObject beaconB;
    private String description;

    private int currentGateState;
    private int initialBeaconState;

    public GateObject(String remoteId, BeaconObject beaconA, BeaconObject beaconB, String description) {
        this.remoteId = remoteId;
        this.beaconA = beaconA;
        this.beaconB = beaconB;
        this.description = description;
        currentGateState = NON_INIT_STATE;
        initialBeaconState = NON_INIT_STATE;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public BeaconObject getBeaconA() {
        return beaconA;
    }

    public BeaconObject getBeaconB() {
        return beaconB;
    }

    public boolean isUsingBeacon(BeaconObject beaconObject){
        return beaconA.isEqual(beaconObject) || beaconB.isEqual(beaconObject);
    }

    public BeaconObject checkStateAndEndOfOperation(){
        int countDistancesBeaconA = beaconA.getArrayLastDistanceRegistered().size();
        int countDistancesBeaconB = beaconB.getArrayLastDistanceRegistered().size();
        int max = Math.max(countDistancesBeaconA, countDistancesBeaconB);

        int index = 0;
        if(currentGateState == NON_INIT_STATE){
            if(DEBUG) Log.d(TAG, "NON_INIT_STATE");

            while(index < max){
                double lastDistanceA = beaconA.getLastDistance(index);
                double lastDistanceB = beaconB.getLastDistance(index);

                //check first position started for the gate
                if(lastDistanceA - lastDistanceB > THRESHHOLD_DISTANCE){
                    initialBeaconState = BEACON_B_STATE;
                    currentGateState = BEACON_B_STATE;
                    if(DEBUG) Log.d(TAG, "NON_INIT_STATE -> BEACON_B_STATE");
                    break;
                } else if(lastDistanceB - lastDistanceA > THRESHHOLD_DISTANCE){
                    initialBeaconState = BEACON_A_STATE;
                    currentGateState = BEACON_A_STATE;
                    if(DEBUG) Log.d(TAG, "NON_INIT_STATE -> BEACON_A_STATE");
                    break;
                }
                index++;
            }
        } else if(currentGateState == BEACON_A_STATE){
            if(DEBUG) Log.d(TAG, "BEACON_A_STATE");

            while(index < max){
                double lastDistanceA = beaconA.getLastDistance(index);
                double lastDistanceB = beaconB.getLastDistance(index);

                //check first position started for the gate
                if(lastDistanceB - lastDistanceA < THRESHHOLD_DISTANCE_MEDIUM){
                    currentGateState = MEDIUM_STATE;
                    if(DEBUG) Log.d(TAG, "BEACON_A_STATE -> MEDIUM_STATE");
                    break;
                }

                index++;
            }
        } else if(currentGateState == BEACON_B_STATE){
            if(DEBUG) Log.d(TAG, "BEACON_B_STATE");

            while(index < max){
                double lastDistanceA = beaconA.getLastDistance(index);
                double lastDistanceB = beaconB.getLastDistance(index);

                //check first position started for the gate
                if(lastDistanceA - lastDistanceB < THRESHHOLD_DISTANCE_MEDIUM){
                    currentGateState = MEDIUM_STATE;
                    if(DEBUG) Log.d(TAG, "BEACON_B_STATE -> MEDIUM_STATE");
                    break;
                }

                index++;
            }
        } else if(currentGateState == MEDIUM_STATE){
            if(DEBUG) Log.d(TAG, "MEDIUM_STATE");

            if(initialBeaconState == BEACON_A_STATE){
                while(index < max){
                    double lastDistanceA = beaconA.getLastDistance(index);
                    double lastDistanceB = beaconB.getLastDistance(index);

                    //check first position started for the gate
                    if(lastDistanceA - lastDistanceB > THRESHHOLD_DISTANCE){
                        if(DEBUG) Log.d(TAG, "MEDIUM_STATE -> FINAL_STATE with beaconB");
                        return beaconB;
                    }

                    index++;
                }
            }
            else{
                while(index < max){
                    double lastDistanceA = beaconA.getLastDistance(index);
                    double lastDistanceB = beaconB.getLastDistance(index);

                    //check first position started for the gate
                    if(lastDistanceB - lastDistanceA > THRESHHOLD_DISTANCE){
                        if(DEBUG) Log.d(TAG, "MEDIUM_STATE -> FINAL_STATE with beaconA");
                        return beaconA;
                    }

                    index++;
                }
            }
        }
        if(DEBUG) Log.d(TAG, "Returning null");
        return null;
    }

    public void resetData(){
        currentGateState = NON_INIT_STATE;
        initialBeaconState = NON_INIT_STATE;
        beaconA.resetDistances();
        beaconB.resetDistances();
    }
}
