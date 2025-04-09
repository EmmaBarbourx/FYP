package com.example.injuryrecoveryapplication.utils;

import java.util.HashMap;
import java.util.Map;

public class InjurySpecialtyUtils {

    // Mapping injury types to related keywords

    private static final Map<String, String[]> specialtyToKeywordsMap = new HashMap<>();
    static {
        // Shoulder injuries
        specialtyToKeywordsMap.put("dislocation", new String[]{"dislocation","shoulder"});
        specialtyToKeywordsMap.put("rotator cuff tear", new String[]{
                "rotator","cuff","shoulder","injury","shockwave"
        });
        specialtyToKeywordsMap.put("tendinitis", new String[]{
                "tendinitis","tendon","shoulder","shockwave"
        });

        // Elbow injuries
        specialtyToKeywordsMap.put("tennis elbow", new String[]{
                "tennis","elbow","shockwave","injury"
        });
        specialtyToKeywordsMap.put("golfer's elbow", new String[]{
                "golf","golfer","elbow","injury"
        });
        specialtyToKeywordsMap.put("bursitis", new String[]{
                "bursitis","bursa","elbow"
        });

        // Wrist injuries
        specialtyToKeywordsMap.put("carpal tunnel syndrome", new String[]{
                "carpal","tunnel","neuromuscular","hand","wrist"
        });
        specialtyToKeywordsMap.put("sprain (wrist)", new String[]{
                "sprain","wrist","injury"
        });
        specialtyToKeywordsMap.put("tendonitis (wrist)", new String[]{
                "tendinitis","tendon","wrist","injury"
        });

        // Lower Back injuries
        specialtyToKeywordsMap.put("herniated disc", new String[]{
                "herniated","disc","spine","back"
        });
        specialtyToKeywordsMap.put("muscle strain (back)", new String[]{
                "muscle","strain","back","injury"
        });
        specialtyToKeywordsMap.put("sciatica", new String[]{
                "sciatica","nerve","neuromuscular","back"
        });

        // Neck injuries
        specialtyToKeywordsMap.put("whiplash", new String[]{
                "whiplash","neck","injury"
        });
        specialtyToKeywordsMap.put("cervical disc injury", new String[]{
                "cervical","disc","neck","injury"
        });
        specialtyToKeywordsMap.put("strain (neck)", new String[]{
                "strain","neck","injury"
        });

        // Knee injuries
        specialtyToKeywordsMap.put("acl tear", new String[]{
                "acl","knee","ligament","injury","shockwave"
        });
        specialtyToKeywordsMap.put("meniscus tear", new String[]{
                "meniscus","knee","injury","shockwave"
        });
        specialtyToKeywordsMap.put("patellar tendinitis", new String[]{
                "patellar","tendinitis","knee","injury","shockwave"
        });

        // Ankle injuries
        specialtyToKeywordsMap.put("sprain", new String[]{
                "sprain","ankle","injury"
        });
        specialtyToKeywordsMap.put("achilles tendinitis", new String[]{
                "achilles","shockwave","ankle","foot","injury"
        });
        specialtyToKeywordsMap.put("fracture", new String[]{
                "fracture","ankle","foot"
        });

        // Foot injuries
        specialtyToKeywordsMap.put("plantar fasciitis", new String[]{
                "plantar","fasciitis","shockwave","foot","ankle","injury"
        });
        specialtyToKeywordsMap.put("fracture (foot)", new String[]{
                "fracture","foot"
        });
        specialtyToKeywordsMap.put("sprain (foot)", new String[]{
                "sprain","foot","injury"
        });

        // Hip injuries
        specialtyToKeywordsMap.put("hip flexor strain", new String[]{
                "hip","flexor","strain","shockwave","injury"
        });
        specialtyToKeywordsMap.put("labral tear", new String[]{
                "labral","hip","shockwave","injury"
        });
        specialtyToKeywordsMap.put("arthritis (hip)", new String[]{
                "arthritis","hip","shockwave"
        });

        // General muscle issues
        specialtyToKeywordsMap.put("strain", new String[]{
                "strain","muscle","injury"
        });
        specialtyToKeywordsMap.put("tear", new String[]{
                "tear","muscle","injury","shockwave"
        });
        specialtyToKeywordsMap.put("cramps", new String[]{
                "cramps","muscle","injury"
        });
    }

    // Mapping injury types to a category label
    private static final Map<String, String> injuryToCategoryMap = new HashMap<>();
    static {
        // Shoulder injuries
        injuryToCategoryMap.put("dislocation", "Sports/Injury");
        injuryToCategoryMap.put("rotator cuff tear", "Shockwave");
        injuryToCategoryMap.put("tendinitis", "Shockwave");

        // Elbow injuries
        injuryToCategoryMap.put("tennis elbow", "Sports/Injury");
        injuryToCategoryMap.put("golfer's elbow", "Sports/Injury");
        injuryToCategoryMap.put("bursitis", "Neuromuscular");

        // Wrist injuries
        injuryToCategoryMap.put("carpal tunnel syndrome", "Neuromuscular");
        injuryToCategoryMap.put("sprain (wrist)", "Sports/Injury");
        injuryToCategoryMap.put("tendonitis (wrist)", "Shockwave");

        // Lower Back injuries
        injuryToCategoryMap.put("herniated disc", "Neuromuscular");
        injuryToCategoryMap.put("muscle strain (back)", "Sports/Injury");
        injuryToCategoryMap.put("sciatica", "Neuromuscular");

        // Neck injuries
        injuryToCategoryMap.put("whiplash", "Sports/Injury");
        injuryToCategoryMap.put("cervical disc injury", "Neuromuscular");
        injuryToCategoryMap.put("strain (neck)", "Sports/Injury");

        // Knee injuries
        injuryToCategoryMap.put("acl tear", "Shockwave");
        injuryToCategoryMap.put("meniscus tear", "Shockwave");
        injuryToCategoryMap.put("patellar tendinitis", "Shockwave");

        // Ankle injuries
        injuryToCategoryMap.put("sprain", "Sports/Injury");
        injuryToCategoryMap.put("achilles tendinitis", "Shockwave");
        injuryToCategoryMap.put("fracture", "Sports/Injury");

        // Foot injuries
        injuryToCategoryMap.put("plantar fasciitis", "Neuromuscular");
        injuryToCategoryMap.put("fracture (foot)", "Sports/Injury");
        injuryToCategoryMap.put("sprain (foot)", "Sports/Injury");

        // Hip injuries
        injuryToCategoryMap.put("hip flexor strain", "Shockwave");
        injuryToCategoryMap.put("labral tear", "Shockwave");
        injuryToCategoryMap.put("arthritis (hip)", "Neuromuscular");

        // General muscle issues
        injuryToCategoryMap.put("strain", "Sports/Injury");
        injuryToCategoryMap.put("tear", "Shockwave");
        injuryToCategoryMap.put("cramps", "Neuromuscular");
    }

    // Returns the keywords for the injury type
    public static String[] getSpecialtyForInjury(String injuryType) {
        if (injuryType == null) return null;
        return specialtyToKeywordsMap.get(injuryType.toLowerCase());
    }

    // Returns the category label for the injury type
    public static String getCategoryForInjury(String injuryType) {
        if (injuryType == null) return null;
        return injuryToCategoryMap.get(injuryType.toLowerCase());
    }

    // Returns the complete mapping of injury types to keywords
    public static Map<String, String[]> getSpecialtyToKeywordsMap() {
        return specialtyToKeywordsMap;
    }
}
