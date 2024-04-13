package com.example.aquamagna.dataClasses;

public class DeviceSensors {
    private Float Ph;
    private Float Turbidity;
    private Float Conductivity;

    public DeviceSensors(Float ph, Float turbidity, Float conductivity) {
        this.Ph = ph;
        this.Turbidity = turbidity;
        this.Conductivity = conductivity;
    }

    public Float getPh() {
        return Ph;
    }

    public Float getTurbidity() {
        return Turbidity;
    }

    public Float getConductivity() {
        return Conductivity;
    }
}
