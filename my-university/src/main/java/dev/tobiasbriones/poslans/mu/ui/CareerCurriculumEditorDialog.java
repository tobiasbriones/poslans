/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui;

import dev.tobiasbriones.poslans.mu.models.career.Course;
import dev.tobiasbriones.poslans.mu.models.career.curriculum.CareerCurriculum;
import dev.tobiasbriones.poslans.mu.models.career.curriculum.CourseNode;
import engineer.mathsoftware.jdesk.resources.StringResourceId;
import engineer.mathsoftware.jdesk.resources.StringResources;
import engineer.mathsoftware.jdesk.ui.dialog.LoadingBarDialog;
import engineer.mathsoftware.jdesk.ui.view.Button;
import engineer.mathsoftware.jdesk.ui.view.Panel;
import engineer.mathsoftware.jdesk.ui.view.ScrollPane;
import engineer.mathsoftware.jdesk.ui.view.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static dev.tobiasbriones.poslans.mu.Strings.*;

public final class CareerCurriculumEditorDialog extends LoadingBarDialog implements Form.FormListener,
                                                                                    ClickListener {
    private static final long serialVersionUID = 6312154433665516056L;

    interface Callback {
        Map<String, Course> getCourses();
    }

    private final Callback callback;

    private final List<TableNodeItem> nodeItems;

    private final TableModel tableModel;

    private final Table table;

    private final JSpinner requirementsSpinner;

    private final Form editForm;

    private final TextLabel errorLabel;

    public CareerCurriculumEditorDialog(
        CareerCurriculumWindow ccw,
        CareerCurriculum curriculum,
        Map<String, Course> courses
    ) {
        super(ccw);
        final StringResources sr = ccw.getStringResources();
        this.callback = ccw.getWDC();
        this.nodeItems = new ArrayList<>();
        this.tableModel = new TableModel(new String[] {
            sr.get(COURSE),
            sr.get(REQUIREMENTS)
        }, nodeItems);
        this.table = new Table(tableModel);
        this.requirementsSpinner = new JSpinner();
        this.editForm = new Form(ccw, EDIT);
        this.errorLabel = new TextLabel(ccw);
        final String careerName = curriculum.getCareerName();
        final Panel panel = new Panel(ccw);
        final Panel topPanel = new Panel(ccw);
        final Panel bottomPanel = new Panel(ccw);
        final Panel rightPanel = new Panel(ccw);
        final TextLabel careerNameLabel = new TextLabel(ccw, careerName);
        final Button saveButton = new Button(this, SAVE);
        Course currentCourse;
        // If curriculum is full
        if (curriculum.size() == courses.size()) {
            for (CourseNode node : curriculum) {
                currentCourse = node.getCourse();
                nodeItems.add(new TableNodeItem(
                    currentCourse,
                    new ArrayList<>(node.getRequiredCourses()),
                    node.getRequirementCodes()
                ));
            }
        }
        // If curriculum is empty or not full
        else {
            if (curriculum.isEmpty()) {
                final Collection<Course> values = courses.values();
                values.forEach(course -> nodeItems.add(new TableNodeItem(
                    course,
                    new ArrayList<>(),
                    ""
                )));
            }
            else {
                for (CourseNode node : curriculum) {
                    currentCourse = node.getCourse();
                    nodeItems.add(new TableNodeItem(
                        currentCourse,
                        new ArrayList<>(node.getRequiredCourses()),
                        node.getRequirementCodes()
                    ));
                }
                final Collection<Course> values = courses.values();
                values.forEach(course -> {
                    if (!curriculum.contains(course)) {
                        nodeItems.add(new TableNodeItem(
                            course,
                            new ArrayList<>(),
                            ""
                        ));
                    }
                });
            }
        }
        table.setBackground(getBackground());
        table.setFont(ccw.getAppStyle().getFont());
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.setRowHeight(18);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setSelectionBackground(Color.decode("#424242"));
        table.setSelectionForeground(Color.decode("#FAFAFA"));
        table.setFocusable(false);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final int selection = table.getSelectedRow();
                if (selection != -1) {
                    //TODO
                }
            }
        });
        editForm.addInput(REQUIREMENTS, requirementsSpinner);
        editForm.addCancelSubmitButtons(SAVE, this);
        errorLabel.setForeground(Color.decode("#E64A19"));
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        topPanel.add(careerNameLabel);
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(errorLabel);
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setPadding(0, 15, 0, 0);
        rightPanel.add(editForm, BorderLayout.NORTH);
        rightPanel.add(saveButton, BorderLayout.SOUTH);
        panel.setLayout(new BorderLayout());
        panel.setPadding(10, 15, 10, 15);
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new ScrollPane(ccw, table), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        panel.add(rightPanel, BorderLayout.EAST);
        // setLBDView(panel); TODO Fix JDesk method signature
        setTitle(CAREER_CURRICULUM);
        setIcon("ic_curriculum");
        setVisible(true);
    }

    @Override
    public void onSubmit() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onCancel() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onClick(Object view, StringResourceId viewTextId) {
        if (viewTextId == EDIT) {
            final int selection = table.getSelectedRow();
            if (selection != -1) {
                // sorry :D
            }
        }
        else if (viewTextId == DELETE) {
            final int[] rows = table.getSelectedRows();
            final TableNodeItem[] delete = new TableNodeItem[rows.length];
            if (rows.length != 0) {
                int i = 0;
                for (int row : rows) {
                    delete[i] = nodeItems.get(row);
                    i++;
                }
                for (TableNodeItem item : delete) {
                    nodeItems.remove(item);
                }
                //updateTable();
            }
        }
        else if (viewTextId == SAVE) {
            ((Button) view).setEnabled(false);
            //save();
        }
    }

    private static final class TableNodeItem {
        private final Course course;
        private final List<Course> requirements;
        private final String requirementCodes;

        private TableNodeItem(
            Course course,
            List<Course> requirements,
            String requirementCodes
        ) {
            this.course = course;
            this.requirements = requirements;
            this.requirementCodes = requirementCodes;
        }
    }

    private static final class TableModel extends AbstractTableModel {
        private static final long serialVersionUID = 6950862578910586662L;
        private static final int COLUMN_COUNT = 2;
        private final String[] columns;
        private final List<TableNodeItem> items;

        private TableModel(String[] columns, List<TableNodeItem> items) {
            this.columns = columns;
            this.items = items;
        }

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0:
                    return items.get(row).course;
                case 1:
                    return items.get(row).requirementCodes;
            }
            return null;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
    }
}
