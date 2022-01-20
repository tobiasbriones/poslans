/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
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
                titles = null;
                setClassUI();
                break;
            case TYPE_PROFESSOR:
                field0 = new JTextField();
                field1 = null;
                field2 = null;
                field3 = null;
                field4 = null;
                titles = new JComboBox<>(Professor.Title.values());
                setProfessorUI();
                break;
            case TYPE_CLASSROOM:
                field0 = new JTextField();
                field1 = new JTextField();
                field2 = null;
                field3 = null;
                field4 = null;
                titles = null;
                setClassroomUI();
                break;
            default:
                field0 = null;
                field1 = null;
                field2 = null;
                field3 = null;
                field4 = null;
                titles = null;
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
                    final int weight;
                    final int days;
                    final float duration;
                    // Data validation
                    if (code.isEmpty() || name.isEmpty()) {
                        JOptionPane.showMessageDialog(this, FILL_ALL_FIELDS);
                        return;
                    }
                    try {
                        weight = Integer.parseInt(weightStr);
                        days = Integer.parseInt(daysStr);
                        duration = Float.parseFloat(durationStr);
                        if (weight < 0 || days < 0 || days > 6 || duration <= 0 || duration > 57600) {
                            throw new NumberFormatException(INVALID_RANGE);
                        }
                    }
                    catch (NumberFormatException nfe) {
                        final String msg =
                            ENTER_VALID_NUMBER + nfe.getMessage();
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
                            duration
                        );
                    }
                    else {
                        callback.deleteClass((Class) edit);
                        callback.createClass(
                            code,
                            name,
                            weight,
                            days,
                            duration
                        );
                    }
                }
                break;
            case TYPE_PROFESSOR:
                if (componentName.equals("create")) {
                    final String name = field0.getText();
                    final Professor.Title title = (Professor.Title) titles.getSelectedItem();
                    // Data validation
                    if (name.isEmpty()) {
                        JOptionPane.showMessageDialog(this, FILL_ALL_FIELDS);
                        return;
                    }
                    // Is creating new professor or creating existing
                    if (edit == null) {
                        callback.createProfessor(name, title);
                    }
                    else {
                        callback.deleteProfessor((Professor) edit);
                        callback.createProfessor(name, title);
                    }
                }
                break;
            case TYPE_CLASSROOM:
                if (componentName.equals("create")) {
                    final String building = field0.getText();
                    final String numberStr = field1.getText();
                    final int number;
                    // Data validation
                    if (building.isEmpty()) {
                        JOptionPane.showMessageDialog(this, FILL_ALL_FIELDS);
                        return;
                    }
                    try {
                        number = Integer.parseInt(numberStr);
                        if (number < 0) {
                            throw new NumberFormatException(INVALID_RANGE);
                        }
                    }
                    catch (NumberFormatException nfe) {
                        final String msg =
                            ENTER_VALID_NUMBER + nfe.getMessage();
                        JOptionPane.showMessageDialog(this, msg);
                        return;
                    }
                    // Is creating new classroom or creating existing
                    if (edit == null) {
                        callback.createClassroom(building, number);
                    }
                    else {
                        callback.deleteClassroom((Classroom) edit);
                        callback.createClassroom(building, number);
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
        final JLabel codeLabel = new JLabel(CLASS_CODE);
        final JLabel nameLabel = new JLabel(CLASS_NAME);
        final JLabel weightLabel = new JLabel(WEIGHT);
        final JLabel daysLabel = new JLabel(DAYS_PER_WEEK);
        final JLabel durationLabel = new JLabel(DURATION_HOURS);
        final JButton cancelButton = new JButton(CANCEL.toUpperCase());
        final Border border =
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
            Color.decode("#212121")), "Class information");
        final Border labelMargin = new EmptyBorder(0, 0, 5, 0);
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
            setTitle("Edit class");
            field0.setText(edit.getCode());
            field1.setText(edit.getName());
            field2.setText(String.valueOf(edit.getWeight()));
            field3.setText(String.valueOf(edit.getDaysPerWeek()));
            field4.setText(String.valueOf(edit.getDurationHours()));
            saveButton.setText("SAVE");
        }
        else {
            setTitle("Create class");
            saveButton.setText("CREATE");
        }
        codeLabel.setBorder(labelMargin);
        nameLabel.setBorder(labelMargin);
        weightLabel.setBorder(labelMargin);
        daysLabel.setBorder(labelMargin);
        durationLabel.setBorder(labelMargin);
        cancelButton.setName("cancel");
        cancelButton.addActionListener(this);
        saveButton.setName("create");
        saveButton.addActionListener(this);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(border);
        mainPanel.add(codeLabel);
        mainPanel.add(field0);
        mainPanel.add(nameLabel);
        mainPanel.add(field1);
        mainPanel.add(weightLabel);
        mainPanel.add(field2);
        mainPanel.add(daysLabel);
        mainPanel.add(field3);
        mainPanel.add(durationLabel);
        mainPanel.add(field4);
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
        final JLabel nameLabel = new JLabel(NAME);
        final JLabel titleLabel = new JLabel(TITLE);
        final JButton cancelButton = new JButton(CANCEL.toUpperCase());
        final Border border =
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
            Color.decode("#212121")), "Class information");
        final Border labelMargin = new EmptyBorder(0, 0, 5, 0);
        final GridBagConstraints gbc = new GridBagConstraints();
        // Fields
        field0.addActionListener(this);
        if (edit != null) {
            final Professor edit = (Professor) this.edit;
            setTitle(EDIT_PROFESSOR);
            field0.setText(edit.getName());
            titles.setSelectedItem(edit.getTitle());
            saveButton.setText(SAVE.toUpperCase());
        }
        else {
            setTitle(CREATE_PROFESSOR);
            saveButton.setText(CREATE.toUpperCase());
        }
        nameLabel.setBorder(labelMargin);
        titleLabel.setBorder(labelMargin);
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
        final JLabel buildingLabel = new JLabel(BUILDING);
        final JLabel numberLabel = new JLabel(CLASSROOM_NUMBER);
        final JButton cancelButton = new JButton(CANCEL.toUpperCase());
        final Border border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(
            Color.decode("#212121")), "Class information");
        final Border labelMargin = new EmptyBorder(0, 0, 5, 0);
        // Fields
        field0.addActionListener(this);
        field1.addActionListener(this);
        if (edit != null) {
            final Classroom edit = (Classroom) this.edit;
            setTitle(EDIT_CLASSROOM);
            field0.setText(edit.getBuilding());
            field1.setText(String.valueOf(edit.getClassroomNumber()));
            saveButton.setText(SAVE.toUpperCase());
        }
        else {
            setTitle(CREATE_CLASSROOM);
            saveButton.setText(CREATE.toUpperCase());
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
        bottomPanel.add(cancelButton);
        bottomPanel.add(saveButton);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.add(mainPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().add(panel);
    }
}
