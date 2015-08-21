package cl.magnet.almasuite.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Date;

/**
 * Created by Tito_Leiva on 05-02-15.
 */
public class PrefsManager {

    private static final String PREFS_NAME = "cl.magnet.almasuite.preferences";

    private static final String PREF_SESSION_COOKIE = "session_cookie";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_NAME = "name";
    private static final String PREF_USER_EMAIL = "email";
    private static final String PREF_USER_PASSWORD = "password";
    private static final String PREF_USER_ACCESS_TOKEN = "access_token";

    private static final String PREF_IS_SYNCING_STATUS = "is_syncing_status";
    private static final String PREF_LAST_SYNC = "last_sync";
    private static final String PREF_FIRST_BOOT = "firstboot";
    private static final String PREF_SETUP_COMPLETE = "setup_complete";
    // Default value for String preferences
    public static final String DEFAULT_STRING = "";

    public static final String GCM_SENT_TOKEN_TO_SERVER = "setSentTokenToServer";
    public static final String GCM_REGISTRATION_COMPLETE = "registrationComplete";

    private PrefsManager() {

    }

    /**
     * Return application Shared Preferences
     *
     * @param context The context where the preferences get called
     * @return Application SharedPreferences
     */
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Returns the session cookie serialized in json string format
     *
     * @param context
     */
    public static String getJsonSessionCookie(Context context) {
        return getPrefs(context).getString(PREF_SESSION_COOKIE, DEFAULT_STRING);
    }

    // #####################################
    // ######### User Methods ##############
    // #####################################

    public static int getUserId(Context context) {
        return getPrefs(context).getInt(PREF_USER_ID, 0);
    }

    public static String getUserToken(Context context) {

        SharedPreferences prefs = getPrefs(context);
        String token = prefs.getString(PREF_USER_ACCESS_TOKEN, "");

        return token;
    }

    public static String getUserName(Context context) {

        SharedPreferences prefs = getPrefs(context);
        String name = prefs.getString(PREF_USER_NAME, "");

        return name;
    }



    public static void setIsSyncingStatus(Context context, boolean isSyncing) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(PREF_IS_SYNCING_STATUS, isSyncing);
        editor.apply();
    }

    public static void saveLastSync(Context context, Date lastSync) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putLong(PREF_LAST_SYNC, lastSync.getTime());
        editor.apply();
    }

    public static Date getLastSync(Context context) {
        long lastSyncMillis = getPrefs(context).getLong(PREF_LAST_SYNC, 0);
        return lastSyncMillis > 0 ? new Date(lastSyncMillis) : null;
    }


    // ################ GCM ###############

    public static void setSentTokenToServer(Context context, boolean isSent) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(GCM_SENT_TOKEN_TO_SERVER, isSent);
        editor.apply();
    }

    public static boolean getSentTokenToServer(Context context) {

        SharedPreferences prefs = getPrefs(context);
        return prefs.getBoolean(GCM_SENT_TOKEN_TO_SERVER, false);
    }

    // ############ Sync ##############

    public static void setSyncSetupComplete(Context context, boolean isComplete) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(PREF_SETUP_COMPLETE, isComplete);
        editor.apply();
    }

    public static boolean getSyncSetupComplete(Context context) {

        SharedPreferences prefs = getPrefs(context);
        return prefs.getBoolean(PREF_SETUP_COMPLETE, false);
    }


    /**
     * Saves the session cookie serialized in json string format in shared preferences
     *
     * @param context The context where the preferences get called
     * @param jsonSessionCookie The session cookie serializes in json string format
     */
    public static void saveJsonSessionCookie(Context context, String jsonSessionCookie) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(PREF_SESSION_COOKIE, jsonSessionCookie);
        editor.apply();
    }

    public static void clearPrefs(Context context) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.clear();
        editor.apply();
    }

    public static boolean isFirstBoot(Context context) {

        SharedPreferences prefs = getPrefs(context);

        return prefs.getBoolean(PREF_FIRST_BOOT, true);

    }

    public static void finishFirstBoot(Context context) {

        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(PREF_FIRST_BOOT, false);
        editor.apply();
    }

}
