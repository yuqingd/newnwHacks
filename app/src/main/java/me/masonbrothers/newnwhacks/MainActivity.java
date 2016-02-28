package me.masonbrothers.newnwhacks;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import java.util.Date;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.*;
import java.util.Calendar;

public class MainActivity extends FragmentActivity {
    private TextView mOutputText;
    ProgressDialog mProgress;

    private CaldroidFragment caldroidFragment;

    public static List<Event> userEvents;

    public static Map<TimePeriod, Integer> squadTimes;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};

    com.google.api.services.calendar.Calendar client;
    GoogleAccountCredential credential;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();


    Drawable drawable;


    /**
     * Create the main activity.
     *
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.drawable = ContextCompat.getDrawable(this, R.drawable.sign_check_icon);


        this.caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);

        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.datePicker, caldroidFragment);
        t.commit();

        setContentView(R.layout.activity_main);


        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");

        credential =
                GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        // Calendar client
        client = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential).setApplicationName("Google-CalendarAndroidSample/1.0")
                .build();

        CaldroidListener listener = new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                for(Event currentEvent : userEvents){
                    EventDateTime originalStartTime = currentEvent.getStart();
                    DateTime startTime;

                    if (originalStartTime.getDate() == null) {
                        startTime = originalStartTime.getDateTime();
                    } else {
                        startTime = originalStartTime.getDate();
                    }
                    Date actualDate = new Date(startTime.getValue());

                    String passedDate = date.toString().substring(0, 10);
                    String actualDateString = actualDate.toString().substring(0, 10);

                    String eventSummary = "No Summary Available. ):";
                    String eventLocation = "No Location Available. ):";
                    String eventTime = "No Time Available. ):";

                    if(currentEvent.getSummary() != null) {
                       eventSummary = "Summary: " + currentEvent.getSummary();
                    }
                    if(currentEvent.getLocation() != null){
                        eventLocation = "Location: " + currentEvent.getLocation();
                    }
                    if(actualDate.toString() != null){
                        eventTime = "Date and Time: " + actualDate.toString();
                    }

                    if(actualDateString.equals(passedDate)){
                        Toast.makeText(getApplicationContext(), "Event Details: " + "\n" + eventSummary + "\n" + eventLocation + "\n" + eventTime + "\n",
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }
        };

        caldroidFragment.setCaldroidListener(listener);

    }

    public void createMeetup(View v){
        EventAttendee[] attendees = new EventAttendee[]{
                new EventAttendee().setEmail("yuqinggdu@gmail.com"),
                new EventAttendee().setEmail("calvinhyxu@gmail.com")
        };

        AsyncFindSquad.run(this);



        //   AsyncAddEvent.run(this, "help", new DateTime(System.currentTimeMillis()), new DateTime(System.currentTimeMillis() + 50000000), attendees);
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, MainActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkGooglePlayServicesAvailable()) {
            haveGooglePlayServices();
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                } else {
                    checkGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    AsyncLoadEvents.run(this);
                } else {
                    chooseAccount();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        AsyncLoadEvents.run(this);
                    }
                }
                break;

        }
    }


    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    private void haveGooglePlayServices() {
        // check if there is already an account selected
        if (credential.getSelectedAccountName() == null) {
            // ask user to choose account
            chooseAccount();
        } else {
            // load calendars
            AsyncLoadEvents.run(this);
        }
    }


    void refreshView() {
        AsyncLoadEvents.run(this);

        for(Event currentEvent : userEvents){
            EventDateTime originalStartTime = currentEvent.getStart();
            DateTime startTime;

            if (originalStartTime.getDate() == null) {
                startTime = originalStartTime.getDateTime();
            } else {
                startTime = originalStartTime.getDate();
            }

            Date actualDate = new Date(startTime.getValue());

            caldroidFragment.setBackgroundDrawableForDate(drawable, actualDate);
            caldroidFragment.refreshView();

        }
    }

    //CONTACTS STUFF
    public Map<String, String> contactsPlease()
    {
        Map<String, String> contacts = new HashMap<String, String>() {};
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null, null, null);

        while (cur.moveToNext())
        {
            String name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME_PRIMARY));
            String email = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
            contacts.put(name,email);
        }


        return contacts;
    }

    public void testContacts(Map<String, String> contacts)
    {
        Set<String> keys = contacts.keySet();
        for(String keyName : keys){
            Log.i("TESTO", String.valueOf(keys.size()));
            Log.i("TESTO", keyName + " " + contacts.get(keyName).toString());
        }

    }

    public String[] dennisContacts()
    {
        Set<String> keys = contactsPlease().keySet();
        String[] dennis = new String[keys.size()];
        int i = 0;
        for(String keyName : keys){
            dennis[i] = keyName;
            i++;
        }
        return dennis;
    }

}