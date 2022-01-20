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
import org.json.JSONArray;
import org.json.JSONException;

import javax.swing.*;
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
 * This current software versions are legacy code I made 4 years ago, so they
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
        catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new Main());
    }

    public static String getIconPath(String iconName) {
        return "sections-manager/icons/" + iconName;
    }

    private final CareerDataHolder careerData;
    private final SectionsEditor sectionsEditor;
    private final List<Section> sections;
    private final MainWindow mw;
    private final String[] careerInfo;
    private final List<String> filter;
    private String term;

    public Main() {
        this.careerData = new CareerDataHolder();
        this.sectionsEditor = new SectionsEditor();
        this.sections = new ArrayList<>();
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
            return History.loadHistory();
        }
        catch (IOException e) {
            showErrorMessage("Fail to load history.", e);
        }
        return null;
    }

    @Override
    public HashMap<String, String> getClassroomsTakenHours() {
        final HashMap<String, String> classroomsTakenHours = new HashMap<>();
        Classroom currentClassroom;
        String currentStr;
        for (Classroom classroom : careerData.getClassrooms()) {
            classroomsTakenHours.put(classroom.toString(), "");
        }
        for (Section section : sections) {
            currentClassroom = section.getClassroom();
            if (classroomsTakenHours.containsKey(currentClassroom.toString())) {
                currentStr =
                    classroomsTakenHours.get(currentClassroom.toString());
                currentStr += " " + section.getTime() + " ";
                classroomsTakenHours.put(
                    currentClassroom.toString(),
                    currentStr
                );
            }
        }
        return classroomsTakenHours;
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
    public void createNewTerm(String name) {
        try {
            saveToHistory();
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
    public void saveToHistory() {
        if (sections.isEmpty()) {
            //showInfoMessage("Nothing to save!");
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

    // Returns null when there's no overlap
    @Override
    public String openSection(
        Class Class,
        Professor professor,
        Classroom classroom,
        Time time,
        int[] days
    ) {
        final Section newSection = sectionsEditor.openSection(
            Class,
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
}
