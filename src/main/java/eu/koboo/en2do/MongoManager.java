package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import eu.koboo.en2do.factory.RepositoryFactory;
import eu.koboo.en2do.mongodb.codec.InternalPropertyCodecProvider;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.UuidRepresentation;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class MongoManager {

    MongoSettings mongoSettings;
    InternalPropertyCodecProvider codecProvider;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    RepositoryFactory repositoryFactory;

    public MongoManager(MongoSettings mongoSettings) {
        if (mongoSettings == null) {
            mongoSettings = new SettingsBuilder().build();
        }
        this.mongoSettings = mongoSettings;

        ConnectionString connectionString = mongoSettings.createConnectionString();
        String database = mongoSettings.createDatabase(connectionString);

        codecProvider = mongoSettings.createInternalCodecProvider();
        CodecRegistry codecRegistry = mongoSettings.createCodecRegistry(this, codecProvider);

        // Prebuild the native client settings.
        MongoClientSettings.Builder clientSettingsBuilder = MongoClientSettings.builder()
            .applicationName("en2do-client")
            .applyConnectionString(connectionString)
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .codecRegistry(codecRegistry);

        mongoSettings.applyClientConfigurators(clientSettingsBuilder);

        // Build the actual mongodb client and database
        mongoClient = MongoClients.create(clientSettingsBuilder.build());
        mongoDatabase = mongoClient.getDatabase(database);

        repositoryFactory = new RepositoryFactory(this);
    }

    public MongoManager() {
        this(new SettingsBuilder().build());
    }

    public void close() {
        try {
            if (repositoryFactory != null) {
                repositoryFactory.close();
            }
            if (mongoClient != null) {
                mongoClient.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while closing: " + MongoManager.class, e);
        }
    }

    public <E, ID, R extends Repository<E, ID>> R create(Class<R> repositoryClass) {
        try {
            return repositoryFactory.create(repositoryClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Unmodifiable {@link List} with all
     * registered and created {@link Repository} of this {@link MongoManager}
     */
    public Set<Repository<?, ?>> getAllRepositories() {
        return repositoryFactory.getAllRepositories();
    }

    public <T> MongoManager registerCodec(Codec<T> typeCodec) {
        codecProvider.registerCodec(typeCodec);
        return this;
    }

    /**
     * Defines the logger level for the mongodb loggers
     * with the following package prefixes:
     * - "org.mongodb"
     * - "com.mongodb"
     * If you want to customize logging even more, look into the mongodb logging documentation:
     * <a href="https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/logging/">Click here</a>
     */
    public static void updateLoggingLevel(Level level) {
        // Applying loggerLevel to mongo db logger
        Logger.getLogger("org.mongodb").setLevel(level);
        Logger.getLogger("com.mongodb").setLevel(level);
    }
}
