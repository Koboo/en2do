package eu.koboo.en2do;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import eu.koboo.en2do.repository.NameConvention;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("unused")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class SettingsBuilder {

    /**
     * This is the connectionString you need to provide, to connect to your {@link MongoDatabase}.
     * <a href="https://www.mongodb.com/docs/manual/reference/connection-string/">See documentation of MongoDB</a>.
     * For easier retrieval you can use {@link ConnectionString} utility class.
     */
    String connectionString = null;

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
     * Disables the usage of {@link UUID}s as keys in {@link Map} fields.
     * MongoDB by default does not allow this, but it should be
     * pretty helpful.
     * Example within an entity:
     * private final Map<UUID, String> uuidToNameMap;
     */
    boolean disallowUUIDKeys = false;

    /**
     * Allows the usage of disk storage for find iterables.
     * This is needed if the size of your results are too large
     * for your memory.
     * If the client executes a find operation and the result is too big to be stored
     * in memory, this options allows the client to temporary save the result onto the disk.
     */
    boolean allowDiskUse = true;

    /**
     * Sets the name of the method from the repository into the
     * mongodb bson query and can then be seen in the mongodb database logs,
     * through tools like MongoDB Compass or MongoDB Atlas.
     * This is mainly used for debug purposes and/or performance analysis.
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
     * Defines the prefix of every collection.
     * Example: collectionPrefix = "test_"
     * Results:
     * - "test_players"
     * - "test_settings"
     * - "test_quests"
     */
    String collectionPrefix = null;

    /**
     * Defines the suffix of every collection.
     * Example: collectionPrefix = "_test"
     * Results:
     * - "players_test"
     * - "settings_test"
     * - "quests_test"
     */
    String collectionSuffix = null;

    /**
     * Enables the automatic generation of collection
     * names, based on the Repository class name and the given convention.
     * The repositories still need @Collection, but you can leave it empty.
     * Examples:
     * - {@link NameConvention#CAMEL_CASE_LOWER}
     *   > CustomerRepository -> "customerRepository"
     * - {@link NameConvention#CAMEL_CASE_UPPER}
     *   > CustomerRepository -> "CustomerRepository"
     * - {@link NameConvention#SNAKE_CASE}
     *   > CustomerRepository -> "customer_repository"
     * - {@link NameConvention#FLAT_CASE}
     *   > CustomerRepository -> "customerrepository"
     * - {@link NameConvention#MACRO_CASE}
     *   > CustomerRepository -> "CUSTOMER_REPOSITORY"
     * - {@link NameConvention#KEBAB_CASE}
     *   > CustomerRepository -> "customer-repository"
     */
    NameConvention collectionNameConvention = null;

    /**
     * A collection of custom implemented {@link ClientConfigurator}, which are
     * executed, to allow access on the native {@link MongoClientSettings.Builder} instance
     * before the native {@link MongoClient} is created.
     * USE WITH CAUTION. You could mess up the en2do configurations.
     * Some prebuilt {@link ClientConfigurator}s:
     * - {@link eu.koboo.en2do.configurators.ClientConfiguratorCompressors}
     * - {@link eu.koboo.en2do.configurators.ClientConfiguratorServerApi}
     */
    Set<ClientConfigurator> clientConfiguratorSet = null;

    /**
     * See field documentation.
     *
     * @param connectionString The value
     * @return This builder
     */
    public SettingsBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

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
     *
     * @return This builder
     */
    public SettingsBuilder collectionNameGeneration(NameConvention convention) {
        this.collectionNameConvention = convention;
        return this;
    }

    /**
     * See field documentation.
     *
     * @param configurator The value
     * @return This builder
     */
    public SettingsBuilder clientConfigurator(ClientConfigurator configurator) {
        if (clientConfiguratorSet == null) {
            clientConfiguratorSet = new HashSet<>();
        }
        clientConfiguratorSet.add(configurator);
        return this;
    }

    /**
     * See field documentation.
     *
     * @param configurators The value
     * @return This builder
     */
    public SettingsBuilder clientConfigurators(Collection<ClientConfigurator> configurators) {
        if (clientConfiguratorSet == null) {
            clientConfiguratorSet = new HashSet<>();
        }
        clientConfiguratorSet.addAll(configurators);
        return this;
    }
}
