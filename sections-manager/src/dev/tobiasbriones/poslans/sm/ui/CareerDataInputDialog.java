/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import dev.tobiasbriones.poslans.sm.career.Class;
import dev.tobiasbriones.poslans.sm.career.Classroom;
import dev.tobiasbriones.poslans.sm.career.Professor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

final class CareerDataInputDialog extends JDialog implements Strings,
                                                             ActionListener {
    static final int TYPE_CLASS = 0;
    static final int TYPE_PROFESSOR = 1;
    static final int TYPE_CLASSROOM = 2;
    private static final long serialVersionUID = 8653115991678982796L;
    private final CareerDataDialog.Callback callback;
    private final Object edit;
    private final int type;
    private final JTextField field0;
    private final JTextField field1;
    private final JTextField field2;
    private final JTextField field3;
    private final JTextField field4;
    private final JComboBox<Professor.Title> titles;
    private final JComboBox<String> specializations; // Shared with class and
    // professor
    private final JButton saveButton;

    CareerDataInputDialog(
        CareerDataDialog dialog,
        CareerDataDialog.Callback callback,
        int type
    ) {
        this(dialog, callback, type, null);
    }

    CareerDataInputDialog(
        CareerDataDialog dialog,
        CareerDataDialog.Callback callback,
        int type,
        Object edit
    ) {
        super(dialog);
        this.callback = callback;
        this.type = type;
        this.edit = edit;
        this.saveButton = new JButton();
        switch (type) {
            case TYPE_CLASS:
                field0 = new JTextField();
                field1 = new JTextField();
                field2 = new JTextField();
                field3 = new JTextField();
                field4 = new JTextField();
                specializations = new JComboBox<>(callback.getProfessorSpecializations());
                titles = null;
                setClassUI();
                break;
            case TYPE_PROFESSOR:
                field0 = new JTextField();
                field1 = new JTextField();
                field2 = null;
                field3 = null;
                field4 = null;
                titles = new JComboBox<>(Professor.Title.values());
                specializations = new JComboBox<>(callback.getProfessorSpecializations());
                setProfessorUI();
                break;
            case TYPE_CLASSROOM:
                field0 = new JTextField();
                field1 = new JTextField();
                field2 = new JTextField();
                field3 = null;
                field4 = null;
                titles = null;
                specializations = null;
                setClassroomUI();
                break;
            default:
                field0 = null;
                field1 = null;
                field2 = null;
                field3 = null;
                field4 = null;
                titles = null;
                specializations = null;
                break;
        }
        setModalityType(ModalityType.APPLICATION_MODAL);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String componentName = ((Component) e.getSource()).getName();
        if (e.getSource() instanceof JTextField) {
            final JTextField tf = (JTextField) e.getSource();
            if (tf == field0 && field1 != null) {
                field1.requestFocus();
            }
            else if (tf == field1 && field2 != null) {
                field2.requestFocus();
            }
            else if (tf == field2 && field3 != null) {
                field3.requestFocus();
            }
            else if (tf == field3 && field4 != null) {
                field4.requestFocus();
            }
            else if (tf == field4 || (type == TYPE_PROFESSOR && tf == field0) || (type == TYPE_CLASSROOM && tf == field1)) {
                saveButton.doClick();
            }
            return;
        }
        switch (type) {
            case TYPE_CLASS:
                if (componentName.equals("create")) {
                    final String code = field0.getText();
                    final String name = field1.getText();
                    final String weightStr = field2.getText();
                    final String daysStr = field3.getText();
                    final String durationStr = field4.getText();
                    final String tag =
                        (String) specializations.getSelectedItem();
                    final int weight;
                    final int days;
                    final float duration;
                    // Data validation
                    if (
                        code.trim().isEmpty() || name.trim().isEmpty() || tag.trim().isEmpty()
                    ) {
                        JOptionPane.showMessageDialog(this,
                                                      Strings.FILL_ALL_FIELDS
                        );
                        return;
                    }
                    try {
                        weight = Integer.parseInt(weightStr);
                        days = Integer.parseInt(daysStr);
                        duration = Float.parseFloat(durationStr);
                        if (weight < 0 || days < 0 || days > 6 || duration <= 0 || duration > 57600) {
                            throw new NumberFormatException(Strings.INVALID_RANGE);
                        }
                    }
                    catch (NumberFormatException nfe) {
                        final String msg =
                            Strings.ENTER_VALID_NUMBER + nfe.getMessage();
                        JOptionPane.showMessageDialog(this, msg);
                        return;
                    }
                    // Is creating new class or editing existing
                    if (edit == null) {
                        callback.createClass(
                            code,
                            name,
                            weight,
                            days,
                            duration,
                            tag
                        );
                    }
                    else {
                        callback.deleteClass((Class) edit);
                        callback.createClass(
                            code,
                            name,
                            weight,
                            days,
                            duration,
                            tag
                        );
                    }
                }
                break;
            case TYPE_PROFESSOR:
                if (componentName.equals("create")) {
                    final String name = field0.getText();
                    final Professor.Title title =
                        (Professor.Title) titles.getSelectedItem();
                    final String specialization =
                        (String) specializations.getSelectedItem();
                    // Data validation
                    if (name.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                                      Strings.FILL_ALL_FIELDS
                        );
                        return;
                    }
                    // Is creating new professor or creating existing
                    if (edit == null) {
                        callback.createProfessor(name, title, specialization);
                    }
                    else {
                        callback.deleteProfessor((Professor) edit);
                        callback.createProfessor(name, title, specialization);
                    }
                }
                break;
            case TYPE_CLASSROOM:
                if (componentName.equals("create")) {
                    final String building = field0.getText();
                    final String numberStr = field1.getText();
                    final String description = field2.getText();
                    final int number;
                    // Data validation
                    if (building.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                                      Strings.FILL_ALL_FIELDS
                        );
                        return;
                    }
                    try {
                        number = Integer.parseInt(numberStr);
                        if (number < 0) {
                            throw new NumberFormatException(Strings.INVALID_RANGE);
                        }
                    }
                    catch (NumberFormatException nfe) {
                        final String msg =
                            Strings.ENTER_VALID_NUMBER + nfe.getMessage();
                        JOptionPane.showMessageDialog(this, msg);
                        return;
                    }
                    // Is creating new classroom or creating existing
                    if (edit == null) {
                        callback.createClassroom(building, number, description);
                    }
                    else {
                        callback.deleteClassroom((Classroom) edit);
                        callback.createClassroom(building, number, description);
                    }
                }
                break;
        }
        dispose();
    }

    private void setClassUI() {
        final JPanel panel = new JPanel();
        final JPanel mainPanel = new JPanel();
        final JPanel bottomPanel = new JPanel();
        final JLabel codeLabel = new JLabel(Strings.CLASS_CODE);
        final JLabel nameLabel = new JLabel(Strings.CLASS_NAME);
        final JLabel weightLabel = new JLabel(Strings.CREDITS);
        final JLabel daysLabel = new JLabel(Strings.DAYS_PER_WEEK);
        final JLabel durationLabel = new JLabel(Strings.DURATION_HOURS);
        final JLabel tagLabel = new JLabel(Strings.TAG);
        final JButton cancelButton = new JButton(Strings.CANCEL.toUpperCase());
        final Border border =
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.decode("#212121")), "Class information");
        final Border labelMargin = new EmptyBorder(0, 0, 5, 0);
        final GridBagConstraints gbc = new GridBagConstraints();
        // Default view configuration
        field4.setText("1");
        // Fields
        field0.addActionListener(this);
        field1.addActionListener(this);
        field2.addActionListener(this);
        field3.addActionListener(this);
        field4.addActionListener(this);
        if (edit != null) {
            final Class edit = (Class) this.edit;
            final ComboBoxModel<String> model = specializations.getModel();
            setTitle(Strings.EDIT_CLASS);
            field0.setText(edit.getCode());
            field1.setText(edit.getName());
            field2.setText(String.valueOf(edit.getWeight()));
            field3.setText(String.valueOf(edit.getDaysPerWeek()));
            field4.setText(String.valueOf(edit.getDurationHours()));
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).equals(edit.getTag())) {
                    specializations.setSelectedIndex(i);
                    break;
                }
            }
            saveButton.setText(Strings.SAVE.toUpperCase());
        }
        else {
            setTitle(Strings.CREATE_CLASS);
            saveButton.setText(Strings.CREATE.toUpperCase());
        }
        codeLabel.setBorder(labelMargin);
        nameLabel.setBorder(labelMargin);
        weightLabel.setBorder(labelMargin);
        daysLabel.setBorder(labelMargin);
        durationLabel.setBorder(labelMargin);
        tagLabel.setBorder(labelMargin);
        cancelButton.setName("cancel");
        cancelButton.addActionListener(this);
        saveButton.setName("create");
        saveButton.addActionListener(this);
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(border);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(codeLabel, gbc);
        gbc.gridy++;
        mainPanel.add(field0, gbc);
        gbc.gridy++;
        mainPanel.add(nameLabel, gbc);
        gbc.gridy++;
        mainPanel.add(field1, gbc);
        gbc.gridy++;
        mainPanel.add(weightLabel, gbc);
        gbc.gridy++;
        mainPanel.add(field2, gbc);
        gbc.gridy++;
        mainPanel.add(daysLabel, gbc);
        gbc.gridy++;
        mainPanel.add(field3, gbc);
        gbc.gridy++;
        mainPanel.add(durationLabel, gbc);
        gbc.gridy++;
        mainPanel.add(field4, gbc);
        gbc.gridy++;
        mainPanel.add(tagLabel, gbc);
        gbc.gridy++;
        mainPanel.add(specializations, gbc);
        bottomPanel.add(cancelButton);
        bottomPanel.add(saveButton);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.add(mainPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().add(panel);
    }

    private void setProfessorUI() {
        final JPanel panel = new JPanel();
        final JPanel mainPanel = new JPanel();
        final JPanel bottomPanel = new JPanel();
        final JLabel nameLabel = new JLabel(Strings.NAME);
        final JLabel titleLabel = new JLabel(Strings.TITLE);
        final JLabel specializationLabel = new JLabel(Strings.SPECIALIZATION);
        final JButton cancelButton = new JButton(Strings.CANCEL.toUpperCase());
        final Border border =
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
                Color.decode("#212121")), "Class information");
        final Border labelMargin = new EmptyBorder(0, 0, 5, 0);
        final GridBagConstraints gbc = new GridBagConstraints();
        // Fields
        field0.addActionListener(this);
        if (edit != null) {
            final Professor edit = (Professor) this.edit;
            final ComboBoxModel<String> model = specializations.getModel();
            setTitle(Strings.EDIT_PROFESSOR);
            field0.setText(edit.getName());
            titles.setSelectedItem(edit.getTitle());
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).equals(edit.getSpecialization())) {
                    specializations.setSelectedIndex(i);
                    break;
                }
            }
            saveButton.setText(Strings.SAVE.toUpperCase());
        }
        else {
            setTitle(Strings.CREATE_PROFESSOR);
            saveButton.setText(Strings.CREATE.toUpperCase());
        }
        nameLabel.setBorder(labelMargin);
        titleLabel.setBorder(labelMargin);
        specializationLabel.setBorder(labelMargin);
        cancelButton.setName("cancel");
        cancelButton.addActionListener(this);
        saveButton.setName("create");
        saveButton.addActionListener(this);
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(border);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(nameLabel, gbc);
        gbc.gridy++;
        mainPanel.add(field0, gbc);
        gbc.gridy++;
        mainPanel.add(titleLabel, gbc);
        gbc.gridy++;
        mainPanel.add(titles, gbc);
        gbc.gridy++;
        mainPanel.add(specializationLabel, gbc);
        gbc.gridy++;
        mainPanel.add(specializations, gbc);
        bottomPanel.add(cancelButton);
        bottomPanel.add(saveButton);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.add(mainPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().add(panel);
    }

    private void setClassroomUI() {
        final JPanel panel = new JPanel();
        final JPanel mainPanel = new JPanel();
        final JPanel bottomPanel = new JPanel();
        final JLabel buildingLabel = new JLabel(Strings.BUILDING);
        final JLabel numberLabel = new JLabel(Strings.CLASSROOM_NUMBER);
        final JLabel descriptionLabel = new JLabel(Strings.DESCRIPTION);
        final JButton cancelButton = new JButton(Strings.CANCEL.toUpperCase());
        final Border border =
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
            Color.decode("#212121")), "Class information");
        final Border labelMargin = new EmptyBorder(0, 0, 5, 0);
        // Fields
        field0.addActionListener(this);
        field1.addActionListener(this);
        field2.addActionListener(this);
        if (edit != null) {
            final Classroom edit = (Classroom) this.edit;
            setTitle(Strings.EDIT_CLASSROOM);
            field0.setText(edit.getBuilding());
            field1.setText(String.valueOf(edit.getClassroomNumber()));
            field2.setText(edit.getDescription());
            saveButton.setText(Strings.SAVE.toUpperCase());
        }
        else {
            setTitle(Strings.CREATE_CLASSROOM);
            saveButton.setText(Strings.CREATE.toUpperCase());
        }
        buildingLabel.setBorder(labelMargin);
        numberLabel.setBorder(labelMargin);
        cancelButton.setName("cancel");
        cancelButton.addActionListener(this);
        saveButton.setName("create");
        saveButton.addActionListener(this);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(border);
        mainPanel.add(buildingLabel);
        mainPanel.add(field0);
        mainPanel.add(numberLabel);
        mainPanel.add(field1);
        mainPanel.add(descriptionLabel);
        mainPanel.add(field2);
        bottomPanel.add(cancelButton);
        bottomPanel.add(saveButton);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.add(mainPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().add(panel);
    }
}
