package eu.koboo.en2do;

import com.mongodb.MongoClientSettings;

public interface ClientConfigurator {

    void configure(MongoClientSettings.Builder settingsBuilder);
}
