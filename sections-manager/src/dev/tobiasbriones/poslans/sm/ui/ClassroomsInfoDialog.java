/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import dev.tobiasbriones.poslans.sm.Main;
import dev.tobiasbriones.poslans.sm.career.Classroom;
import dev.tobiasbriones.poslans.sm.current.Days;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public final class ClassroomsInfoDialog extends JDialog implements Strings {
    private static final long serialVersionUID = -8514772709282794043L;
    private static final Dimension SCROLL_SIZE = new Dimension(1200, 600);

    public ClassroomsInfoDialog(MainWindow mw, MainWindow.Callback callback) {
        super(mw, "Classroom availability");
        final Font font = new Font("Roboto-Light", Font.TRUETYPE_FONT, 12);
        final JPanel panel = new JPanel();
        final JPanel col = new JPanel(new GridBagLayout());
        final JPanel grid = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        final List<Classroom> classrooms = callback.getClassrooms();
        final JScrollPane scroll = new JScrollPane(grid);
        col.setBackground(Color.WHITE);
        grid.setBackground(Color.WHITE);
        grid.setBorder(new EmptyBorder(6, 0, 0, 0));
        scroll.setPreferredSize(SCROLL_SIZE);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.getHorizontalScrollBar().setUnitIncrement(12);
        scroll.setBackground(Color.WHITE);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 10, 12, 10);
        col.add(label(font, "CLASSROOM"), gbc);
        gbc.gridy++;
        gbc.insets.top = 6;
        col.add(label(font, "TIME"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(8, 10, 8, 10);
        col.add(label(font, "07:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "08:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "09:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "10:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "11:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "12:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "13:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "14:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "15:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "16:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "17:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "18:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "19:00"), gbc);
        gbc.gridy++;
        col.add(label(font, "20:00"), gbc);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        for (Classroom classroom : classrooms) {
            final JLabel label = label(font, classroom.toString());
            gbc.gridy = 0;
            gbc.gridwidth = 6;
            label.setPreferredSize(new Dimension(300, 20));
            label.setFont(font.deriveFont(Font.BOLD, 14));
            grid.add(label, gbc);
            gbc.gridy++;
            gbc.gridwidth = 1;
            for (String day : Days.MINI_DAYS) {
                grid.add(label(font, day), gbc);
                gbc.gridx++;
            }
        }
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        for (Classroom classroom : classrooms) {
            for (int hour = 7; hour < 21; hour++) {
                gbc.gridy = hour - 5;
                for (int day = 0; day < 6; day++) {
                    final boolean isTaken = callback.isClassroomAvailable(
                        classroom,
                        day,
                        hour
                    );
                    if (isTaken) {
                        grid.add(new JLabel(new ImageIcon(
                            Main.getIconPath("ic_pan_tool.png"))), gbc
                        );
                    }
                    else {
                        grid.add(new JLabel(new ImageIcon(
                            Main.getIconPath("ic_check_circle.png"))), gbc
                        );
                    }
                    gbc.gridx++;
                }
                gbc.gridx -= 6;
            }
            gbc.gridx += 6;
        }
        // Panel
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        //panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        //panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(col);
        panel.add(scroll);
        //panel.add(totalLabel, BorderLayout.SOUTH);
        getContentPane().add(panel);
        setResizable(false);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setIconImage(Toolkit.getDefaultToolkit()
                            .getImage(Main.getIconPath("ic_classroom.png")));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static JLabel label(Font font, String text) {
        final JLabel label = new JLabel(text);
        label.setFont(font);
        return label;
    }
}
