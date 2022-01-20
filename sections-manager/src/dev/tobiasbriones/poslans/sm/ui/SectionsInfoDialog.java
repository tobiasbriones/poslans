/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import dev.tobiasbriones.poslans.sm.current.Section;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class SectionsInfoDialog extends JDialog implements Strings {
    private static final long serialVersionUID = -8514772709282794043L;

    public SectionsInfoDialog(MainWindow mw, Iterator<Section> sections) {
        super(mw);
        final JPanel panel = new JPanel();
        final JLabel titleLabel = new JLabel(Strings.SECTIONS_INFO_TITLE);
        final JLabel totalLabel = new JLabel();
        final DefaultListModel<String> listModel = new DefaultListModel<>();
        final JList<String> list = new JList<>(listModel);
        final HashMap<String, Integer> sectionsCount = new HashMap<>();
        final Font font = new Font("Roboto Light", Font.TRUETYPE_FONT, 12);
        int total = 0;
        Section currentSection = null;
        String currentClassCode = null;
        Integer currentSectionCount = null;
        titleLabel.setFont(font);
        titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        totalLabel.setFont(font);
        totalLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        list.setFixedCellHeight(25);
        list.setFixedCellWidth(160);
        list.setBorder(new EmptyBorder(0, 10, 10, 10));
        while (sections.hasNext()) {
            total++;
            currentSection = sections.next();
            currentClassCode = currentSection.getSectionClass().getCode();
            currentSectionCount = sectionsCount.get(currentClassCode);
            if (currentSectionCount == null) {
                sectionsCount.put(currentClassCode, 1);
            }
            else {
                sectionsCount.put(currentClassCode, currentSectionCount + 1);
            }
        }
        totalLabel.setText(Strings.TOTAL_SECTIONS_OPENED + total);
        final Iterator<Map.Entry<String, Integer>> it = sectionsCount.entrySet()
                                                                     .iterator();
        while (it.hasNext()) {
            final Map.Entry<String, Integer> pair = it.next();
            listModel.addElement("<html><body><strong>(" + pair.getValue() +
                                 ")</strong> " + pair.getKey() + "</body"
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
                            .getImage("icons/ic_sections_info.png"));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
