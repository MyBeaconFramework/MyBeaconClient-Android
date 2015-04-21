package guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule;

public abstract class BaseModule {
    protected boolean isActive = false;

    public boolean isActive() {
        return isActive;
    }

    public void init(boolean isActive){
        this.isActive = isActive;
    }
}
