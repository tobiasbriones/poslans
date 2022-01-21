/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import dev.tobiasbriones.poslans.sm.AppConfig;
import dev.tobiasbriones.poslans.sm.Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

final class ConfigWindow extends JFrame implements Strings {
    private static final long serialVersionUID = 2173081877663704615L;

    ConfigWindow(MainWindow mw) {
        super();
        final JPanel panel = new JPanel();
        final JLabel l1 = new JLabel("Current term data saving file");
        final JLabel l2 = new JLabel("Career data");
        final JButton b1 = new JButton("SET");
        final JButton b11 = new JButton("GO TO FILE");
        final JButton b2 = new JButton("SET");
        final ActionListener l = e -> {
            if (e.getSource() == b1) {
                JOptionPane.showMessageDialog(
                    mw,
                    "Select a folder to save your data through the save "
                    + "option"
                );
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new FileNameExtensionFilter(
                    "Excel files",
                    "xlsx"
                ));
                if (fileChooser.showSaveDialog(mw) == JFileChooser.APPROVE_OPTION) {
                    File selection = fileChooser.getSelectedFile();
                    if (!selection.toString().endsWith(".xlsx")) {
                        selection = new File(selection +
                                             ".xlsx");
                    }
                    try {
                        AppConfig.saveTermFile(selection);
                    }
                    catch (IOException ex) {
                        JOptionPane.showMessageDialog(
                            ConfigWindow.this,
                            ex,
                            "Fail",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
            else if (e.getSource() == b11) {
                try {
                    final File file = AppConfig.getTermFile();
                    Desktop.getDesktop().open(file.getParentFile());
                }
                catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                        ConfigWindow.this,
                        ex,
                        "Fail",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
            else {
                new CareerDataDialog(
                    mw,
                    mw.getCareerData(),
                    mw.getCareerDataDialogCallback()
                );
            }
        };
        b1.addActionListener(l);
        b11.addActionListener(l);
        b2.addActionListener(l);
        panel.setLayout(new GridLayout(3, 2));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(l1);
        panel.add(b1);
        panel.add(new JPanel());
        panel.add(b11);
        panel.add(l2);
        panel.add(b2);
        getContentPane().add(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setIconImage(Toolkit.getDefaultToolkit()
                            .getImage(Main.getIconPath("ic_config.png")));
        setVisible(true);
    }
}
