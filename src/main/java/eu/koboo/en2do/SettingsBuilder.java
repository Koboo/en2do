package eu.koboo.en2do;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class SettingsBuilder {

    boolean disallowUUIDKeys;
    boolean disableLogger;
    String collectionPrefix;
    String collectionSuffix;

    public SettingsBuilder disallowUUIDKeys() {
        this.disallowUUIDKeys = true;
        return this;
    }

    public SettingsBuilder disableLogger() {
        this.disableLogger = true;
        return this;
    }

    public SettingsBuilder collectionPrefix(String prefix) {
        this.collectionPrefix = prefix;
        return this;
    }

    public SettingsBuilder collectionSuffix(String suffix) {
        this.collectionSuffix = suffix;
        return this;
    }
}
