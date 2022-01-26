/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu;

import dev.tobiasbriones.poslans.mu.database.User;
import dev.tobiasbriones.poslans.mu.ui.LoginWindow;
import dev.tobiasbriones.poslans.mu.ui.MainWindow;
import dev.tobiasbriones.poslans.mu.ui.SplashWindow;
import dev.tobiasbriones.poslans.mu.ui.controller.LoginWindowController;
import dev.tobiasbriones.poslans.mu.ui.controller.MainWindowController;
import engineer.mathsoftware.jdesk.App;
import engineer.mathsoftware.jdesk.AppInstance;
import engineer.mathsoftware.jdesk.resources.Resources;
import engineer.mathsoftware.jdesk.resources.StringResources;
import engineer.mathsoftware.jdesk.ui.style.AppStyle;
import engineer.mathsoftware.jdesk.ui.style.DefaultStyle;
import engineer.mathsoftware.jdesk.ui.style.Style;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class Main extends App implements AppInstance {
    private static final DefaultStyle.ColorPair[] APP_COLORS = {
        new DefaultStyle.ColorPair(
            Style.ACCENT,
            Color.decode("#00B8D4")
        )
    };

    // -------------------- STATIC METHODS -------------------- //
    public static void main(String[] args) {
        final Font font = Resources.loadFont("Roboto-Light");
        try {
            new Main(font);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final AppStyle appStyle;
    private final StringResources stringResources;
    private final User user;

    public Main(Font font) throws IOException {
        super(font);
        this.appStyle = new DefaultStyle(font, APP_COLORS);
        this.stringResources = new StringResources(getClass());
        this.user = new User();
        try {
            final File loginFile = new File("user.key");
            if (loginFile.exists()) {
                login(loginFile);
            }
            else {
                loginWindow();
            }
        }
        catch (Exception e) {
            loginWindow();
        }
    }

    User getUser() {
        return user;
    }

    @Override
    public String getAppConfigFile() {
        return "config.properties";
    }

    @Override
    protected AppInstance getAppInstance() {
        return this;
    }

    @Override
    public AppStyle getAppStyle() {
        return appStyle;
    }

    @Override
    public StringResources getStringResources() {
        return stringResources;
    }

    public void login() {
        if (!user.isSet()) {
            return;
        }
        final File loginFile = new File("user.key");
        final JSONObject loginJSON = new JSONObject();
        user.get(loginJSON);
        try {
            // TODO ADD NEW IMPL
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fail to store user login.");
        }
        mainWindow();
    }

    private void login(File loginFile) throws Exception {
        try (final InputStream is = new FileInputStream(loginFile)) {
            // TODO ADD NEW IMPL
        }
        final SplashWindow.Callback callback = new SplashWindow.Callback() {

            @Override
            public void done(String dbName) {
                user.setDatabaseName(dbName);
                mainWindow();
            }

            @Override
            public void failed() {
                loginWindow(true);
            }
        };
        final SplashWindow splashWindow = new SplashWindow(
            this,
            callback,
            user
        );
        addMainWindow(splashWindow);
    }

    private void loginWindow(boolean fillForm) {
        final LoginWindowController lwc = new LoginWindowController(this, user);
        final LoginWindow lw = new LoginWindow(this, lwc);
        if (fillForm) {
            lw.setForm(
                user.getWebsiteURL().toString(),
                user.getDatabase(),
                user.getUser(),
                user.getPassword()
            );
        }
        addMainWindow(lw);
    }

    private void loginWindow() {
        loginWindow(false);
    }

    private void mainWindow() {
        final String db = user.getDatabaseName();
        final String dbName = (db.length() < 30) ? db : db.substring(0, 30);
        final MainWindowController mwc = new MainWindowController(
            user,
            getStringResources()
        );
        final MainWindow mw = new MainWindow(this, mwc, dbName);
        addMainWindow(mw);
    }
}
