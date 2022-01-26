/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui;

import dev.tobiasbriones.poslans.mu.Strings;
import dev.tobiasbriones.poslans.mu.ui.controller.MainWindowController;
import engineer.mathsoftware.jdesk.App;
import engineer.mathsoftware.jdesk.AppInstance;
import engineer.mathsoftware.jdesk.Window;
import engineer.mathsoftware.jdesk.resources.Resources;
import engineer.mathsoftware.jdesk.ui.dialog.AppDialog;
import engineer.mathsoftware.jdesk.ui.view.*;
import engineer.mathsoftware.jdesk.ui.view.Button;
import engineer.mathsoftware.jdesk.ui.view.Panel;
import org.json.JSONException;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import static dev.tobiasbriones.poslans.mu.Strings.*;

public final class EmailLoginWindow extends Window implements ActionListener {

    private static final long serialVersionUID = 2783377326027854740L;
    public interface Callback {
        void saveEmailInfo(
            String host,
            int port,
            String email,
            String password
        ) throws IOException;
    }

    private final Callback callback;
    private final InputText hostIT;
    private final InputText portIT;
    private final InputText emailIT;
    private final PasswordInputText passwordIT;
    private final Button saveButton;

    public EmailLoginWindow(App app, Callback callback) {
        super((AppInstance) app);
        this.callback = callback;
        this.hostIT = new InputText(this, 20);
        this.portIT = new InputText(this, 20);
        this.emailIT = new InputText(this, 20);
        this.passwordIT = new PasswordInputText(this, 20);
        this.saveButton = new Button(this, SAVE);
        setResizable(false);
        setTitle(getStringResources().get(SET_EMAIL_ACCOUNT));
        setIconImage(getToolkit().getImage(Resources.ICON_DIRECTORY +
                                           "/ic_email.png"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Component source = (Component) e.getSource();
        if (source == saveButton) {
            final String host = hostIT.getText();
            final String portStr = portIT.getText();
            final String email = emailIT.getText();
            final String password = new String(passwordIT.getPassword());
            if (host.isEmpty() || portStr.isEmpty() || email.isEmpty() || password.isEmpty()) {
                AppDialog.showMessage(this, SET_ALL_FIELDS);
                return;
            }
            try {
                final int port = Integer.valueOf(portStr);
                callback.saveEmailInfo(host, port, email, password);
                AppDialog.showMessage(
                    this,
                    EMAIL_INFO_SAVED,
                    AppDialog.Type.SUCCESS
                );
                dispose();
            }
            catch (NumberFormatException ex) {
                AppDialog.showMessage(
                    this,
                    PORT_IS_A_NUMBER,
                    AppDialog.Type.WARNING
                );
            }
            catch (IOException ex) {
                AppDialog.showMessage(
                    this,
                    ex.getMessage(),
                    AppDialog.Type.ERROR
                );
            }
        }
        else if (source == hostIT) {
            portIT.requestFocus();
        }
        else if (source == portIT) {
            emailIT.requestFocus();
        }
        else if (source == emailIT) {
            passwordIT.requestFocus();
        }
        else if (source == passwordIT) {
            saveButton.doClick();
        }
    }

    @Override
    protected void createWindow(Panel panel) {
        final Panel topPanel = new Panel(this);
        final TextLabel l0 = new TextLabel(this, HOST);
        final TextLabel l1 = new TextLabel(this, PORT);
        final TextLabel l2 = new TextLabel(this, EMAIL);
        final TextLabel l3 = new TextLabel(this, PASSWORD);
        final GridBagConstraints gbc = new GridBagConstraints();

        hostIT.addActionListener(this);
        portIT.addActionListener(this);
        emailIT.addActionListener(this);
        passwordIT.addActionListener(this);
        saveButton.addActionListener(this);
        topPanel.setLayout(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Enter email information"));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.bottom = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(l0, gbc);
        gbc.insets.bottom = 5;
        gbc.gridy++;
        topPanel.add(hostIT, gbc);
        gbc.insets.bottom = 2;
        gbc.gridy++;
        topPanel.add(l1, gbc);
        gbc.insets.bottom = 5;
        gbc.gridy++;
        topPanel.add(portIT, gbc);
        gbc.insets.bottom = 2;
        gbc.gridy++;
        topPanel.add(l2, gbc);
        gbc.insets.bottom = 5;
        gbc.gridy++;
        topPanel.add(emailIT, gbc);
        gbc.insets.bottom = 2;
        gbc.gridy++;
        topPanel.add(l3, gbc);
        gbc.insets.bottom = 5;
        gbc.gridy++;
        topPanel.add(passwordIT, gbc);
        gbc.insets.bottom = 2;
        gbc.gridy++;
        topPanel.add(saveButton, gbc);
        panel.setPadding(0, 10, 10, 10);
        panel.add(topPanel);
    }

    @Override
    protected void windowCreated() {
        try {
            final JSONObject emailJSON = MainWindowController.loadEmailLogin();
            hostIT.setText(emailJSON.getString("host"));
            portIT.setText(String.valueOf(emailJSON.getInt("port")));
            emailIT.setText(emailJSON.getString("email"));
        }
        catch (JSONException | IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                AppDialog.showMessage(
                    this,
                    e.getMessage(),
                    AppDialog.Type.ERROR
                );
            }
        }
        setVisible(true);
    }
}
