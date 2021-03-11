package com.example.weather;

import android.content.Context;


import com.example.weather.model.WeatherResult;
import java.io.BufferedReader;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileOperations {
    private static String FILENAME ="last_forecast_file";

    public static void saveData(Context context, WeatherResult weatherResult) {
        int hourFromFile2 = Integer.parseInt(weatherResult.getList().get(0).dt_txt.substring(11, 13));
        int indexToDisp = ((24 - hourFromFile2) / 3);

        String text_to_save =  (Math.floor(weatherResult.city.getCoord().getLat()*100)/100) + "\n"
                + (Math.floor(weatherResult.city.getCoord().getLon()*100)/100)  + "\n" +
                weatherResult.getCity().getName() + "\n"
                + weatherResult.getList().get(0).dt_txt + "\n"
                + weatherResult.getList().get(0).main.getTemp() + "\n"
                + weatherResult.getList().get(0).getMain().getPressure() + "\n"
                + weatherResult.getList().get(0).wind.getSpeed() + "\n"
                + weatherResult.getList().get(0).main.getHumidity() + "\n"

                + weatherResult.getList().get(indexToDisp + 4).dt_txt + "\n"
                + weatherResult.getList().get(indexToDisp + 4).main.getTemp() + "\n"


                + weatherResult.getList().get(indexToDisp + 12).dt_txt + "\n"
                + weatherResult.getList().get(indexToDisp + 12).main.getTemp() + "\n"


                + weatherResult.getList().get(indexToDisp + 20).dt_txt + "\n"
                + weatherResult.getList().get(indexToDisp + 20).main.getTemp() + "\n"

                + weatherResult.getList().get(0).getWeather().get(0).getIcon() + "\n"
                + weatherResult.getList().get((indexToDisp + 4)).getWeather().get(0).getIcon() + "\n"
                + weatherResult.getList().get((indexToDisp + 12)).getWeather().get(0).getIcon() + "\n"
                + weatherResult.getList().get((indexToDisp + 20)).getWeather().get(0).getIcon();
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(text_to_save.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    public static String[] openWeatherFile(Context context){
        String[] data_from_file = new String[18];
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                data_from_file[i] = line;
                i++;
            }
            inputStreamReader.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data_from_file;
    }
}
