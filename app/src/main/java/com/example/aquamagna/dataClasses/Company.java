package com.example.aquamagna.dataClasses;

public class Company {
    private String id, name, adress, city, county, country;

    public Company() {
    }

    public Company(String id, String name, String adress, String city, String county, String country) {
        this.id = id;
        this.name = name;
        this.adress = adress;
        this.city = city;
        this.county = county;
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAdress() {
        return adress;
    }

    public String getCity() {
        return city;
    }

    public String getCounty() {
        return county;
    }

    public String getCountry() {
        return country;
    }
}
