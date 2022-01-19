package com.example.pocketDR.DTO;

import com.google.type.DateTime;

import java.util.ArrayList;
import java.util.List;

public class MedicineDTO {

    private String userId;
    private String medicineId;
    private String medicineName;
    private String description;
    private String startDate;
    private String endDate;
    private ArrayList<Boolean> days;
    private ArrayList<String> hours;

    private int tamHours = 1;
    private int tamDays = 7;

    public MedicineDTO() {
        this.days = new ArrayList<Boolean>(tamDays);
        this.hours = new ArrayList<String>(tamHours);
    }

    public void setDays(ArrayList<Boolean> days) {
        this.days = days;
    }

    public void setHours(ArrayList<String> hours) {
        this.hours = hours;
    }

    public boolean getOneDay(int pos) {
        return this.days.get(pos);

    }

    public String getOneHour(int pos) {
        return this.hours.get(pos);

    }

    public void setOneDay(Boolean hours, int pos) {
        this.days.add(pos, hours);
    }

    public List<String> getHours() {
        return hours;
    }

    public List<Boolean> getDays() {
        return days;
    }

    public void setOneHour(String hours) {
        this.hours.add(hours);
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }


    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}