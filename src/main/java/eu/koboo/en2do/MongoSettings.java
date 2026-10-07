package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import eu.koboo.en2do.mongodb.codec.InternalPropertyCodecProvider;
import eu.koboo.en2do.mongodb.convention.IdConvention;
import eu.koboo.en2do.mongodb.convention.MethodMappingConvention;
import eu.koboo.en2do.mongodb.convention.TransformFieldConvention;
import eu.koboo.en2do.mongodb.convention.TransientConvention;
import eu.koboo.en2do.repository.NameConvention;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public record MongoSettings(
    String connectionString,
    ExecutorService executorService,
    boolean allowDiskUse,
    boolean appendMethodAsComment,
    boolean enableMethodProperties,
    String collectionPrefix,
    String collectionSuffix,
    NameConvention collectionNameConvention,
    Set<ClientConfigurator> clientConfiguratorSet,
    Set<Codec<?>> codecSet
) {

    public ConnectionString createConnectionString() {
        // Building and validating the given connection string.
        String settingsConnectionString = connectionString;
        if (settingsConnectionString == null || settingsConnectionString.isEmpty()) {
            throw new NullPointerException("connectionString is null or empty!");
        }
        return new ConnectionString(settingsConnectionString);
    }

    public String createDatabase(ConnectionString connectionString) {
        String database = connectionString.getDatabase();
        if (database == null || database.isEmpty()) {
            throw new NullPointerException("No database provided in connectionString!");
        }
        return database;
    }

    private InternalPropertyCodecProvider createInternalCodecProvider() {
        // Registering user-provided codecs from SettingsBuilder
        InternalPropertyCodecProvider internalPropertyCodecProvider = new InternalPropertyCodecProvider();
        for (Codec<?> codec : codecSet) {
            internalPropertyCodecProvider.registerCodec(codec);
        }
        return internalPropertyCodecProvider;
    }

    public CodecRegistry createCodecRegistry(MongoManager mongoManager) {
        return fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder()
                .register(createInternalCodecProvider())
                .automatic(true)
                .conventions(List.of(
                    Conventions.ANNOTATION_CONVENTION,
                    Conventions.SET_PRIVATE_FIELDS_CONVENTION,
                    Conventions.USE_GETTERS_FOR_SETTERS,
                    new TransientConvention(),
                    new TransformFieldConvention(),
                    new IdConvention(),
                    new MethodMappingConvention(mongoManager)
                ))
                .build())
        );
    }

    public void applyClientConfigurators(MongoClientSettings.Builder builder) {
        // Apply the user-provided client configurators
        for (ClientConfigurator clientConfigurator : clientConfiguratorSet) {
            clientConfigurator.configure(builder);
        }
    }
}
