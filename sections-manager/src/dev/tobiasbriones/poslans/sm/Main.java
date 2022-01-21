/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm;

import dev.tobiasbriones.poslans.sm.career.Class;
import dev.tobiasbriones.poslans.sm.career.*;
import dev.tobiasbriones.poslans.sm.current.*;
import dev.tobiasbriones.poslans.sm.ui.CareerDataDialog;
import dev.tobiasbriones.poslans.sm.ui.MainWindow;
import dev.tobiasbriones.poslans.sm.ui.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.json.JSONArray;
import org.json.JSONException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Entry point for Sections Manager: Gives insights and management for opening
 * and closing of academic terms, course sections taking into account course
 * overlaps, professors, etc.
 *
 * These current software versions are legacy code I made 4 years ago, so they
 * are being transformed until release 0.1.
 *
 * @author Tobias Briones
 */
public final class Main extends Application implements MainWindow.Callback {
    public static void main(String[] args) {
        try {
            final GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            UIManager.setLookAndFeel(
                "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            ge.registerFont(Font.createFont(
                Font.TRUETYPE_FONT,
                new File("sections-manager/fonts/Roboto-Bold.ttf")
            ));
            ge.registerFont(Font.createFont(
                Font.TRUETYPE_FONT,
                new File("sections-manager/fonts/Roboto-Light.ttf")
            ));
        }
        catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> new Main());
    }

    public static String getIconPath(String iconName) {
        return "sections-manager/icons/" + iconName;
    }

    private final CareerDataHolder careerData;
    private final SectionsEditor sectionsEditor;
    private final List<Section> sections;
    private final HashSet<String> openSections;
    private final MainWindow mw;
    private final String[] careerInfo;
    private final List<String> filter;
    private String term;

    public Main() {
        this.careerData = new CareerDataHolder();
        this.sectionsEditor = new SectionsEditor();
        this.sections = new ArrayList<>();
        this.openSections = new HashSet<>();
        this.mw = new MainWindow(careerData, this);
        this.careerInfo = new String[2];
        this.filter = new ArrayList<>();
        try {
            this.term = Term.load();
        }
        catch (IOException e) {
            this.term = "";
        }
        loadData();
        updateUI();
        mw.setTitle(careerInfo[0] + " " + careerInfo[1]);
        mw.setTerm(term);
        checkForFillingCareerData();
    }

    @Override
    protected JFrame getWindow() {
        return mw;
    }

    // -------------------- MAIN WINDOW CALLBACK -------------------- //
    @Override
    public CareerDataDialog.Callback getCareerDataDialogCallback() {
        return new CareerDataEditorUIController(careerData, this);
    }

    @Override
    public List<String> getFilter() {
        return filter;
    }

    @Override
    public void setFilter(List<String> filter) {
        try {
            AppConfig.saveFilter(filter);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(mw, e.getMessage());
        }
        this.filter.clear();
        this.filter.addAll(filter);
        updateUI();
    }

    @Override
    public List<HistoryItem> getHistory() {
        try {
            return History.loadHistoryItems();
        }
        catch (IOException e) {
            showErrorMessage("Fail to load history.", e);
        }
        return null;
    }

    @Override
    public List<Classroom> getClassrooms() {
        return java.util.Collections.unmodifiableList(careerData.getClassrooms());
    }

    @Override
    public Enumeration<ProfessorAcademicLoad> getProfessorsLoad() {
        return ProfessorAcademicLoad.get(sections, careerData.getProfessors());
    }

    @Override
    public Iterator<Section> listSections() {
        return sections.iterator();
    }

    @Override
    public boolean isClassroomAvailable(
        Classroom classroom,
        int day,
        int hour
    ) {
        final String str = day + "-" + hour + "-" + classroom;
        return openSections.contains(str);
    }

    @Override
    public void createNewTerm(String name) {
        try {
            saveTermToHistory();
            Term.set(name);
            this.term = Term.load();
        }
        catch (IOException e) {
            showErrorMessage("Fail to save.", e);
        }
        deleteAllSections();
        loadData();
        updateUI();
        mw.setTerm(term);
    }

    @Override
    public void saveTerm() throws IOException {
        final File file = AppConfig.getTermFile();
        if (file == null) {
            JOptionPane.showMessageDialog(
                mw,
                "Select a folder to save your data through the save option"
            );
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Excel files",
                "xlsx"
            ));
            if (fileChooser.showSaveDialog(mw) == JFileChooser.APPROVE_OPTION) {
                File selection = fileChooser.getSelectedFile();
                if (!selection.toString().endsWith(".xlsx")) {
                    selection = new File(selection + ".xlsx");
                }
                AppConfig.saveTermFile(selection);
                saveTerm();
            }
            return;
        }
        History.save(sections, careerInfo[1], term, file);
        showInfoMessage(Strings.SUCCESSFULLY_SAVED);
    }

    @Override
    public void importSections(Sheet sheet) {
        final List<Class> classesIterator = careerData.getClasses();
        final List<Professor> professorsIterator = careerData.getProfessors();
        final List<Classroom> classroomsIterator = careerData.getClassrooms();
        String currentClassCodeName = null;
        String currentProfessorName = null;
        String[] currentDaysString = null;
        String currentBuildingNumberClassroom = null;
        String currentTimeString = null;
        Class currentClass = null;
        Professor currentProfessor = null;
        int[] currentDays = null;
        Classroom currentClassroom = null;
        Time currentTime = null;
        int count = 0;
        try {
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                // Check if cell is footer sign
                if (row.getCell(0).getStringCellValue()
                       .equalsIgnoreCase("POWERED BY POSLANS SECTIONS "
                                         + "MANAGER")) {
                    break;
                }
                currentClassCodeName = row.getCell(0).getStringCellValue();
                currentProfessorName = row.getCell(1).getStringCellValue();
                currentDaysString = row.getCell(2).getStringCellValue()
                                       .split(",");
                currentBuildingNumberClassroom = row.getCell(3)
                                                    .getStringCellValue();
                currentTimeString = row.getCell(4).getStringCellValue();
                currentClass = null;
                for (Class next : classesIterator) {
                    if (next.toString().equals(currentClassCodeName)) {
                        currentClass = next;
                        break;
                    }
                }
                if (currentClass == null) {
                    throw new Exception("Class not found: " + currentClassCodeName);
                }
                currentProfessor = null;
                for (Professor next : professorsIterator) {
                    if (next.getName().equals(currentProfessorName)) {
                        currentProfessor = next;
                        break;
                    }
                }
                if (currentProfessor == null) {
                    throw new Exception("Professor not found: " + currentProfessorName);
                }
                currentDays = new int[currentDaysString.length];
                for (int i = 0; i < currentDays.length; i++) {
                    currentDays[i] = Days.fromMiniDayStringToDay(
                        currentDaysString[i]);
                    if (currentDays[i] == -1) {
                        throw new Exception("Invalid day: " + currentDaysString[i]);
                    }
                }
                currentClassroom = null;
                for (Classroom next : classroomsIterator) {
                    if (next.toString()
                            .equals(currentBuildingNumberClassroom)) {
                        currentClassroom = next;
                        break;
                    }
                }
                if (currentClassroom == null) {
                    throw new Exception("Classroom not found: " + currentBuildingNumberClassroom);
                }
                currentTime = Time.fromString(currentTimeString);
                if (currentTime == null) {
                    throw new Exception("Invalid time: " + currentTimeString);
                }
                // Open the current section
                final Section newSection = sectionsEditor.openSection(
                    currentClass,
                    currentDays,
                    currentClassroom,
                    currentTime,
                    currentProfessor
                );
                final String check = checkOverlapping(newSection, null);
                count++;
                // Overlapping verification
                if (check != null) {
                    throw new Exception(check);
                }
            }
        }
        catch (Exception e) {
            final String msg = (e instanceof NullPointerException)
                               ? "There's a null empty cell in the sheet"
                               : e.getMessage();
            // Clear editor
            for (int i = 0; i < count; i++) {
                sectionsEditor.delete(sectionsEditor.getSize() - 1);
            }
            JOptionPane.showMessageDialog(
                mw,
                msg,
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        if (!save()) {
            JOptionPane.showMessageDialog(
                mw,
                "It couldn't save changes",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
        loadData();
        updateUI();
        JOptionPane.showMessageDialog(mw, count + " sections added.");
    }

    @Override
    public void importSectionsFromGenericFile(Sheet sheet) {
        final List<Class> classes = careerData.getClasses();
        final List<Professor> professors = careerData.getProfessors();
        final List<Classroom> classrooms = careerData.getClassrooms();
        Cell currentClassCodeCell = null;
        Cell currentProfessorIdCell = null;
        Cell currentDaysCell = null;
        Cell currentClassroomCell = null;
        Cell currentTimeCell = null;
        String[] currentDaysString = null;
        Class currentClass = null;
        Professor currentProfessor = null;
        int[] currentDays = null;
        Classroom currentClassroom = null;
        Time currentTime = null;
        int count = 0;
        try {
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                currentClassCodeCell = row.getCell(0);
                currentProfessorIdCell = row.getCell(1);
                currentDaysCell = row.getCell(2);
                currentClassroomCell = row.getCell(3);
                currentTimeCell = row.getCell(4);
                // Validate row
                if (!isCellSet(currentClassCodeCell) || !isCellSet(
                    currentProfessorIdCell)
                    || !isCellSet(currentDaysCell) || !isCellSet(
                    currentClassroomCell)
                    || !isCellSet(currentTimeCell)) {
                    throw new NullPointerException();
                }
                if (currentProfessorIdCell.getCellType() != CellType.NUMERIC) {
                    throw new Exception("Professor ID is an interger");
                }
                currentDaysString = currentDaysCell.getStringCellValue()
                                                   .split(",");
                currentClass = null;
                for (Class next : classes) {
                    if (next.getCode()
                            .equals(currentClassCodeCell.getStringCellValue())) {
                        currentClass = next;
                        break;
                    }
                }
                if (currentClass == null) {
                    throw new Exception("Class not found: " + currentClassCodeCell.getStringCellValue());
                }
                currentProfessor = null;
                for (Professor next : professors) {
                    if (next.getId() == currentProfessorIdCell.getNumericCellValue()) {
                        currentProfessor = next;
                        break;
                    }
                }
                if (currentProfessor == null) {
                    throw new Exception("Professor not found: " + currentProfessorIdCell.getNumericCellValue());
                }
                currentDays = new int[currentDaysString.length];
                for (int i = 0; i < currentDays.length; i++) {
                    try {
                        currentDays[i] =
                            Integer.parseInt(currentDaysString[i].trim());
                    }
                    catch (Exception e) {
                        throw new Exception("Invalid day: " + currentDaysString[i]);
                    }
                    if (currentDays[i] < 0 || currentDays[i] > 5) {
                        throw new Exception("Invalid day: " + currentDaysString[i]);
                    }
                }
                currentClassroom = null;
                for (Classroom next : classrooms) {
                    final String str =
                        next.getBuilding() + " " + next.getClassroomNumber();
                    if (str.equals(currentClassroomCell.getStringCellValue())) {
                        currentClassroom = next;
                        break;
                    }
                }
                if (currentClassroom == null) {
                    throw new Exception("Classroom not found: " + currentClassroomCell.getStringCellValue());
                }
                currentTime = null;
                // Parse time like "0700, 1300, 800"
                String timeStr;
                if (isCellInteger(currentTimeCell)) {
                    timeStr =
                        String.valueOf((int) currentTimeCell.getNumericCellValue());
                    if (timeStr.length() == 4 || timeStr.length() == 3) {
                        timeStr = timeStr.substring(0, timeStr.length() - 2);
                    }
                    else {
                        throw new Exception("Invalid time: " + timeStr);
                    }
                }
                else {
                    timeStr = currentTimeCell.getStringCellValue();
                }
                currentTime = Time.fromString(timeStr);
                if (currentTime == null) {
                    throw new Exception("Invalid time: " + timeStr);
                }
                // Open the current section
                final Section newSection = sectionsEditor.openSection(
                    currentClass,
                    currentDays,
                    currentClassroom,
                    currentTime,
                    currentProfessor
                );
                final String check = checkOverlapping(newSection, null);
                count++;
                // Overlapping verification
                if (check != null) {
                    throw new Exception(check);
                }
            }
        }
        catch (Exception e) {
            final String msg = (e instanceof NullPointerException)
                               ? "There's a null empty cell in the sheet, "
                                 + "row: " + (count + 2)
                               : e.getMessage() + ", row: " + (count + 2);
            // Clear editor
            for (int i = 0; i < count; i++) {
                sectionsEditor.delete(sectionsEditor.getSize() - 1);
            }
            JOptionPane.showMessageDialog(
                mw,
                msg,
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        if (!save()) {
            JOptionPane.showMessageDialog(
                mw,
                "It couldn't save changes",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
        loadData();
        updateUI();
        JOptionPane.showMessageDialog(mw, count + " sections added.");
    }

    // Returns null when there's no overlap
    @Override
    public String openSection(
        Class course,
        Professor professor,
        Classroom classroom,
        Time time,
        int[] days
    ) {
        final Section newSection = sectionsEditor.openSection(
            course,
            days,
            classroom,
            time,
            professor
        );
        final String check = checkOverlapping(newSection, null);
        // Overlapping verification
        if (check != null) {
            sectionsEditor.delete(sectionsEditor.getSize() - 1);
            return check;
        }
        if (!save()) {
            return null;
        }
        loadData();
        updateUI();
        return null;
    }

    @Override
    public String editSection(
        int position,
        Class course,
        Professor professor,
        Classroom classroom,
        Time time,
        int[] days
    ) {
        // Overlapping verification - Create temporarily section
        final String check = checkOverlapping(sectionsEditor.openSection(
            course,
            days,
            classroom,
            time,
            professor
        ), sections.get(position));
        if (check != null) {
            sectionsEditor.delete(sectionsEditor.getSize() - 1);
            return check;
        }
        sectionsEditor.delete(sectionsEditor.getSize() - 1);
        sectionsEditor.editSection(
            position,
            course,
            days,
            classroom,
            time,
            professor
        );
        if (!save()) {
            return null;
        }
        loadData();
        updateUI();
        return null;
    }

    @Override
    public void deleteSection(int position) {
        sectionsEditor.delete(position);
        save();
        loadData();
        updateUI();
    }

    @Override
    public void setNoFilter() {
        try {
            AppConfig.saveNoFilter();
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(mw, e.getMessage());
        }
        filter.clear();
        updateUI();
    }

    // -------------------- PRIVATE METHODS -------------------- //
    private void loadData() {
        final ClassesEditor classEditor = new ClassesEditor();
        final ProfessorsEditor professorEditor = new ProfessorsEditor();
        final ClassroomsEditor classroomEditor = new ClassroomsEditor();
        final ProfessorSpecializationEditor specializationEditor =
            new ProfessorSpecializationEditor();
        careerData.clear();
        sections.clear();
        openSections.clear();
        try {
            CareerInformation.loadInformation(careerInfo);
            classEditor.load();
            professorEditor.load();
            classroomEditor.load();
            specializationEditor.load();
            classEditor.load(careerData.getClasses());
            professorEditor.load(careerData.getProfessors());
            classroomEditor.load(careerData.getClassrooms());
            specializationEditor.load(careerData.getProfessorSpecializations());
            sectionsEditor.load();
            sectionsEditor.load(sections);
            final JSONArray filterArray = AppConfig.loadFilter();
            filter.clear();
            try {
                for (int i = 0; i < filterArray.length(); i++) {
                    filter.add(filterArray.getString(i));
                }
            }
            catch (JSONException e) {
                filter.clear();
                AppConfig.saveNoFilter();
            }
            for (Section section : sections) {
                for (int day : section.getDays()) {
                    openSections.add(
                        day + "-" + section.getTime()
                                           .getHour() + "-" + section.getClassroom()
                                                                     .toString()
                    );
                }
            }
        }
        catch (IOException e) {
            showUnexpectedErrorMessage("Fail to load. ", e);
            System.exit(0);
        }
        catch (Exception e) {
            showErrorMessage(
                "Corrupted data, please restart the app twice. ",
                e
            );
            System.exit(0);
        }
    }

    private void checkForFillingCareerData() {
        if (careerData.getClasses().isEmpty()
            || careerData.getClassrooms().isEmpty()
            || careerData.getProfessors().isEmpty()) {
            mw.welcome();
        }
    }

    private String checkOverlapping(Section checkSection, Section noCompare) {
        for (Section section : sections) {
            if (section == noCompare) {
                continue;
            }
            if (checkSection.overlaps(section)) {
                return checkSection.takeOverlapResult();
            }
        }
        return null;
    }

    private boolean save() {
        try {
            sectionsEditor.save();
        }
        catch (IOException e) {
            showErrorMessage("Fail when saving.", e);
            return false;
        }
        return true;
    }

    private void saveTermToHistory() {
        if (sections.isEmpty()) {
            return;
        }
        try {
            History.save(sections, careerInfo[1], term);
            showInfoMessage(Strings.SUCCESSFULLY_SAVED);
        }
        catch (IOException e) {
            showErrorMessage("Fail to save. ", e);
        }
    }

    private void deleteAllSections() {
        sectionsEditor.clear();
        save();
    }

    private void updateUI() {
        mw.clear();
        for (Section section : sections) {
            // Use no filter
            if (filter.isEmpty()) {
                mw.addSection(section);
            }
            // Filter by class tags (professor specializations)
            else if (filter.contains(section.getSectionClass().getTag())) {
                mw.addSection(section);
            }
        }
    }

    private static boolean isCellSet(Cell cell) {
        return cell != null && cell.getCellType() != CellType.BLANK;
    }

    private static boolean isCellInteger(Cell cell) {
        return cell.getCellType() == CellType.NUMERIC
               && (cell.getNumericCellValue() == ((int) cell.getNumericCellValue()));
    }
}
