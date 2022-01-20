/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import dev.tobiasbriones.poslans.sm.career.CareerDataHolder;
import dev.tobiasbriones.poslans.sm.career.Class;
import dev.tobiasbriones.poslans.sm.career.Classroom;
import dev.tobiasbriones.poslans.sm.career.Professor;
import dev.tobiasbriones.poslans.sm.current.Days;
import dev.tobiasbriones.poslans.sm.current.Section;
import dev.tobiasbriones.poslans.sm.current.Time;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

public final class SectionDialog extends JDialog implements R,
                                                            Strings,
                                                            Days,
                                                            ActionListener {
    private static final long serialVersionUID = 8110170505164198763L;

    public interface Callback {
        String openSection(
            int classValue,
            int professorValue,
            int classroomValue,
            Time time,
            int[] days
        );

        String editSection(
            int classValue,
            int professorValue,
            int classroomValue,
            Time time,
            int[] days
        );
    }

    private final Callback callback;
    private final boolean isEditing;
    private final JComboBox<Class> classesComboBox;
    private final JComboBox<Classroom> classroomsComboBox;
    private final JComboBox<Professor> professorsComboBox;
    private final JCheckBox mondayCheckBox;
    private final JCheckBox tuesdayCheckBox;
    private final JCheckBox wednesdayCheckBox;
    private final JCheckBox thursdayCheckBox;
    private final JCheckBox fridayCheckBox;
    private final JCheckBox saturdayCheckBox;
    private final JTextField timeTF;
    private final List<Classroom> classrooms;

    public SectionDialog(MainWindow mw, CareerDataHolder careerData) {
        this(mw, careerData, null);
    }

    public SectionDialog(
        MainWindow mw,
        CareerDataHolder careerData,
        Section edit
    ) {
        super(mw);
        this.callback = mw;
        this.isEditing = edit != null;
        this.classesComboBox = new JComboBox<>();
        this.classroomsComboBox = new JComboBox<>();
        this.professorsComboBox = new JComboBox<>();
        this.mondayCheckBox = new JCheckBox(Days.DAYS[MONDAY]);
        this.tuesdayCheckBox = new JCheckBox(Days.DAYS[TUESDAY]);
        this.wednesdayCheckBox = new JCheckBox(Days.DAYS[WEDNESDAY]);
        this.thursdayCheckBox = new JCheckBox(Days.DAYS[THURSDAY]);
        this.fridayCheckBox = new JCheckBox(Days.DAYS[FRIDAY]);
        this.saturdayCheckBox = new JCheckBox(Days.DAYS[SATURDAY]);
        this.timeTF = new JTextField();
        this.classrooms = careerData.getClassrooms();
        final JPanel panel = new JPanel();
        final JPanel mainPanel = new JPanel();
        final JPanel daysPanel = new JPanel();
        final JPanel bottomPanel = new JPanel();
        final JLabel classLabel = new JLabel(Strings.CLASS);
        final JLabel professorLabel = new JLabel(Strings.PROFESSOR);
        final JLabel classroomLabel = new JLabel(Strings.CLASSROOM);
        final JLabel timeLabel = new JLabel(Strings.TIME);
        final JLabel daysLabel = new JLabel(Strings.DAYS);
        final ListComboBoxModel<Class> classesCBModel = new ListComboBoxModel<>(
            careerData.getClasses());
        final ListComboBoxModel<Classroom> classroomsCBModel =
            new ListComboBoxModel<>(
                careerData.getClassrooms());
        final ListComboBoxModel<Professor> professorsCBModel =
            new ListComboBoxModel<>(
                careerData.getProfessors());
        final JButton selectClassroomButton = new JButton(Strings.SELECT.toUpperCase());
        final JButton cancelButton = new JButton(Strings.CANCEL.toUpperCase());
        final JButton saveButton = new JButton();
        final Border border = new EmptyBorder(5, 10, 5, 10);
        final Border mainPanelBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.decode("#212121")),
            Strings.SECTION
        );
        final GridBagConstraints gbc = new GridBagConstraints();
        classesComboBox.setModel(classesCBModel);
        classesComboBox.addItemListener(e -> daysLabel.setText(Strings.DAYS + " (" + ((Class) e.getItem()).getDaysPerWeek() + ")"));
        classroomsComboBox.setModel(classroomsCBModel);
        professorsComboBox.setModel(professorsCBModel);
        if (edit == null) {
            setTitle(Strings.OPEN_SECTION);
            saveButton.setText(Strings.OPEN.toUpperCase());
            // Default view configuration
            mondayCheckBox.setSelected(true);
            tuesdayCheckBox.setSelected(true);
            wednesdayCheckBox.setSelected(true);
            thursdayCheckBox.setSelected(true);
            fridayCheckBox.setSelected(true);
        }
        else {
            setTitle(Strings.EDIT_SECTION);
            saveButton.setText(Strings.SAVE.toUpperCase());
            restoreViewState(careerData, edit, timeTF);
        }
        selectClassroomButton.setName(R.SECTION_DIALOG_SELECT_CLASSROOM);
        selectClassroomButton.addActionListener(this);
        daysPanel.setLayout(new GridLayout(2, 3));
        daysPanel.add(mondayCheckBox);
        daysPanel.add(tuesdayCheckBox);
        daysPanel.add(wednesdayCheckBox);
        daysPanel.add(thursdayCheckBox);
        daysPanel.add(fridayCheckBox);
        daysPanel.add(saturdayCheckBox);
        // Main panel
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(mainPanelBorder);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(classLabel, gbc);
        gbc.gridy++;
        mainPanel.add(classesComboBox, gbc);
        gbc.gridy++;
        mainPanel.add(professorLabel, gbc);
        gbc.gridy++;
        mainPanel.add(professorsComboBox, gbc);
        gbc.gridy++;
        mainPanel.add(classroomLabel, gbc);
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(selectClassroomButton, gbc);
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(classroomsComboBox, gbc);
        gbc.gridy++;
        mainPanel.add(timeLabel, gbc);
        gbc.gridy++;
        mainPanel.add(timeTF, gbc);
        gbc.gridy++;
        mainPanel.add(daysLabel, gbc);
        gbc.gridy++;
        mainPanel.add(daysPanel, gbc);
        // Bottom panel
        cancelButton.setName(R.SECTION_DIALOG_CANCEL);
        cancelButton.addActionListener(this);
        saveButton.setName(R.SECTION_DIALOG_SAVE);
        saveButton.addActionListener(this);
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(cancelButton);
        bottomPanel.add(saveButton);
        // Panel
        panel.setLayout(new BorderLayout());
        panel.setBorder(border);
        panel.add(mainPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().add(panel);
        setResizable(false);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setIconImage(Toolkit.getDefaultToolkit().getImage("icons/ic_add.png"));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private int getDaysCount() {
        int count = 0;
        if (mondayCheckBox.isSelected()) {
            count++;
        }
        if (tuesdayCheckBox.isSelected()) {
            count++;
        }
        if (wednesdayCheckBox.isSelected()) {
            count++;
        }
        if (thursdayCheckBox.isSelected()) {
            count++;
        }
        if (fridayCheckBox.isSelected()) {
            count++;
        }
        if (saturdayCheckBox.isSelected()) {
            count++;
        }
        return count;
    }

    private void setClassroom(int classroomIndex) {
        classroomsComboBox.setSelectedItem(classrooms.get(classroomIndex));
        classroomsComboBox.requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String name = ((Component) e.getSource()).getName();
        String overlapCheck = null;
        if (name.equals(R.SECTION_DIALOG_SELECT_CLASSROOM)) {
            new SelectClassroomDialog(this);
            return;
        }
        if (name.equals(R.SECTION_DIALOG_SAVE)) {
            final int classValue = classesComboBox.getSelectedIndex();
            final int professorValue = professorsComboBox.getSelectedIndex();
            final int classroomValue = classroomsComboBox.getSelectedIndex();
            final Time time = Time.fromString(timeTF.getText());
            final int[] days = new int[getDaysCount()];
            int i = 0;
            // Data validation
            if (classValue == -1 || professorValue == -1 || classroomValue == -1 || timeTF.getText()
                                                                                          .isEmpty() || days.length == 0) {
                JOptionPane.showMessageDialog(this, Strings.FILL_ALL_FIELDS);
                return;
            }
            if (time == null) {
                JOptionPane.showMessageDialog(this, Strings.INVALID_TIME);
                return;
            }
            if (mondayCheckBox.isSelected()) {
                days[i] = MONDAY;
                i++;
            }
            if (tuesdayCheckBox.isSelected()) {
                days[i] = TUESDAY;
                i++;
            }
            if (wednesdayCheckBox.isSelected()) {
                days[i] = WEDNESDAY;
                i++;
            }
            if (thursdayCheckBox.isSelected()) {
                days[i] = THURSDAY;
                i++;
            }
            if (fridayCheckBox.isSelected()) {
                days[i] = FRIDAY;
                i++;
            }
            if (saturdayCheckBox.isSelected()) {
                days[i] = SATURDAY;
            }
            if (isEditing) {
                overlapCheck = callback.editSection(
                    classValue,
                    professorValue,
                    classroomValue,
                    time,
                    days
                );
            }
            else {
                overlapCheck = callback.openSection(
                    classValue,
                    professorValue,
                    classroomValue,
                    time,
                    days
                );
            }
        }
        if (overlapCheck != null) {
            JOptionPane.showMessageDialog(
                this,
                overlapCheck,
                Strings.OVERLAP,
                JOptionPane.WARNING_MESSAGE
            );
        }
        else {
            dispose();
        }
    }

    private void restoreViewState(
        CareerDataHolder careerData,
        Section edit,
        JTextField timeTF
    ) {
        int i = 0;
        for (Class Class : careerData.getClasses()) {
            if (Class.getCode().equals(edit.getSectionClass().getCode())) {
                classesComboBox.setSelectedIndex(i);
                break;
            }
            i++;
        }
        i = 0;
        for (Classroom classroom : careerData.getClassrooms()) {
            if (classroom.getBuilding()
                         .equals(edit.getClassroom().getBuilding())
                && classroom.getClassroomNumber() == edit.getClassroom()
                                                         .getClassroomNumber()) {
                classroomsComboBox.setSelectedIndex(i);
                break;
            }
            i++;
        }
        i = 0;
        for (Professor professor : careerData.getProfessors()) {
            if (professor.getName().equals(edit.getProfessor().getName())) {
                professorsComboBox.setSelectedIndex(i);
                break;
            }
            i++;
        }// Trim is necessary
        timeTF.setText(edit.getTime().toString().trim());
        for (int j : edit.getDays()) {
            switch (j) {
                case MONDAY:
                    mondayCheckBox.setSelected(true);
                    break;
                case TUESDAY:
                    tuesdayCheckBox.setSelected(true);
                    break;
                case WEDNESDAY:
                    wednesdayCheckBox.setSelected(true);
                    break;
                case THURSDAY:
                    thursdayCheckBox.setSelected(true);
                    break;
                case FRIDAY:
                    fridayCheckBox.setSelected(true);
                    break;
                case SATURDAY:
                    saturdayCheckBox.setSelected(true);
                    break;
            }
        }
    }

    private static final class SelectClassroomDialog extends JDialog {
        private static final long serialVersionUID = -703601238222986271L;
        private static final Dimension DIALOG_SIZE = new Dimension(480, 480);

        public SelectClassroomDialog(SectionDialog sectionDialog) {
            super(sectionDialog, Strings.SELECT_CLASSROOM);
            final JPanel panel = new JPanel();
            final DefaultListModel<String> listModel = new DefaultListModel<>();
            final JList<String> list = new JList<>(listModel);
            final JScrollPane scroll = new JScrollPane(list);
            list.setFixedCellHeight(24);
            scroll.setPreferredSize(DIALOG_SIZE);
            for (Classroom classroom : sectionDialog.classrooms) {
                listModel.addElement("<html><body style='font-family:Roboto;"
                                     + "'>" + classroom.toString() + " "
                                     + "<strong>" + classroom.getDescription() + "</strong></body></html>");
            }
            list.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    sectionDialog.setClassroom(list.getSelectedIndex());
                    dispose();
                }
            });
            panel.setLayout(new BorderLayout());
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            panel.add(scroll);
            getContentPane().add(panel);
            setSize(DIALOG_SIZE);
            setResizable(false);
            setModalityType(ModalityType.APPLICATION_MODAL);
            setIconImage(Toolkit.getDefaultToolkit()
                                .getImage("icons/ic_add.png"));
            setLocationRelativeTo(null);
            setVisible(true);
        }
    }
}
