package guilherme.krzisch.com.mybeaconclient.mybeaconframework.ServerModule;

import android.content.Context;

import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.MyBeaconServerHandlerInterface;

public interface ServerModuleInterface {
    void init(Context context, String baseUrl, int port, String appCode);
    String getCurrentUsername();
    boolean isUserLoggedIn();
    void createUser(String username, String password, final MyBeaconServerHandlerInterface handler);
    boolean createUser(String username, String password);
    void login(String username, String password, final MyBeaconServerHandlerInterface handler);
    boolean login(String username, String password);
    void logout(final MyBeaconServerHandlerInterface handler);
    boolean logout();
    void syncAll();
}
