/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui;

import dev.tobiasbriones.poslans.mu.AppConfig;
import dev.tobiasbriones.poslans.mu.Strings;
import engineer.mathsoftware.jdesk.resources.Resources;
import engineer.mathsoftware.jdesk.ui.dialog.AppDialog;
import engineer.mathsoftware.jdesk.ui.dialog.Dialog;
import engineer.mathsoftware.jdesk.ui.view.Panel;
import engineer.mathsoftware.jdesk.ui.view.TextLabel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static dev.tobiasbriones.poslans.mu.Strings.CONFIG_LANGUAGE;
import static dev.tobiasbriones.poslans.mu.Strings.SETTINGS;

final class SettingsDialog extends Dialog {
    private static final long serialVersionUID = -5464955234605153753L;

    public SettingsDialog(MainWindow mw) {
        super(mw);
        final Panel panel = new Panel(mw);
        final TextLabel languageLabel = new TextLabel(mw, CONFIG_LANGUAGE);
        final JComboBox<String> languageBox =
            new JComboBox<>(AppConfig.LANGUAGES_CODES);
        final String selectedItem = AppConfig.getLanguage();
        languageBox.setSelectedItem(selectedItem);
        languageBox.setFocusable(false);
        languageBox.addActionListener(e -> {
            if (languageBox.getSelectedIndex() != -1) {
                try {
                    AppConfig.setLanguage((String) languageBox.getSelectedItem());
                }
                catch (IOException ex) {
                    AppDialog.showMessage(
                        mw,
                        ex.getMessage(),
                        AppDialog.Type.ERROR
                    );
                }
            }
        });
        panel.setLayout(new GridLayout(1, 1, 10, 5));
        panel.setPadding(10, 10, 10, 10);
        panel.add(languageLabel);
        panel.add(languageBox);
        getContentPane().add(panel);
        pack();
        setTitle(mw.getStringResources().get(SETTINGS));
        setIconImage(getToolkit().getImage(Resources.ICON_DIRECTORY + "/ic_settings.png"));
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
