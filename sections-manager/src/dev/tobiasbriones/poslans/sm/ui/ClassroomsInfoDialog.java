/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClassroomsInfoDialog extends JDialog implements Strings {
    private static final long serialVersionUID = -8514772709282794043L;

    public ClassroomsInfoDialog(MainWindow mw, HashMap<String, String> info) {
        super(mw);
        final JPanel panel = new JPanel();
        final JLabel titleLabel = new JLabel(Strings.SECTIONS_INFO_TITLE);
        final JLabel totalLabel = new JLabel();
        final DefaultListModel<String> listModel = new DefaultListModel<>();
        final JList<String> list = new JList<>(listModel);
        final Font font = new Font("Roboto Light", Font.TRUETYPE_FONT, 12);
        titleLabel.setFont(font);
        titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        totalLabel.setFont(font);
        totalLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        list.setFixedCellHeight(50);
        list.setFixedCellWidth(480);
        list.setBorder(new EmptyBorder(0, 10, 10, 10));
        final Iterator<Map.Entry<String, String>> it = info.entrySet()
                                                           .iterator();
        while (it.hasNext()) {
            final Map.Entry<String, String> pair = it.next();
            if (pair.getValue().isEmpty()) {
                continue;
            }
            listModel.addElement("<html><body>" + pair.getKey() + "<strong> "
                                 + "(" + pair.getValue() + ")</strong></body"
                                 + "></html>");
            it.remove(); // avoids a ConcurrentModificationException
        }
        // Panel
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.add(totalLabel, BorderLayout.SOUTH);
        getContentPane().add(panel);
        setResizable(false);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setIconImage(Toolkit.getDefaultToolkit()
                            .getImage("icons/ic_classroom.png"));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
