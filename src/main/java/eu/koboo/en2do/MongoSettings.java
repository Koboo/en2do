package eu.koboo.en2do;

import java.util.LinkedHashSet;
import java.util.Set;

public enum MongoSettings {

    DISABLE_LOGGER,
    DISALLOW_UUID_MAP_KEYS,
    ;

    private static final Set<MongoSettings> MONGO_SETTINGS_SET = new LinkedHashSet<>();

    public static void applySetting(MongoSettings settings) {
        MONGO_SETTINGS_SET.add(settings);
    }

    public static boolean hasSetting(MongoSettings settings) {
        return MONGO_SETTINGS_SET.contains(settings);
    }
}
