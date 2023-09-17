package com.hekmatullahamin.click.utils;

import android.os.Parcelable;

import java.io.Serializable;

public class UserStatus implements Serializable {
    private String date;
    private String state;
    private String time;

    public UserStatus() {
    }

    public UserStatus(String date, String state, String time) {
        this.date = date;
        this.state = state;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
