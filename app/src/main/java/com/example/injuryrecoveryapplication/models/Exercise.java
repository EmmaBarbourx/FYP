package com.example.injuryrecoveryapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Exercise implements Parcelable {

    private String id;
    private String name;
    private String instructions;
    private String bodyPart;
    private String target;
    private String equipment;
    private String gifUrl;
    private boolean angleSupported = false; // true if this ID lives in AngleReadyLibrary
    private String  family;                 // e.g. squat
    private String  difficulty;

    public Exercise() {
    }

    public Exercise(String id, String name, String instructions,
                    String bodyPart, String target,
                    String equipment, String gifUrl) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.bodyPart = bodyPart;
        this.target = target;
        this.equipment = equipment;
        this.gifUrl = gifUrl;
    }


    protected Exercise(Parcel in) {
        id = in.readString();
        name = in.readString();
        instructions = in.readString();
        bodyPart = in.readString();
        target = in.readString();
        equipment = in.readString();
        gifUrl = in.readString();
        angleSupported = in.readByte() != 0;
        family         = in.readString();
        difficulty     = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(instructions);
        parcel.writeString(bodyPart);
        parcel.writeString(target);
        parcel.writeString(equipment);
        parcel.writeString(gifUrl);
        parcel.writeByte((byte) (angleSupported ? 1 : 0));
        parcel.writeString(family);
        parcel.writeString(difficulty);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

    // --- GETTERS AND SETTERS ---

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getInstructions() {
        return instructions;
    }
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getBodyPart() {
        return bodyPart;
    }
    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }

    public String getEquipment() {
        return equipment;
    }
    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getGifUrl() {
        return gifUrl;
    }
    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
    }

    public boolean isAngleSupported()          { return angleSupported; }
    public void    setAngleSupported(boolean b){ this.angleSupported = b; }

    public String  getFamily()                 { return family; }
    public void    setFamily(String f)         { this.family = f; }

    public String  getDifficulty()             { return difficulty; }
    public void    setDifficulty(String d)     { this.difficulty = d; }
}
