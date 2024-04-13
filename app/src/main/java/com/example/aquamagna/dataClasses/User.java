package com.example.aquamagna.dataClasses;

import java.time.LocalDate;
import java.util.Date;

public class User {
    private String name, email, phone, company;

    public User(){}
    public User(String name, String email, String phone, String company) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.company = company;
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

    public String getCompany(){
        return company;
    }
}
