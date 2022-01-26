/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui;

import dev.tobiasbriones.poslans.mu.Strings;
import engineer.mathsoftware.jdesk.AppInstance;
import engineer.mathsoftware.jdesk.Window;
import engineer.mathsoftware.jdesk.resources.StringResources;
import engineer.mathsoftware.jdesk.ui.dialog.AppDialog;
import engineer.mathsoftware.jdesk.ui.dialog.TaskDialog;
import engineer.mathsoftware.jdesk.ui.view.Button;
import engineer.mathsoftware.jdesk.ui.view.Panel;
import engineer.mathsoftware.jdesk.ui.view.*;
import engineer.mathsoftware.jdesk.ui.view.loading.BarLoadingView;
import engineer.mathsoftware.jdesk.work.WorkRunnable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import static dev.tobiasbriones.poslans.mu.Strings.*;

public final class LoginWindow extends Window implements AppInstance,
                                                         ActionListener {
    private static final long serialVersionUID = 7189406897677005096L;

    public interface Callback {
        void connect(
            URL websiteURL,
            String database,
            String user,
            String password
        ) throws Exception;

        void login();
    }

    private final Callback callback;
    private final InputText websiteURLIT;
    private final InputText databaseURLIT;
    private final InputText databaseUserIT;
    private final PasswordInputText databasePasswordIT;
    private final Button connectDBButton;

    public LoginWindow(AppInstance app, Callback callback) {
        super(app);
        this.callback = callback;
        this.websiteURLIT = new InputText(this, 20);
        this.databaseURLIT = new InputText(this, 20);
        this.databaseUserIT = new InputText(this, 20);
        this.databasePasswordIT = new PasswordInputText(this, 20);
        this.connectDBButton = new Button(this, CONNECT);
        setResizable(false);
    }

    @Override
    public String getAppConfigFile() {
        return "config.properties";
    }

    public void setForm(String... params) {
        websiteURLIT.setText(params[0]);
        databaseURLIT.setText(params[1]);
        databaseUserIT.setText(params[2]);
        databasePasswordIT.setText(params[3]);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Component source = (Component) e.getSource();
        if (source == connectDBButton) {
            final String website = websiteURLIT.getText();
            final String database = databaseURLIT.getText();
            final String user = databaseUserIT.getText();
            final String password =
                new String(databasePasswordIT.getPassword());
            final URL websiteURL;
            if (website.isEmpty() || database.isEmpty() || user.isEmpty() || password.isEmpty()) {
                AppDialog.showMessage(this, SET_ALL_FIELDS);
                return;
            }
            try {
                websiteURL = new URL(website);
            }
            catch (MalformedURLException ex) {
                AppDialog.showMessage(
                    this,
                    ex.getMessage(),
                    AppDialog.Type.FAIL
                );
                return;
            }
            connect(websiteURL, database, user, password);
        }
        else if (source == websiteURLIT) {
            databaseURLIT.requestFocus();
        }
        else if (source == databaseURLIT) {
            databaseUserIT.requestFocus();
        }
        else if (source == databaseUserIT) {
            databasePasswordIT.requestFocus();
        }
        else if (source == databasePasswordIT) {
            connectDBButton.doClick();
        }
    }

    @Override
    protected void createWindow(Panel panel) {
        final StringResources sr = getStringResources();
        final Panel topPanel = new Panel(this);
        final TextLabel l0 = new TextLabel(this, WEBSITE_URL);
        final TextLabel l1 = new TextLabel(this, DATABASE);
        final TextLabel l2 = new TextLabel(this, DB_USER);
        final TextLabel l3 = new TextLabel(this, DB_PASSWORD);
        final TextLabel poslans = PoslansLabel.newInstance(this);
        final GridBagConstraints gbc = new GridBagConstraints();

        websiteURLIT.addActionListener(this);
        databaseURLIT.addActionListener(this);
        databaseUserIT.addActionListener(this);
        databasePasswordIT.addActionListener(this);
        connectDBButton.addActionListener(this);
        poslans.setBorder(new EmptyBorder(0, 0, 0, 4));
        topPanel.setLayout(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder(sr.get(DB_CONNECTION)));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.bottom = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(l0, gbc);
        gbc.insets.bottom = 5;
        gbc.gridy++;
        topPanel.add(websiteURLIT, gbc);
        gbc.insets.bottom = 2;
        gbc.gridy++;
        topPanel.add(l1, gbc);
        gbc.insets.bottom = 5;
        gbc.gridy++;
        topPanel.add(databaseURLIT, gbc);
        gbc.insets.bottom = 2;
        gbc.gridy++;
        topPanel.add(l2, gbc);
        gbc.insets.bottom = 5;
        gbc.gridy++;
        topPanel.add(databaseUserIT, gbc);
        gbc.insets.bottom = 2;
        gbc.gridy++;
        topPanel.add(l3, gbc);
        gbc.insets.bottom = 5;
        gbc.gridy++;
        topPanel.add(databasePasswordIT, gbc);
        gbc.insets.bottom = 2;
        gbc.gridy++;
        topPanel.add(connectDBButton, gbc);
        panel.setLayout(new BorderLayout());
        panel.setPadding(10, 15, 10, 15);
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(poslans, BorderLayout.CENTER);

        setLocalHost();
    }

    @Override
    protected void windowCreated() {
        setVisible(true);
    }

    private void connect(
        URL websiteURL,
        String database,
        String user,
        String password
    ) {
        final TaskDialog<Object> td = new TaskDialog<>(
            this,
            CONNECTING,
            CONNECTING_ELLIPSIS,
            false
        );
        final WorkRunnable<Object> runnable = () -> {
            callback.connect(websiteURL, database, user, password);
            return null;
        };
        final TaskDialog.TaskDialogCallback<Object> taskCallback =
            new TaskDialog.TaskDialogCallback<Object>(this) {
                @Override
                public void workFinished(Object result) {
                    callback.login();
                }

                @Override
                public boolean cancelRequest() {
                    return false;
                }
            };
        td.getBarLoadingView()
          .setSpeed(BarLoadingView.BAR_WIDTH_CHANGE_PER_SECOND_FAST);
        td.setCallback(taskCallback);
        td.execute(runnable);
    }

    private void setLocalHost() {
        websiteURLIT.setText("http://localhost");
        databaseURLIT.setText("university");
        databaseUserIT.setText("admin");
        databasePasswordIT.setText("password");
    }
}
