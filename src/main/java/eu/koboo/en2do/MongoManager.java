package eu.koboo.en2do;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import eu.koboo.en2do.config.MongoConfig;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MongoManager {

    MongoClient client;
    MongoDatabase database;

    public MongoManager(MongoConfig config) {
        ConnectionString connectionString;
        if (!config.useAuthSource()) {
            connectionString = new ConnectionString(
                    "mongodb://" + config.username() + ":" + config.password() + "@"
                            + config.host()
                            + ":" + config.port() + "/" + config.database());
        } else {
            connectionString = new ConnectionString(
                    "mongodb://" + config.username() + ":" + config.password() + "@"
                            + config.host()
                            + ":" + config.port() + "/?authSource=admin");
        }

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .retryWrites(true)
                .build();

        client = MongoClients.create(clientSettings);
        database = client.getDatabase(config.database());
    }

    public MongoManager() {
        this(MongoConfig.readConfig());
    }

    protected MongoDatabase getDatabase() {
        return database;
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