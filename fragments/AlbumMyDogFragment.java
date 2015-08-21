package social.laika.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.android.volley.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import social.laika.app.R;
import social.laika.app.activities.PhotosFragmentActivity;
import social.laika.app.adapters.AlbumAdapter;
import social.laika.app.models.Dog;
import social.laika.app.models.Photo;
import social.laika.app.network.RequestManager;
import social.laika.app.network.VolleyManager;
import social.laika.app.responses.DogPhotosResponse;
import social.laika.app.utils.Photographer;
import social.laika.app.utils.PrefsManager;

/**
 * Created by Tito_Leiva on 09-03-15.
 */
public class AlbumMyDogFragment extends Fragment {

    public static final String KEY_DOG = "mDog";

    private int mIdLayout = R.layout.lk_album_my_dog_fragment;
    public Dog mDog;
    public List<Photo> mPhotos;
    public GridView mGridView;
    public AlbumAdapter mAlbumAdapter;
    private Photographer mPhotographer;


    public AlbumMyDogFragment() { }

    public AlbumMyDogFragment(Dog mDog) {
        this.mDog = mDog;
        this.mPhotos = getPhotos();
    }

    @Override
    public void onStart() {

        if (mAlbumAdapter.getCount() == 0) {

            //requestPhotos();

        }
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

        refreshPhotos();
    }

    public static final AlbumMyDogFragment newInstance(int dogId)
    {
        AlbumMyDogFragment f = new AlbumMyDogFragment();
        Bundle bdl = new Bundle(1);
        bdl.putInt(KEY_DOG, dogId);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        int dogId = getArguments().getInt(KEY_DOG);
        mDog = Dog.getSingleDog(dogId);
        mPhotos = getPhotos();

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(mIdLayout, container, false);

        mGridView= (GridView) view.findViewById(R.id.album_my_dog_gridview);
        mAlbumAdapter = new AlbumAdapter(view.getContext(), mPhotos);
        mGridView.setAdapter(mAlbumAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Intent intent = new Intent(v.getContext(), PhotosFragmentActivity.class);
                intent.putExtra(PhotosFragmentActivity.KEY_CURRENT_ITEM, position);
                intent.putExtra(PhotosFragmentActivity.KEY_DOG_ID, mDog.mDogId);
                v.getContext().startActivity(intent);
            }
        });


        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    private List<Photo> getPhotos() {

        return Photo.getPhotos(mDog.mDogId);
    }

    public void setPhotographer(Photographer photographer) {
        this.mPhotographer = photographer;
    }

    public void requestPhotos() {

        Context context = getActivity().getApplicationContext();
        Map<String,String> params = new HashMap<>();
        String address = RequestManager.ADDRESS_USER_DOG_PHOTOS;
        DogPhotosResponse response = new DogPhotosResponse(mDog, this);
        String token = PrefsManager.getUserToken(context);

        params.put(Dog.COLUMN_DOG_ID, Integer.toString(mDog.mDogId));

        Request request = RequestManager.getRequest(params, address, response, response, token);

        VolleyManager.getInstance(context).addToRequestQueue(request);

    }

    public void refreshPhotos() {

        mPhotos = getPhotos();
        mAlbumAdapter.notifyDataSetChanged();

    }
}
