package eu.koboo.en2do;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class SettingsBuilder {

    boolean disableLogger;
    boolean disallowUUIDKeys;
    boolean disallowDiskUsage;
    boolean appendMethodAsComment;
    String collectionPrefix;
    String collectionSuffix;

    public SettingsBuilder disableMongoDBLogger() {
        this.disableLogger = true;
        return this;
    }

    public SettingsBuilder disallowUUIDMapKeys() {
        this.disallowUUIDKeys = true;
        return this;
    }

    public SettingsBuilder disallowDiskUsageOnPagination() {
        this.disallowDiskUsage = true;
        return this;
    }

    public SettingsBuilder appendMethodNameAsQueryComment() {
        this.appendMethodAsComment = true;
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
