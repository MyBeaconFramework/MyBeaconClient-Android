package guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule;

import android.content.Context;

import java.util.List;

import guilherme.krzisch.com.mybeaconclient.mybeaconframework.GateModule.GateManager;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.GateModule.GateObject;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.ObjectModule.ObjectTrackerManager;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.PersonModule.PersonTrackerManager;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.ServerModule.NoOpServerManager;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.ServerModule.BaasBoxServerManager;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.ServerModule.ServerModuleInterface;

public class MyBeaconFacade {
    private static ServerModuleInterface serverModule;

    private static void init(Context context, String beaconRangeIdentifier, String uuid){
        MyBeaconManager.getInstance().init(context, beaconRangeIdentifier, uuid); // this needs to be the first, to init the listeners
        GateManager.getInstance().init();
        ObjectTrackerManager.getInstance().init();
        PersonTrackerManager.getInstance().init();
    }

    public static void initFramework(Context context, boolean isUsingServerModule, String baseUrl, int port,  String appCode,
                                     String beaconRangeIdentifier, String uuid){
        init(context, beaconRangeIdentifier, uuid);
        if(isUsingServerModule){
            serverModule = BaasBoxServerManager.getInstance();
            serverModule.init(context, baseUrl, port, appCode);
        } else{
            serverModule = new NoOpServerManager();
        }
    }

    public static void initFramework(Context context, String baseUrl, int port,  String appCode,
                                     String beaconRangeIdentifier, String uuid){
        initFramework(context, false, baseUrl, port, appCode, beaconRangeIdentifier, uuid);
    }

    public static void initFramework(Context context, ServerModuleInterface serverModuleParam, String beaconRangeIdentifier, String uuid){
        init(context,beaconRangeIdentifier,uuid);
        serverModule = serverModuleParam;
    }

    // Server module methods
    public static String getCurrentUsername(){
        return serverModule.getCurrentUsername();
    }

    public static boolean isUserLoggedIn(){
        return serverModule.isUserLoggedIn();
    }

    public static void createUserAsync(String username, String password, MyBeaconServerHandlerInterface handler){
        serverModule.createUser(username, password, handler);
    }

    public static boolean createUserSync(String username, String password){
        return serverModule.createUser(username, password);
    }

    public static void loginAsync(String username, String password, MyBeaconServerHandlerInterface handler){
        serverModule.login(username, password, handler);
    }

    public static boolean loginSync(String username, String password){
        return serverModule.login(username, password);
    }

    public static void logout(MyBeaconServerHandlerInterface handler){
        serverModule.logout(handler);
    }

    public static boolean logout(){
        return serverModule.logout();
    }

    public static void syncAll(){
        serverModule.syncAll();
    }

    // MyBeacon module methods
    public static void startMyBeaconsManagerOperation(){
        MyBeaconManager.getInstance().startMyBeaconsManagerOperation();
    }

    public static void stopMyBeaconsManagerOperation(){
        MyBeaconManager.getInstance().stopMyBeaconsManagerOperation();
    }

    public static boolean verifyBluetooth() {
        return MyBeaconManager.getInstance().verifyBluetooth();
    }

    // Local insertion methods
    public static void addBeaconsLocally(List<BeaconObject> arrayNewBeacons){
        MyBeaconManager.getInstance().handleBeaconsOnCache(arrayNewBeacons);
    }

    public static void addGatesLocally(List<GateObject> arrayNewGates){
        GateManager.getInstance().handleGatesOnCache(arrayNewGates);
    }
}
