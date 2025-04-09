package com.example.injuryrecoveryapplication.utils;

import java.util.HashMap;
import java.util.Map;

public class InjuryCategoryMapping {

    private static final Map<String, String> areaToCategoryIdMap = new HashMap<>();

    static {
        // These mappings are based on the data you provided
        areaToCategoryIdMap.put("shoulder", "13");
        areaToCategoryIdMap.put("elbow", "8");
        areaToCategoryIdMap.put("wrist", "8");
        areaToCategoryIdMap.put("lower back", "12");
        areaToCategoryIdMap.put("neck", "12");
        areaToCategoryIdMap.put("knee", "9");
        areaToCategoryIdMap.put("ankle", "9");
        areaToCategoryIdMap.put("foot", "9");
        areaToCategoryIdMap.put("hip", "9");
        areaToCategoryIdMap.put("general muscle", "8");
    }

    public static String getCategoryIdForArea(String injuryArea) {
        return areaToCategoryIdMap.get(injuryArea.toLowerCase());
    }
    }


