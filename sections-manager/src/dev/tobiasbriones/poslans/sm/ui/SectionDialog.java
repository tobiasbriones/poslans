/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class SectionDialog extends JDialog implements R,
                                                            Strings,
                                                            Days,
                                                            ActionListener {
    private static final long serialVersionUID = 8110170505164198763L;

    public interface Callback {
        boolean openSection(
            int classValue,
            int professorValue,
            int classroomValue,
            Time time,
            int[] days
        );

        boolean editSection(
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

    SectionDialog(MainWindow mw, CareerDataHolder careerData) {
        this(mw, careerData, null);
    }

    SectionDialog(
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
        final JPanel panel = new JPanel();
        final JPanel mainPanel = new JPanel();
        final JPanel mainPanelTop = new JPanel();
        final JPanel daysPanel = new JPanel();
        final JPanel bottomPanel = new JPanel();
        final JLabel classLabel = new JLabel(CLASS);
        final JLabel professorLabel = new JLabel(PROFESSOR);
        final JLabel classroomLabel = new JLabel(CLASSROOM);
        final JLabel timeLabel = new JLabel(TIME);
        final JLabel daysLabel = new JLabel(Strings.DAYS);
        final ListComboBoxModel<Class> classesCBModel = new ListComboBoxModel<>(
            careerData.getClasses());
        final ListComboBoxModel<Classroom> classroomsCBModel =
            new ListComboBoxModel<>(
                careerData.getClassrooms());
        final ListComboBoxModel<Professor> professorsCBModel =
            new ListComboBoxModel<>(
                careerData.getProfessors());
        final JButton cancelButton = new JButton(CANCEL.toUpperCase());
        final JButton saveButton = new JButton();
        final Border border = new EmptyBorder(5, 10, 5, 10);
        classesComboBox.setModel(classesCBModel);
        classesComboBox.addItemListener(e -> daysLabel.setText(Strings.DAYS + " (" + ((Class) e.getItem()).getDaysPerWeek() + ")"));
        classroomsComboBox.setModel(classroomsCBModel);
        professorsComboBox.setModel(professorsCBModel);
        if (edit == null) {
            setTitle(OPEN_SECTION);
            saveButton.setText(OPEN.toUpperCase());
            // Default view configuration
            mondayCheckBox.setSelected(true);
            tuesdayCheckBox.setSelected(true);
            wednesdayCheckBox.setSelected(true);
            thursdayCheckBox.setSelected(true);
            fridayCheckBox.setSelected(true);
        }
        else {
            setTitle(EDIT_SECTION);
            saveButton.setText(SAVE.toUpperCase());
            restoreViewState(careerData, edit, timeTF);
        }
        // Main panel
        mainPanelTop.setLayout(new GridLayout(9, 1));
        mainPanelTop.add(classLabel);
        mainPanelTop.add(classesComboBox);
        mainPanelTop.add(professorLabel);
        mainPanelTop.add(professorsComboBox);
        mainPanelTop.add(classroomLabel);
        mainPanelTop.add(classroomsComboBox);
        mainPanelTop.add(timeLabel);
        mainPanelTop.add(timeTF);
        mainPanelTop.add(daysLabel);
        daysPanel.setLayout(new GridLayout(2, 3));
        daysPanel.add(mondayCheckBox);
        daysPanel.add(tuesdayCheckBox);
        daysPanel.add(wednesdayCheckBox);
        daysPanel.add(thursdayCheckBox);
        daysPanel.add(fridayCheckBox);
        daysPanel.add(saturdayCheckBox);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
            Color.decode("#212121")), "Section"));
        mainPanel.add(mainPanelTop);
        mainPanel.add(daysPanel);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        final String name = ((Component) e.getSource()).getName();
        boolean hasOverlap = false;
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
                JOptionPane.showMessageDialog(this, FILL_ALL_FIELDS);
                return;
            }
            if (time == null) {
                JOptionPane.showMessageDialog(this, INVALID_TIME);
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
                hasOverlap = !callback.editSection(
                    classValue,
                    professorValue,
                    classroomValue,
                    time,
                    days
                );
            }
            else {
                hasOverlap = !callback.openSection(
                    classValue,
                    professorValue,
                    classroomValue,
                    time,
                    days
                );
            }
        }
        if (hasOverlap) {
            JOptionPane.showMessageDialog(this, SECTION_NOT_AVAILABLE);
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
}
