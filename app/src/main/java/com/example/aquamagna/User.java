package com.example.aquamagna;

import java.time.LocalDate;
import java.util.Date;

public class User {
    private String name, email, phone, birth;
    private Boolean male;

    public User(){}
    public User(String name, String email, String phone, String birth, Boolean male) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birth = birth;
        this.male = male;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getBirth() {
        return birth;
    }

    public Boolean getMale() {
        return male;
    }
}
