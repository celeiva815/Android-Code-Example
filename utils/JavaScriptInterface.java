package cl.magnet.almasuite.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;

import java.io.File;

/**
 * Created by Tito_Leiva on 10-08-15.
 */
public class JavaScriptInterface {
    public static final int TAKE_PICTURE = 1;
    public Uri imageUri;

    private Activity activity;

    public JavaScriptInterface(Activity activiy) {
        this.activity = activiy;
    }

    @JavascriptInterface
    public void doSomething(String message){

        Do.showShortToast(message, activity);

    }

    @JavascriptInterface
    public void startCamera(String url){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStorageDirectory(),  url + ".jpg"); //FIXME
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        activity.startActivityForResult(intent, TAKE_PICTURE);
    }
}
