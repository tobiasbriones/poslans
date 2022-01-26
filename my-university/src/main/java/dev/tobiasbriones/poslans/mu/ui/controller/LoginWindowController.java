/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.controller;

import dev.tobiasbriones.poslans.mu.Main;
import dev.tobiasbriones.poslans.mu.Strings;
import dev.tobiasbriones.poslans.mu.database.User;
import dev.tobiasbriones.poslans.mu.ui.LoginWindow;
import engineer.mathsoftware.jdesk.ui.style.Style;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class LoginWindowController implements LoginWindow.Callback {
    private final Main main;
    private final User user;

    public LoginWindowController(Main main, User user) {
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
            final boolean dbIntegrity = verifyDBIntegrity(connection);
            final String databaseName;
            if (!dbIntegrity) {
                throw new DBIntegrityException();
            }
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
        catch (DBIntegrityException e) {
            final String msg = Style.wrapDialogTextInHTML(
                main.getAppStyle(),
                main.getStringResources().get(Strings.DB_INTEGRITY_FAIL_HTML)
            );
            user.reset();
            throw new Exception(msg);
        }
    }

    @Override
    public void login() {
        main.login();
    }

    // TODO update
    private boolean verifyDBIntegrity(Connection connection)
        throws SQLException {
        boolean isValid = true;
        ResultSet resultSet;

        try (Statement statement = connection.createStatement()) {
            resultSet = statement.executeQuery("SHOW TABLES LIKE 'campus'");
            isValid &= verifyResultSet(resultSet);
            resultSet = statement.executeQuery("SHOW TABLES LIKE 'career'");
            isValid &= verifyResultSet(resultSet);
            resultSet = statement.executeQuery("SHOW TABLES LIKE "
                                               + "'course_node'");
            isValid &= verifyResultSet(resultSet);
            resultSet = statement.executeQuery("SHOW TABLES LIKE "
                                               + "'current_term'");
            isValid &= verifyResultSet(resultSet);
            resultSet = statement.executeQuery(
                "SHOW TABLES LIKE 'student_current_term_enroll'");
            isValid &= verifyResultSet(resultSet);
            resultSet = statement.executeQuery("SHOW TABLES LIKE 'professor'");
            isValid &= verifyResultSet(resultSet);
            resultSet = statement.executeQuery("SHOW TABLES LIKE 'student'");
            isValid &= verifyResultSet(resultSet);
        }
        return isValid;
    }

    private static boolean verifyResultSet(ResultSet resultSet)
        throws SQLException {
        if (resultSet == null) {
            return false;
        }
        final boolean isValid = resultSet.next();
        resultSet.close();
        return isValid;
    }
}
