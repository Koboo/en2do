package eu.koboo.en2do.configurators;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCompressor;
import eu.koboo.en2do.ClientConfigurator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ClientConfiguratorCompressors implements ClientConfigurator {

    Collection<MongoCompressor> compressors;

    @Override
    public void configure(MongoClientSettings.Builder clientSettingsBuilder) {
        if (compressors == null) {
            throw new NullPointerException("compressors is null");
        }
        if (compressors.isEmpty()) {
            throw new IllegalArgumentException("compressors is empty");
        }
        clientSettingsBuilder.compressorList(List.copyOf(compressors));
    }
}
