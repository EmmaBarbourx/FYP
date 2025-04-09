package com.example.injuryrecoveryapplication;

public class ArchivedPlan {
    private String planId;
    private String injuryArea;
    private String injuryType;
    private String archivedDate;

    // No-argument constructor required for Firestore
    public ArchivedPlan() {}

    // Getters and setters
    public String getPlanId() {
        return planId;
    }
    public void setPlanId(String planId) {
        this.planId = planId;
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
    public String getArchivedDate() {
        return archivedDate;
    }
    public void setArchivedDate(String archivedDate) {
        this.archivedDate = archivedDate;
    }
}
