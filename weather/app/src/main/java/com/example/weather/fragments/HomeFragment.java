package com.example.weather.fragments;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.weather.FileOperations;
import com.example.weather.R;
import com.example.weather.api.Permissions;
import com.example.weather.api.NetworkClient;
import com.example.weather.api.WeatherAPIs;
import com.example.weather.api.WeatherImage;
import com.example.weather.model.City;
import com.example.weather.model.WeatherResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


import static java.lang.Math.abs;
public class HomeFragment extends Fragment implements View.OnClickListener {
    private FragmentHomeListener listener;
    private String cityNameText = null;
    private CheckBox checkBox;
    private boolean isAddButtonPressed = false;

    private static final String API_KEY = "06d5e4957656768f786a9fd4dac684f0";
    private static final String UNITS = "metric";
    private static final String CITY = "city";

    private TextView current_weather_temperature_label, current_weather_pressure_label, current_weather_wind_label, current_weather_humidity_label;

    private TextView current_weather_temperature, current_weather_pressure, current_weather_wind, current_weather_humidity;

    private ImageView current_weather_icon, main_secondary_screen_weather_week_day_first_icon,
            main_secondary_screen_weather_week_day_second_icon,
            main_secondary_screen_weather_week_day_third_icon;

    private TextView main_secondary_screen_weather_first, main_secondary_screen_weather_second,
            main_secondary_screen_weather_third, main_secondary_screen_weather_week_day_first,
            main_secondary_screen_weather_week_day_second,
            main_secondary_screen_weather_week_day_third, cityName;

    private double lat, tmp_lat, lon, tmp_lon;

    private LocationRequest mLocationRequest;
    private boolean localizationSwitch = true;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private WeatherResult weatherResult;

    private long UPDATE_INTERVAL = 1 * 1000;  /* 1 secs */
    private long FASTEST_INTERVAL = 1000; /* 1 sec */

    /**
     * Interface to provide communication between Fragments/Activities through the MainActivity
     */
    public interface FragmentHomeListener {

        /**
         * It sends currently displayed city when user exits home fragment
         *
         * @param data: city name
         */
        void cityToRetain(String data);

        /**
         * Sends city name that user added to favourites or removed from them. Used when clicking heart icon.
         *
         * @param data:   city name
         * @param action: true = add, false = remove
         */
        void addOrRemoveCity(String data, boolean action);

        /**
         * Sends city name to determinate whether is it favourite or not
         *
         * @param name: city to check
         * @return true = is favourite, false = is not
         */
        boolean checkPresenceInFavourites(String name);
    }

    /**
     * Called to have the fragment instantiate its user interface view. This is optional,
     * and non-graphical fragments can return null. This will be called between onCreate(Bundle)
     * and onActivityCreated(Bundle)
     *
     * @param inflater:          The LayoutInflater object that can be used to inflate any
     *                           views in the fragment,
     * @param container:         If non-null, this is the parent view that the fragment's UI should
     *                           be attached to. The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState : If non-null, this fragment is being re-constructed from
     *                           a previous saved state as given here.
     **/
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);



        checkBox = view.findViewById(R.id.add);
        checkBox.post(new Runnable() {
            @Override
            public void run() {
                checkBox.setChecked(isAddButtonPressed);
            }
        });


        checkBox.setOnClickListener(this);

        cityName = view.findViewById(R.id.city_name);

// find text views in main screen
        current_weather_temperature_label = view.findViewById(R.id.main_screen_weather_temperature_label);
        current_weather_temperature = view.findViewById(R.id.main_screen_weather_temperature);
        current_weather_pressure_label = view.findViewById(R.id.main_screen_weather_pressure_label);
        current_weather_pressure = view.findViewById(R.id.main_screen_weather_pressure);
        current_weather_wind_label = view.findViewById(R.id.main_screen_weather_wind_label);
        current_weather_wind = view.findViewById(R.id.main_screen_weather_wind);
        current_weather_humidity_label = view.findViewById(R.id.main_screen_weather_humidity_label);
        current_weather_humidity = view.findViewById(R.id.main_screen_weather_humidity);

// find main icon
        current_weather_icon = view.findViewById(R.id.current_weather_icon);

