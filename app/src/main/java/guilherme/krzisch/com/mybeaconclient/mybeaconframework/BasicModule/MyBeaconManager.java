package guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.estimote.sdk.Utils;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import guilherme.krzisch.com.mybeaconclient.mybeaconframework.GateModule.GateManager;

public class MyBeaconManager extends BaseModule implements BeaconConsumer {
    private static final String TAG = "MyBeaconManager";
//    private static final String baseSDKUsed = "altBeacon";
    private static final int ALT_BEACON = 1;
    private static final int ESTIMOTE = 2;
    private static final int baseSDKUsed = ESTIMOTE;

    // String used to test gate operation using an example text file. If it is null, it will get real beacon.
    //private static final String TEST_FILE = "caseTest02";
    private static final String TEST_FILE = null;
    // Beacon layout format
    // Estimote: "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
    // AltBeacon: "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
    private static final String BEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    // Application context
    private Context context;

    // Array of BeaconObject synced from server
    private List<BeaconObject> arrayBeaconsCache;

    // Beacon information
    private String uuid;
    private String beaconRangeIdentifier;
    private org.altbeacon.beacon.BeaconManager altBeaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;
    private com.estimote.sdk.BeaconManager estimoteBeaconManager;
    private com.estimote.sdk.Region estimoteRegion;

    // HandleBeaconsRangedInterface
    private List<HandleBeaconsRangedInterface> handleBeaconsRangedInterfaceList;

    private static MyBeaconManager ourInstance = new MyBeaconManager();

    public static MyBeaconManager getInstance() {
        return ourInstance;
    }

    private MyBeaconManager() {
        arrayBeaconsCache = new ArrayList<>();
        handleBeaconsRangedInterfaceList = new ArrayList<>();
    }

