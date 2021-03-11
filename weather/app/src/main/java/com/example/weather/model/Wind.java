package com.example.weather.model;


import java.text.DecimalFormat;

public class Wind {
    private float speed;
    private int deg;
    DecimalFormat df = new DecimalFormat("###.##");

    public Wind() {
    }

    public String getSpeed() {
        return  df.format(speed);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getDeg() {
        return deg;
    }

    public void setDeg(int deg) {
        this.deg = deg;
    }
}
