package com.example.injuryrecoveryapplication;

public class PainLog {
    private String painLevel;
    private String notes;
    private String timestamp;
    private boolean archived; // new field
    private String injuryId;  // new field for linking to a specific injury

    // Empty constructor for Firestore
    public PainLog() {
    }

    public PainLog(String painLevel, String notes, String timestamp, String injuryId) {
        this.painLevel = painLevel;
        this.notes = notes;
        this.timestamp = timestamp;
        this.archived = false; // default value
        this.injuryId = injuryId;
    }

    public String getPainLevel() {
        return painLevel;
    }

    public String getNotes() {
        return notes;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getInjuryId() {
        return injuryId;
    }

    public void setInjuryId(String injuryId) {
        this.injuryId = injuryId;
    }
}
