package eu.koboo.en2do.configurators;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCompressor;
import com.mongodb.ServerApi;
import eu.koboo.en2do.ClientConfigurator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@SuppressWarnings("unused")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ClientConfiguratorCompressors implements ClientConfigurator {

    Collection<MongoCompressor> compressors;

    @Override
    public void configure(MongoClientSettings.Builder settingsBuilder) {
        if(compressors == null) {
            throw new NullPointerException("compressors is null");
        }
        if(compressors.isEmpty()) {
            throw new IllegalArgumentException("compressors is empty");
        }
        settingsBuilder.compressorList(List.copyOf(compressors));
    }
}
