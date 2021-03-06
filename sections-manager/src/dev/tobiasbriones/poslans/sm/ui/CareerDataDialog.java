/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import dev.tobiasbriones.poslans.sm.Main;
import dev.tobiasbriones.poslans.sm.career.CareerDataHolder;
import dev.tobiasbriones.poslans.sm.career.Class;
import dev.tobiasbriones.poslans.sm.career.Classroom;
import dev.tobiasbriones.poslans.sm.career.Professor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Vector;

public final class CareerDataDialog extends JDialog implements R,
                                                               Strings,
                                                               WindowListener,
                                                               ActionListener,
                                                               MouseListener,
                                                               ImportExportDialog.Callback {
    private static final long serialVersionUID = -906303588392152876L;
    private static final Dimension FRAME_SIZE = new Dimension(1024, 360);
    private static final Dimension TOOLBAR_SIZE = new Dimension(1024, 56);

    public interface Callback {
        Vector<String> getProfessorSpecializations();

        void openEditor(CareerDataDialog dialog);

        void closeEditor();

        void reload();

        void setCareerInformation(
            String university,
            String career
        );

        void createClass(
            String code,
            String name,
            int credits,
            int days,
            float duration,
            String classTag
        );

        void deleteClass(Class course);

        void createProfessor(
            int id,
            String name,
            Professor.Title title,
            String specialization
        );

        void deleteProfessor(Professor professor);

        void createProfessorSpecialization(String specialization);

        void deleteProfessorSpecialization(String specialization);

        void createClassroom(
            String building,
            int number,
            String description
        );

        void deleteClassroom(Classroom classroom);

        void clear();

        void save();

        void exportData();
    }

    private final CareerDataHolder info;
    private final Callback callback;
    private final DefaultListModel<Class> classesListModel;
    private final DefaultListModel<Professor> professorsListModel;
    private final DefaultListModel<Classroom> classroomListModel;
    private final DefaultListModel<String> professorSpecializationsListModel;
    private final JList<Class> classesList;
    private final JList<Professor> professorsList;
    private final JList<Classroom> classroomsList;
    private final JList<String> professorSpecializationsList;
    private final JPopupMenu popup;
    private JList<?> popupTarget;

    CareerDataDialog(
        MainWindow mw,
        CareerDataHolder careerData,
        Callback callback
    ) {
        super(mw, CAREER);
        this.info = careerData;
        this.callback = callback;
        this.classesListModel = new DefaultListModel<>();
        this.professorsListModel = new DefaultListModel<>();
        this.classroomListModel = new DefaultListModel<>();
        this.professorSpecializationsListModel = new DefaultListModel<>();
        this.classesList = new JList<>(classesListModel);
        this.professorsList = new JList<>(professorsListModel);
        this.classroomsList = new JList<>(classroomListModel);
        this.professorSpecializationsList = new JList<>(
            professorSpecializationsListModel);
        this.popup = new JPopupMenu();
        this.popupTarget = null;
        final JPanel panel = new JPanel();
        final JPanel mainPanel = new JPanel();
        final JPanel actionsPanel = new JPanel();
        final JToolBar toolBar = new JToolBar();
        final JScrollPane classesScroll = new JScrollPane(classesList);
        final JScrollPane professorsScroll = new JScrollPane(professorsList);
        final JScrollPane classroomsScroll = new JScrollPane(classroomsList);
        final JScrollPane professorSpecializationsScroll = new JScrollPane(
            professorSpecializationsList);
        final JLabel classesLabel = new JLabel(CLASSES);
        final JLabel professorsLabel = new JLabel(PROFESSORS);
        final JLabel classroomsLabel = new JLabel(CLASSROOMS);
        final JLabel professorSpecializationsLabel = new JLabel(
            PROFESSOR_SPECIALIZATIONS);
        final JButton careerInfoButton = new JButton(new ImageIcon(
            Main.getIconPath("ic_career_info.png")));
        final JButton importExportButton = new JButton(new ImageIcon(
            Main.getIconPath("ic_import_export.png")));
        final JButton deleteButton = new JButton(new ImageIcon(
            Main.getIconPath("ic_delete.png")));
        final JButton addClassButton = new JButton(ADD.toUpperCase());
        final JButton addProfessorButton = new JButton(ADD.toUpperCase());
        final JButton addClassroomButton = new JButton(ADD.toUpperCase());
        final JButton addProfessorSpecializationButton =
            new JButton(ADD.toUpperCase());
        final JButton discardButton = new JButton(DISCARD.toUpperCase());
        final JButton saveButton = new JButton(SAVE.toUpperCase());
        final JMenuItem editItem = new JMenuItem(EDIT);
        final JMenuItem deleteItem = new JMenuItem(DELETE);
        final GridBagConstraints gbc = new GridBagConstraints();
        // Set lists
        classesList.addMouseListener(this);
        professorsList.addMouseListener(this);
        classroomsList.addMouseListener(this);
        professorSpecializationsList.addMouseListener(this);
        // Set buttons
        careerInfoButton.setName(CAREER_DIALOG_CAREER);
        careerInfoButton.setToolTipText(CAREER_INFO_TIP);
        careerInfoButton.addActionListener(this);
        importExportButton.setName(CAREER_DIALOG_IMPORT_EXPORT);
        importExportButton.setToolTipText(IMPORT_EXPORT);
        importExportButton.addActionListener(this);
        deleteButton.setName(CAREER_DIALOG_DELETE);
        deleteButton.setToolTipText(DELETE_FOREVER_TIP);
        deleteButton.addActionListener(this);
        addClassButton.setName(CAREER_DIALOG_ADD_CLASS);
        addClassButton.addActionListener(this);
        addProfessorButton.setName(CAREER_DIALOG_ADD_PROFESSOR);
        addProfessorButton.addActionListener(this);
        addClassroomButton.setName(CAREER_DIALOG_ADD_CLASSROOM);
        addClassroomButton.addActionListener(this);
        addProfessorSpecializationButton.setName(
            CAREER_DIALOG_ADD_PROFESSOR_SPECIALIZATION);
        addProfessorSpecializationButton.addActionListener(this);
        discardButton.setName(CAREER_DIALOG_DISCARD);
        discardButton.addActionListener(this);
        saveButton.setName(CAREER_DIALOG_SAVE);
        saveButton.addActionListener(this);
        // Set popup menu
        editItem.setName(CAREER_DIALOG_MENU_EDIT);
        editItem.addActionListener(this);
        deleteItem.setName(CAREER_DIALOG_MENU_DELETE);
        deleteItem.addActionListener(this);
        popup.add(editItem);
        popup.add(deleteItem);
        // ToolBar
        toolBar.setSize(TOOLBAR_SIZE);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        toolBar.setBorder(BorderFactory.createMatteBorder(
            0,
            0,
            1,
            0,
            Color.decode("#737373")
        ));
        toolBar.setFloatable(false);
        toolBar.add(careerInfoButton);
        toolBar.add(importExportButton);
        toolBar.add(deleteButton);
        // Main panel
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = 1;
        gbc.insets.set(0, 5, 0, 5);
        // Row 1
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.1;
        mainPanel.add(classesLabel, gbc);
        gbc.gridx++;
        mainPanel.add(professorsLabel, gbc);
        gbc.gridx++;
        mainPanel.add(classroomsLabel, gbc);
        gbc.gridx++;
        mainPanel.add(professorSpecializationsLabel, gbc);
        // Row 2
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets.set(5, 5, 5, 5);
        mainPanel.add(classesScroll, gbc);
        gbc.gridx++;
        mainPanel.add(professorsScroll, gbc);
        gbc.gridx++;
        mainPanel.add(classroomsScroll, gbc);
        gbc.gridx++;
        mainPanel.add(professorSpecializationsScroll, gbc);
        // Row 3
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 0.05;
        gbc.insets.set(0, 5, 5, 5);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(addClassButton, gbc);
        gbc.gridx++;
        mainPanel.add(addProfessorButton, gbc);
        gbc.gridx++;
        mainPanel.add(addClassroomButton, gbc);
        gbc.gridx++;
        mainPanel.add(addProfessorSpecializationButton, gbc);
        // Actions panel
        actionsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.add(discardButton);
        actionsPanel.add(saveButton);
        // Panel
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(5, 15, 5, 15));
        panel.add(toolBar);
        panel.add(mainPanel);
        panel.add(actionsPanel);
        getContentPane().add(panel);
        addWindowListener(this);
        setResizable(false);
        setSize(FRAME_SIZE);
        setLocationRelativeTo(null);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setIconImage(Toolkit.getDefaultToolkit()
                            .getImage("icons/ic_career_data.png"));
        updateUI();
        setVisible(true);
    }

    // -------------------- WINDOW LISTENER --------------- //
    @Override
    public void windowOpened(WindowEvent e) {
        callback.openEditor(this);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        callback.closeEditor();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        callback.closeEditor();
    }

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}

    // -------------------- ACTION LISTENER --------------- //
    @Override
    public void actionPerformed(ActionEvent e) {
        final String name = ((Component) e.getSource()).getName();
        switch (name) {
            case CAREER_DIALOG_CAREER:
                final String university = JOptionPane.showInputDialog(
                    this,
                    UNIVERSITY_BELONG
                );
                final String career = JOptionPane.showInputDialog(
                    this,
                    SET_CAREER_NAME
                );
                if (university == null || career == null || university.isEmpty() || career.isEmpty()) {
                    JOptionPane.showMessageDialog(this, FILL_ALL_FIELDS);
                    break;
                }
                callback.setCareerInformation(university, career);
                break;
            case CAREER_DIALOG_IMPORT_EXPORT:
                new ImportExportDialog(this);
                break;
            case CAREER_DIALOG_DELETE:
                final String msg = DELETE_CAREER_DATA;
                final String title = DELETE;
                final int opt = JOptionPane.OK_CANCEL_OPTION;
                final int msgType = JOptionPane.QUESTION_MESSAGE;
                final ImageIcon icon = new ImageIcon("icons/ic_delete.png");
                final int answer = JOptionPane.showConfirmDialog(
                    this,
                    msg,
                    title,
                    opt,
                    msgType,
                    icon
                );
                if (answer == JOptionPane.OK_OPTION) {
                    callback.clear();
                    callback.save();
                    callback.reload();
                    updateUI();
                }
                break;
            case CAREER_DIALOG_DISCARD:
                dispose();
                break;
            case CAREER_DIALOG_SAVE:
                callback.save();
                dispose();
                break;
            case CAREER_DIALOG_MENU_EDIT:
                openEditInputDialog(popupTarget);
                popupTarget = null;
                break;
            case CAREER_DIALOG_MENU_DELETE:
                if (popupTarget == classesList) {
                    callback.deleteClass(classesList.getSelectedValue());
                }
                else if (popupTarget == professorsList) {
                    callback.deleteProfessor(professorsList.getSelectedValue());
                }
                else if (popupTarget == classroomsList) {
                    callback.deleteClassroom(classroomsList.getSelectedValue());
                }
                else if (popupTarget == professorSpecializationsList) {
                    callback.deleteProfessorSpecialization(
                        professorSpecializationsList.getSelectedValue());
                }
                popupTarget = null;
                break;
            case CAREER_DIALOG_ADD_CLASS:
                new CareerDataInputDialog(
                    this,
                    callback,
                    CareerDataInputDialog.TYPE_CLASS
                );
                break;
            case CAREER_DIALOG_ADD_PROFESSOR:
                new CareerDataInputDialog(
                    this,
                    callback,
                    CareerDataInputDialog.TYPE_PROFESSOR
                );
                break;
            case CAREER_DIALOG_ADD_CLASSROOM:
                new CareerDataInputDialog(
                    this,
                    callback,
                    CareerDataInputDialog.TYPE_CLASSROOM
                );
                break;
            case CAREER_DIALOG_ADD_PROFESSOR_SPECIALIZATION:
                final String specialization = JOptionPane.showInputDialog(
                    this,
                    NEW_PROFESSOR_SPECIALIZATION
                );
                if (specialization != null && !specialization.trim()
                                                             .isEmpty()) {
                    callback.createProfessorSpecialization(specialization);
                }
                break;
        }
    }

    // -------------------- MOUSE LISTENER --------------- //
    @Override
    public void mouseClicked(MouseEvent e) {
        checkPopupMenu(e);
        if (e.getClickCount() == 2) {
            openEditInputDialog(e.getSource());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        checkPopupMenu(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        checkPopupMenu(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        checkPopupMenu(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        checkPopupMenu(e);
    }

    // -------------------- IMPORT / EXPORT DIALOG --------------- //
    @Override
    public void onImport(
        Sheet classesSheet,
        Sheet professorsSheet,
        Sheet classroomsSheet,
        Sheet professorSpecializationsSheet
    ) {
        final HashSet<String> specializations = new HashSet<>();
        final HashSet<Integer> professorsId = new HashSet<>();
        String classCode = null;
        String className = null;
        String classTag = null;
        int professorId = -1;
        String professorName = null;
        String classroomBuilding = null;
        String classroomDescription = null;
        Professor.Title professorTitle = null;
        String professorSpecialization = null;
        int classCredits = -1;
        int classDays = -1;
        float classDuration = -1.0f;
        int classroomNumber = -1;
        int i = 0;
        boolean isFirstRow = true;
        String currentSheet = "";
        int currentRow = -1;
        callback.clear();
        try {
            currentSheet = "Professor specialization sheet";
            for (Row row : professorSpecializationsSheet) {
                currentRow++;
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                final Cell cell = row.getCell(0);
                callback.createProfessorSpecialization(cell.getStringCellValue());
                specializations.add(cell.getStringCellValue());
            }
            isFirstRow = true;
            currentSheet = "Classes sheet";
            currentRow = -1;
            for (Row row : classesSheet) {
                currentRow++;
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                for (Cell cell : row) {
                    switch (i) {
                        case 0:
                            classCode = cell.getStringCellValue();
                            break;
                        case 1:
                            className = cell.getStringCellValue();
                            break;
                        case 2:
                            classCredits = (int) cell.getNumericCellValue();
                            break;
                        case 3:
                            classDays = (int) cell.getNumericCellValue();
                            break;
                        case 4:
                            classDuration = (float) cell.getNumericCellValue();
                            break;
                        case 5:
                            classTag = cell.getStringCellValue();
                            break;
                    }
                    i++;
                }
                if (i != 6) {
                    final String msg = "Please check your document, the "
                                       + "number of cells"
                                       + " don't match properly. Classes "
                                       + "sheet.";
                    handleImportError(msg);
                    return;
                }
                if (!specializations.contains(classTag)) {
                    final String msg =
                        "Please check your document, the tag " + classTag
                        + " for class " + classCode + " don"
                        + "'t match with those in "
                        + "Professor specializations sheet. "
                        + "Classes sheet.";
                    handleImportError(msg);
                    return;
                }
                i = 0;
                callback.createClass(
                    classCode,
                    className,
                    classCredits,
                    classDays,
                    classDuration,
                    classTag
                );
            }
            isFirstRow = true;
            currentSheet = "Professors sheet";
            currentRow = -1;
            for (Row row : professorsSheet) {
                currentRow++;
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                for (Cell cell : row) {
                    switch (i) {
                        case 0:
                            professorId = (int) cell.getNumericCellValue();
                            if (professorsId.contains(professorId)) {
                                final String msg = "Please check your "
                                                   + "document, duplicated id "
                                                   + "for professors "
                                                   + "Professors sheet. Row: "
                                                   + (currentRow + 1);
                                handleImportError(msg);
                                return;
                            }
                            professorsId.add(professorId);
                            break;
                        case 1:
                            professorName = cell.getStringCellValue();
                            break;
                        case 2:
                            professorTitle =
                                Professor.Title.fromString(cell.getStringCellValue());
                            break;
                        case 3:
                            professorSpecialization = cell.getStringCellValue();
                            break;
                    }
                    i++;
                }
                if (i != 4) {
                    if (i == 1) {
                        professorTitle = Professor.Title.OTHER;
                    }
                    else {
                        final String msg = "Please check your document, the "
                                           + "number of cells don't"
                                           + " match properly. Professors "
                                           + "sheet.";
                        handleImportError(msg);
                        return;
                    }
                }
                if (!specializations.contains(professorSpecialization)) {
                    final String msg = "Please check your document, the "
                                       + "specialization " + professorSpecialization
                                       + " for professor " + professorName +
                                       " doesn't match with those in "
                                       + "Professor specializations sheet. "
                                       + "Professors sheet.";
                    handleImportError(msg);
                    return;
                }
                i = 0;
                if (professorTitle == null) {
                    throw new NullPointerException("Wrong title for professor"
                                                   + " " + professorName);
                }
                callback.createProfessor(
                    professorId,
                    professorName,
                    professorTitle,
                    professorSpecialization
                );
            }
            isFirstRow = true;
            currentSheet = "Classrooms sheet";
            currentRow = -1;
            for (Row row : classroomsSheet) {
                currentRow++;
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                for (Cell cell : row) {
                    switch (i) {
                        case 0:
                            if (cell.getCellType() == CellType.STRING) {
                                classroomBuilding = cell.getStringCellValue();
                            }
                            else {
                                classroomBuilding =
                                    String.valueOf((int) cell.getNumericCellValue());
                            }
                            break;
                        case 1:
                            classroomNumber = (int) cell.getNumericCellValue();
                            break;
                        case 2:
                            classroomDescription = cell.getStringCellValue();
                            break;
                    }
                    i++;
                }
                if (i != 3) {
                    final String msg = "Please check your document, the "
                                       + "number of cells don't"
                                       + " match properly. Classrooms sheet.";
                    handleImportError(msg);
                    return;
                }
                i = 0;
                callback.createClassroom(
                    classroomBuilding,
                    classroomNumber,
                    classroomDescription
                );
            }
        }
        catch (NullPointerException e) {
            handleImportError("There is a cell with no value. " + e);
        }
        catch (Exception e) {
            handleImportError(e + " Sheet: " + currentSheet + ", Row: " + currentRow);
            return;
        }
        callback.save();
        updateUI();
    }

    @Override
    public void onExport() {
        callback.exportData();
    }

    public void updateClassesUI() {
        classesListModel.clear();
        for (Class Class : info.getClasses()) {
            classesListModel.addElement(Class);
        }
    }

    public void updateProfessorsUI() {
        professorsListModel.clear();
        for (Professor professor : info.getProfessors()) {
            professorsListModel.addElement(professor);
        }
    }

    public void updateClassroomsUI() {
        classroomListModel.clear();
        for (Classroom classroom : info.getClassrooms()) {
            classroomListModel.addElement(classroom);
        }
    }

    public void updateProfessorSpecializationsUI() {
        professorSpecializationsListModel.clear();
        for (String specialization : info.getProfessorSpecializations()) {
            professorSpecializationsListModel.addElement(specialization);
        }
    }

    public void updateUI() {
        updateClassesUI();
        updateProfessorsUI();
        updateClassroomsUI();
        updateProfessorSpecializationsUI();
    }

    private void checkPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger()) {
            if (e.getSource() == classesList) {
                popupTarget = classesList;
                classesList.setSelectedIndex(classesList.locationToIndex(e.getPoint()));
                popup.show(classesList, e.getX(), e.getY());
            }
            else if (e.getSource() == professorsList) {
                popupTarget = professorsList;
                professorsList.setSelectedIndex(professorsList.locationToIndex(e.getPoint()));
                popup.show(professorsList, e.getX(), e.getY());
            }
            else if (e.getSource() == classroomsList) {
                popupTarget = classroomsList;
                classroomsList.setSelectedIndex(classroomsList.locationToIndex(e.getPoint()));
                popup.show(classroomsList, e.getX(), e.getY());
            }
            else {
                popupTarget = professorSpecializationsList;
                professorSpecializationsList.setSelectedIndex(
                    professorSpecializationsList.locationToIndex(e.getPoint()));
                popup.show(professorSpecializationsList, e.getX(), e.getY());
            }
        }
    }

    private void openEditInputDialog(Object src) {
        if (src == classesList) {
            new CareerDataInputDialog(
                this,
                callback,
                CareerDataInputDialog.TYPE_CLASS,
                classesList.getSelectedValue()
            );
        }
        else if (src == professorsList) {
            new CareerDataInputDialog(
                this,
                callback,
                CareerDataInputDialog.TYPE_PROFESSOR,
                professorsList.getSelectedValue()
            );
        }
        else if (src == classroomsList) {
            new CareerDataInputDialog(
                this,
                callback,
                CareerDataInputDialog.TYPE_CLASSROOM,
                classroomsList.getSelectedValue()
            );
        }
    }

    private void handleImportError(String msg) {
        JOptionPane.showMessageDialog(this, msg);
        callback.reload();
    }
}
