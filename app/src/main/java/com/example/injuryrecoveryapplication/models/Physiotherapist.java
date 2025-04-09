package com.example.injuryrecoveryapplication.models;

import java.util.List;

public class Physiotherapist {
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private String specialty;
    private List<Category> categories; // List of business categories

    public Physiotherapist(String name, String address, double latitude, double longitude, String specialty, List<Category> categories) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.specialty = specialty;
        this.categories = categories;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getSpecialty() {
        return specialty;
    }

    public List<Category> getCategories() {
        return categories;
    }


     // Represents a business category from Yelp

    public static class Category {
        private String alias;
        private String title;

        public Category(String alias, String title) {
            this.alias = alias;
            this.title = title;
        }

        // Getters and Setters

        public String getAlias() {
            return alias;
        }

        public String getTitle() {
            return title;
        }
    }
}
