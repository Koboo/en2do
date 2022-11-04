package eu.koboo.en2do;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public record Credentials(String connectString, String database) {

    private static final String DEFAULT_CREDENTIAL_FILE = "credentials.properties";

    private static Credentials fromStreamProperties(InputStream inputStream) {
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return new Credentials(properties.getProperty("mongodb.connect"), properties.getProperty("mongodb.database"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Credentials fromResource() {
        return fromResource("/" + DEFAULT_CREDENTIAL_FILE);
    }

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
            return fromStreamProperties(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read resource from path \"" + resourcePath + "\": ", e);
        }
    }

    public static Credentials fromFile() {
        return fromFile(DEFAULT_CREDENTIAL_FILE);
    }

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

    public static Credentials of(String connectString, String database) {
        return new Credentials(connectString, database);
    }
}
