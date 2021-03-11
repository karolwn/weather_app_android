package com.example.weather;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weather.model.FavouritesModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * First screen, quick activity displaying the icon and name,
 * then it goes to the main activity.
 */
public class StartupActivity extends AppCompatActivity {

    private static final String FILE_NAME = "weather.txt";
    private static final String CITY = "city";
    private static final String SWITCH = "switch";
    private static final String SHARED_PREFERENCES = "sharedPreferences";
    private static final String FAVOURITES = "favourites";

    private boolean loadedSwitchSate;
    private String loadedCurrentCity;
    private ArrayList<FavouritesModel> loadedFavourites;

    /**
     * Creates the activity, calls other activities after 2000 milliseconds = 2 sec.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(this, "Please enable WiFi or mobile data", Toast.LENGTH_LONG).show();
            //Intent turnWifiOn = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            //startActivity(turnWifiOn);
        }

        new LoadingData(this).execute();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        loadedCurrentCity = sharedPreferences.getString(CITY, "");
        loadedSwitchSate = sharedPreferences.getBoolean(SWITCH, true);

        FileInputStream inFile = null;

        try {
            inFile = openFileInput(FILE_NAME);
            Scanner scanner = new Scanner(inFile);
            loadedFavourites = new ArrayList<>();
            while (scanner.hasNext()) {
                loadedFavourites.add(new FavouritesModel(scanner.nextLine(), false));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inFile != null) {
                try {
                    inFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This inner class is used to load data while splash screen is being displayed.
     * It extends AsyncTask which was intended to enable proper and easy use of the UI thread.
     * AsyncTask is designed to be a helper class around Thread and Handler and does not constitute
     * a generic threading framework. AsyncTasks should ideally be used for short operations
     * (a few seconds at the most.)
     * static class LoadingData extends AsyncTask<1, 2, 3>
     *     1 - what we want to pass to AsyncTask to operate on it
     *     2 - progress units, we can use it to notify user how much longer operation will take
     *     3 - what we will get in return
     * This class is static to prevent memory leak: StartupActivity is o longer there, but
     * LoadingData still has implicit reference, thus StartupActivity con't be garbage collected.
     * Making LoadingData static results in not being able to call StartupActivity methods or access
     * its variables. To overcome this problem we can create WeakReference - reference that doesn't
     * block garbage collecting. We can use it to recreate strong reference using weakReference.get()
     * This strong reference lives only in a range of scope we called .get() meaning that StartupActivity
     * isn't "blocked".
     */
    static class LoadingData extends AsyncTask<Void, Void, Void>{


        private WeakReference<StartupActivity> activityReference;

        // only retain a weak reference to the activity
        LoadingData(StartupActivity context) {
            activityReference = new WeakReference<>(context);
        }

        /**
         * AsyncTask executes this code in background thread.
         * @param voids: 1 argument of AsyncTask<1, 2, 3>, in this case void, as we doesn't pass anything
         * @return 3 argument of AsyncTask<1, 2, 3>
         */
        @Override
        protected Void doInBackground(Void... voids) {
            StartupActivity activity = activityReference.get();
            activity.loadData();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void Void) {
            StartupActivity activity = activityReference.get();

//          Checking whether StartupActivity is "there"
            if (activity == null || activity.isFinishing()) {
                return;
            }
            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra(SWITCH, activity.loadedSwitchSate);
            intent.putExtra(CITY, activity.loadedCurrentCity);
            intent.putParcelableArrayListExtra(FAVOURITES, activity.loadedFavourites);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    /**
     * When user changes the window he's focused on,
     * the app should be still fullscreen.
     *
     * @param hasFocus says if it's fullscreen or running in the background.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();

//      hiding action bar
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null){
            actionbar.hide();
        }

//      makes notification bar completely transparent
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    /**
     * Hides the system UI with several flags.
     * Sets the IMMERSIVE flag.
     * Sets the content to appear under the system bars so that the content
     * doesn't resize when the system bars hide and show.
     */
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }




}