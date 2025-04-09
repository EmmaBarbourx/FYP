package com.example.injuryrecoveryapplication;

import java.util.List;

public class WeeklyRecoveryPlan {
    private List<DailyPlan> dailyPlans;


    public WeeklyRecoveryPlan(List<DailyPlan> dailyPlans) {
        this.dailyPlans = dailyPlans;
    }

    // Getters and Setters
    public List<DailyPlan> getDailyPlans() {
        return dailyPlans;
    }

    public void setDailyPlans(List<DailyPlan> dailyPlans) {
        this.dailyPlans = dailyPlans;
    }
}
