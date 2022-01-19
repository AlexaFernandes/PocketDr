package com.example.pocketDR.DTO;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DependantDTO implements Parcelable {

    private String dependantId;
    private String name;
    private String email;
    private String caretakerId;
    private List<NotificationMedDTO> listMeds;

    public DependantDTO(){
        //NotificationMedDTO dummyNotification = new NotificationMedDTO();
        this.listMeds = new ArrayList<NotificationMedDTO>();
    }

    protected DependantDTO(Parcel in) {
        //ownerAcct = in.readParcelable(GoogleSignInAccount.class.getClassLoader());
        dependantId = in.readString();
        name = in.readString();
        email = in.readString();
        caretakerId = in.readString();
    }

    public static final Creator<DependantDTO> CREATOR = new Creator<DependantDTO>() {
        @Override
        public DependantDTO createFromParcel(Parcel in) {
            return new DependantDTO(in);
        }

        @Override
        public DependantDTO[] newArray(int size) {
            return new DependantDTO[size];
        }
    };

    public String getDependantId() {
        return dependantId;
    }

    public void setDependantId(String dependantId) {
        this.dependantId = dependantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCaretakerId() {
        return caretakerId;
    }

    public void setCaretakerId(String caretakerId) {
        this.caretakerId = caretakerId;
    }

    public List<NotificationMedDTO> getListMeds() {
        return listMeds;
    }

    public void setListMeds(List<NotificationMedDTO> listMeds) {
        this.listMeds = listMeds;
    }

    public void setOneListMed(NotificationMedDTO med) {
        this.listMeds.add(med);
    }

    public NotificationMedDTO getOneListMed(Integer pos) {
        return this.listMeds.get(pos);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeParcelable(ownerAcct, flags);
        dest.writeString(dependantId);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(caretakerId);
    }
}
