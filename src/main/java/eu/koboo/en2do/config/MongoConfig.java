package eu.koboo.en2do.config;

import eu.koboo.config.Config;
import eu.koboo.config.FileConfig;

public record MongoConfig(String username, String password, String host, int port, String database,
                          boolean useAuthSource) {

    public static MongoConfig readConfig() {
        FileConfig config = Config.of("mongodb.cfg", c -> {
            c.init("username", "mongodb_user");
            c.init("password", "mongodb_password");
            c.init("host", "127.0.0.1");
            c.init("port", 27017);
            c.init("database", "mongodb_database");
            c.init("useAuthSource", false);
        });
        String username = config.getString("username");
        String password = config.getString("password");
        String host = config.getString("host");
        int port = config.getInt("port");
        String database = config.getString("database");
        boolean useAuthSource = config.getBoolean("useAuthSource");
        return new MongoConfig(username, password, host, port, database, useAuthSource);
    }
}