package com.example.injuryrecoveryapplication;

public class UserProfile {
    private String age;
    private String gender;
    private String fitnessLevel;
    private String injuryArea;
    private String injuryType;
    private boolean profileCreated;

    // Constructor
    public UserProfile(String age, String gender, String fitnessLevel, String injuryArea, String injuryType) {
        this.age = age;
        this.gender = gender;
        this.fitnessLevel = fitnessLevel;
        this.injuryArea = injuryArea;
        this.injuryType = injuryType;
        this.profileCreated = true; // Set to true when creating the profile
    }

    // constructor
    public UserProfile() {}

    // Getters and setters
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFitnessLevel() {
        return fitnessLevel;
    }

    public void setFitnessLevel(String fitnessLevel) {
        this.fitnessLevel = fitnessLevel;
    }

    public String getInjuryArea() {
        return injuryArea;
    }

    public void setInjuryArea(String injuryArea) {
        this.injuryArea = injuryArea;
    }

    public String getInjuryType() {
        return injuryType;
    }

    public void setInjuryType(String injuryType) {
        this.injuryType = injuryType;
    }
}

