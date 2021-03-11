package com.example.weather.api;

import com.example.weather.model.WeatherResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherAPIs {


    /**
     * Get request to fetch city weather
     * @param lat - Latitude
     * @param lon - Longitude
     * @param units - response units (Standard, metric, and imperial units are available)
     * @param apiKey - key from OpenWeatherMap.org
     * @return Response from OpenWeatherMap.org (forecast for next four days based on location)
     */
    @GET("/data/2.5/forecast/") //endpointlat={lat}&lon={lon
    Call<WeatherResult> getWeatherByCoordinates(@Query("lat") double lat, @Query("lon") double lon, @Query("units")  String units, @Query("appid") String apiKey);


    /**
     * Get request to fetch city weather
     * @param city - city name
     * @param units - response units (Standard, metric, and imperial units are available)
     * @param apiKey - key from OpenWeatherMap.org
     * @return Response from OpenWeatherMap.org (forecast for next four days based on city name)
     */
    @GET("/data/2.5/forecast/")
    Call<WeatherResult> getWeatherByCity(@Query("q") String city, @Query("units")  String units, @Query("appid") String apiKey);
}