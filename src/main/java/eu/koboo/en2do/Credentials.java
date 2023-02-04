package eu.koboo.en2do;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

/**
 * This object is used to simplify creating credentials to the mongodb server.
 * See documentation: <a href="https://koboo.gitbook.io/en2do/get-started/create-the-mongomanager">...</a>
 */
@SuppressWarnings("unused")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class Credentials {

    /**
     * Empty representation of the credentials object
     */
    private static final Credentials EMPTY = new Credentials(null, null);
    /**
     * The default key of the connection string.
     */
    private static final String CONNECT_KEY = "en2do.connectstring";
    /**
     * The default key of the database.
     */
    private static final String DATABASE_KEY = "en2do.database";
    /**
     * The default name of the credentials file.
     */
    private static final String DEFAULT_CREDENTIAL_FILE = "credentials.properties";

    /**
     * Utility method for reading credentials from an input stream.
     * @param inputStream The input stream, which should be read.
     * @return The new created credentials object.
     */
    private static @NotNull Credentials fromStreamProperties(@NotNull InputStream inputStream) {
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return new Credentials(properties.getProperty(CONNECT_KEY), properties.getProperty(DATABASE_KEY));
        } catch (IOException e) {
            throw new RuntimeException("Error while loading credentials");
        }
    }

    /**
     * Automatically reading credentials from the default resourcePath, which is
     * "{applicationJar}/credentials.properties"
     * @return The new created credentials object.
     */
    public static @Nullable Credentials fromResource() {
        return fromResource("/" + DEFAULT_CREDENTIAL_FILE);
    }

    /**
     * Automatically reading credentials from a resource file from given resourcePath.
     * @param resourcePath The resource path with the containing credentials.
     * @return The new created credentials object.
     */
    public static @Nullable Credentials fromResource(@Nullable String resourcePath) {
        if (resourcePath == null) {
            throw new RuntimeException("Couldn't read resource from null path!");
        }
        Class<MongoManager> managerClass = MongoManager.class;
        URL resourceFile = managerClass.getResource(resourcePath);
        if (resourceFile == null) {
            return null;
        }
        try (InputStream inputStream = managerClass.getResourceAsStream(resourcePath)) {
            if(inputStream == null) {
                throw new RuntimeException("Couldn't create a stream from the resource in the path \"" + resourcePath + "\"!");
            }
            return fromStreamProperties(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read resource from path \"" + resourcePath + "\": ", e);
        }
    }

    /**
     * Automatically reading credentials from the default filePath, which is
     * "{applicationDirectory}/credentials.properties"
     * @return The new created credentials object.
     */
    public static @Nullable Credentials fromFile() {
        return fromFile(DEFAULT_CREDENTIAL_FILE);
    }

    /**
     * Automatically reading credentials from a file from given filePath.
     * @param filePath The file path with the containing credentials.
     * @return The new created credentials object.
     */
    public static @Nullable Credentials fromFile(@Nullable String filePath) {
        if (filePath == null) {
            throw new RuntimeException("Couldn't read file from null path!");
        }
        File diskFile = new File(filePath);
        if (!diskFile.exists()) {
            return null;
        }
        try (InputStream inputStream = new FileInputStream(diskFile)) {
            return fromStreamProperties(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read file from path \"" + filePath + "\": ", e);
        }
    }

    /**
     * Automatically reading credentials from the system properties.
     * @return The new created credentials object.
     */
    public static @NotNull Credentials fromSystemProperties() {
        return fromSystemProperties(CONNECT_KEY, DATABASE_KEY);
    }

    /**
     * Automatically reading credentials from the system properties,
     * using custom keys for the connectString and database
     * @param propertyConnectKey The property key for the connection string
     * @param propertyDatabaseKey The property key for the database
     * @return The new created credentials object.
     */
    public static @NotNull Credentials fromSystemProperties(@NotNull String propertyConnectKey, @NotNull String propertyDatabaseKey) {
        return new Credentials(System.getProperty(propertyConnectKey), System.getProperty(propertyDatabaseKey));
    }

    /**
     * Automatically reading credentials from the system environmental variables.
     * @return The new created credentials object.
     */
    public static @NotNull Credentials fromSystemEnvVars() {
        return fromSystemEnvVars(CONNECT_KEY.toUpperCase(Locale.ROOT).replaceFirst("\\.", "_"),
                DATABASE_KEY.toUpperCase(Locale.ROOT).replaceFirst("\\.", "_"));
    }

    /**
     * Automatically reading credentials from the system environmental variables.
     * using custom keys for the connectString and database
     * @param envVarConnectKey The environmental variable key for the connection string
     * @param envVarDatabaseKey The environmental variable key for the database
     * @return The new created credentials object.
     */
    public static @NotNull Credentials fromSystemEnvVars(@NotNull String envVarConnectKey, @NotNull String envVarDatabaseKey) {
        return new Credentials(System.getenv(envVarConnectKey), System.getenv(envVarDatabaseKey));
    }

    /**
     * Create a new credentials object by passing the two values directly.
     * @param connectString The connection string to the mongodb server.
     * @param database The database which should be used.
     * @return A new created credentials object.
     */
    public static @NotNull Credentials of(@Nullable String connectString, @Nullable String database) {
        return new Credentials(connectString, database);
    }

    /**
     * The connection string to the mongodb database server
     */
    @Nullable
    String connectString;
    /**
     * The database, which should be used
     */
    @Nullable
    String database;
}
