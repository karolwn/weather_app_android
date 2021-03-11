package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;



import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;


import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;


import com.example.weather.fragments.FavouritesFragment;
import com.example.weather.fragments.HomeFragment;
import com.example.weather.fragments.SearchFragment;
import com.example.weather.fragments.AboutFragment;

import com.example.weather.model.FavouritesModel;

import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;



/**
 * Main activity, the main menu so to speak.
 * It displays footer and weather, it handles fragments.
 * "root" class
 */
public class MainActivity extends AppCompatActivity implements FavouritesFragment.FragmentFavouritesListener,
        HomeFragment.FragmentHomeListener, SearchFragment.FragmentSearchListener {

    private BottomNavigationView navBar;
    private HomeFragment homeFragment;
    private FavouritesFragment favouritesFragment;
    private SearchFragment searchFragment;
    private AboutFragment aboutFragment;

    private String FILENAME = "last_forecast_file";
    private static final String FILE_NAME = "weather.txt";
    private static final String CITY = "city";
    private static final String SWITCH = "switch";
    private static final String SHARED_PREFERENCES = "sharedPreferences";
    private static final String FAVOURITES = "favourites";

    private ArrayList<Integer> selectedIndex;
    private ArrayList<FavouritesModel> favourites;


    /**
     * Creates the activity.
     * It is the frame which hosts all fragments, it also handles communication between them
     * @param savedInstanceState: previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFragments(getIntent());

        navBar = findViewById(R.id.nav_bar_bottom);
        navBar.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                homeFragment).commit();

        selectedIndex = new ArrayList<>();
    }


    /**
     * initializes app's fragments
     */

    private void initializeFragments(Intent intent){

        ArrayList<FavouritesModel> loadedFavourites = intent.getParcelableArrayListExtra(FAVOURITES);
        boolean loadedSwitchSate = intent.getBooleanExtra(SWITCH, true);
        String loadedCurrentCity = intent.getStringExtra(CITY);

        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }

        if (favouritesFragment == null) {
            favouritesFragment = new FavouritesFragment();
        }

        if (searchFragment == null) {
            searchFragment = new SearchFragment();
        }
        if (aboutFragment == null) {
            aboutFragment = new AboutFragment();
        }

        assert loadedCurrentCity != null;
        if (!loadedCurrentCity.isEmpty() && !loadedSwitchSate) {
            homeFragment.updateCityName(loadedCurrentCity);
        }
        favouritesFragment.updateListItems(loadedFavourites);
        searchFragment.changeLocalizationSwitch(loadedSwitchSate);
    }

    private void saveData() {
        String currentCity = homeFragment.getCityNameText();
        ArrayList<FavouritesModel> listOfFavourites = favouritesFragment.getListItems();
        boolean switchState = searchFragment.getLocalizationSwitchState();

        StringBuilder sb = new StringBuilder();
        for (FavouritesModel element : listOfFavourites) {
            sb.append(element.getCityName()).append('\n');
        }

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CITY, currentCity);
        editor.putBoolean(SWITCH, switchState);
        editor.apply();

        FileOutputStream outFile = null;
        try {
            outFile = openFileOutput(FILE_NAME, MODE_PRIVATE);
            outFile.write(sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outFile != null) {
                try {
                    outFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Creates listener for navigation bar to detect user's input and make switch to desired Fragment
     * It is outside onCreate() to make it more easy to read and reduce "size" of it
     **/
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                /**
                 * @param item: item that was selected by the user
                 **/
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = homeFragment;
                            favouritesFragment.resetSelection();
                            break;
                        case R.id.nav_favourites:
                            selectedFragment = favouritesFragment;
                            break;
                        case R.id.nav_search:
                            selectedFragment = searchFragment;
                            favouritesFragment.resetSelection();
                            break;
                        case R.id.nav_about:
                            selectedFragment = aboutFragment;
                            favouritesFragment.resetSelection();
                            break;
                    }

                    assert selectedFragment != null;
                    searchFragment.changeLocalizationSwitch(homeFragment.getLocalizationSwitch());

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                    fragmentTransaction.setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down);
                    fragmentTransaction.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
                    fragmentTransaction.replace(R.id.fragment_container, selectedFragment);
                    fragmentTransaction.commit();

                    return true; //display changes to navigation bar, false = do not update nav bar
                }

            };

    /**
     * Handles pressed back button, hitting it on home fragment exits to phone's home screen
     * when user is currently on different fragment app goes to home/current weather
     */
    @Override
    public void onBackPressed() {
        if (navBar.getSelectedItemId() == R.id.nav_home) {
            super.onBackPressed();
        } else {
            navBar.setSelectedItemId(R.id.nav_home);
        }
    }

    /**
     * Called by the system when the device configuration changes while your activity is running.
     * Note that this will only be called if you have selected configurations you would like to
     * handle with the R.attr.configChanges attribute in your manifest. If any configuration change
     * occurs that is not selected to be reported by that attribute, then instead of reporting it
     * the system will stop and restart the activity (to have it launched with the new configuration).
     * It is called because of: <activity android:name=".MainActivity"
     *                  android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"/>
     * in manifest.
     * @param newConfig: The new device configuration. This value must never be null.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (navBar.getSelectedItemId()) {
            case R.id.nav_home:
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    homeFragment = new HomeFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            homeFragment).commit();

                } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
                    homeFragment = new HomeFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            homeFragment).commit();
                }

                break;
            case R.id.nav_favourites:
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    favouritesFragment = new FavouritesFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            favouritesFragment).commit();

                } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
                    favouritesFragment = new FavouritesFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            favouritesFragment).commit();
                }
                break;
            case R.id.nav_search:
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    searchFragment = new SearchFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            searchFragment).commit();

                } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
                    searchFragment = new SearchFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            searchFragment).commit();
                }
                break;
            case R.id.nav_about:
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    aboutFragment = new AboutFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            aboutFragment).commit();

                } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
                    aboutFragment = new AboutFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            aboutFragment).commit();
                }
                break;
        }
    }


    /**
     * Calls favouritesFragment and updates its data (itemsList) using data that was
     * previously sent from it (return to the sender, kinda)
     * @param data: obtained from favouritesFragment
     */
    @Override
    public void sendArrayList(ArrayList<FavouritesModel> data) {
        favouritesFragment.updateListItems(data);
    }

    /**
     * Calls homeFragment and updates its data (city name) using data that was
     * previously sent from favouritesFragment
     * @param city: data obtained from favouritesFragment
     */
    @Override
    public void sendSingleCity(String city, boolean isFavourite) {
        searchFragment.changeLocalizationSwitch(false);
        homeFragment.localizationSwitchState(false);
        homeFragment.setCheckbox(isFavourite);
        homeFragment.updateCityName(city);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, homeFragment);
        fragmentTransaction.commit();
        navBar.setSelectedItemId(R.id.nav_home);
    }

    /**
     * Calls favourite fragment to check if currently displayed location is still favourite.
     * For details see method declaration in interface in FavouritesFragment
     */
    @Override
    public void buttonUpdate() {
        String name = homeFragment.getCityNameText().toLowerCase();
        boolean isFavourite = favouritesFragment.isCityFavourite(name);
        homeFragment.setCheckbox(isFavourite);
    }

    /**
     * Calls homeFragment and updates its data (cityNameText) using data that was
     * previously sent from it (return to the sender, kinda)
     * @param data obtained from homeFragment
     */
    @Override
    public void cityToRetain(String data) {
        homeFragment.updateCityName(data);
    }

    /**
     * Calls favouritesFragment and updates its data (itemsList) using data that was
     * previously sent from homeFragment
     * @param data: obtained from homeFragment - city name
     * @param action: true = add, false = delete
     */
    @Override
    public void addOrRemoveCity(String data, boolean action) {
        if (action) {
            favouritesFragment.addNewCity(data);
        } else {
            favouritesFragment.deleteCityBasedOnName(data);
        }
    }

    /**
     * Receives city name from home fragment and checks if this city is favourite.
     * @param name: city to check
     * @return true if is favourite, otherwise false;
     */
    @Override
    public boolean checkPresenceInFavourites(String name) {
        return favouritesFragment.isCityFavourite(name);
    }
    /**
     * When user changes the window he's focused on,
     * the app should be still fullscreen.
     * @param hasFocus: says if it's fullscreen or running in the background.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();

        //hiding action bar
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }
//        makes notification bar completely transparent
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

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
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    public void sendSearchedCity(String input) {
        homeFragment.updateCityName(input);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, homeFragment);
        fragmentTransaction.commit();
        navBar.setSelectedItemId(R.id.nav_home); // "podswietlenie" odpowiedniego przycisku
    }

    @Override
    public void sendSwitchState(boolean state) {
        homeFragment.localizationSwitchState(state);
    }

    @Override
    public void saveSwitchState(boolean state) {
        searchFragment.changeLocalizationSwitch(state);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }
}