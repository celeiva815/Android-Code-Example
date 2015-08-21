package social.laika.app.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import social.laika.app.R;
import social.laika.app.fragments.NavigationDrawerFragment;
import social.laika.app.fragments.PlaceHolderFragment;
import social.laika.app.models.AdoptDogForm;
import social.laika.app.models.AlarmReminder;
import social.laika.app.models.CalendarReminder;
import social.laika.app.models.Dog;
import social.laika.app.models.Event;
import social.laika.app.models.Owner;
import social.laika.app.models.OwnerDog;
import social.laika.app.models.Personality;
import social.laika.app.models.Publication;
import social.laika.app.models.Photo;
import social.laika.app.models.Story;
import social.laika.app.models.Tip;
import social.laika.app.models.UserAdoptDog;
import social.laika.app.models.VetVisit;
import social.laika.app.network.RequestManager;
import social.laika.app.network.VolleyManager;
import social.laika.app.network.gcm.LaikaRegistrationIntentService;
import social.laika.app.network.sync.SyncUtils;
import social.laika.app.responses.PostulatedDogsResponse;
import social.laika.app.utils.Do;
import social.laika.app.utils.PrefsManager;
import social.laika.app.utils.Tag;

/**
 * Created by Tito_Leiva on 10-02-15.
 */
public class BaseActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String TAG = BaseActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    protected BroadcastReceiver mRegistrationBroadcastReceiver;
    protected NavigationDrawerFragment mNavigationDrawerFragment;
    protected CharSequence mTitle;
    protected PlaceHolderFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_base_activity);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.laika_red));

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), mTitle);

        registerGCM();
        SyncUtils.createSyncAccount(getApplicationContext());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        if (!this.getClass().equals(MainActivity.class))
            getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(PrefsManager.GCM_REGISTRATION_COMPLETE));

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {

        // update the main content by replacing fragments

        Intent intent;
        Context context = getApplicationContext();

        switch (position) {
            case 0: // Añadir nueva mascota
                Do.changeActivity(context, CreateDogActivity.class,
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                break;

            case 1: // Mis postulaciones
                openPostulatedDogs();

                break;

            case 2: // Favoritos

                intent = new Intent(context, PublicationsActivity.class);
                intent.putExtra(PublicationsActivity.KEY_FAVORITE, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case 3: // Mi perfil

                intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.KEY_OWNER_ID, PrefsManager.getUserId(context));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


                break;

            case 4: // Cerrar Sesión

                clearDataBase();
                Do.changeActivity(context, TutorialActivity.class, this,
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                break;

            default:
                intent = new Intent(context, MainActivity.class);
                startActivity(intent);

                break;
        }
    }

    public void setActivityView(View view) {

    }

    public void createFragmentView(int layoutId) {

        //FIXME agregar la clase hija
        mFragment = new PlaceHolderFragment(layoutId, this);
        getFragmentManager().beginTransaction().add(R.id.container, mFragment).commit();
    }

    private void clearDataBase() {

        PrefsManager.clearPrefs(getApplicationContext());
        Dog.deleteAll();
        Owner.deleteAll();
        OwnerDog.deleteAll();
        UserAdoptDog.deleteAll();
        Photo.deleteAll();
        VetVisit.deleteAll();
        AdoptDogForm.deleteAll();
        CalendarReminder.deleteAll();
        AlarmReminder.deleteAll();
        Event.deleteAll();
        Publication.deleteAll();
        Tip.deleteAll();
        Story.deleteAll();
        Personality.deleteAll();

    }

    private void openPostulatedDogs() {

        Context context = getApplicationContext();

        if (Do.isNetworkAvailable(context)){
            requestPostulations(context);

        } else {
            Do.changeActivity(context, PostulatedDogsFragmentActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK);
            Do.showLongToast("Mostrando postulaciones guardadas", context);

        }
    }

    private void requestPostulations(Context context) {

        ProgressDialog dialog = ProgressDialog.show(BaseActivity.this, "Espere un momento",
                "Estamos actualizando el estado de tus postulaciones...");

        PostulatedDogsResponse response = new PostulatedDogsResponse(this, dialog);
        Request adoptDogRequest = RequestManager.getRequest(null,
                RequestManager.ADDRESS_USER_POSTULATIONS, response, response,
                PrefsManager.getUserToken(getApplicationContext()));

        VolleyManager.getInstance(getApplicationContext())
                .addToRequestQueue(adoptDogRequest, TAG);

    }

    protected void registerGCM() {

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                boolean sentToken = PrefsManager.getSentTokenToServer(getApplicationContext());

            }
        };

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, LaikaRegistrationIntentService.class);
            startService(intent);
        }


    }

    protected boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
