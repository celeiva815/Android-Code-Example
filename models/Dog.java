package social.laika.app.models;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.annotation.Table;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import social.laika.app.R;
import social.laika.app.utils.DB;
import social.laika.app.utils.Do;
import social.laika.app.utils.Tag;


@Table(name = Dog.TABLE_DOG)
public class Dog extends Model {

    public static int ID = 1;

    public final static String TABLE_DOG = "dogs";
    public final static String COLUMN_DOG_ID = "dog_id";
    public final static String COLUMN_NAME = "name";
    public final static String COLUMN_BIRTHDATE = "birth";
    public final static String COLUMN_BREED_ID = "breed_id";
    public final static String COLUMN_GENDER = "gender";
    public final static String COLUMN_PERSONALITY = "personality";
    public final static String COLUMN_IS_STERILIZED = "is_sterilized";
    public final static String COLUMN_CHIP_CODE = "chip_code";
    public final static String COLUMN_STATUS = "status";
    public final static String COLUMN_DETAILS = "details";
    public final static String COLUMN_COMPATIBILITY = "compatibility";
    public final static String COLUMN_IS_TRAINED = "is_trained";
    public final static String COLUMN_URL_IMAGE = "url_image";
    public final static String COLUMN_OWNER_ID = "owner_id";
    public final static String COLUMN_FOUNDATION_ID = "foundation_id";
    public final static String COLUMN_FOUNDATION_NAME = "foundation_name";
    public final static String COLUMN_USER_ADOPT_DOG_ID = "user_adopt_dog_id";

    public final static String IMAGE = "image";

    public final static int ID_NOT_SET = 0;


    @Column(name = COLUMN_DOG_ID)
    public int mDogId;

    @Column(name = COLUMN_NAME)
    public String mName;

    @Column(name = COLUMN_BIRTHDATE)
    public String mBirth;

    @Column(name = COLUMN_BREED_ID)
    public int mBreedId;

    @Column(name = COLUMN_GENDER)
    public int mGender;

    @Column(name = COLUMN_PERSONALITY)
    public int mPersonality;

    @Column(name = COLUMN_IS_STERILIZED)
    public boolean mIsSterilized;

    @Column(name = COLUMN_IS_TRAINED)
    public boolean mIsTrained;

    @Column(name = COLUMN_CHIP_CODE)
    public String mChipCode;

    @Column(name = COLUMN_STATUS)
    public int mStatus;

    @Column(name = COLUMN_OWNER_ID)
    public int mOwnerId;

    @Column(name = COLUMN_COMPATIBILITY)
    public int mCompatibility;

    @Column(name = COLUMN_USER_ADOPT_DOG_ID)
    public int mUserAdoptDogId;

    @Column(name = COLUMN_URL_IMAGE)
    public String mUrlImage;

    @Column(name = COLUMN_DETAILS)
    public String mDetail;

    @Column(name = COLUMN_FOUNDATION_NAME)
    public String mFoundationName;

    @Column(name = COLUMN_FOUNDATION_ID)
    public int mFoundationId;

    public Bitmap mDogImage;

    public Dog() {
    }

    public Dog(int mDogId, String mName, String mBirth, int mBreedId, int mGender,
               int mPersonality, boolean mIsSterilized, boolean mIsTrained, String mChipCode,
               int mStatus, int mOwnerId) {

        this.mDogId = mDogId;
        this.mName = mName;
        this.mBirth = mBirth;
        this.mBreedId = mBreedId;
        this.mGender = mGender;
        this.mPersonality = mPersonality;
        this.mIsSterilized = mIsSterilized;
        this.mIsTrained = mIsTrained;
        this.mChipCode = mChipCode;
        this.mStatus = mStatus;
        this.mOwnerId = mOwnerId;
    }

    public Dog(String mName, String mBirth, int mBreedId, int mGender,
               int mPersonality, boolean mIsSterilized, boolean mIsTrained, String mChipCode,
               int mStatus, int mOwnerId) {

        this.mDogId = ID_NOT_SET;
        this.mName = mName;
        this.mBirth = mBirth;
        this.mBreedId = mBreedId;
        this.mGender = mGender;
        this.mPersonality = mPersonality;
        this.mIsSterilized = mIsSterilized;
        this.mIsTrained = mIsTrained;
        this.mChipCode = mChipCode;
        this.mStatus = mStatus;
        this.mOwnerId = mOwnerId;
    }