// secondary screen stuff
        main_secondary_screen_weather_first = view.findViewById(R.id.main_secondary_screen_weather_first);
        main_secondary_screen_weather_second = view.findViewById(R.id.main_secondary_screen_weather_second);
        main_secondary_screen_weather_third = view.findViewById(R.id.main_secondary_screen_weather_third);

        main_secondary_screen_weather_week_day_first = view.findViewById(R.id.main_secondary_screen_weather_week_day_first);
        main_secondary_screen_weather_week_day_second = view.findViewById(R.id.main_secondary_screen_weather_week_day_second);
        main_secondary_screen_weather_week_day_third = view.findViewById(R.id.main_secondary_screen_weather_week_day_third);

        main_secondary_screen_weather_week_day_first_icon = view.findViewById(R.id.main_secondary_screen_weather_week_day_first_icon);
        main_secondary_screen_weather_week_day_second_icon = view.findViewById(R.id.main_secondary_screen_weather_week_day_second_icon);
        main_secondary_screen_weather_week_day_third_icon = view.findViewById(R.id.main_secondary_screen_weather_week_day_third_icon);

        if (cityNameText != null) {
            cityName.setText(cityNameText);
        }


        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        if(localizationSwitch){
            startLocationUpdates();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        fetchWeatherDetails();
    }

    private void displayWeather(String[] previousWeather){

        cityName.setText(previousWeather[2]);
        cityNameText = previousWeather[2];
        current_weather_temperature.setText(getString(R.string.celc, previousWeather[4]));
        current_weather_pressure.setText(getString(R.string.hPa, previousWeather[5]));
        current_weather_wind.setText(getString(R.string.speed, previousWeather[6]));
        current_weather_humidity.setText(getString(R.string.hum_per, previousWeather[7]));

        main_secondary_screen_weather_first.setText(getString(R.string.first_day_weather_message, previousWeather[9]));
        main_secondary_screen_weather_second.setText(getString(R.string.second_day_weather_message, previousWeather[11]));
        main_secondary_screen_weather_third.setText(getString(R.string.third_day_weather_message, previousWeather[13]));

        main_secondary_screen_weather_week_day_first.setText(convertStringDateToWeekDay(previousWeather[8].substring(0, 10)));
        main_secondary_screen_weather_week_day_second.setText(convertStringDateToWeekDay(previousWeather[10].substring(0, 10)));
        main_secondary_screen_weather_week_day_third.setText(convertStringDateToWeekDay(previousWeather[12].substring(0, 10)));

        String icon = previousWeather[14];
        current_weather_icon.setImageResource(WeatherImage.switchImage(icon));

        String icon1 = previousWeather[15];
        main_secondary_screen_weather_week_day_first_icon.setImageResource(WeatherImage.switchImage(icon1));

        String icon2 = previousWeather[16];
        main_secondary_screen_weather_week_day_second_icon.setImageResource(WeatherImage.switchImage(icon2));

        String icon3 = previousWeather[17];
        main_secondary_screen_weather_week_day_third_icon.setImageResource(WeatherImage.switchImage(icon3));
    }

    private String getCurrentTime(){
        final String DATE_FORMAT = "yyyy/MM/dd HH/mm/ss";
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        Calendar currentTime = Calendar.getInstance();
        return formatter.format(currentTime.getTime());
    }

    private boolean checkIfWeatherForThatCityExist() {
        String [] previousWeather = FileOperations.openWeatherFile(requireContext());
        String timeStr = getCurrentTime();

        if (previousWeather[3] != null & cityNameText != null) {
            int year= Integer.parseInt(timeStr.substring(0, 4));
            int yearFromFile = Integer.parseInt(previousWeather[3].substring(0, 4));

            int month = Integer.parseInt(timeStr.substring(5, 7));
            int monthFromFile = Integer.parseInt(previousWeather[3].substring(5, 7));

            int day = Integer.parseInt(timeStr.substring(8, 10));
            int dayFromFile = Integer.parseInt(previousWeather[3].substring(8, 10));

            int hour = Integer.parseInt(timeStr.substring(11, 13));
            int hourFromFile = Integer.parseInt(previousWeather[3].substring(11, 13));

            boolean checkYear = abs(yearFromFile - year) == 0;
            boolean checkMonth = abs(month - monthFromFile) == 0;
            boolean checkDay = abs(day - dayFromFile) == 0;
            boolean checkHour = abs(hour - hourFromFile) < 3;
            boolean checkCity = previousWeather[2].toLowerCase().equals(cityNameText.toLowerCase());

            if ( checkYear & checkMonth & checkDay & checkHour & checkCity){
                isAddButtonPressed = listener.checkPresenceInFavourites(cityNameText);
                checkBox.setChecked(isAddButtonPressed);
                displayWeather(previousWeather);
                return true;
            } else {
                return false;
            }


        } else {
            return false;
        }
    }

    private boolean checkIfWeatherForThatLatLonExist() {
        String [] previousWeather = FileOperations.openWeatherFile(getContext());
        String timeStr = getCurrentTime();

        if (previousWeather[3] != null & cityNameText != null) {
            int year= Integer.parseInt(timeStr.substring(0, 4));
            int yearFromFile = Integer.parseInt(previousWeather[3].substring(0, 4));

            int month = Integer.parseInt(timeStr.substring(5, 7));
            int monthFromFile = Integer.parseInt(previousWeather[3].substring(5, 7));

            int day = Integer.parseInt(timeStr.substring(8, 10));
            int dayFromFile = Integer.parseInt(previousWeather[3].substring(8, 10));

            int hour = Integer.parseInt(timeStr.substring(11, 13));
            int hourFromFile = Integer.parseInt(previousWeather[3].substring(11, 13));

            boolean checkYear = abs(yearFromFile - year) == 0;
            boolean checkMonth = abs(month - monthFromFile) == 0;
            boolean checkDay = abs(day - dayFromFile) == 0;
            boolean checkHour = abs(hour - hourFromFile) < 3;
            boolean checkLat = abs(abs(Double.parseDouble(previousWeather[0])) -  abs(lat)) < 0.04;
            boolean checkLon = abs(abs(Double.parseDouble(previousWeather[1])) - abs(lon)) < 0.04;
            boolean checkCity = previousWeather[2].toLowerCase().equals(cityNameText.toLowerCase());

            if ( checkYear & checkMonth & checkDay & checkHour & checkLat & checkLon & checkCity){
                isAddButtonPressed = listener.checkPresenceInFavourites(cityNameText);
                checkBox.setChecked(isAddButtonPressed);

                displayWeather(previousWeather);
                return true;
            } else {
                return false;
            }


        } else {
            return false;
        }
    }

    private void fetchWeatherDetails() {
        boolean weatherExist = false;
        Call call = null;
        if (cityNameText != null && !localizationSwitch) {
            weatherExist = checkIfWeatherForThatCityExist();
            if (!weatherExist) {
                Permissions.isInternetEnable(getContext());
                //Obtain an instance of Retrofit by calling the static method.
                Retrofit retrofit = NetworkClient.getRetrofitClient();
                /*
                The main purpose of Retrofit is to create HTTP calls from the Java interface based on the annotation associated with each method.
                This is achieved by just passing the interface class as parameter to the create method
                */
                WeatherAPIs weatherAPIs = retrofit.create(WeatherAPIs.class);
                 /*
                 Invoke the method corresponding to the HTTP request which will return a Call object.
                 This Call object will used to send the actual network request with the specified parameters
                 */
                call = weatherAPIs.getWeatherByCity(cityNameText, UNITS, API_KEY);
                isAddButtonPressed = listener.checkPresenceInFavourites(cityNameText);
                checkBox.setChecked(isAddButtonPressed);
            }else{
                return;
            }
        } else {
            weatherExist = checkIfWeatherForThatLatLonExist();

            if(!weatherExist) {
                Permissions.isInternetEnable(getContext());
                Retrofit retrofit = NetworkClient.getRetrofitClient();
                WeatherAPIs weatherAPIs = retrofit.create(WeatherAPIs.class);

                call = weatherAPIs.getWeatherByCoordinates(lat, lon, UNITS, API_KEY);
            }else{
                return;
            }
        }
        /*
        This is the line which actually sends a network request.
        Calling enqueue() executes a call asynchronously. It has two callback listeners which will invoked on the main thread
        */
        if (!weatherExist & call != null) {
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of WeatherResponse POJO class
                     */
                    if (response.body() != null) {
                        weatherResult = (WeatherResult) response.body();
                        String city = weatherResult.city.getName();
                        cityName.setText(city);
                        cityNameText = city;
                        isAddButtonPressed = listener.checkPresenceInFavourites(city);
                        checkBox.setChecked(isAddButtonPressed);

                        FileOperations.saveData(getContext(), weatherResult);
                        String [] previousWeather = FileOperations.openWeatherFile(getContext());
                        displayWeather(previousWeather);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.wrong_city_name), Toast.LENGTH_LONG).show();
                        cityName.setText(getString(R.string.wrong_city_name));
                    }
                }

                @Override
                public void onFailure(Call call, Throwable t) {
                    cityName.setText("Could't fetch data");
                }
            });
        }
    }

    protected void startLocationUpdates() {
        if (Permissions.checkPermissions(getContext())) {
            if (Permissions.isLocationEnabled(getActivity())) {
                // Create the location request to start receiving updates
                mLocationRequest = new LocationRequest();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setInterval(UPDATE_INTERVAL);
                mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
                // Create LocationSettingsRequest object using location request
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
                builder.addLocationRequest(mLocationRequest);

                LocationSettingsRequest locationSettingsRequest = builder.build();

                SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
                settingsClient.checkLocationSettings(locationSettingsRequest);

                fusedLocationProviderClient = new FusedLocationProviderClient(getActivity());
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                onLocationChanged(locationResult.getLastLocation());
                            }
                        },
                        Looper.myLooper());
            } else {
                Toast.makeText(getContext(), getString(R.string.turn_on_GPS), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            Permissions.requestPermissions(getActivity());
        }
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        tmp_lat = location.getLatitude();
        tmp_lon = location.getLongitude();
        if (abs(tmp_lat - lat) > 0.01 || abs(tmp_lon - lon) > 0.01) {
            lat = tmp_lat;
            lon = tmp_lon; //check if change isn't too small
            fetchWeatherDetails();
        }
    }

    /**
     * converts date from string to week day e.g. 03-02-2012 -> monday
     * @param input string date in format dd-MM-yyyy
     * @return weekday
     */
    public String convertStringDateToWeekDay(String input) {
        SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date dt1 = formatIn.parse(input);
            DateFormat formatOut = new SimpleDateFormat("EEEE", Locale.US);
            assert dt1 != null;
            return formatOut.format(dt1);
        } catch (ParseException e) {
            return input;
        }
    }

    /**
     * Updates city name and turns off localization switch, used when searching city or selecting
     * it form favourites
     *
     * @param name: new city to be displayed
     */
    public void updateCityName(String name) {
        if(!name.equals(cityNameText)) {
            cityNameText = name;
            localizationSwitch = false;
        }
    }
    /**
     * Returns current city name, used in data saving
     *
     * @return city name
     */
    public String getCityNameText() {
        return (String) cityName.getText();
    }

    /**
     * Changes icon of heart checkbox (border or full), it also updates a value of variable which holds
     * checkbox state
     *
     * @param newState: true = full, false = border
     */
    public void setCheckbox(boolean newState) {
        isAddButtonPressed = newState;
        checkBox.setChecked(newState);
    }

    public void localizationSwitchState(boolean state) {
        localizationSwitch = state;
    }

    public boolean getLocalizationSwitch(){
        return localizationSwitch;
    }
    /**
     * It handles heart button clicking (checkBox). it decides whether add city to favourites or remove it
     *
     * @param v: heart check box that was clicked
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add) {
            if (checkBox.isChecked()) {
                listener.addOrRemoveCity(String.valueOf(cityName.getText()), true);
                Toast.makeText(getContext(), R.string.add_home_toast, Toast.LENGTH_LONG).show();
                isAddButtonPressed = !isAddButtonPressed;
            } else {
                listener.addOrRemoveCity(String.valueOf(cityName.getText()), false);
                Toast.makeText(getContext(), R.string.del_home_toast, Toast.LENGTH_LONG).show();
                isAddButtonPressed = !isAddButtonPressed;
            }
        }
    }
    /**
     * Called when a fragment is first attached to its context (Main Activity).
     * onCreate(android.os.Bundle) will be called after this.
     * If you override this method you must call through to the superclass implementation.
     * To ensure that the host activity implements this interface, fragment A's onAttach() callback
     * method (which the system calls when adding the fragment to the activity) instantiates an
     * instance of FragmentFavouritesListener by casting the Activity that is passed into onAttach():
     *
     * @param context: Context
     **/
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentHomeListener) {
            listener = (FragmentHomeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentHomeListener");
        }
    }
    /**
     * Called when the fragment is being disassociated from the activity.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    /**
     * Called when the view hierarchy associated with user leaves fragment.
     * It saves currently displayed city, and stops location services to save power.
     */
    @Override
    public void onStop() {
        super.onStop();
        listener.cityToRetain(cityNameText);
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}