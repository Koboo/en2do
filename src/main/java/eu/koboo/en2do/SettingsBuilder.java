package eu.koboo.en2do;

import com.mongodb.ServerApi;
import eu.koboo.en2do.repository.NameConvention;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.logging.Level;

@SuppressWarnings("unused")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SettingsBuilder {

    /**
     * Defines the logger level for the mongodb loggers
     * with the following package prefixes:
     * - "org.mongodb"
     * - "com.mongodb"
     * If you want to customize logging even more, look into the mongodb logging documentation:
     * <a href="https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/logging/">Click here</a>
     */
    Level mongoLoggerLevel = null;

    /**
     * Disables the usage of uuids as keys in Map fields.
     * MongoDB by default does not allow this, but it should be
     * pretty helpful.
     */
    boolean disallowUUIDKeys = false;

    /**
     * Allows the usage of disk storage for find iterables.
     * This is needed if the size of your results are too large
     * for your ram.
     */
    boolean allowDiskUse = true;

    /**
     * Sets the name of the method from the repository into the
     * mongodb bson construct and can then be seen in the mongodb database logs,
     * through tools like MongoDB Compass or MongoDB Atlas.
     */
    boolean appendMethodAsComment = false;

    /**
     * This setting enables the conversion and usage of getter/setter methods
     * of entities in bson. Most of the time you don't need to enable this.
     * If your entity has methods like "entity.isValid()" and that is not a getter
     * of the field "valid", mongodb would still save it as "valid: {value_of_method}".
     * To avoid this, the getter/setter mapping is disabled by default.
     */
    boolean enableMethodProperties = false;

    /**
     * Defines the prefix of every collection
     */
    String collectionPrefix = null;

    /**
     * Defines the suffix of every collection
     */
    String collectionSuffix = null;

    /**
     * Enables the automatic generation of collection
     * names, based on the Repository class name and the given convention.
     * The repositories still need @Collection, but you can leave it empty.
     */
    NameConvention collectionNameConvention = null;

    /**
     * Defines the wanted ServerApi of the mongodb server.
     * This enables several compatbility settings inside the
     * mongodb driver itself.
     * See here: <a href="https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/stable-api">...</a>
     */
    ServerApi serverApi = null;

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
     * See field documentation.

     * @return This builder
     */
    public SettingsBuilder collectionNameGeneration(NameConvention convention) {
        this.collectionNameConvention = convention;
        return this;
    }

    /**
     * See field documentation.
     *
     * @param serverApi The value
     * @return This builder
     */
    public SettingsBuilder serverApi(ServerApi serverApi) {
        this.serverApi = serverApi;
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
