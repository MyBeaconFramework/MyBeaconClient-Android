package guilherme.krzisch.com.mybeaconclient.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import guilherme.krzisch.com.mybeaconclient.R;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.MyBeaconFacade;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.BasicModule.MyBeaconServerHandlerInterface;
import guilherme.krzisch.com.mybeaconclient.mybeaconframework.GateModule.GateManager;
import guilherme.krzisch.com.mybeaconclient.view.login.LoginActivity;


public class MainActivity extends AppCompatActivity {
    @InjectView(R.id.usernameTextView) TextView usernameTextView;
    @InjectView(R.id.mainActivityView) View mainActivityView;
    @InjectView(R.id.editTextGateDescription) EditText editTextGateDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        if(MyBeaconFacade.isUserLoggedIn()){
            usernameTextView.setText(MyBeaconFacade.getCurrentUsername());
            if(!MyBeaconFacade.verifyBluetooth()) {
                mainActivityView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        } else{
            goToLoginActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        MyBeaconFacade.logout(new MyBeaconServerHandlerInterface() {
            @Override
            public void handle(final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            goToLoginActivity();
                        } else {
                            Toast.makeText(getBaseContext(), "Could not logout!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void goToLoginActivity(){
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void sync(View view) {
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                MyBeaconFacade.syncAll();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getBaseContext(), "Synced!", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    public void startOperation(View view) {
        if(!TextUtils.isEmpty(editTextGateDescription.getText().toString())) {
            GateManager.getInstance().writeToFile("//" + editTextGateDescription.getText().toString());
        }
        MyBeaconFacade.startMyBeaconsManagerOperation();
    }

    public void stopOperation(View view) {
        MyBeaconFacade.stopMyBeaconsManagerOperation();
    }
}
