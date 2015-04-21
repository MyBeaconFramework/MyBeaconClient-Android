package guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule;

import java.util.ArrayList;
import java.util.List;

public class BeaconObject {
    public static final int MAX_LIST_LAST_DISTANCE_SIZE = 120;
    public static final double MAX_DISTANCE = 80.0;
    private static final int DISTANCE_HISTORY = 3;
    public static final int INVALID_DISTANCE = -1;

    public static final int SECTOR_BEACON_TYPE = 0;
    public static final int OBJECT_BEACON_TYPE = 1;
    public static final int GATE_BEACON_TYPE = 2;
    public static final int GATE_SECTOR_BEACON_TYPE = 3;

    public static final double NO_LATITUDE = -200;
    public static final double NO_LONGITUDE = -200;

    public static final String REMOTE_ID = "remoteId";
    public static final String BEACON_TYPE = "beaconType";
    public static final String UUID_FIELD = "uuid";
    public static final String MAJOR = "major";
    public static final String MINOR = "minor";
    public static final String DESCRIPTION = "description";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    private String remoteId;
    private String uuid;
    private int major;
    private int minor;
    private int beaconType;
    private String description;
    private double latitude;
    private double longitude;
    private List<Double> arrayLastDistanceRegistered;

    public BeaconObject(String remoteId, String uuid, int major, int minor, int beaconType, String description, double latitude, double longitude) {
        this.remoteId = remoteId;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.beaconType = beaconType;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.arrayLastDistanceRegistered = new ArrayList<>();
    }

    public String getRemoteId() {
        return remoteId;
    }

    public List<Double> getArrayLastDistanceRegistered() {
        return arrayLastDistanceRegistered;
    }

    public boolean isSector(){
        return beaconType == SECTOR_BEACON_TYPE || beaconType == GATE_SECTOR_BEACON_TYPE;
    }

    public boolean isObject(){
        return beaconType == OBJECT_BEACON_TYPE;
    }

    public boolean isGate(){
        return beaconType == GATE_BEACON_TYPE || beaconType == GATE_SECTOR_BEACON_TYPE;
    }


    public void addLastDistance(double lastDistance){
        if(arrayLastDistanceRegistered.size() > MAX_LIST_LAST_DISTANCE_SIZE){
            arrayLastDistanceRegistered.remove(MAX_LIST_LAST_DISTANCE_SIZE);
        }
        arrayLastDistanceRegistered.add(0, lastDistance);
    }

    public void setLastDistance(double lastDistance){
        arrayLastDistanceRegistered.set(0, lastDistance);
    }

    public void resetDistances(){
        double aux = -1;
        if(!arrayLastDistanceRegistered.isEmpty()){
            aux = arrayLastDistanceRegistered.get(0);
        }
        arrayLastDistanceRegistered.clear();
        if(aux != -1) {
            arrayLastDistanceRegistered.add(aux);
        }
    }

    public boolean isEqual(String remoteId){
        return this.remoteId.equalsIgnoreCase(remoteId);
    }

    public boolean isEqual(String uuid, int major, int minor){
        return this.uuid.equalsIgnoreCase(uuid) && this.major == major && this.minor == minor;
    }

    public boolean isEqual(BeaconObject beaconObject) {
        return this.isEqual(beaconObject.getRemoteId());
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    /**
     * Utils
     */

    public double getLastDistanceRegistered(){
        return getLastDistance(0);
    }

    public double getLastDistance(int index) {
        // return getLastDistanceAlgorithm1(index);
        return getLastDistanceAlgorithm2(index);
    }

    private double getLastDistanceAlgorithm1(int index){
        return arrayLastDistanceRegistered.size() > index? arrayLastDistanceRegistered.get(index) : MAX_DISTANCE;
    }

    private double getLastDistanceAlgorithm2(int index){
        double acumulador = 0;
        int count = 0;

        for(int i=0;i<DISTANCE_HISTORY;i++){
            int atualIndex = index + i;
            double ret = getDistance(atualIndex);
            if(ret != INVALID_DISTANCE){
                acumulador += ret;
                count++;
            }
        }
        if(count != 0){
            return acumulador/count;
        } else{
            return MAX_DISTANCE;
        }
    }

    private double getDistance(int index){
        if(arrayLastDistanceRegistered.size() > index){
            return arrayLastDistanceRegistered.get(index);
        } else{
            return INVALID_DISTANCE;
        }
    }
}
