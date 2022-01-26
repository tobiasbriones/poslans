/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.database;

import org.json.JSONObject;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Defines a system user profile.
 *
 * @author Tobias Briones
 */
public final class User {
    private URL websiteURL;
    private String database;
    private String user;
    private String password;
    private String driverURL;
    private String databaseName;

    public User() {
        reset();
    }

    public URL getWebsiteURL() {
        return websiteURL;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverURL() {
        return driverURL;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public boolean isSet() {
        return !(
            websiteURL == null || database.trim().isEmpty()
            || user == null || user.trim().isEmpty()
            || password == null || password.trim().isEmpty()
            || driverURL == null || driverURL.trim().isEmpty()
            || databaseName == null || databaseName.trim().isEmpty()
        );
    }

    public void set(JSONObject jsonObject) throws Exception {
        final URL url = new URL(jsonObject.getString("url"));
        final String database = jsonObject.getString("database");
        final String user = jsonObject.getString("user");
        final String password = jsonObject.getString("password");
        set(url, database, user, password);
    }

    public void set(
        URL websiteURL,
        String database,
        String user,
        String password
    ) {
        this.websiteURL = websiteURL;
        this.database = database;
        this.user = user;
        this.password = password;
        this.driverURL = "jdbc:mysql://" + websiteURL.getHost() + "/" + database;
    }

    public void get(JSONObject jsonObject) {
        jsonObject.put("url", websiteURL.toString());
        jsonObject.put("database", database);
        jsonObject.put("user", user);
        jsonObject.put("password", password);
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(driverURL, user, password);
    }

    public void reset() {
        this.websiteURL = null;
        this.database = null;
        this.user = null;
        this.password = null;
        this.driverURL = null;
        this.databaseName = null;
    }
}
