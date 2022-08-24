package eu.koboo.en2do;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import eu.koboo.en2do.config.MongoConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MongoManager {

    MongoClient client;
    @Getter
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

    public boolean close() {
        try {
            client.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(String coll, Bson filter, Document document) {
        MongoCollection<Document> collection = database.getCollection(coll);
        if (document.isEmpty()) {
            return delete(coll, filter);
        }
        UpdateOptions options = new UpdateOptions()
                .upsert(true);
        UpdateResult result = collection.updateOne(filter,
                new BasicDBObject("$set", document), options);
        return result.wasAcknowledged();
    }

    public boolean delete(String coll, Bson filter) {
        DeleteResult result = database.getCollection(coll).deleteOne(filter);
        return result.wasAcknowledged();
    }

    public Document find(String coll, Bson filter) {
        return database.getCollection(coll).find(filter).first();
    }

    public List<Document> into(String coll, Bson filter) {
        return database.getCollection(coll).find(filter).into(new ArrayList<>());
    }

    public List<Document> all(String coll) {
        return database.getCollection(coll).find().into(new ArrayList<>());
    }

    public boolean exists(String coll, Bson filter) {
        Document document = database.getCollection(coll).find(filter).first();
        return document != null && !document.isEmpty();
    }
}