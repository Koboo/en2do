package eu.koboo.en2do.utility.parse;

import eu.koboo.en2do.MongoManager;
import lombok.experimental.UtilityClass;

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
@UtilityClass
public class ConnectionStringUtils {

    /**
     * The default key of the connection string.
     */
    private static final String KEY = "en2do.connectstring";

    /**
     * The default name of the credentials file.
     */
    public static final String DEFAULT_CREDENTIALS_FILE_NAME = "credentials.properties";

    /**
     * Utility method for reading credentials from an input stream.
     *
     * @param inputStream The input stream, which should be read.
     * @return The new created credentials object.
     */
    private static String fromStreamProperties(InputStream inputStream) {
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(KEY);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading credentials");
        }
    }

    /**
     * Automatically reading credentials from the default resourcePath, which is
     * "{applicationJar}/credentials.properties"
     *
     * @return The new created credentials object.
     */
    public String fromResource() {
        return fromResource("/" + DEFAULT_CREDENTIALS_FILE_NAME);
    }

    /**
     * Automatically reading credentials from a resource file from given resourcePath.
     *
     * @param resourcePath The resource path with the containing credentials.
     * @return The new created credentials object.
     */
    public String fromResource(String resourcePath) {
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
    public String fromFile() {
        return fromFile(DEFAULT_CREDENTIALS_FILE_NAME);
    }

    /**
     * Automatically reading credentials from a file from given file.
     *
     * @param file The file with the containing credentials.
     * @return The new created credentials object.
     */
    public String fromFile(File file) {
        if (!file.exists()) {
            return null;
        }
        try (InputStream inputStream = new FileInputStream(file)) {
            return fromStreamProperties(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read file from path \"" + file.getAbsolutePath() + "\": ", e);
        }
    }

    /**
     * Automatically reading credentials from a file from given filePath.
     *
     * @param filePath The file path with the containing credentials.
     * @return The new created credentials object.
     */
    public String fromFile(String filePath) {
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
    public String fromSystemProperties() {
        return fromSystemProperties(KEY);
    }

    /**
     * Automatically reading credentials from the system properties,
     * using custom keys for the connectString and database
     *
     * @param propertyKey The property key for the connection string
     * @return The new created credentials object.
     */
    public String fromSystemProperties(String propertyKey) {
        return System.getProperty(propertyKey);
    }

    /**
     * Automatically reading credentials from the system environmental variables.
     *
     * @return The new created credentials object.
     */
    public String fromSystemEnvVars() {
        String envVarKey = KEY.toUpperCase(Locale.ROOT).replaceFirst("\\.", "_");
        return System.getenv(envVarKey);
    }

    /**
     * Automatically reading credentials from the system environmental variables.
     * using custom keys for the connectString and database
     *
     * @param envVarKey The environmental variable key for the connection string
     * @return The new created credentials object.
     */
    public String fromSystemEnvVars(String envVarKey) {
        return System.getenv(envVarKey);
    }
}
