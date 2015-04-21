package guilherme.krzisch.com.mybeaconclient.mybeaconframework.ServerModule;

import android.content.Context;
import android.util.Log;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasHandler;
import com.baasbox.android.BaasResult;
import com.baasbox.android.BaasUser;
import com.baasbox.android.Rest;
import com.baasbox.android.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.BaseModule;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.BeaconObject;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.MyBeaconManager;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.MyBeaconServerHandlerInterface;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.GateModule.GateManager;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.GateModule.GateObject;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.GateModule.UserPassedByGateInterface;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.ObjectModule.ObjectTrackerManager;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.ObjectModule.UserPassedByObjectInterface;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.PersonModule.PersonTrackerManager;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.PersonModule.UserPassedByBeaconSectorInterface;

public class BaasBoxServerManager extends BaseModule implements ServerModuleInterface, UserPassedByGateInterface, UserPassedByObjectInterface, UserPassedByBeaconSectorInterface {
    public static final String TAG = "ServerManager";
    private static final int SOCKET_TIMEOUT = 60000;
    public static final String BEACON_COLLECTION = "Beacon";
    public static final String GATE_COLLECTION = "Gate";

    private static BaasBoxServerManager ourInstance = new BaasBoxServerManager();

    public static BaasBoxServerManager getInstance() {
        return ourInstance;
    }

    private BaasBoxServerManager() {
    }

    public void init(Context context, String baseUrl, int port, String appCode){
        super.init(true);
        BaasBox.Builder b = new BaasBox.Builder(context);
        b.setApiDomain(baseUrl)
                .setAppCode(appCode)
                .setPort(port)
                .setUseHttps(false)
                .setHttpSocketTimeout(SOCKET_TIMEOUT)
                .init();
        GateManager.getInstance().addUserPassedByGateInterface(this);
        ObjectTrackerManager.getInstance().addUserPassedByObjectInterface(this);
        PersonTrackerManager.getInstance().addUserPassedByBeaconSectorInterface(this);
    }

    /**
     * User lifecycle
     */

    public String getCurrentUsername() {
        return isUserLoggedIn()? BaasUser.current().getName() : null;
    }

    public boolean isUserLoggedIn(){
        return BaasUser.current() != null;
    }

    // Async
    public void createUser(String username, String password, final MyBeaconServerHandlerInterface handler){
        BaasUser user = BaasUser.withUserName(username).setPassword(password);
        user.signup(new BaasHandler<BaasUser>() {
            @Override
            public void handle(BaasResult<BaasUser> baasResult) {
                handler.handle(baasResult.isSuccess());
            }
        });
    }

    // Sync
    public boolean createUser(String username, String password){
        BaasUser user = BaasUser.withUserName(username).setPassword(password);
        BaasResult<BaasUser> baasUserBaasResult = user.signupSync();
        return baasUserBaasResult.isSuccess();
    }

    // Async
    public void login(String username, String password, final MyBeaconServerHandlerInterface handler){
        BaasUser user = BaasUser.withUserName(username).setPassword(password);
        user.login(new BaasHandler<BaasUser>() {
            @Override
            public void handle(BaasResult<BaasUser> baasResult) {
                handler.handle(baasResult.isSuccess());
            }
        });
    }

    // Sync
    public boolean login(String username, String password){
        BaasUser user = BaasUser.withUserName(username).setPassword(password);
        BaasResult<BaasUser> baasUserBaasResult = user.loginSync();
        return baasUserBaasResult.isSuccess();
    }

    // Async
    public void logout(final MyBeaconServerHandlerInterface handler){
        BaasUser current = BaasUser.current();
        if(current != null){
            current.logout(new BaasHandler<Void>() {
                @Override
                public void handle(BaasResult<Void> baasResult) {
                    handler.handle(baasResult.isSuccess());
                }
            });
        } else {
            handler.handle(true);
        }
    }

    // Sync
    public boolean logout(){
        BaasUser current = BaasUser.current();
        if(current != null){
            BaasResult<Void> voidBaasResult = current.logoutSync();
            return voidBaasResult.isSuccess();
        } else{
            return true;
        }
    }

