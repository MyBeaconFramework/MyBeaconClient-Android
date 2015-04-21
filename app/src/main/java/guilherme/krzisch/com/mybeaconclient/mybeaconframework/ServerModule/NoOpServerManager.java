package guilherme.krzisch.com.mybeaconclient.mybeaconframework.ServerModule;

import android.content.Context;

import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.MyBeaconServerHandlerInterface;

public class NoOpServerManager implements ServerModuleInterface {
    @Override
    public void init(Context context, String baseUrl, int port, String appCode) {

    }

    @Override
    public String getCurrentUsername() {
        return null;
    }

    @Override
    public boolean isUserLoggedIn() {
        return true;
    }

    @Override
    public void createUser(String username, String password, MyBeaconServerHandlerInterface handler) {

    }

    @Override
    public boolean createUser(String username, String password) {
        return false;
    }

    @Override
    public void login(String username, String password, MyBeaconServerHandlerInterface handler) {

    }

    @Override
    public boolean login(String username, String password) {
        return false;
    }

    @Override
    public void logout(MyBeaconServerHandlerInterface handler) {

    }

    @Override
    public boolean logout() {
        return false;
    }

    @Override
    public void syncAll() {

    }
}
