package com.example.weather.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    public static final String BASE_URL = "https://api.openweathermap.org";
    public static Retrofit retrofit;
    /**
    This public static method will return Retrofit client anywhere in the appplication
     If condition to ensure we don't create multiple retrofit instances in a single application
    */
    public static Retrofit getRetrofitClient() {

        if (retrofit == null) {
            //Defining the Retrofit using Builder
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) //This is the only mandatory call on Builder object.
                    .addConverterFactory(GsonConverterFactory.create()) // Converter library used to convert response into POJO
                    .build();
        }
        return retrofit;
    }
}