/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm;

import dev.tobiasbriones.poslans.sm.career.Class;
import dev.tobiasbriones.poslans.sm.career.*;
import dev.tobiasbriones.poslans.sm.ui.CareerDataDialog;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

final class CareerDataEditorUIController implements CareerDataDialog.Callback {
    private final CareerDataHolder careerData;
    private final ClassesEditor classesEditor;
    private final ProfessorsEditor professorsEditor;
    private final ClassroomsEditor classroomsEditor;
    private final ProfessorSpecializationEditor professorSpecializationEditor;
    private final ApplicationMessageReporter amr;
    private CareerDataDialog dialog;
    private boolean hasChanges;

    CareerDataEditorUIController(
        CareerDataHolder data,
        ApplicationMessageReporter amr
    ) {
        this.careerData = data;
        this.classesEditor = new ClassesEditor();
        this.professorsEditor = new ProfessorsEditor();
        this.classroomsEditor = new ClassroomsEditor();
        this.professorSpecializationEditor =
            new ProfessorSpecializationEditor();
        this.amr = amr;
        this.dialog = null;
        this.hasChanges = false;
    }

    @Override
    public Vector<String> getProfessorSpecializations() {
        return new Vector<String>(careerData.getProfessorSpecializations());
    }

    @Override
    public void openEditor(CareerDataDialog dialog) {
        this.dialog = dialog;
        this.hasChanges = false;
        try {
            classesEditor.load();
            professorsEditor.load();
            classroomsEditor.load();
            professorSpecializationEditor.load();
        }
        catch (Exception e) {
            amr.showUnexpectedErrorMessage("Fail to load", e);
            dialog.dispose();
        }
    }

    @Override
    public void closeEditor() {
        dialog = null;
        classesEditor.clear();
        professorsEditor.clear();
        classesEditor.clear();
        if (hasChanges) {
            careerData.clear();
            try {
                classesEditor.load();
                professorsEditor.load();
                classroomsEditor.load();
                professorSpecializationEditor.load();
                classesEditor.load(careerData.getClasses());
                professorsEditor.load(careerData.getProfessors());
                classroomsEditor.load(careerData.getClassrooms());
                professorSpecializationEditor.load(careerData.getProfessorSpecializations());
            }
            catch (Exception e) {
                amr.showErrorMessage(
                    "Fail to save changes, please restart the app.",
                    e
                );
                System.exit(0);
            }
        }
    }

    @Override
    public void reload() {
        hasChanges = false;
        classesEditor.clear();
        professorsEditor.clear();
        classesEditor.clear();
        careerData.clear();
        try {
            classesEditor.load();
            professorsEditor.load();
            classroomsEditor.load();
            professorSpecializationEditor.load();
            classesEditor.load(careerData.getClasses());
            professorsEditor.load(careerData.getProfessors());
            classroomsEditor.load(careerData.getClassrooms());
            professorSpecializationEditor.load(careerData.getProfessorSpecializations());
        }
        catch (Exception e) {
            amr.showUnexpectedErrorMessage("Fail to load", e);
            dialog.dispose();
        }
        dialog.updateUI();
    }

    @Override
    public void setCareerInformation(String university, String career) {
        try {
            CareerInformation.saveInformation(university, career);
        }
        catch (IOException e) {
            amr.showErrorMessage(
                "The data couldn't be saved, please try again.",
                e
            );
        }
    }

    @Override
    public void createClass(
        String code,
        String name,
        int weight,
        int days,
        float duration,
        String tag
    ) {
        hasChanges = true;
        classesEditor.create(code, name, weight, days, duration, tag);
        classesEditor.performLastChange(careerData.getClasses());
        dialog.updateClassesUI();
    }

    @Override
    public void deleteClass(Class Class) {
        hasChanges = true;
        classesEditor.delete(Class);
        classesEditor.performLastChange(careerData.getClasses());
        dialog.updateClassesUI();
    }

    @Override
    public void createProfessor(
        int id,
        String name,
        Professor.Title title,
        String specialization
    ) {
        hasChanges = true;
        professorsEditor.create(id, name, title, specialization);
        professorsEditor.performLastChange(careerData.getProfessors());
        dialog.updateProfessorsUI();
    }

    @Override
    public void deleteProfessor(Professor professor) {
        hasChanges = true;
        professorsEditor.delete(professor);
        professorsEditor.performLastChange(careerData.getProfessors());
        dialog.updateProfessorsUI();
    }

    @Override
    public void createProfessorSpecialization(String specialization) {
        hasChanges = true;
        professorSpecializationEditor.create(specialization);
        professorSpecializationEditor.performLastChange(careerData.getProfessorSpecializations());
        dialog.updateProfessorSpecializationsUI();
    }

    @Override
    public void deleteProfessorSpecialization(String specialization) {
        hasChanges = true;
        professorSpecializationEditor.delete(specialization);
        professorSpecializationEditor.performLastChange(careerData.getProfessorSpecializations());
        dialog.updateProfessorSpecializationsUI();
    }

    @Override
    public void createClassroom(
        String building,
        int classroomNumber,
        String description
    ) {
        hasChanges = true;
        classroomsEditor.create(building, classroomNumber, description);
        classroomsEditor.performLastChange(careerData.getClassrooms());
        dialog.updateClassroomsUI();
    }

    @Override
    public void deleteClassroom(Classroom classroom) {
        hasChanges = true;
        classroomsEditor.delete(classroom);
        classroomsEditor.performLastChange(careerData.getClassrooms());
        dialog.updateClassroomsUI();
    }

    @Override
    public void clear() {
        hasChanges = true;
        classesEditor.clear();
        professorsEditor.clear();
        classroomsEditor.clear();
        professorSpecializationEditor.clear();
        careerData.clear();
    }

