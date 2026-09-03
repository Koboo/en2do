package eu.koboo.en2do;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;

public interface ClientConfigurator {

    /**
     * Gets executed right before the actual {@link MongoClient} gets created.
     * @param clientSettingsBuilder The native {@link MongoClientSettings.Builder} to be configured.
     */
    void configure(MongoClientSettings.Builder clientSettingsBuilder);
}
