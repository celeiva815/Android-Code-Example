package cl.magnet.almasuite.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cl.magnet.almasuite.R;
import cl.magnet.almasuite.utils.Do;
import cl.magnet.almasuite.utils.JavaScriptInterface;
import cl.magnet.almasuite.utils.network.NetworkStateReceiver;
import cl.magnet.almasuite.utils.network.RequestManager;

/**
 * Created by Tito_Leiva on 24-03-15.
 */
public class WebActivity extends Activity {

    public static final String URL = "url";
    private final static int FILECHOOSER_RESULTCODE = 1;

    private int mIdLayout = R.layout.activity_web;
    public ProgressBar mProgressBar;
    public WebView mWebView;
    public TextView mInternetTextView;
    public LinearLayout mInternetLayout;
    private ValueCallback<Uri> mUploadMessage;
    public Uri mOutputFileUri;
    public String mUrl;
    public boolean mIsConnected;

    private NetworkStateReceiver mNetworkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(mIdLayout);

        mUrl = RequestManager.BASE_URL;
        mIsConnected = Do.isNetworkAvailable(this);
        mProgressBar = (ProgressBar) findViewById(R.id.web_progressBar);
        mWebView = (WebView) findViewById(R.id.webview);
        mInternetTextView = (TextView) findViewById(R.id.internet_connection_textview);
        mInternetLayout = (LinearLayout) findViewById(R.id.internet_connection_layout);

        mProgressBar.setMax(100);
        mProgressBar.setProgress(0);
        setWebConfigurations();
        setNetworkStateReceiver();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (resultCode == RESULT_OK) {
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == mUploadMessage) return;

                final boolean isCamera;
                if (intent == null) {
                    isCamera = true;
                } else {
                    final String action = intent.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = mOutputFileUri;
                } else {
                    selectedImageUri = intent == null ? null : intent.getData();
                }

                mUploadMessage.onReceiveValue(selectedImageUri);
                mUploadMessage = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter iff = new IntentFilter();
        iff.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // Put whatever message you want to receive as the action
        this.registerReceiver(this.mNetworkStateReceiver, iff);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.unregisterReceiver(this.mNetworkStateReceiver);
    }

    public Uri getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            Uri uri = Uri.parse(cursor.getString(column_index));
            return uri;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //flipscreen not loading again
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // To handle "Back" key press event for WebView to go back to previous screen.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            mUrl = mWebView.getUrl();
            Log.i("URL", mUrl);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void setValue(int progress) {

        if (progress >= 95 && mProgressBar.getVisibility() != View.GONE) {
            this.mProgressBar.setVisibility(View.GONE);
            mInternetLayout.setVisibility(View.GONE);
        }

        this.mProgressBar.setProgress(progress);
    }

    public void setWebConfigurations() {

        mWebView.loadUrl(mUrl);
        Log.i("URL", mUrl);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());

        JavaScriptInterface jsInterface = new JavaScriptInterface(this);
        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(jsInterface, "JSInterface");

    }

    private void setNetworkStateReceiver() {

        mNetworkStateReceiver = new NetworkStateReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i("Internet Status", "Network connectivity change");

                if (intent.getExtras() != null) {
                    NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);

                    if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                        Log.i("Internet Status", "Network " + ni.getTypeName() + " connected");

                        if (Do.isNullOrEmpty(mUrl)) {
                            mUrl = mWebView.getUrl();
                        }

                        if (!mIsConnected) { //FIXME Idk if I have to reload the webpage.
                            mIsConnected = true;
                            mWebView.loadUrl(mUrl);
                            Log.i("URL", mUrl);

                        }


                    } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                        Log.i("Internet Status", "There's no network connectivity");

                        mIsConnected = false;
                    }
                }

                showInternetAlert(mIsConnected);
            }
        };

    }

    public void showInternetAlert(boolean isConnected) {

        if (isConnected) {

            String message = Do.getRString(getApplicationContext(), R.string.has_internet_connection)
                    + "\nCargando la aplicación...";

            mInternetTextView.setText(message);
            mWebView.setVisibility(View.VISIBLE);

        } else {

            mInternetTextView.setText(R.string.no_has_internet_connection);
            mInternetLayout.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);

        }
    }

    private class MyWebChromeClient extends WebChromeClient {


        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            mProgressBar.setVisibility(View.VISIBLE);
            WebActivity.this.setValue(newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {

            Log.d("LogTag", message);
            result.confirm();
            return true;
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg) {

            mUploadMessage = uploadMsg;
            Intent i = getChooserIntent(getCameraIntent(), getGalleryIntent("image/*"));
            i.addCategory(Intent.CATEGORY_OPENABLE);
            WebActivity.this.startActivityForResult(Intent.createChooser(i, "Selecciona la imagen"), FILECHOOSER_RESULTCODE);

        }

        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = getChooserIntent(getCameraIntent(), getGalleryIntent("*/*"));
            i.addCategory(Intent.CATEGORY_OPENABLE);
            WebActivity.this.startActivityForResult(
                    Intent.createChooser(i, "Selecciona la imagen"),
                    FILECHOOSER_RESULTCODE);
        }

        //For Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;
            Intent i = getChooserIntent(getCameraIntent(), getGalleryIntent("image/*"));
            WebActivity.this.startActivityForResult(Intent.createChooser(i, "Selecciona la imagen"), WebActivity.FILECHOOSER_RESULTCODE);

        }

        private List<Intent> getCameraIntent() {

// Determine Uri of camera image to save.
            final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "almasuite" + File.separator);
            root.mkdirs();
            final String fname = Do.getUniqueImageFilename();
            final File sdImageMainDirectory = new File(root, fname);
            mOutputFileUri = Uri.fromFile(sdImageMainDirectory);

            // Camera.
            final List<Intent> cameraIntents = new ArrayList<Intent>();
            final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
            for (ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputFileUri);
                cameraIntents.add(intent);
            }

            return cameraIntents;

        }

        private Intent getGalleryIntent(String type) {

            // Filesystem.
            final Intent galleryIntent = new Intent();
            galleryIntent.setType(type);
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

            return galleryIntent;

        }

        private Intent getChooserIntent(List<Intent> cameraIntents, Intent galleryIntent) {

            // Chooser of filesystem options.
            final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

            // Add the camera options.
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

            return chooserIntent;
        }

    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mUrl = url;
            Log.i("URL", mUrl);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

//            if (mIsConnected || Do.isNetworkAvailable(view.getContext())) {
                mUrl = url;
                Log.i("URL", mUrl);
                view.loadUrl(mUrl);
                return true;

//            } else {
//
//                view.stopLoading();
//                return false;
//            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            mProgressBar.setVisibility(View.GONE);
        }
    }
}