    @Override
    public void save() {
        if (!hasChanges) {
            return;
        }
        try {
            classesEditor.save();
            professorsEditor.save();
            classroomsEditor.save();
            professorSpecializationEditor.save();
        }
        catch (IOException e) {
            amr.showErrorMessage("Fail to save. ", e);
        }
        hasChanges = false;
    }

    @Override
    public void exportData() {
        final Workbook workbook = new XSSFWorkbook();
        final Sheet classesSheet = workbook.createSheet("Classes");
        final Sheet professorsSheet = workbook.createSheet("Professors");
        final Sheet classroomsSheet = workbook.createSheet("Classrooms");
        final Sheet professorSpecializationsSheet = workbook.createSheet(
            "Professor specializations");
        final JFileChooser fileChooser = new JFileChooser();
        final CellStyle headerStyle = workbook.createCellStyle();
        final XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        Row row = null;
        Cell header = null;
        int x = 0;
        classesSheet.setColumnWidth(0, 6000);
        classesSheet.setColumnWidth(1, 12000);
        classesSheet.setColumnWidth(4, 4000);
        classesSheet.setColumnWidth(5, 12000);
        professorsSheet.setColumnWidth(0, 6000);
        professorsSheet.setColumnWidth(1, 12000);
        professorsSheet.setColumnWidth(2, 6000);
        professorsSheet.setColumnWidth(3, 12000);
        classroomsSheet.setColumnWidth(0, 8000);
        classroomsSheet.setColumnWidth(1, 6000);
        classroomsSheet.setColumnWidth(2, 16000);
        professorSpecializationsSheet.setColumnWidth(0, 12000);
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        headerStyle.setFont(font);
        row = classesSheet.createRow(x);
        header = row.createCell(0);
        header.setCellStyle(headerStyle);
        header.setCellValue("CODE");
        header = row.createCell(1);
        header.setCellStyle(headerStyle);
        header.setCellValue("NAME");
        header = row.createCell(2);
        header.setCellStyle(headerStyle);
        header.setCellValue("CREDITS");
        header = row.createCell(3);
        header.setCellStyle(headerStyle);
        header.setCellValue("DAYS");
        header = row.createCell(4);
        header.setCellStyle(headerStyle);
        header.setCellValue("DURATION");
        header = row.createCell(5);
        header.setCellStyle(headerStyle);
        header.setCellValue("TAG");
        x++;
        for (Class course : careerData.getClasses()) {
            row = classesSheet.createRow(x);
            row.createCell(0).setCellValue(course.getCode());
            row.createCell(1).setCellValue(course.getName());
            row.createCell(2).setCellValue(course.getWeight());
            row.createCell(3).setCellValue(course.getDaysPerWeek());
            row.createCell(4).setCellValue(course.getDurationHours());
            row.createCell(5).setCellValue(course.getTag());
            x++;
        }
        x = 0;
        row = professorsSheet.createRow(x);
        header = row.createCell(0);
        header.setCellStyle(headerStyle);
        header.setCellValue("ID");
        header = row.createCell(1);
        header.setCellStyle(headerStyle);
        header.setCellValue("NAME");
        header = row.createCell(2);
        header.setCellStyle(headerStyle);
        header.setCellValue("TITLE");
        header = row.createCell(3);
        header.setCellStyle(headerStyle);
        header.setCellValue("SPECIALIZATION");
        x++;
        for (Professor professor : careerData.getProfessors()) {
            row = professorsSheet.createRow(x);
            row.createCell(0).setCellValue(professor.getId());
            row.createCell(1).setCellValue(professor.getName());
            row.createCell(2)
               .setCellValue(professor.getTitle().toString().toUpperCase());
            row.createCell(3).setCellValue(professor.getSpecialization());
            x++;
        }
        x = 0;
        row = classroomsSheet.createRow(x);
        header = row.createCell(0);
        header.setCellStyle(headerStyle);
        header.setCellValue("BUILDING");
        header = row.createCell(1);
        header.setCellStyle(headerStyle);
        header.setCellValue("CLASSROOM NUMBER");
        header = row.createCell(2);
        header.setCellStyle(headerStyle);
        header.setCellValue("DESCRIPTION");
        x++;
        for (Classroom classroom : careerData.getClassrooms()) {
            row = classroomsSheet.createRow(x);
            row.createCell(0).setCellValue(classroom.getBuilding());
            row.createCell(1).setCellValue(classroom.getClassroomNumber());
            row.createCell(2).setCellValue(classroom.getDescription());
            x++;
        }
        x = 0;
        row = professorSpecializationsSheet.createRow(x);
        header = row.createCell(0);
        header.setCellStyle(headerStyle);
        header.setCellValue("PROFESSOR SPECIALIZATION");
        x++;
        for (String specialization : careerData.getProfessorSpecializations()) {
            row = professorSpecializationsSheet.createRow(x);
            row.createCell(0).setCellValue(specialization);
            x++;
        }
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
            final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            final Date date = new Date();
            final String[] careerInfo = new String[2];
            try {
                CareerInformation.loadInformation(careerInfo);
                final String dateStr = dateFormat.format(date).replace('/', '-')
                                                 .replace(':', ' ');
                final String fileName = "Sections Manager - " + careerInfo[1] + " data " + dateStr + ".xlsx";
                final File directory = fileChooser.getSelectedFile();
                final File fileLocation = new File(directory, fileName);
                final FileOutputStream outputStream = new FileOutputStream(
                    fileLocation);
                workbook.write(outputStream);
                workbook.close();
                outputStream.close();
            }
            catch (IOException e) {
                amr.showErrorMessage("Fail to export.", e);
                return;
            }
        }
        amr.showInfoMessage("Data successfully exported");
    }
}
