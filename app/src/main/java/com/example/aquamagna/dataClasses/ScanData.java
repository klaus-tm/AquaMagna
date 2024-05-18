package com.example.aquamagna.dataClasses;

public class ScanData {
    private String date, location, user, company, ph, turbidity, conductivity;

    public ScanData() {}

    public ScanData(String date, String location, String user, String company, String ph, String turbidity, String conductivity) {
        this.date = date;
        this.location = location;
        this.user = user;
        this.company = company;
        this.ph = ph;
        this.turbidity = turbidity;
        this.conductivity = conductivity;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getUser() {
        return user;
    }

    public String getCompany() {
        return company;
    }

    public String getPh() {
        return ph;
    }

    public String getTurbidity() {
        return turbidity;
    }

    public String getConductivity() {
        return conductivity;
    }
}