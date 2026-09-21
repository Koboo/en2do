package eu.koboo.en2do.configurators;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import eu.koboo.en2do.ClientConfigurator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ClientConfiguratorServerApi implements ClientConfigurator {

    ServerApi serverApi;

    @Override
    public void configure(MongoClientSettings.Builder clientSettingsBuilder) {
        if (serverApi == null) {
            throw new NullPointerException("serverApi is null");
        }
        clientSettingsBuilder.serverApi(serverApi);
    }
}
