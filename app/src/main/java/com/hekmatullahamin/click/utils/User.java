package com.hekmatullahamin.click.utils;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String userId;
    private String userProfile;
    private String userAbout;
    private String contact;
    private String request_type;
    private UserStatus userStatus;

    public User() {
    }

    public User(String name, String surname, String email, String password, String userId, String userProfile,
                String userAbout, String contact, String request_type, UserStatus userStatus) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.userId = userId;
        this.userProfile = userProfile;
        this.userAbout = userAbout;
        this.contact = contact;
        this.request_type = request_type;
        this.userStatus = userStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public String getUserAbout() {
        return userAbout;
    }

    public void setUserAbout(String userAbout) {
        this.userAbout = userAbout;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
