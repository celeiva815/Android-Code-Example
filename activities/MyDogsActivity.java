package social.laika.app.activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.activeandroid.Model;
import com.activeandroid.content.ContentProvider;
import com.android.volley.Request;
import com.soundcloud.android.crop.Crop;

import org.json.JSONObject;

import social.laika.app.R;
import social.laika.app.fragments.AlbumMyDogFragment;
import social.laika.app.fragments.DogProfileFragment;
import social.laika.app.fragments.HistoryMyDogFragment;
import social.laika.app.fragments.OwnersFragment;
import social.laika.app.fragments.RemindersMyDogFragment;
import social.laika.app.fragments.VetVisitsFragment;
import social.laika.app.interfaces.Photographable;
import social.laika.app.listeners.AddOwnerDogOnClickListener;
import social.laika.app.models.AlarmReminder;
import social.laika.app.models.CalendarReminder;
import social.laika.app.models.Dog;
import social.laika.app.models.VetVisit;
import social.laika.app.network.RequestManager;
import social.laika.app.network.VolleyManager;
import social.laika.app.network.observers.AlarmReminderObserver;
import social.laika.app.network.observers.CalendarReminderObserver;
import social.laika.app.network.observers.VetVisitObserver;
import social.laika.app.network.sync.SyncUtils;
import social.laika.app.responses.ImageUploadResponse;
import social.laika.app.utils.Do;
import social.laika.app.utils.Photographer;
import social.laika.app.utils.Tag;
import social.laika.app.utils.views.CustomPagerSlidingTabStrip;

public class MyDogsActivity extends ActionBarActivity implements Photographable {

    public static final String ID = "id";
    public static final String DOG_ID = "dog_id";
    public static final String HISTORY = "Historial";
    public static final String REMINDERS = "Recordatorios";
    public static final String OWNERS = "Dueños";
    public static final String ALBUM = "Album";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    public static final String[] CONTENT = new String[]{HISTORY, REMINDERS, OWNERS, ALBUM};
    public static final String[] OPTIONS = new String[]{"Editar", "Agregar",
            "Crear Ficha", "Tomar Foto"};
    public static final String[] TITLES = new String[]{"Perfil", "Recordatorios",
            "Ficha Médica", "Álbum"};
    public static final int[] ICONS = new int[]{
            R.drawable.laika_history_selector,
            R.drawable.laika_reminder_selector,
            R.drawable.laika_owner_selector,
            R.drawable.laika_album_selector
    };


    public int mIdLayout = R.layout.activity_my_dog;
    public Menu mMenu;
    public Dog mDog;
    public DogProfileFragment mDogProfileFragment;
    public HistoryMyDogFragment mHistoryFragment;
    public VetVisitsFragment mVetVisitsFragment;
    public RemindersMyDogFragment mRemindersFragment;
    public OwnersFragment mOwnerFragment;
    public AlbumMyDogFragment mAlbumFragment;
    public String mCurrentPhotoPath;
    public ViewPager mPager;
    public PagerAdapter mPagerAdapter;
    public CustomPagerSlidingTabStrip mIndicator;
    public Photographer mPhotographer;
    public int mPosition;
    public boolean mChanged;

