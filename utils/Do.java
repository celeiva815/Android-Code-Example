package cl.magnet.almasuite.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Tito_Leiva on 19-01-15.
 */
public class Do {

    // Date Patterns
    public static final String DAY_FIRST = "dd-MM-yyyy";
    public static final String DAY_FIRST_NO_YEAR = "dd-MM";
    public static final String HOUR_MINUTE = "HH:mm";
    public static final String FIRST_TIME_OF_DAY = " 00:00:00";
    public static final String LAST_TIME_OF_DAY = " 23:59:59";
    public static final int WEEK = 7;
    public static final int HOURS = 24;
    public static final int MINUTES = 60;
    public static final int SECONDS = 60;
    public static final int MILLIS = 1000;

    /**
     *
     * @author Tito_Leiva
     * @param context
     * @param name
     */
    public static void changeActivity(Context context, Class<?> name){
        Intent intent = new Intent(context, name);
        context.startActivity(intent);
    }

    /**
     * @author Tito_Leiva
     * @param context
     * @param name
     * @param closed
     */
    public static void changeActivity(Context context, Class<?> name, Activity closed){
        Intent intent = new Intent(context, name);
        context.startActivity(intent);
        closed.finish();
    }

    public static void changeActivity(Context context, Class<?> name, int flag) {
        Intent intent = new Intent(context, name);
        intent.addFlags(flag);
        context.startActivity(intent);
    }

    public static void changeActivity(Context context, Class<?> name, Activity closed, int flag) {
        Intent intent = new Intent(context, name);
        intent.addFlags(flag);
        context.startActivity(intent);
        closed.finish();
    }

    public static void changeFragment(Fragment removed, Fragment added, int container, String tag) {

        Activity activity = removed.getActivity();

        activity.getFragmentManager().beginTransaction().remove(removed).commit();
        activity.getFragmentManager().beginTransaction().add(container, added, tag).commit();
    }

    public static void showShortToast(String message, Context context){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(String message, Context context) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showShortToast(int resource, Context context){

        String message = getRString(context, resource);
        showShortToast(message, context);
    }

    public static void showLongToast(int resource, Context context) {

        String message = getRString(context, resource);
        showLongToast(message, context);
    }

    public static String getToken(Context context){

        return ""; //FIXME getToken
    }

    public static String getToStringDate(int day, int month, int year) {

        String yearText = Integer.toString(year);
        String monthText = month < 10 ? "0"+ Integer.toString(month + 1) : Integer.toString(month + 1);;
        String dayText = day < 10 ? "0"+ Integer.toString(day) : Integer.toString(day);

        String date = dayText + "-" + monthText + "-" + yearText;
        return date;
    }

    public static String getToStringTime(int hour, int minute) {

        String hourText = hour < 10 ? "0"+ Integer.toString(hour) : Integer.toString(hour);
        String minuteText = minute < 10 ? "0"+ Integer.toString(minute) : Integer.toString(minute);

        return hourText + ":" + minuteText;
    }

    public static String dateToString(Date date, String pattern){

        String newDate = new SimpleDateFormat(pattern).format(date);

        return newDate;
    }

    public static Date stringToDate(String date, String pattern){

        Date newDate = new Date();
        try {
            newDate = new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {

            e.printStackTrace();
        }

        return newDate;
    }

    public static String today() {

        Date date = new Date();

        return Do.dateToString(date, Do.DAY_FIRST);
    }

    public static String now() {

        Date date = new Date();

        return Do.dateToString(date, Do.HOUR_MINUTE);
    }

    public static boolean isLoggedIn(Activity activity){

        return true; //FIXME isLoggedIn
    }
    public static int[] dateInArray() {

        Calendar cal = Calendar.getInstance();

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int[] date = {year, month, day};

        return date;
    }

    public static int[] timeInArray() {

        Calendar cal = Calendar.getInstance();

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);

        int[] time = {hour, min};

        return time;
    }

    //Method to remove "tildes" while replacing capital letters
    public static String removeToLowerSpecialCharacters(String string){
        return Do.removeSpecialCharacters(string.toLowerCase());
    }
    // Method to remove "tildes" and special characters
    public static String removeSpecialCharacters(String string){
        return Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    /**
     * Method to compare two strings, independent from special characters and cases.
     * @author johnpeebles
     * @param string1
     * @param string2
     * @return
     */
    public static boolean smartCompareStrings(String string1, String string2){
        return removeToLowerSpecialCharacters(string1).contains(removeToLowerSpecialCharacters(string2));
    }

    public static void addStringToSharedPreferences(Context context, String key, String value) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getRString(Context context, int resource) {

        return context.getResources().getString(resource);
    }
    public static int getColor(Context context, int resource) {

        return context.getResources().getColor(resource);
    }

    public static int randomInteger(int min, int max) {

        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public static boolean isNullOrEmpty(String string) {

        if (string == null || string.isEmpty()) {
            return true;
        } else return false;
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void showAlertDialog(Context context, String title, String message) {

        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

            // Setting OK Button
            alertDialog.setButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });

        // Showing Alert Message
        alertDialog.show();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getCountryIso(Context context) {

        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        }
        catch (Exception e) { }

        return null;
    }

    public static void ableView(View view, boolean enabled) {
        view.setEnabled(enabled);
    }

    public static void showView(View view) {
        view.setVisibility(View.VISIBLE);
    }

    public static void hideView(View view) {
        view.setVisibility(View.GONE);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void hideKeyboard(Activity activity) {

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static String getUniqueImageFilename() {

        int[] date = dateInArray();
        int[] time = timeInArray();

        return "image" + date[0] + date[1] + date[2] + time[0] + time[1] + ".jpg";

    }
}
