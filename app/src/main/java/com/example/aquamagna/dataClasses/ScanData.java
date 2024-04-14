package com.example.aquamagna.dataClasses;

public class ScanData {
    private String id, date, location, user, company, ph, turbidity, conductivity;

    public ScanData() {}

    public ScanData(String id, String date, String location, String user, String company, String ph, String turbidity, String conductivity) {
        this.id = id;
        this.date = date;
        this.location = location;
        this.user = user;
        this.company = company;
        this.ph = ph;
        this.turbidity = turbidity;
        this.conductivity = conductivity;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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