    public Dog(JSONObject jsonObject, int status) {


        this.mDogId = jsonObject.optInt(COLUMN_DOG_ID);
        this.mName = jsonObject.optString(COLUMN_NAME);
        this.mBirth = jsonObject.optString(COLUMN_BIRTHDATE);
        this.mBreedId = jsonObject.optInt(COLUMN_BREED_ID);
        this.mGender = jsonObject.optInt(COLUMN_GENDER);
        this.mPersonality = jsonObject.optInt(COLUMN_PERSONALITY);
        this.mIsSterilized = jsonObject.optBoolean(COLUMN_IS_STERILIZED);
        this.mIsTrained = jsonObject.optBoolean(COLUMN_IS_TRAINED);
        this.mChipCode = jsonObject.optString(COLUMN_CHIP_CODE);
        this.mUrlImage = jsonObject.optString(COLUMN_URL_IMAGE);
        this.mDetail = jsonObject.optString(COLUMN_DETAILS, "");
        this.mCompatibility = jsonObject.optInt(COLUMN_COMPATIBILITY, 1);
        this.mFoundationId = jsonObject.optInt(COLUMN_FOUNDATION_ID);
        this.mFoundationName = jsonObject.optString(COLUMN_FOUNDATION_NAME);

        switch (status) {

            case Tag.DOG_FOUNDATION:
            case Tag.DOG_POSTULATED:
                this.mStatus = status;
                break;

            default:
                this.mStatus = jsonObject.optInt(COLUMN_STATUS, status);
                break;
        }
    }

    public void update(Dog dog) {

        this.mDogId = dog.mDogId;
        this.mName = dog.mName;
        this.mBirth = dog.mBirth;
        this.mBreedId = dog.mBreedId;
        this.mGender = dog.mGender;
        this.mPersonality = dog.mPersonality;
        this.mIsSterilized = dog.mIsSterilized;
        this.mIsTrained = dog.mIsTrained;
        this.mChipCode = dog.mChipCode;
        this.mUrlImage = dog.mUrlImage;
        this.mDetail = dog.mDetail;
        this.mCompatibility = dog.mCompatibility;
        this.mFoundationId = dog.mFoundationId;
        this.mFoundationName = dog.mFoundationName;

        if (!(mStatus == Tag.DOG_OWNED && dog.mStatus == Tag.DOG_POSTULATED)) {
            this.mStatus = dog.mStatus;
        }

        this.save();
    }

