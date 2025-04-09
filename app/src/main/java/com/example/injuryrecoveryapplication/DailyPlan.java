package com.example.injuryrecoveryapplication;

import com.example.injuryrecoveryapplication.models.Exercise;

import java.util.List;

public class DailyPlan {
    private String day;
    private List<Exercise> exercises;
    private boolean completed;

    // Constructor
    public DailyPlan(String day, List<Exercise> exercises, boolean completed) {
        this.day = day;
        this.exercises = exercises;
        this.completed = completed;
    }

    // No-argument constructor required for Firestore
    public DailyPlan() {}

    // Getters and Setters
    public String getDay() {
        return day; }

    public void setDay(String day) {
        this.day = day; }

    public List<Exercise> getExercises() {
        return exercises; }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises; }

    public boolean isCompleted() {
        return completed; }

    public void setCompleted(boolean completed) {
        this.completed = completed; }
}
