package eu.koboo.en2do;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.logging.Level;

@SuppressWarnings("unused")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class SettingsBuilder {

    /**
     * Defines the logger level for the mongodb loggers
     * with the following package prefixes:
     * - "org.mongodb"
     * - "com.mongodb"
     */
    Level mongoLoggerLevel;

    /**
     * Disables the usage of uuids as keys in any map structure.
     */
    boolean disallowUUIDKeys;

    /**
     * Allows the usage of disk storage for find iterables.
     */
    boolean allowDiskUse;

    /**
     * Sets the name of the method from the repository into the
     * mongodb json construct. Can be seen in the mongodb database logs.
     */
    boolean appendMethodAsComment;

    /**
     * This setting enables the conversion and usage of getter/setter methods
     * of entities in bson. Most of the time you don't need to enable this.
     */
    boolean enableMethodProperties;

    /**
     * Defines the prefix of every collection
     */
    String collectionPrefix;

    /**
     * Defines the suffix of every collection
     */
    String collectionSuffix;

    /**
     * See field documentation.
     *
     * @param level The value
     * @return This builder
     */
    public SettingsBuilder setMongoDBLoggerLevel(Level level) {
        this.mongoLoggerLevel = level;
        return this;
    }

    /**
     * See field documentation.
     *
     * @return This builder
     */
    public SettingsBuilder disableMongoDBLogger() {
        return setMongoDBLoggerLevel(Level.OFF);
    }

    /**
     * See field documentation.
     *
     * @return This builder
     */
    public SettingsBuilder disallowUUIDMapKeys() {
        this.disallowUUIDKeys = true;
        return this;
    }

    /**
     * See field documentation.
     *
     * @return This builder
     */
    public SettingsBuilder enableMethodProperties() {
        this.enableMethodProperties = true;
        return this;
    }

    /**
     * See field documentation.
     *
     * @return This builder
     */
    public SettingsBuilder disallowDiskUsageOnPagination() {
        this.allowDiskUse = false;
        return this;
    }

    /**
     * See field documentation.
     *
     * @return This builder
     */
    public SettingsBuilder appendMethodNameAsQueryComment() {
        this.appendMethodAsComment = true;
        return this;
    }

    /**
     * See field documentation.
     *
     * @param prefix The value
     * @return This builder
     */
    public SettingsBuilder collectionPrefix(String prefix) {
        this.collectionPrefix = prefix;
        return this;
    }

    /**
     * See field documentation.
     *
     * @param suffix The value
     * @return This builder
     */
    public SettingsBuilder collectionSuffix(String suffix) {
        this.collectionSuffix = suffix;
        return this;
    }

    /**
     * Merges two different settings builder together.
     *
     * @param otherBuilder The builder you want to merge into this builder.
     */
    protected void merge(SettingsBuilder otherBuilder) {
        this.mongoLoggerLevel = otherBuilder.getMongoLoggerLevel();
        this.disallowUUIDKeys = otherBuilder.isDisallowUUIDKeys();
        this.allowDiskUse = otherBuilder.isAllowDiskUse();
        this.appendMethodAsComment = otherBuilder.isAppendMethodAsComment();
        this.enableMethodProperties = otherBuilder.isEnableMethodProperties();
        this.collectionPrefix = otherBuilder.getCollectionPrefix();
        this.collectionSuffix = otherBuilder.getCollectionSuffix();
    }
}
