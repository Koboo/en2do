package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MongoManager {

    private static final String FILE_NAME = "credentials.properties";

    MongoClient client;
    MongoDatabase database;
    RepositoryFactory factory;

    public MongoManager(String connectString, String databaseString) {

        if (connectString == null && databaseString == null) {
            String[] credentials = readConfig();
            if (credentials == null) {
                throw new NullPointerException("No credentials given! Please make sure to provide " +
                        "accessible credentials.");
            }
            connectString = credentials[0];
            databaseString = credentials[1];
        }
        if (connectString == null) {
            throw new NullPointerException("No connectString given! Please make sure to provide a " +
                    "accessible connectString.");
        }
        if (databaseString == null) {
            throw new NullPointerException("No databaseString given! Please make sure to provide a " +
                    "accessible databaseString.");
        }
        ConnectionString connection = new ConnectionString(connectString);

        CodecRegistry pojoCodec = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry registry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodec);

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connection)
                .codecRegistry(registry)
                .build();

        client = MongoClients.create(clientSettings);
        database = client.getDatabase(databaseString);

        factory = new RepositoryFactory(this);
    }

    public MongoManager() {
        this(null, null);
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

    private static String[] readConfig() {
        File diskFile = new File(FILE_NAME);
        if (diskFile.exists()) {
            try (InputStream inputStream = new FileInputStream(diskFile)) {
                return readProperties(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        Class<MongoManager> managerClass = MongoManager.class;
        URL resourceFile = managerClass.getResource(FILE_NAME);
        if (resourceFile != null) {
            try (InputStream inputStream = managerClass.getResourceAsStream(FILE_NAME)) {
                return readProperties(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String[] readProperties(InputStream inputStream) {
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return new String[]{
                    properties.getProperty("mongodb.connect"),
                    properties.getProperty("mongodb.database")
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
