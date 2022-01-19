package com.example.pocketDR.DTO;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.pocketDR._LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class CaretakerDTO implements Parcelable {

    //public GoogleSignInClient ownerGoogleSignInClient;
    //public String ownerAcctIdToken;
    private String caretakerId;
    private String name;
    private String email;
    //private List<String> listDepend;
    //private List<NotificationMedDTO> listMeds;

    public CaretakerDTO(){
        //this.ownerAcctIdToken = "";
        //this.caretakerId = "";
        this.name = "";
        this.email = "";
        //this.listDepend = new ArrayList<String>();
        //this.listMeds = new ArrayList<NotificationMedDTO>();
    }

    protected CaretakerDTO(Parcel in) {
        //ownerAcctIdToken = in.readString();
        caretakerId = in.readString();
        name = in.readString();
        email = in.readString();
    }

    public static final Creator<CaretakerDTO> CREATOR = new Creator<CaretakerDTO>() {
        @Override
        public CaretakerDTO createFromParcel(Parcel in) {
            return new CaretakerDTO(in);
        }

        @Override
        public CaretakerDTO[] newArray(int size) {
            return new CaretakerDTO[size];
        }
    };

    public String getCaretakerId() {
        return caretakerId;
    }

    public void setCaretakerId(String caretakerId) {
        this.caretakerId = caretakerId;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeString(ownerAcctIdToken);
        dest.writeString(caretakerId);
        dest.writeString(name);
        dest.writeString(email);
    }

    /*public List<String> getListCaretakers() {
        return listDepend;
    }

    public void setListCaretakers(List<String> listCaretakers) {
        this.listDepend = listCaretakers;
    }

    public void setOneListCaretakers(String dependant) {
        this.listDepend.add(dependant);
    }

    public String getOneListCaretakers(Integer pos) {
        return this.listDepend.get(pos);
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
    }*/

}