    public JSONObject getJsonObject() {

        JSONObject jsonObject = new JSONObject();

        try {

            if (mDogId > ID_NOT_SET) {
                jsonObject.put(COLUMN_DOG_ID, this.mDogId);
            }

            jsonObject.put(COLUMN_NAME, this.mName);
            jsonObject.put(COLUMN_BIRTHDATE, this.mBirth);
            jsonObject.put(COLUMN_BREED_ID, this.mBreedId);
            jsonObject.put(COLUMN_GENDER, this.mGender);
            jsonObject.put(COLUMN_PERSONALITY, this.mPersonality);
            jsonObject.put(COLUMN_IS_STERILIZED, this.mIsSterilized);
            jsonObject.put(COLUMN_IS_TRAINED, this.mIsTrained);
            jsonObject.put(COLUMN_CHIP_CODE, false);
            jsonObject.put(COLUMN_STATUS, this.mStatus);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * Este método es un parche, mientras se repara la API
     *
     * @return un objeto breed mal hecho
     */
    private JSONObject getJsonBreed() {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("breed_id", mBreedId);
            jsonObject.put("size_id", getSize().mSizeId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;

    }

    public String getAge() {

        Calendar dateOfBirth = Calendar.getInstance();
        dateOfBirth.setTime(Do.stringToDate(mBirth, Do.DAY_FIRST));

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);

        if (today.get(Calendar.MONTH) < dateOfBirth.get(Calendar.MONTH)) {

            age--;

        } else if (today.get(Calendar.MONTH) == dateOfBirth.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) < dateOfBirth.get(Calendar.DAY_OF_MONTH)) {
            age--;
        }

        if (age == 0) {

            int months = today.get(Calendar.MONTH) - dateOfBirth.get(Calendar.MONTH);

            if (months == 1) {
                return Integer.toString(months) + "mes";
            }

            return Integer.toString(months) + "meses";

        } else if (age == 1) {
            return Integer.toString(age) + " año";

        } else {
            return Integer.toString(age) + " años";
        }
    }

    public void addOwner(Owner owner, int role) {

        OwnerDog ownerDog = new OwnerDog(owner.mOwnerId, this.mDogId, role);
        OwnerDog.createOrUpdate(ownerDog);

    }

    public void setDetail(String detail) {
        this.mDetail = detail;
    }

    public String getGender(Context context) {

        switch (mGender) {

            case Tag.GENDER_MALE:
                return Do.getRString(context, R.string.gender_male);

            case Tag.GENDER_FEMALE:
                return Do.getRString(context, R.string.gender_female);
        }

        return null;
    }

    public String getSterilized(Context context) {

        if (mIsSterilized) {
            return Do.getRString(context, R.string.is_sterilized);

        } else {
            return Do.getRString(context, R.string.is_not_sterilized);

        }
    }


    public String getChip(Context context) {

        if (!Do.isNullOrEmpty(mChipCode)) {
            return Do.getRString(context, R.string.has_chip);

        } else {
            return Do.getRString(context, R.string.has_not_chip);

        }
    }

    public String getTrained(Context context) {

        if (mIsTrained) {
            return Do.getRString(context, R.string.is_trained);

        } else {
            return Do.getRString(context, R.string.is_not_trained);

        }
    }

    public Size getSize() {

        Breed breed = getBreed();
        return Size.getSingleSize(breed.mSizeId);

    }

    public Breed getBreed() {

        return Breed.getSingleBreed(mBreedId);

    }

    public int getBreedIdFromJson(JSONObject jsonObject) {

        int breedId = -1;

        try {
            breedId = jsonObject.getInt(COLUMN_BREED_ID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return breedId;
    }

    public Personality getPersonality() {

        return Personality.getSinglePersonality(mPersonality);
    }

    public void setUrlImage(String mUrlImage) {
        this.mUrlImage = mUrlImage;
    }

    public void setPostulatedDog() {

        this.mStatus = Tag.DOG_POSTULATED;
        this.save();

    }

    public String getImage(String size) {

        String url = mUrlImage.replaceAll("original", size);
        return url;

    }

    public List<String> getThumbsPhotos() {

        List<String> thumbs = new ArrayList<>();
        List<Photo> photos = Photo.getPhotos(mDogId);

        for (Photo photo : photos) {

            thumbs.add(photo.mUrlThumbnail);
        }

        return thumbs;

    }

    //DataBase

    public static void saveDogs(JSONObject jsonObject, int status) {

        if (jsonObject.has(TABLE_DOG)) {
            try {
                JSONArray jsonDogs = jsonObject.getJSONArray(TABLE_DOG);

                for (int i = 0; i < jsonDogs.length(); i++) {
                    saveDog(jsonDogs.getJSONObject(i), status);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public static Dog saveDog(JSONObject jsonDog, int status) {

        Dog dog = new Dog(jsonDog, status);
        return createOrUpdate(dog);

    }

    public static Dog createOrUpdate(Dog dog) {

        if (!isSaved(dog)) {
            dog.save();
            return dog;

        } else {
            Dog oldDog = getSingleDog(dog.mDogId);
            oldDog.update(dog);
            return oldDog;
        }
    }

    public static boolean isSaved(Dog dog) {

        String condition = COLUMN_DOG_ID + DB.EQUALS + dog.mDogId;
        return new Select().from(Dog.class).where(condition).exists();

    }

    public static Dog getSingleDog(int dogId) {

        String condition = COLUMN_DOG_ID + DB.EQUALS + dogId;
        return new Select().from(Dog.class).where(condition).executeSingle();

    }

    public static List<Dog> getDogs(int status) {

        String condition = COLUMN_STATUS + DB.EQUALS + status;

        return new Select().from(Dog.class).where(condition).execute();

    }

    public static List<Dog> getDogs(int status, int ownerId) {

        String condition = COLUMN_STATUS + DB.EQUALS + status + DB.AND;
        condition += COLUMN_OWNER_ID + DB.EQUALS + ownerId;
        return new Select().from(Dog.class).where(condition).execute();

    }

    public static List<Dog> getDogs(List<UserAdoptDog> userAdoptDogs) {

        List<Dog> dogs = new ArrayList<>();

        for (UserAdoptDog userAdoptDog : userAdoptDogs) {
            dogs.add(Dog.getSingleDog(userAdoptDog.mDogId));

        }

        return dogs;

    }

    public static void deleteAll() {

        new Delete().from(Dog.class).execute();

    }

    public static void deleteDogs(int process) {

        String condition = COLUMN_STATUS + DB.EQUALS + process;
        new Delete().from(Dog.class).where(condition).execute();

    }

    public static void deleteDog(Dog dog) {

        String condition = COLUMN_DOG_ID + DB.EQUALS + dog.mDogId;
        new Delete().from(Dog.class).where(condition).execute();

    }

}