    public void init(@NonNull Context context, @NonNull String beaconRangeIdentifier, @NonNull String uuid){
        super.init(true);
        this.context = context.getApplicationContext();
        this.uuid = uuid;
        this.beaconRangeIdentifier = beaconRangeIdentifier;
        if(baseSDKUsed == ALT_BEACON) {
            altBeaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this.context);
            altBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_LAYOUT));
            backgroundPowerSaver = new BackgroundPowerSaver(this.context);
        } else if(baseSDKUsed == ESTIMOTE){
            estimoteBeaconManager = new com.estimote.sdk.BeaconManager(this.context);
            estimoteRegion = new com.estimote.sdk.Region(beaconRangeIdentifier, uuid, null, null);
            estimoteBeaconManager.setRangingListener(new com.estimote.sdk.BeaconManager.RangingListener() {
                @Override
                public void onBeaconsDiscovered(com.estimote.sdk.Region region, final List<com.estimote.sdk.Beacon> beacons) {
                    new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... voids) {
                            Log.d(TAG, "Beacons ranged: " + beacons.size());

                            // Adding INVALID_DISTANCE to all beacons
                            for(BeaconObject beaconObject:arrayBeaconsCache){
                                beaconObject.addLastDistance(BeaconObject.INVALID_DISTANCE);
                            }

                            List<BeaconObject> arrayBeaconRanged = new ArrayList<>();
                            for (com.estimote.sdk.Beacon beaconRanged : beacons) {
                                String uuid = beaconRanged.getProximityUUID();
                                int major = beaconRanged.getMajor();
                                int minor = beaconRanged.getMinor();

                                BeaconObject beaconFoundCache = getBeaconObject(uuid, major, minor);
                                if (beaconFoundCache != null) {
                                    double distance = Utils.computeAccuracy(beaconRanged);

                                    // Setting correct last distance to ranged beacons. For beacons that were not found, this value will remain INVALID_DISTANCE.
                                    beaconFoundCache.setLastDistance(distance);
                                    arrayBeaconRanged.add(beaconFoundCache);
                                }
                            }

                            // Send the arrays to the respective modules
                            for (HandleBeaconsRangedInterface handleBeaconsRangedInterface : handleBeaconsRangedInterfaceList) {
                                handleBeaconsRangedInterface.handleBeaconRanged(arrayBeaconRanged);
                            }
                            return null;
                        }
                    }.execute();


                }
            });
        }
    }

    public void destroy(){
        if(baseSDKUsed == ESTIMOTE) {
            estimoteBeaconManager.disconnect();
        }
    }

    public void addHandleBeaconsRangedInterface(HandleBeaconsRangedInterface handleBeaconsRangedInterface){
        handleBeaconsRangedInterfaceList.add(handleBeaconsRangedInterface);
    }

    public void removeHandleBeaconsRangedInterface(HandleBeaconsRangedInterface handleBeaconsRangedInterface){
        handleBeaconsRangedInterfaceList.remove(handleBeaconsRangedInterface);
    }

    public void handleBeaconsOnCache(List<BeaconObject> arrayNewBeacons){
        arrayBeaconsCache.clear();
        arrayBeaconsCache.addAll(arrayNewBeacons);
    }

    public BeaconObject getBeaconObject(String remoteId){
        for(BeaconObject beaconObject : arrayBeaconsCache){
            if(beaconObject.isEqual(remoteId)){
                return beaconObject;
            }
        }
        return null;
    }

    public BeaconObject getBeaconObject(String uuid, int major, int minor){
        for(BeaconObject beaconObject : arrayBeaconsCache){
            if(beaconObject.isEqual(uuid, major, minor)){
                return beaconObject;
            }
        }
        return null;
    }

    public void startMyBeaconsManagerOperation(){
        if(TEST_FILE != null){
            GateManager.getInstance().initTestCase(TEST_FILE, uuid);
        } else if(baseSDKUsed == ALT_BEACON){
            altBeaconManager.bind(this);
        } else if(baseSDKUsed == ESTIMOTE){
            estimoteBeaconManager.connect(new com.estimote.sdk.BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    try {
                        estimoteBeaconManager.startRanging(estimoteRegion);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Cannot start ranging", e);
                    }
                }
            });
        }
    }

    public void stopMyBeaconsManagerOperation(){
        GateManager.getInstance().clearFile();
        if(baseSDKUsed == ALT_BEACON) {
            altBeaconManager.unbind(this);
        } else if(baseSDKUsed == ESTIMOTE){
            try {
                estimoteBeaconManager.stopRanging(estimoteRegion);
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot stop but it does not matter now", e);
            }
        }
    }

    /**
     * Bluetooth
     */
    public boolean verifyBluetooth() {
        if(baseSDKUsed == ALT_BEACON) {
            return altBeaconManager.checkAvailability();
        } else if(baseSDKUsed == ESTIMOTE){
            return estimoteBeaconManager.hasBluetooth() && estimoteBeaconManager.isBluetoothEnabled();
        }
        return false;
    }

    /**
     * BeaconConsumer callbacks - AltBeacon
     */

    @Override
    public void onBeaconServiceConnect() {
        altBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beaconsRanged, Region region) {
                Log.d(TAG, "Beacons ranged: " + beaconsRanged.size());

                // Adding INVALID_DISTANCE to all beacons
                for(BeaconObject beaconObject:arrayBeaconsCache){
                    beaconObject.addLastDistance(BeaconObject.INVALID_DISTANCE);
                }

                List<BeaconObject> arrayBeaconRanged = new ArrayList<>();
                for (Beacon beaconRanged : beaconsRanged) {
                    String uuid = beaconRanged.getId1().toUuidString();
                    int major = beaconRanged.getId2().toInt();
                    int minor = beaconRanged.getId3().toInt();

                    BeaconObject beaconFoundCache = getBeaconObject(uuid, major, minor);
                    if (beaconFoundCache != null) {
                        // Setting correct last distance to ranged beacons. For beacons that were not found, this value will remain INVALID_DISTANCE.
                        beaconFoundCache.setLastDistance(beaconRanged.getDistance());
                        arrayBeaconRanged.add(beaconFoundCache);
                    }
                }

                // Send the arrays to the respective modules
                for (HandleBeaconsRangedInterface handleBeaconsRangedInterface : handleBeaconsRangedInterfaceList) {
                    handleBeaconsRangedInterface.handleBeaconRanged(arrayBeaconRanged);
                }
            }
        });

        altBeaconManager.setBackgroundBetweenScanPeriod(0L);
        altBeaconManager.setBackgroundScanPeriod(1100L);
        altBeaconManager.setBackgroundMode(true);

        try {
            Identifier identifier = null;
            if(uuid != null){
                identifier = Identifier.parse(uuid);
            }
            altBeaconManager.startRangingBeaconsInRegion(new Region(beaconRangeIdentifier, identifier, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Context getApplicationContext() {
        return context;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return context.bindService(intent, serviceConnection, i);
    }
}
