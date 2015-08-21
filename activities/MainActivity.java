package cl.magnet.puntotrip.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cl.magnet.puntotrip.R;
import cl.magnet.puntotrip.models.Company;
import cl.magnet.puntotrip.models.Company$Table;
import cl.magnet.puntotrip.utils.Do;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {

    public static final int STARTED = 1;
    public static final int PAUSED = 2;
    public static final int CODE_SELECT_COMPANY = 1;
    public static int INTERVAL_TIME = 10000;
    public static int INTERVAL_DISTANCE = 5;

    public LocationManager mLocationManager;
    public int mState;
    public Button mMainButton;
    public TextView mCompanyTextView;
    public Location mCurrentBestLocation = null;
    public Company mCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActivityView();
        setTitle("PuntoTrip");

        mState = PAUSED;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Do.showShortToast("el GPS de tu dispositivo está activado", this);
    }

    private void setActivityView() {

        mMainButton = (Button) findViewById(R.id.main_button);
        mCompanyTextView = (TextView) findViewById(R.id.name_company_textview);

        mMainButton.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            showGPSDisabledAlertToUser();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CODE_SELECT_COMPANY && resultCode == RESULT_OK && data != null) {

            long companyId = data.getLongExtra(Company$Table.ID, 0);
            mCompany = Company.getSingle(companyId);

            startGPS();

        }
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("El GPS de tu dispositivo está activado. ¿Desea activarlo?")
                .setCancelable(false)
                .setPositiveButton("Ir a la activación",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void onClick(View view) {

        switch (mState) {

            case STARTED:

                pauseGPS();

                break;

            case PAUSED:

                if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    selectCompany();

                } else {
                    showGPSDisabledAlertToUser();
                }

                break;

        }
    }

    public void startGPS() {

        Do.showShortToast("Se ha iniciado el seguimiento por GPS", this);
        mState = STARTED;

        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, INTERVAL_TIME, INTERVAL_DISTANCE, this);

        mCurrentBestLocation = getLastBestLocation();
        showCurrentLocation();

        mMainButton.setText("Detener");
        mCompanyTextView.setText(mCompany.getName());

    }

    public void pauseGPS() {

        mState = PAUSED;

        mLocationManager.removeUpdates(this);
        Do.showShortToast("Seguimiento detenido", this);
        mMainButton.setText("Iniciar");
        mCompanyTextView.setText(Do.getRString(this, R.string.company_not_selected));

    }

    public void selectCompany() {

        Intent intent = new Intent(this, SelectCompanyActivity.class);
        startActivityForResult(intent, CODE_SELECT_COMPANY);
    }

    @Override
    public void onLocationChanged(Location location) {

        makeUseOfNewLocation(location);
        showCurrentLocation();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

        startGPS();

    }

    @Override
    public void onProviderDisabled(String s) {

        pauseGPS();

    }

    public void showCurrentLocation() {

        if (mCurrentBestLocation != null) {

            Do.showShortToast(
                    "Ubicación Actual\nLatitud: " + mCurrentBestLocation.getLatitude() + ", Longitud: "
                            + mCurrentBestLocation.getLongitude(), this);

            Log.i("GPS Coords", "" + mCurrentBestLocation.getLongitude() + ", " +
                    mCurrentBestLocation.getLatitude());

        }
    }

    void makeUseOfNewLocation(Location location) {
        if (isBetterLocation(location, mCurrentBestLocation)) {
            mCurrentBestLocation = location;
        }
    }

    private Location getLastBestLocation() {
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }

    public boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > INTERVAL_TIME;
        boolean isSignificantlyOlder = timeDelta < -INTERVAL_TIME;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location,
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse.
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}