    ContentResolver mResolver;
    ContentObserver mAlarmObserver;
    ContentObserver mVetVisitObserver;
    ContentObserver mCalendarObserver;
    ContentObserver mPhotoObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(mIdLayout);
        setTitle(TITLES[0]);
        mPosition = 0;
        mChanged = false;

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.laika_red));
        actionBar.setDisplayHomeAsUpEnabled(true);

        int dogId = getIntent().getIntExtra(DOG_ID, 0);
        mDog = Dog.getSingleDog(dogId);
        mPhotographer = new Photographer();

        // Instantiate a ViewPager and a PagerAdapter
        mPager = (ViewPager) findViewById(R.id.my_dog_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        MyDogPageListener pagerListener = new MyDogPageListener();
        mIndicator = (CustomPagerSlidingTabStrip) findViewById(R.id.my_dog_indicator);

        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(pagerListener);
        mIndicator.setViewPager(mPager);
        mIndicator.setOnPageChangeListener(pagerListener);

    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshFragments();

        mChanged = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        /* We register the content observer */
        registerContentObserver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* We unregister the content observer */
        unregisterContentObserver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        if (!this.getClass().equals(MainActivity.class)) {

            getMenuInflater().inflate(R.menu.my_dog_menu, menu);

            this.mMenu = menu;
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.edit_profile_option).setVisible(false);
        menu.findItem(R.id.create_reminder_option).setVisible(false);
        menu.findItem(R.id.create_vet_visit_option).setVisible(false);
        menu.findItem(R.id.take_picture_option).setVisible(false);

        switch (mPosition) {

            case 0:

                menu.findItem(R.id.edit_profile_option).setVisible(true);
                return true;

            case 1:

                menu.findItem(R.id.create_reminder_option).setVisible(true);
                return true;

            case 2:

                menu.findItem(R.id.create_vet_visit_option).setVisible(true);
                return true;

            case 3:

                menu.findItem(R.id.take_picture_option).setVisible(true);
                return true;
        }

        return false;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        mChanged = true;

        switch (id) {

            case android.R.id.home:

                super.onBackPressed();
                return true;

            case R.id.edit_profile_option:
            case R.id.edit_dog_profile_settings:

                changeActivity(EditDogActivity.class);

                return true;

            case R.id.create_reminder_option:

                changeActivity(CreateReminderActivity.class);

                return true;

            case R.id.create_vet_visit_option:

                changeActivity(CreateVetVisitActivity.class);

                return true;

            case R.id.take_picture_settings:
            case R.id.take_picture_option:

                takePhoto();

                return true;

            case R.id.add_owner_settings:

                addOwner();

                return true;

            case R.id.delete_settings:

                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void addOwner() {

        AddOwnerDogOnClickListener ownerManager = new AddOwnerDogOnClickListener(mDog);
        ownerManager.addOwner(MyDogsActivity.this, mDogProfileFragment);

    }

    public void setDogProfileFragment(Dog mDog) {

        if (mDogProfileFragment != null) {
            mDogProfileFragment.refresh();

        } else {
            mDogProfileFragment = DogProfileFragment.newInstance(mDog.mDogId);
        }

        mPager.setCurrentItem(getPagerPosition(HISTORY), true);

    }

    public void setReminderFragment(AlarmReminder alarmReminder) {

        mRemindersFragment = RemindersMyDogFragment.newInstance(mDog.mDogId,
                alarmReminder.mAlarmReminderId, RemindersMyDogFragment.KEY_ALARM);

        mPager.setCurrentItem(getPagerPosition(REMINDERS), true);
    }

    public void setReminderFragment(CalendarReminder calendarReminder) {

        mRemindersFragment = RemindersMyDogFragment.newInstance(mDog.mDogId,
                calendarReminder.mCalendarReminderId, RemindersMyDogFragment.KEY_CALENDAR);

        mPager.setCurrentItem(getPagerPosition(REMINDERS), true);
    }

    public void setHealthFragment() {


    }

    public void setOwnerFragment(Dog mDog) {


    }

    public void setAlbumFragment(Dog mDog) {


    }

    public int getPagerPosition(String tab) {

        for (int i = 0; i < CONTENT.length; i++) {

            if (CONTENT[i].equals(tab)) {
                return i;
            }
        }

        return 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {

        if (requestCode == Photographer.SQUARE_CAMERA_REQUEST_CODE &&
                resultCode == RESULT_OK) {

            if (result != null) {

                cropPhoto(result.getData());

            } else if (mPhotographer.mSourceImage != null) {

                cropPhoto(mPhotographer.mSourceImage);

            } else {
                Do.showLongToast(R.string.generic_networking_error, getApplicationContext());
            }

            super.onActivityResult(requestCode, resultCode, result);

        }

        if (requestCode == Crop.REQUEST_PICK
                && resultCode == RESULT_OK) {

            cropPhoto(result.getData());

        } else if (requestCode == Crop.REQUEST_CROP) {

            uploadPhoto();

        }
    }

    @Override
    public void takePhoto() {

        mPhotographer.takePicture(this);

    }

    @Override
    public void pickPhoto() {

        mPhotographer.pickImage(this);
    }

    @Override
    public void cropPhoto(Uri source) {

        mPhotographer.beginCrop(source, this);
    }

    @Override
    public void uploadPhoto() {

        if (Do.isNetworkAvailable(getApplicationContext())) {

            Context context = getApplicationContext();

            JSONObject jsonPhoto = mPhotographer.getJsonPhoto(context);
            ImageUploadResponse response = new ImageUploadResponse(mDog, this, context);
            response.mProgressDialog = ProgressDialog.show(MyDogsActivity.this, "Espera un momento",
                    "Estamos subiendo la foto");

            Request request = RequestManager.postImage(mDog.mDogId, jsonPhoto, context,
                    response, response);

            VolleyManager.getInstance(context).addToRequestQueue(request);

        } else {

            Do.showLongToast("Debes estar conectado para subir fotos", getApplicationContext());
        }

    }

    @Override
    public void succeedUpload() {

        Do.showShortToast("la foto ha sido subida correctamente", getApplicationContext());
        mAlbumFragment.refreshPhotos();

    }

    @Override
    public void failedUpload() {

        Do.showShortToast("La foto no se subió, pero quedó guardada en tu dispositivo.",
                getApplicationContext());

    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter implements CustomPagerSlidingTabStrip.IconTabProvider {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            int dogId = mDog.mDogId;

            switch (position) {

                case 0:

                    if (mDogProfileFragment == null) {
                        mDogProfileFragment = DogProfileFragment.newInstance(dogId);
                    }

                    return mDogProfileFragment;

                case 1:

                    if (mHistoryFragment == null) {
                        mHistoryFragment = HistoryMyDogFragment.newInstance(dogId);

                    }


                    return mHistoryFragment;

                case 2:

                    if (mVetVisitsFragment == null) {
                        mVetVisitsFragment = VetVisitsFragment.newInstance(dogId);

                    }


                    return mVetVisitsFragment;


                case 3:

                    if (mAlbumFragment == null) {
                        mAlbumFragment = AlbumMyDogFragment.newInstance(dogId);
                        mAlbumFragment.setPhotographer(mPhotographer);

                    }

                    return mAlbumFragment;

                default:
                    if (mDogProfileFragment == null) {
                        mDogProfileFragment = DogProfileFragment.newInstance(dogId);
                    }

                    return mDogProfileFragment;

            }
        }

        @Override
        public int getCount() {
            return ICONS.length;
        }

        @Override
        public int getPageIconResId(int position) {
            return ICONS[position];
        }
    }

    private void updateMenuTitles(int position) {

        setTitle(TITLES[position]);

    }

    private class MyDogPageListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            mPosition = position;
            updateMenuTitles(mPosition);
            invalidateOptionsMenu();

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    public void changeActivity(Class activityClass) {

        Intent intent = new Intent(getApplicationContext(), activityClass);
        intent.putExtra(DOG_ID, mDog.mDogId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    public void refreshFragments() {

        if (mChanged) {

            switch (mPosition) {

                case 1:

                    if (mHistoryFragment != null) {
                        mHistoryFragment.refresh();

                    }
                    break;

            }

        }
    }

    public void registerContentObserver() {

        if(mAlarmObserver == null) {
            mAlarmObserver = new AlarmReminderObserver(new Handler(getMainLooper()));

            getContentResolver().
                    registerContentObserver(ContentProvider.createUri(AlarmReminder.class, null),
                            true, mAlarmObserver);
        }

        if(mCalendarObserver == null) {
            mCalendarObserver = new CalendarReminderObserver(new Handler(getMainLooper()));

            getContentResolver().
                    registerContentObserver(ContentProvider.createUri(CalendarReminder.class, null),
                            true, mCalendarObserver);
        }

        if(mVetVisitObserver == null) {
            mVetVisitObserver = new VetVisitObserver(new Handler(getMainLooper()));

            getContentResolver().
                    registerContentObserver(ContentProvider.createUri(VetVisit.class, null),
                            true, mVetVisitObserver);
        }
    }

    private void unregisterContentObserver() {
        getContentResolver().
                unregisterContentObserver(mAlarmObserver);
        mAlarmObserver = null;

        getContentResolver().
                unregisterContentObserver(mVetVisitObserver);
        mVetVisitObserver = null;
    }
}