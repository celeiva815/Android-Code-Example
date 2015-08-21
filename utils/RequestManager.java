package cl.magnet.almasuite.utils.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.magnet.almasuite.utils.PrefsManager;

/**
 * Encapsulates the creation of Treid Api volley {@link Request}
 *
 * Created by lukas on 17-11-14.
 * Improved by Tito_Leiva on 05-02-15.
 *
 */
public class RequestManager {

    public static final String TAG = RequestManager.class.getSimpleName();

//    public static final String BASE_URL = "http://test.magnet.cl/almasuite";
    public static final String BASE_URL = "http://magnet.almasuite-test.com/";
    public static final String TEST_BASE_URL = "api/";
    public static final String API_URL = BASE_URL + TEST_BASE_URL;

    //API Address
    public static final String ADDRESS_SUBSCRIBE = "subscribe/";

    //Request Methods
    public static final int METHOD_DELETE = Request.Method.DELETE;
    public static final int METHOD_GET = Request.Method.GET;
    public static final int METHOD_PATCH = Request.Method.PATCH;
    public static final int METHOD_POST = Request.Method.POST;

    public static final int METHOD_PUT = Request.Method.PUT;
    public static final String URL_SPACE = "%20";
    public static final String NORMAL_SPACE = " ";

    public static final String ID = "id";
    public static final String USER_ID = "user_id";
    public static final String FULL_NAME = "full_name";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String EMAIL = "email";

    public static final String API_PHOTO = "photo";
    public static final String API_CONTENT = "content";
    public static final String API_FILE_NAME = "file_name";



    public static String apiRelativeUrl;

    public RequestManager() {

        this.apiRelativeUrl = API_URL;
    }

    public static Request getRequest(Map<String,String> params, String address,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener, final String token) {

        String url = addParamsToURL(getURL(address), params);
        Log.d(RequestManager.TAG, url);

        return new JsonObjectRequest(METHOD_GET, url, null, listener, errorListener);
    }

    public static Request postRequest(JSONObject jsonParams, String address,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener, final String token) {

        return defaultRequest(METHOD_POST,jsonParams,address,listener,errorListener,token);
    }

    public static Request putRequest(JSONObject jsonParams, String address,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener, final String token) {

        return defaultRequest(METHOD_PUT, jsonParams, address, listener, errorListener, token);
    }

    public static Request patchRequest(JSONObject jsonParams, String address,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener, final String token) {

        return defaultRequest(METHOD_PATCH, jsonParams, address, listener, errorListener, token);
    }

    public static Request deleteRequest(JSONObject jsonParams, String address,
                                       Response.Listener<JSONObject> listener,
                                       Response.ErrorListener errorListener, final String token) {

        return defaultRequest(METHOD_DELETE, jsonParams, address, listener, errorListener, token);
    }

    public static Request defaultRequest(int method, JSONObject jsonParams, String address,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener, final String token) {
        String url = getURL(address);
        Log.d(RequestManager.TAG, address);

        return new JsonObjectRequest(method, url, jsonParams, listener, errorListener);
    }

    public static Request postImage(int dogId, JSONObject jsonPhoto, Context context,
                                    Response.Listener<JSONObject> listener,
                                    Response.ErrorListener errorListener) {

        String address = ""; //FIXME
        String token = PrefsManager.getUserToken(context);

        JSONObject jsonParams = new JSONObject();

        try {

            jsonParams.put(API_PHOTO, jsonPhoto);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = postRequest(jsonParams, address, listener, errorListener, token);

        request.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        return request;
    }

    // ############## Utils ################ //

    public static JSONObject getJsonPhoto(String encodedImage, String name) {

        Map<String,String> params = new HashMap<>();
        params.put(API_CONTENT, encodedImage);
        params.put(API_FILE_NAME, name);

        return getJsonParams(params);
    }

    public static String buildImag(String relativeUrl) {

        String url = BASE_URL + "" + relativeUrl;
        Log.d(TAG, url);
        return url;
    }

    public static JSONObject getJsonParams(Map<String, String> params) {

        JSONObject jsonParams = new JSONObject(params);

        return jsonParams;
    }

    private static String getURL(String address) {

        return API_URL + address;

    }

    private static String addParamsToURL(String url, Map<String, String> params) {

        if (params != null) {
            if (!url.endsWith("?"))
                url += "?";

            for (Map.Entry<String, String> entry : params.entrySet()) {

                String key = entry.getKey();
                String value = entry.getValue();

                url += key + "=" + value + "&";
            }

            if (url.charAt(url.length() - 1) == '&') {

                url = url.substring(0, url.length() - 1);
            }
        }

        return url;
    }

    public static Request stringRequest(List<String> params, String address, int method, JSONObject jsonParams,
                                        Response.Listener<String> listener, Response.ErrorListener errorListener) {

        final byte[] body = jsonParams.toString().getBytes();

        String url = getURL(address);
        StringRequest request = new StringRequest(METHOD_POST, url, listener, errorListener) {

            @Override
            public byte[] getBody() throws AuthFailureError {
                return body;
            }
        };

        return request;
    }


}
