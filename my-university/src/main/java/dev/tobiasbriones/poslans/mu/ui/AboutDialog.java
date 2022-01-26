/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui;

import engineer.mathsoftware.jdesk.AppInstance;
import engineer.mathsoftware.jdesk.resources.Resources;
import engineer.mathsoftware.jdesk.ui.dialog.Dialog;
import engineer.mathsoftware.jdesk.ui.style.Style;
import engineer.mathsoftware.jdesk.ui.view.Panel;
import engineer.mathsoftware.jdesk.ui.view.TextLabel;

import java.awt.*;

import static dev.tobiasbriones.poslans.mu.Strings.ABOUT;
import static dev.tobiasbriones.poslans.mu.Strings.ABOUT_MSG;

public final class AboutDialog extends Dialog {
    private static final long serialVersionUID = 4769445321127758362L;

    public AboutDialog(MainWindow mw, AppInstance app) {
        super(mw);
        final Panel panel = new Panel(app);
        final TextLabel iconLabel = new TextLabel(
            app,
            Resources.loadIcon("ic_launcher_medium")
        );
        final TextLabel label = new TextLabel(app);
        final String labelText = Style.wrapDialogTextInHTML(
            app.getAppStyle(),
            app.getStringResources()
               .get(ABOUT_MSG)
        );

        label.setText(labelText);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setPadding(10, 10, 10, 10);
        panel.add(iconLabel);
        panel.add(label);
        getContentPane().add(panel);
        pack();
        setTitle(app.getStringResources().get(ABOUT));
        setIconImage(getToolkit().getImage(Resources.ICON_DIRECTORY +
                                           "/ic_about_24.png"));
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