    /**
     * Sync
     */

    public void syncAll(){
        List<BeaconObject> beaconObjectList = fetchAllBeacons();
        MyBeaconManager.getInstance().handleBeaconsOnCache(beaconObjectList);

        List<GateObject> gateObjects = fetchAllGates();
        GateManager.getInstance().handleGatesOnCache(gateObjects);
    }

    private List<BeaconObject> fetchAllBeacons(){
        BaasResult<List<BaasDocument>> listBaasResult = BaasDocument.fetchAllSync(BEACON_COLLECTION);
        if (listBaasResult.isSuccess()) {
            List<BeaconObject> beaconObjectList = new ArrayList<>();
            for (BaasDocument doc : listBaasResult.value()) {
                String remoteId = doc.getId();
                int remoteBeaconType = doc.getInt(BeaconObject.BEACON_TYPE, -1);
                String remoteUUID = doc.getString(BeaconObject.UUID_FIELD);
                int remoteMajor = doc.getInt(BeaconObject.MAJOR, -1);
                int remoteMinor = doc.getInt(BeaconObject.MINOR, -1);
                String remoteDescription = doc.getString(BeaconObject.DESCRIPTION);
                double remoteLatitude = doc.getDouble(BeaconObject.LATITUDE, -1);
                double remoteLongitude = doc.getDouble(BeaconObject.LONGITUDE, -1);

                BeaconObject beacon = new BeaconObject(remoteId, remoteUUID, remoteMajor, remoteMinor, remoteBeaconType, remoteDescription,
                        remoteLatitude, remoteLongitude);
                beaconObjectList.add(beacon);
            }
            return beaconObjectList;
        } else{
            return null;
        }
    }

    private List<GateObject> fetchAllGates(){
        BaasResult<List<BaasDocument>> listBaasResult = BaasDocument.fetchAllSync(GATE_COLLECTION);
        if (listBaasResult.isSuccess()) {
            List<GateObject> gateObjectList = new ArrayList<>();
            for (BaasDocument doc : listBaasResult.value()) {
                String remoteId = doc.getId();
                String remoteDescription = doc.getString(GateObject.DESCRIPTION);
                String remoteBeaconAId = doc.getString(GateObject.BEACON_A_ID);
                String remoteBeaconBId = doc.getString(GateObject.BEACON_B_ID);

                BeaconObject beaconObjectA = MyBeaconManager.getInstance().getBeaconObject(remoteBeaconAId);
                BeaconObject beaconObjectB = MyBeaconManager.getInstance().getBeaconObject(remoteBeaconBId);

                if(beaconObjectA != null && beaconObjectB != null) {
                    GateObject gate = new GateObject(remoteId, beaconObjectA, beaconObjectB, remoteDescription);
                    gateObjectList.add(gate);
                }
            }
            return gateObjectList;
        } else {
            return null;
        }
    }

    /**
     * Update data to server
     */

    // URL
    private static final String INDICATE_USER_PASSED_BY_SECTOR_ENDPOINT = "plugin/indicate.UserPassedBySector";
    // Params
    public static final String SECTOR_BEACON_ID = "beaconId";

    // URL
    private static final String INDICATE_USER_PASSED_BY_GATE_ENDPOINT = "plugin/indicate.UserPassedByGate";
    // Params
    private static final String GATE_LAST_GATE_BEACON_ID_ = "lastGateBeaconId";
    private static final String GATE_GATE_ID = "gateId";

    // URL
    private static final String INDICATE_USER_PASSED_BY_OBJECT_ENDPOINT = "plugin/indicate.UserPassedByObject";
    // Params
    private static final String OBJECT_CLOSER_BEACON_SECTOR = "closerBeaconSector";
    private static final String OBJECT_BEACON_LINKED_OBJECT_ID = "beaconLinkedObjectId"; // ID of beacon linked to the object

