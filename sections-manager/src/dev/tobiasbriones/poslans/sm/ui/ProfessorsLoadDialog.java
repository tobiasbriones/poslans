/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import dev.tobiasbriones.poslans.sm.current.ProfessorAcademicLoad;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

final class ProfessorsLoadDialog extends JDialog implements Strings {
    private static final long serialVersionUID = -7761058948968530584L;

    ProfessorsLoadDialog(
        MainWindow mw,
        Enumeration<ProfessorAcademicLoad> loads
    ) {
        super(mw, Strings.PROFESSORS_LOAD);
        final JList<ProfessorAcademicLoad> list = new JList<>();
        final JPanel panel = new JPanel();
        final JPanel titlePanel = new JPanel();
        final ListPALModel model = new ListPALModel();
        final JScrollPane scroll = new JScrollPane(list);
        ProfessorAcademicLoad load = null;
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBorder(new EmptyBorder(5, 15, 5, 30));
        titlePanel.setBackground(Color.decode("#F0F0F0"));
        titlePanel.add(new JLabel(Strings.TITLE_PAL), BorderLayout.WEST);
        titlePanel.add(new JLabel(Strings.TITLE_LOAD), BorderLayout.EAST);
        while (loads.hasMoreElements()) {
            load = loads.nextElement();
            model.addItem(load);
        }
        list.setModel(model);
        list.setCellRenderer(new Row());
        list.setBackground(Color.decode("#F0F0F0"));
        scroll.setBorder(new EmptyBorder(5, 5, 5, 5));
        scroll.setBackground(Color.decode("#F0F0F0"));
        panel.setPreferredSize(new Dimension(320, 360));
        panel.setLayout(new BorderLayout());
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        setResizable(false);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setIconImage(Toolkit.getDefaultToolkit()
                            .getImage("icons/ic_professors.png"));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static final class ListPALModel implements ListModel<ProfessorAcademicLoad> {
        private final List<ProfessorAcademicLoad> items;

        ListPALModel() {
            this.items = new ArrayList<>();
        }

        @Override
        public int getSize() {
            return items.size();
        }

        @Override
        public ProfessorAcademicLoad getElementAt(int index) {
            return items.get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {}

        @Override
        public void removeListDataListener(ListDataListener l) {}

        void addItem(ProfessorAcademicLoad item) {
            items.add(item);
        }
    }

    private static final class Row extends JPanel implements ListCellRenderer<ProfessorAcademicLoad> {

        private static final long serialVersionUID = -4529068666204573522L;
        private final JLabel labelLeft;
        private final JLabel labelRight;

        Row() {
            this.labelLeft = new JLabel();
            this.labelRight = new JLabel();
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(5, 15, 15, 15));
            setBackground(Color.decode("#F0F0F0"));
            add(labelLeft, BorderLayout.WEST);
            add(labelRight, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
            JList<? extends ProfessorAcademicLoad> list,
            ProfessorAcademicLoad item,
            int index,
            boolean isSelected,
            boolean cellHasFocus
        ) {
            labelLeft.setText(item.getName());
            labelRight.setText(String.valueOf(item.getLoad()));
            return this;
        }
    }
}
