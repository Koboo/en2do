package eu.koboo.en2do;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class Credentials {

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
    private static final String DEFAULT_CREDENTIALS_FILE_NAME = "credentials.properties";

    /**
     * Utility method for reading credentials from an input stream.
     *
     * @param inputStream The input stream, which should be read.
     * @return The new created credentials object.
     */
    private static Credentials fromStreamProperties(InputStream inputStream) {
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return new Credentials(properties.getProperty(CONNECT_KEY), properties.getProperty(DATABASE_KEY));
        } catch (IOException e) {
            throw new RuntimeException("Error while loading credentials");
        }
    }

    /**
     * Converts the given string to the respective environment variable key,
     * by using upper-case and replacing dots with underscore.
     *
     * @param string the string, which should be converted
     * @return the converted string.
     */
    private static String convertToEnvVarKey(String string) {
        return string.toUpperCase(Locale.ROOT).replaceFirst("\\.", "_");
    }

    /**
     * Automatically reading credentials from the default resourcePath, which is
     * "{applicationJar}/credentials.properties"
     *
     * @return The new created credentials object.
     */
    public static Credentials fromResource() {
        return fromResource("/" + DEFAULT_CREDENTIALS_FILE_NAME);
    }

    /**
     * Automatically reading credentials from a resource file from given resourcePath.
     *
     * @param resourcePath The resource path with the containing credentials.
     * @return The new created credentials object.
     */
    public static Credentials fromResource(String resourcePath) {
        if (resourcePath == null) {
            throw new RuntimeException("Couldn't read resource from null path!");
        }
        Class<MongoManager> managerClass = MongoManager.class;
        URL resourceFile = managerClass.getResource(resourcePath);
        if (resourceFile == null) {
            return null;
        }
        try (InputStream inputStream = managerClass.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
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
     *
     * @return The new created credentials object.
     */
    public static Credentials fromFile() {
        return fromFile(DEFAULT_CREDENTIALS_FILE_NAME);
    }

    /**
     * Automatically reading credentials from a file from given filePath.
     *
     * @param filePath The file path with the containing credentials.
     * @return The new created credentials object.
     */
    public static Credentials fromFile(String filePath) {
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
     *
     * @return The new created credentials object.
     */
    public static Credentials fromSystemProperties() {
        return fromSystemProperties(CONNECT_KEY, DATABASE_KEY);
    }

    /**
     * Automatically reading credentials from the system properties,
     * using custom keys for the connectString and database
     *
     * @param propertyConnectKey  The property key for the connection string
     * @param propertyDatabaseKey The property key for the database
     * @return The new created credentials object.
     */
    public static Credentials fromSystemProperties(String propertyConnectKey, String propertyDatabaseKey) {
        return new Credentials(System.getProperty(propertyConnectKey), System.getProperty(propertyDatabaseKey));
    }

    /**
     * Automatically reading credentials from the system environmental variables.
     *
     * @return The new created credentials object.
     */
    public static Credentials fromSystemEnvVars() {
        return fromSystemEnvVars(convertToEnvVarKey(CONNECT_KEY), convertToEnvVarKey(DATABASE_KEY));
    }

    /**
     * Automatically reading credentials from the system environmental variables.
     * using custom keys for the connectString and database
     *
     * @param envVarConnectKey  The environmental variable key for the connection string
     * @param envVarDatabaseKey The environmental variable key for the database
     * @return The new created credentials object.
     */
    public static Credentials fromSystemEnvVars(String envVarConnectKey, String envVarDatabaseKey) {
        return new Credentials(System.getenv(envVarConnectKey), System.getenv(envVarDatabaseKey));
    }

    /**
     * Create a new credentials object by passing the two values directly.
     *
     * @param connectString The connection string to the mongodb server.
     * @param database      The database which should be used.
     * @return A new created credentials object.
     */
    public static Credentials of(String connectString, String database) {
        return new Credentials(connectString, database);
    }

    /**
     * The connection string to the mongodb database server
     */
    String connectString;
    /**
     * The database, which should be used
     */
    String database;
}
