/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm;

import dev.tobiasbriones.poslans.sm.career.Class;
import dev.tobiasbriones.poslans.sm.career.*;
import dev.tobiasbriones.poslans.sm.current.*;
import dev.tobiasbriones.poslans.sm.ui.CareerDataDialog;
import dev.tobiasbriones.poslans.sm.ui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
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
    private String term;

    private Main() {
        this.careerData = new CareerDataHolder();
        this.sectionsEditor = new SectionsEditor();
        this.sections = new ArrayList<>();
        this.mw = new MainWindow(careerData, this);
        this.careerInfo = new String[2];
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
    public Enumeration<ProfessorAcademicLoad> getProfessorsLoad() {
        return ProfessorAcademicLoad.get(sections, careerData.getProfessors());
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
            showInfoMessage("Nothing to save!");
            return;
        }
        try {
            History.save(sections, careerInfo[1], term);
            showInfoMessage("Successfully saved");
        }
        catch (IOException e) {
            showErrorMessage("Fail to save.", e);
        }
    }

    // Returns true when there's no overlap
    @Override
    public boolean openSection(
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
        // Overlapping verification
        if (!checkOverlapping(newSection, null)) {
            sectionsEditor.delete(sectionsEditor.getSize() - 1);
            return false;
        }
        if (!save()) {
            return true;
        }
        loadData();
        updateUI();
        return true;
    }

    @Override
    public boolean editSection(
        int position,
        Class Class,
        Professor professor,
        Classroom classroom,
        Time time,
        int[] days
    ) {
        // Overlapping verification - Create temporarily section
        if (!checkOverlapping(sectionsEditor.openSection(
            Class,
            days,
            classroom,
            time,
            professor
        ), sections.get(position))) {
            sectionsEditor.delete(sectionsEditor.getSize() - 1);
            return false;
        }
        sectionsEditor.delete(sectionsEditor.getSize() - 1);
        sectionsEditor.editSection(
            position,
            Class,
            days,
            classroom,
            time,
            professor
        );
        if (!save()) {
            return true;
        }
        loadData();
        updateUI();
        return true;
    }

    @Override
    public void deleteSection(int position) {
        sectionsEditor.delete(position);
        save();
        loadData();
        updateUI();
    }

    // -------------------- PRIVATE METHODS -------------------- //
    private void loadData() {
        final ClassesEditor classEditor = new ClassesEditor();
        final ProfessorsEditor professorEditor = new ProfessorsEditor();
        final ClassroomsEditor classroomEditor = new ClassroomsEditor();
        careerData.clear();
        sections.clear();
        try {
            CareerInformation.loadInformation(careerInfo);
            classEditor.load();
            professorEditor.load();
            classroomEditor.load();
            classEditor.load(careerData.getClasses());
            professorEditor.load(careerData.getProfessors());
            classroomEditor.load(careerData.getClassrooms());
            sectionsEditor.load();
            sectionsEditor.load(sections);
        }
        catch (IOException e) {
            showUnexpectedErrorMessage("Fail to load.", e);
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

    private boolean checkOverlapping(Section checkSection, Section noCompare) {
        for (Section section : sections) {
            if (section == noCompare) {
                continue;
            }
            if (checkSection.overlaps(section)) {
                return false;
            }
        }
        return true;
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
            mw.addSection(section);
        }
    }
}
