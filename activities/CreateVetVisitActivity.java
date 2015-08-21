package social.laika.app.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.activeandroid.Model;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.soundcloud.android.crop.Crop;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import social.laika.app.R;
import social.laika.app.interfaces.Photographable;
import social.laika.app.listeners.PhotographerListener;
import social.laika.app.models.Dog;
import social.laika.app.models.Photo;
import social.laika.app.models.VetVisit;
import social.laika.app.network.RequestManager;
import social.laika.app.network.VolleyManager;
import social.laika.app.responses.CreateVetVisitResponse;
import social.laika.app.utils.Do;
import social.laika.app.utils.Photographer;
import social.laika.app.utils.PrefsManager;
import social.laika.app.utils.Tag;

public class CreateVetVisitActivity extends ActionBarActivity
        implements DatePickerDialog.OnDateSetListener, Photographable {

    public int mIdLayout = R.layout.lk_create_vet_visit_activity;

    private static final String TAG = CreateVetVisitActivity.class.getSimpleName();
    public static final String KEY_DOG = "dog_id";
    public static final String KEY_VET_VISIT = "vet_visit";
    public static final String API_EMAIL = "email";
    public static final String API_PASSWORD = "password";
    public static final String API_PASSWORD_CONFIRMATION = "password_confirmation";
    public static final String API_FULL_NAME = "full_name";
    public static final String API_BIRTHDATE = "birth_date";

    public EditText mNameEditText;
    public EditText mDoctorEditText;
    public EditText mReasonEditText;
    public EditText mDetailEditText;
    public EditText mTreatmentEditText;
    public Button mDateButton;
    public Button mCreateButton;
    public String mDate;
    public ProgressDialog mProgressDialog;
    public ProgressBar mProgressBar;
    public Dog mDog;
    public ImageView mVetImageView;
    public Photographer mPhotographer;
    public VetVisit mVetVisit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int dogId = getIntent().getExtras().getInt(KEY_DOG);
        long vetVisitId = getIntent().getExtras().getLong(KEY_VET_VISIT);
        mDog = Dog.getSingleDog(dogId);
        mVetVisit = Model.load(VetVisit.class, vetVisitId);

        setContentView(mIdLayout);
        setActivityView();
        setValues();

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.laika_red));
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Do.hideKeyboard(this);
    }

    public void setActivityView() {

        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), false);

        mNameEditText = (EditText) findViewById(R.id.name_vet_visit_edittext);
        mDoctorEditText = (EditText) findViewById(R.id.doctor_vet_visit_edittext);
        mReasonEditText = (EditText) findViewById(R.id.reason_vet_visit_edittext);
        mDetailEditText = (EditText) findViewById(R.id.detail_vet_visit_edittext);
        mTreatmentEditText = (EditText) findViewById(R.id.treatment_vet_visit_edittext);
        mDateButton = (Button) findViewById(R.id.birthdate_vet_visit_button);
        mCreateButton = (Button) findViewById(R.id.update_vet_visit_button);
        mVetImageView = (ImageView) findViewById(R.id.profile_vet_visit_imageview);
        mPhotographer = new Photographer();
        mProgressBar = (ProgressBar) findViewById(R.id.download_image_progressbar);

        PhotographerListener listener = new PhotographerListener(mPhotographer, this);

        mVetImageView.setOnClickListener(listener);
        mVetImageView.setOnLongClickListener(listener);

        mDate = Do.getToStringDate(calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        mDateButton.setText(mDate);
        mDateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                datePickerDialog.setYearRange(calendar.get(Calendar.YEAR) - 100,
                        calendar.get(Calendar.YEAR));
                datePickerDialog.setCloseOnSingleTapDay(false);
                datePickerDialog.show(getSupportFragmentManager(), Tag.DATE_PICKER);

            }
        });

        mCreateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Context context = v.getContext();
                int ownerId = PrefsManager.getUserId(getApplicationContext());
                String name = mNameEditText.getText().toString();
                String doctor = mDoctorEditText.getText().toString();
                String reason = mReasonEditText.getText().toString();
                String detail = mDetailEditText.getText().toString();
                String treatment = mTreatmentEditText.getText().toString();
                String date = mDateButton.getText().toString();

                if (mVetVisit != null) {

                    mVetVisit.mVetName = name;
                    mVetVisit.mVetDoctor = doctor;
                    mVetVisit.mReason = reason;
                    mVetVisit.mDetail = detail;
                    mVetVisit.mTreatment = treatment;
                    mVetVisit.mDate = date;

                    mVetVisit.update();
                    Do.showLongToast("La ficha médica ha sido actualizada", getApplicationContext());

                } else {

                    VetVisit vetVisit = new VetVisit(ownerId, mDog.mDogId, date, detail, "", "", reason,
                            treatment, doctor, name);

                    vetVisit.create();
                    Do.showLongToast("Enhorabuena! Has creado una nueva ficha médica", getApplicationContext());

                }

                onBackPressed();
            }
        });

    }

    private void setValues() {

        if (mVetVisit != null) {

            mNameEditText.setText(mVetVisit.mVetName);
            mDoctorEditText.setText(mVetVisit.mVetDoctor);
            mDateButton.setText(mVetVisit.mDate);
            mReasonEditText.setText(mVetVisit.mReason);
            mDetailEditText.setText(mVetVisit.mDetail);
            mTreatmentEditText.setText(mVetVisit.mTreatment);

            RequestManager.getImage(mVetVisit.mLargeUrl, null, mVetImageView,
                    getApplicationContext());

        }
    }

    @Override
    protected void onPause() {
        VolleyManager.getInstance(getApplicationContext()).cancelPendingRequests(TAG);
        super.onPause();
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

        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

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

        }  else if (requestCode == Crop.REQUEST_CROP) {
            mPhotographer.handleCrop(resultCode, result, this, mVetImageView);

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


    }

    @Override
    public void succeedUpload() {

    }

    @Override
    public void failedUpload() {

    }


    public void enableViews(boolean enable) {

        mNameEditText.setEnabled(enable);
        mDoctorEditText.setEnabled(enable);
        mReasonEditText.setEnabled(enable);
        mDetailEditText.setEnabled(enable);
        mTreatmentEditText.setEnabled(enable);
        mDateButton.setEnabled(enable);
        mCreateButton.setEnabled(enable);

    }


    public void requestCreateVetVisit() {

        final String name = mNameEditText.getText().toString();

        if (TextUtils.isEmpty(name)) {
            mNameEditText.setError(getString(R.string.field_not_empty_error));
            return;
        }

        enableViews(false);
        mProgressDialog = ProgressDialog.show(CreateVetVisitActivity.this, "Espere un momento",
                "Creando la ficha médica...");

        int ownerId = PrefsManager.getUserId(getApplicationContext());
        String doctor = mDoctorEditText.getText().toString();
        String reason = mReasonEditText.getText().toString();
        String detail = mDetailEditText.getText().toString();
        String treatment = mTreatmentEditText.getText().toString();
        String date = mDateButton.getText().toString();

        VetVisit vetVisit = new VetVisit(ownerId, mDog.mDogId, date, detail, "", "", reason,
                treatment, doctor, name);

        JSONObject jsonParams = vetVisit.getJsonObject();

        if (mPhotographer.hasPhotoChanged()) {

            JSONObject jsonPhoto = mPhotographer.getJsonPhoto(getApplicationContext());

            try {
                jsonParams.put(Photo.API_PHOTO, jsonPhoto);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        CreateVetVisitResponse response = new CreateVetVisitResponse(this);
        String token = PrefsManager.getUserToken(getApplicationContext());

        Request registerRequest = RequestManager.postRequest(jsonParams,
                RequestManager.ADDRESS_VET_VISITS, response, response, token);

        registerRequest.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleyManager.getInstance(getApplicationContext())
                .addToRequestQueue(registerRequest, TAG);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {

        mDate = Do.getToStringDate(day, month, year);
        mDateButton.setText(mDate);

    }
}
