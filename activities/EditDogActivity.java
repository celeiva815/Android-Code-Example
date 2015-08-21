package social.laika.app.activities;

import android.content.Intent;
import android.view.View;

import social.laika.app.R;
import social.laika.app.adapters.BreedAdapter;
import social.laika.app.adapters.PersonalityAdapter;
import social.laika.app.adapters.SizeAdapter;
import social.laika.app.listeners.EditDogOnClickListener;
import social.laika.app.models.Dog;
import social.laika.app.models.Breed;
import social.laika.app.models.Personality;
import social.laika.app.models.Size;
import social.laika.app.utils.Do;
import social.laika.app.utils.Tag;

public class EditDogActivity extends CreateDogActivity {

    @Override
    protected void onResume() {
        super.onResume();

        completeDogForm();
        mIsSizeSelected = false;
    }

    public void completeDogForm() {

        Do.hideKeyboard(this);
        setDog();

        mNameEditText.setText(mDog.mName);
        mBirthButton.setText(mDog.mBirth);

        Breed breed = mDog.getBreed();
        Size size = mDog.getSize();
        Personality personality = mDog.getPersonality();

        int sizePosition = ((SizeAdapter) mSizeSpinner.getAdapter()).getPosition(size);
        mSizeSpinner.setSelection(sizePosition);

        int breedPosition = ((BreedAdapter) mBreedSpinner.getAdapter()).getPosition(breed);
        mBreedSpinner.setSelection(breedPosition);

        int personalityPosition = ((PersonalityAdapter) mPersonalitySpinner.getAdapter()).
                getPosition(personality);
        mPersonalitySpinner.setSelection(personalityPosition);

        if (mDog.mGender == Tag.GENDER_MALE) {
            mGenderRadioGroup.check(R.id.male_new_dog_register_radiobutton);

        } else {
            mGenderRadioGroup.check(R.id.female_new_dog_register_radiobutton);

        }

        if (mDog.mIsSterilized) {
            mSterilizedRadioGroup.check(R.id.sterilized_new_dog_register_radiobutton);

        } else {
            mSterilizedRadioGroup.check(R.id.not_sterilized_new_dog_register_radiobutton);
        }

        if (Do.isNullOrEmpty(mDog.mChipCode)) {
            mChipRadioGroup.check(R.id.not_chip_new_dog_register_radiobutton);

        } else {
            mChipRadioGroup.check(R.id.chip_new_dog_register_radiobutton);
            mChipEditText.setVisibility(View.VISIBLE);
            mChipEditText.setText(mDog.mChipCode);
        }

        mAddButton.setText(Do.getRString(this, R.string.save_reminder));
        mAddButton.setOnClickListener(new EditDogOnClickListener(this, mDog));

    }

    public void setDog() {

        Intent intent = getIntent();
        int dogId = intent.getIntExtra(KEY_DOG_ID, 0);

        mDog = Dog.getSingleDog(dogId);
    }
}
