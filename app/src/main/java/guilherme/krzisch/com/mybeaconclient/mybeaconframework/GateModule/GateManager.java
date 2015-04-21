package guilherme.krzisch.com.mybeaconclient.mybeaconframework.GateModule;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import guilherme.krzisch.com.mybeaconclient.MyApp;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.BaseModule;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.BeaconObject;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.HandleBeaconsRangedInterface;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.MyBeaconManager;

public class GateManager extends BaseModule implements HandleBeaconsRangedInterface {
    private static boolean IS_DEBUG = true;

    private List<GateObject> gateObjectList;

    // UserPassedByGateInterface
    private List<UserPassedByGateInterface> userPassedByGateInterfaceList;

    private static GateManager ourInstance = new GateManager();

    public static GateManager getInstance() {
        return ourInstance;
    }

    private GateManager() {
        gateObjectList= new ArrayList<>();
        userPassedByGateInterfaceList = new ArrayList<>();
    }

    public void init(){
        super.init(true);
        MyBeaconManager.getInstance().addHandleBeaconsRangedInterface(this);
    }

    public void handleGatesOnCache(List<GateObject> arrayNewGates){
        gateObjectList.clear();
        gateObjectList.addAll(arrayNewGates);
    }

    public void addUserPassedByGateInterface(UserPassedByGateInterface userPassedByGateInterface){
        userPassedByGateInterfaceList.add(userPassedByGateInterface);
    }

    public void removeUserPassedByGateInterface(HandleBeaconsRangedInterface userPassedByGateInterface){
        userPassedByGateInterfaceList.remove(userPassedByGateInterface);
    }

    @Override
    public void handleBeaconRanged(List<BeaconObject> arrayBeaconsRanged) {
        for(GateObject gateObject:gateObjectList){
            analyzeGate(gateObject);
        }
    }

    private void analyzeGate(GateObject gateObject) {
        writeToFile(gateObject);
        BeaconObject beaconObject = gateObject.checkStateAndEndOfOperation();
        if(beaconObject != null){
            gateObject.resetData();
            for(UserPassedByGateInterface userPassedByGateInterface : userPassedByGateInterfaceList){
                writeToFile("UserPassedByGateObject with last beacon = " + beaconObject.getRemoteId());
                userPassedByGateInterface.userPassedByGate(gateObject, beaconObject);
            }
        }
    }

    private File file;
    private File getFile(){
        if(file == null){
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "file" + String.valueOf(new Date()
                    .getTime()) + ".txt");
        }
        return file;
    }

    public void clearFile() {
        file = null;
    }

    private void appendText(String text) {
        if (!getFile().exists()) {
            try {
                getFile().createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try{
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(getFile(), true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void writeToFile(GateObject gateObject) {
        if(IS_DEBUG && gateObject.getBeaconA().getMinor() == 3){
            appendText(gateObject.getBeaconA().getArrayLastDistanceRegistered().get(0) + "\t\t\t\t\t"
                    + gateObject.getBeaconB().getArrayLastDistanceRegistered().get(0));
        }
    }

    public void writeToFile(String text){
        if(IS_DEBUG){
            appendText(text);
        }
    }

    public void initTestCase(final String testFile, final String uuid) {
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                boolean previousIsDebug = IS_DEBUG;
                IS_DEBUG = false;
                AssetManager am = MyApp.getAppContext().getAssets();
                InputStream is = null;
                BufferedReader reader = null;
                try {
                    is = am.open(testFile);
                    reader = new BufferedReader(new InputStreamReader(is));
                    String mLine = reader.readLine();
                    while (mLine != null) {
                        if(mLine.length() > 2 && mLine.startsWith("//")){
                            mLine = reader.readLine();
                            continue;
                        }

                        String[] split = mLine.split("\\s+");
                        if(split.length == 2){
                            double distanceA = Double.valueOf(split[0]);
                            double distanceB = Double.valueOf(split[1]);

                            int majorA = 40;
                            int minorA = 3;
                            int majorB = 40;
                            int minorB = 4;

                            BeaconObject beaconA = MyBeaconManager.getInstance().getBeaconObject(uuid, majorA, minorA);
                            BeaconObject beaconB = MyBeaconManager.getInstance().getBeaconObject(uuid, majorB, minorB);

                            if(beaconA != null && beaconB != null){
                                beaconA.addLastDistance(distanceA);
                                beaconB.addLastDistance(distanceB);

                                List<BeaconObject> arrayBeaconGate = new ArrayList<>();
                                arrayBeaconGate.add(beaconA);
                                arrayBeaconGate.add(beaconB);
                                GateManager.getInstance().handleBeaconRanged(arrayBeaconGate);
                            }
                        }
                        mLine = reader.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(is != null){
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                IS_DEBUG = previousIsDebug;
                return null;
            }
        }.execute();
    }
}
