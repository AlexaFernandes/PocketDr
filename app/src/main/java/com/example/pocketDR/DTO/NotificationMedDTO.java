package com.example.pocketDR.DTO;

public class NotificationMedDTO {

    private String idMed;
    private String nameMed;
    private String idUser;
    private String date;
    private String hour;
    private String id;
    private boolean isTaken;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdMed() {
        return idMed;
    }

    public String getNameMed() {
        return nameMed;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setNameMed(String nameMed) {
        this.nameMed = nameMed;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public void setIdMed(String idMed) {
        this.idMed = idMed;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public void setIsTaken(boolean taken) {
        isTaken = taken;
    }

    public boolean getIsTaken(){
        return isTaken;
    }

    public void addNotId(String id){

        this.setId(id);

    }
}


