package eu.koboo.en2do.mongodb.codec;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.mongodb.codec.types.ClassCodec;
import eu.koboo.en2do.mongodb.codec.types.GenericMapCodec;
import eu.koboo.en2do.mongodb.codec.types.StringConversionCodec;
import eu.koboo.en2do.mongodb.codec.types.ZonedDateTimeCodec;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.bson.codecs.Codec;
import org.bson.codecs.pojo.PropertyCodecProvider;
import org.bson.codecs.pojo.PropertyCodecRegistry;
import org.bson.codecs.pojo.TypeWithTypeParameters;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;
import java.util.*;

/**
 * This codec provider enables the usage of the en2do custom codecs and adds them to the CodecRegistry.
 */
@Log
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class InternalPropertyCodecProvider implements PropertyCodecProvider {

    MongoManager manager;
    Map<Class<?>, Codec<?>> customCodecRegistry;

    public InternalPropertyCodecProvider(MongoManager manager) {
        this.manager = manager;
        this.customCodecRegistry = new LinkedHashMap<>();
        registerCodec(new ClassCodec());
        registerCodec(new ZonedDateTimeCodec());
        registerCodec(new StringConversionCodec<>(URI.class, URI::toString, URI::create));
        registerCodec(new StringConversionCodec<>(Locale.class, Locale::toLanguageTag, Locale::forLanguageTag));
        registerCodec(new StringConversionCodec<>(Currency.class, Currency::getCurrencyCode, Currency::getInstance));
        registerCodec(new StringConversionCodec<>(ZoneId.class, ZoneId::getId, ZoneId::of));
        registerCodec(new StringConversionCodec<>(Path.class, Path::toString, Path::of));
        registerCodec(new StringConversionCodec<>(Duration.class, Duration::toString, Duration::parse));
    }

    public <T> void registerCodec(Codec<T> typeCodec) {
        this.customCodecRegistry.put(typeCodec.getEncoderClass(), typeCodec);
    }

    /**
     * @param type     the class and bound type parameters for which to get a Codec
     * @param registry the registry to use for resolving dependent Codec instances
     * @param <T>      The type of the codec
     * @return The codec from the type
     * @see PropertyCodecProvider
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Codec<T> get(TypeWithTypeParameters<T> type, PropertyCodecRegistry registry) {
        Class<T> typeClass = type.getType();
        List<? extends TypeWithTypeParameters<?>> typeParameters = type.getTypeParameters();

        if (Map.class.isAssignableFrom(typeClass) && typeParameters.size() == 2) {
            return new GenericMapCodec(manager, typeClass, registry.get(typeParameters.get(0)),
                registry.get(typeParameters.get(1)));
        }
        Codec<?> codec = customCodecRegistry.get(typeClass);
        if (codec != null) {
            return (Codec<T>) codec;
        }
        return null;
    }

}
