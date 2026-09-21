package eu.koboo.en2do;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

@UtilityClass
public class ConnectionString {

    /**
     * Automatically loading the {@link InputStream} into a new {@link Properties} object
     * and reading the connection string from it.
     *
     * @param inputStream The input stream, which should be read.
     * @return The read connection string
     */
    public static String fromInputStream(InputStream inputStream, String propertyKey) {
        Objects.requireNonNull(inputStream, "inputStream");
        Objects.requireNonNull(propertyKey, "propertyKey");
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(propertyKey);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading connectionString from InputStream", e);
        }
    }

    /**
     * Automatically reading the file in {@link Path}
     * and reading the connection string from it.
     *
     * @param path The {@link Path} for the given path.
     * @return The read connection string
     */
    public static String fromFile(Path path, String propertyKey) {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(propertyKey, "propertyKey");
        try (InputStream inputStream = Files.newInputStream(path)) {
            return fromInputStream(inputStream, propertyKey);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading connection string from file", e);
        }
    }

    /**
     * See {@link ConnectionString#fromFile(Path, String)}
     *
     * @param file        The {@link File} to read
     * @param propertyKey The property key to read
     * @return The read connection string
     */
    public static String fromFile(File file, String propertyKey) {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(propertyKey, "propertyKey");
        return fromFile(file.toPath(), propertyKey);
    }

    /**
     * See {@link ConnectionString#fromFile(Path, String)}
     *
     * @param filePath    The path of the file to read
     * @param propertyKey The property key to read
     * @return The read connection string
     */
    public static String fromFile(String filePath, String propertyKey) {
        Objects.requireNonNull(filePath, "filePath");
        Objects.requireNonNull(propertyKey, "propertyKey");
        return fromFile(Paths.get(filePath), propertyKey);
    }

    /**
     * Automatically reading the connection string from the system properties
     * using the given property key.
     *
     * @param propertyKey The property key for the connection string
     * @return The read connection string
     */
    public static String fromSystemProperties(String propertyKey) {
        Objects.requireNonNull(propertyKey, "propertyKey");
        return System.getProperty(propertyKey);
    }

    /**
     * Automatically reading the connection string from the systems environmental variables
     * using the given property key.
     *
     * @param environmentKey The environmental variable key for the connection string
     * @return The read connection string
     */
    public static String fromEnvironment(String environmentKey) {
        Objects.requireNonNull(environmentKey, "environmentKey");
        return System.getenv(environmentKey);
    }

    /**
     * Automatically reading the connection string from the given resource file as property
     * and reading the connection string using the given property key.
     * @param resourcePath The file resource path
     * @param propertyKey The property key of the connection string
     * @return The read connection string
     */
    public static String fromResource(String resourcePath, String propertyKey) {
        Objects.requireNonNull(resourcePath, "resourcePath");
        Objects.requireNonNull(propertyKey, "propertyKey");
        try (InputStream inputStream = ConnectionString.class
            .getClassLoader()
            .getResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            return fromInputStream(inputStream, propertyKey);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading connection string from resource: " + resourcePath, e);
        }
    }
}
