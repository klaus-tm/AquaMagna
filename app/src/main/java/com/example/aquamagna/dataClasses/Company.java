package com.example.aquamagna.dataClasses;

public class Company {
    private String name, adress, city, country, email;

    public Company() {
    }

    public Company(String name, String adress, String city, String country, String email) {
        this.name = name;
        this.adress = adress;
        this.city = city;
        this.country = country;
        this.email = email;
    }

    public String getName() {
        return name;
    }
}
