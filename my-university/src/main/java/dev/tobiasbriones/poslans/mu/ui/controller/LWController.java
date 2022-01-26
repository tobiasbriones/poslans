/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.controller;

import dev.tobiasbriones.poslans.mu.Main;
import dev.tobiasbriones.poslans.mu.Strings;
import dev.tobiasbriones.poslans.mu.database.User;
import dev.tobiasbriones.poslans.mu.ui.LoginWindow;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class LWController implements LoginWindow.Callback {
    private final Main main;
    private final User user;

    public LWController(Main main, User user) {
        this.main = main;
        this.user = user;
    }

    @Override
    public void connect(
        URL websiteURL,
        String database,
        String userName,
        String password
    ) throws Exception {
        user.set(websiteURL, database, userName, password);
        try (
            Connection connection = this.user.connect();
            Statement statement = connection.createStatement()
        ) {
            final ResultSet resultSet = statement.executeQuery("SELECT DATABASE()");
            final String databaseName;
            if (resultSet.next()) {
                final String dbStr = main.getStringResources()
                                         .get(Strings.DATABASE);
                databaseName = dbStr + resultSet.getString(1);
            }
            else {
                throw new SQLException("Database not selected/found");
            }
            user.setDatabaseName(databaseName);
        }
        catch (SQLException e) {
            final String msg = main.getStringResources()
                                   .get(Strings.CHECK_INPUT);
            user.reset();
            throw new Exception(msg);
        }
    }

    @Override
    public void login() {
        main.login();
    }
}