    @Override
    public void userPassedByBeaconSector(BeaconObject beaconObject){
        Log.d(TAG, "[Sending] User passed by sector with beacon [" + beaconObject.getMajor() + "][" + beaconObject.getMinor() + "]");

        JsonObject jsonObject = new JsonObject();
        jsonObject.put(SECTOR_BEACON_ID, beaconObject.getRemoteId());
        BaasResult<JsonObject> res = BaasBox.rest().sync(Rest.Method.POST, INDICATE_USER_PASSED_BY_SECTOR_ENDPOINT, jsonObject, true);
        if (res.isSuccess()) {
            Log.d(TAG, "[Sent] User passed by sector with beacon [" + beaconObject.getMajor() + "][" + beaconObject.getMinor() + "]");
        } else {
            Log.d(TAG, "[Error] User passed by sector with beacon [" + beaconObject.getMajor() + "][" + beaconObject.getMinor() + "]");
            Log.e("faddfas", "Error! need to send another time. " + res.error().getLocalizedMessage());
        }
    }

    @Override
    public void userPassedByObject(BeaconObject beaconLinkedObject, BeaconObject closerBeaconSector){
        Log.d(TAG, "[Sending] User passed by object [" +  beaconLinkedObject.getMajor() + "][" + beaconLinkedObject.getMinor() + "]" +
                "with closer beacon [" + closerBeaconSector.getMajor() + "][" + closerBeaconSector.getMinor() + "]");

        JsonObject jsonObject = new JsonObject();
        jsonObject.put(OBJECT_CLOSER_BEACON_SECTOR, closerBeaconSector.getRemoteId());
        jsonObject.put(OBJECT_BEACON_LINKED_OBJECT_ID, beaconLinkedObject.getRemoteId());

        BaasResult<JsonObject> res = BaasBox.rest().sync(Rest.Method.POST, INDICATE_USER_PASSED_BY_OBJECT_ENDPOINT, jsonObject, true);
        if (res.isSuccess()) {
            Log.d(TAG, "[Sent] User passed by object [" +  beaconLinkedObject.getMajor() + "][" + beaconLinkedObject.getMinor() + "]" +
                    "with closer beacon [" + closerBeaconSector.getMajor() + "][" + closerBeaconSector.getMinor() + "]");
        } else {
            Log.d(TAG, "[Error] User passed by object [" +  beaconLinkedObject.getMajor() + "][" + beaconLinkedObject.getMinor() + "]" +
                    "with closer beacon [" + closerBeaconSector.getMajor() + "][" + closerBeaconSector.getMinor() + "]");
            Log.e("faddfas", "Error! need to send another time. " + res.error().getLocalizedMessage());
        }
    }

    @Override
    public void userPassedByGate(GateObject gate, BeaconObject closerBeacon) {
        Log.d(TAG, "[Sending] User passed by gate [" +  gate.getRemoteId() + "][" +
                "with closer beacon [" + closerBeacon.getMajor() + "][" + closerBeacon.getMinor() + "]");

        JsonObject jsonObject = new JsonObject();
        jsonObject.put(GATE_LAST_GATE_BEACON_ID_, closerBeacon.getRemoteId());
        jsonObject.put(GATE_GATE_ID, gate.getRemoteId());

        BaasResult<JsonObject> res = BaasBox.rest().sync(Rest.Method.POST,INDICATE_USER_PASSED_BY_GATE_ENDPOINT, jsonObject, true);
        if (res.isSuccess()) {
            Log.d(TAG, "[Sent] User passed by gate [" +  gate.getRemoteId() + "][" +
                    "with closer beacon [" + closerBeacon.getMajor() + "][" + closerBeacon.getMinor() + "]");
        } else {
            Log.d(TAG, "[Error] User passed by gate [" +  gate.getRemoteId() + "][" +
                    "with closer beacon [" + closerBeacon.getMajor() + "][" + closerBeacon.getMinor() + "]");
            Log.e("faddfas", "Error! need to send another time. " + res.error().getLocalizedMessage());
        }
    }
}
