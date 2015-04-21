package guilherme.krzisch.com.mybeaconclient;

import android.app.Application;
import android.content.Context;

import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.MyBeaconFacade;

public class MyApp extends Application {
    private static final String BAASBOX_URL = "put_here_server_url";
    private static final int PORT = 80;
    private static final String APP_CODE = "put_here_app_code";
    private static final String BEACON_RANGE_IDENTIFIER = "put_here_beacon_range_identifier";
    private static final String UUID = "put_here_beacon_uuid";

    // App context
    private static Context context;

    public static Context getAppContext() {
        return MyApp.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.context = getApplicationContext();
        MyBeaconFacade.initFramework(this, true, BAASBOX_URL, PORT, APP_CODE, BEACON_RANGE_IDENTIFIER, UUID);
    }
}
