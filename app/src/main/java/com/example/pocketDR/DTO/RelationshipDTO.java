package com.example.pocketDR.DTO;

public class RelationshipDTO {

    private String Dependant;
    private String Caretaker;

    public String getDependant() {
        return Dependant;
    }

    public void setDependant(String dependant) {
        Dependant = dependant;
    }

    public String getCaretaker() {
        return Caretaker;
    }

    public void setCaretaker(String caretaker) {
        Caretaker = caretaker;
    }
}
