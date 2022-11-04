package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import eu.koboo.en2do.codec.MapCodecProvider;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MongoManager {

    MongoClient client;
    MongoDatabase database;
    RepositoryFactory factory;

    public MongoManager(Credentials credentials) {

        // If no credentials given, try loading them from default file.
        if (credentials == null) {
            credentials = Credentials.fromFile();
        }
        // If no credentials given, try loading them from default resource.
        if (credentials == null) {
            credentials = Credentials.fromResource();
        }
        // If no credentials given, throw exception.
        if(credentials == null) {
            throw new NullPointerException("No credentials given! Please make sure to provide " +
                    "accessible credentials.");
        }

        String connectString = credentials.connectString();
        // If credentials connectString is null, throw exception
        if (connectString == null) {
            throw new NullPointerException("No connectString given! Please make sure to provide a " +
                    "accessible connectString.");
        }
        // If credentials databaseString is null, throw exception
        String databaseString = credentials.database();
        if (databaseString == null) {
            throw new NullPointerException("No databaseString given! Please make sure to provide a " +
                    "accessible databaseString.");
        }

        ConnectionString connection = new ConnectionString(connectString);

        CodecRegistry pojoCodec = CodecRegistries.fromProviders(PojoCodecProvider.builder()
                .register(new MapCodecProvider())
                .automatic(true)
                .build());
        CodecRegistry registry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodec);

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connection)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(registry)
                .build();

        client = MongoClients.create(clientSettings);
        database = client.getDatabase(databaseString);

        factory = new RepositoryFactory(this);
    }

    public MongoManager() {
        this(null);
    }

    protected MongoDatabase getDatabase() {
        return database;
    }

    public <E, ID, R extends Repository<E, ID>> R create(Class<R> repoClass) {
        try {
            return factory.create(repoClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean close() {
        try {
            client.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
