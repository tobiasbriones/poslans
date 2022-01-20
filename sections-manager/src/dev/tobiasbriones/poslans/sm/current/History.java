/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.current;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public final class History {
    private static final String DATA_DIRECTORY = "sections-manager/data/history";

    public static void save(
        List<Section> sections,
        String career,
        String term
    ) throws IOException {
        final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        final Date date = new Date();
        final String dateStr = dateFormat.format(date).replace('/', '-')
                                         .replace(':', ' ');
        final String fileName =
            "Sections Manager - " + career + " " + term + " " + dateStr +
            ".xlsx";
        check();
        final File file = new File(DATA_DIRECTORY, fileName);
        final Workbook workbook = makeHistory(sections, career, term);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
        workbook.close();
    }

    public static List<HistoryItem> loadHistory() throws IOException {
        check();
        final List<HistoryItem> history = new ArrayList<>();
        final File[] files = new File(DATA_DIRECTORY).listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String extension = "";
                int i = pathname.getName().lastIndexOf('.');
                if (i > 0) {
                    extension = pathname.getName().substring(i + 1);
                }
                return (pathname.isFile() && extension.equals("xlsx"));
            }
        });
        for (File file : files) {
            history.add(new HistoryItem(file));
        }
        return history;
    }

    private static void check() throws IOException {
        final File data = new File(DATA_DIRECTORY);
        if (!data.exists() || !data.isDirectory()) {
            if (!data.mkdir()) {
                throw new IOException("Error creating directory " + data);
            }
        }
    }

    private static Workbook makeHistory(
        List<Section> history,
        String career,
        String term
    ) {
        final Workbook workbook = new XSSFWorkbook();
        final int year = Calendar.getInstance().get(Calendar.YEAR);
        final Sheet sheet =
            workbook.createSheet(career + " " + term + " " + year);
        final CellStyle headerStyle = workbook.createCellStyle();
        final CellStyle footStyle = workbook.createCellStyle();
        final XSSFFont headerFont = ((XSSFWorkbook) workbook).createFont();
        final XSSFFont footFont = ((XSSFWorkbook) workbook).createFont();
        Row row = null;
        Cell cell = null;
        int x = 0;
        sheet.protectSheet(
            ")�zad02_X _pad$%41pS�oo-__O21#PokWFIjkmsapOsjjnNh7KlaiZ&ux(k_{ll");
        sheet.setColumnWidth(0, 12000);
        sheet.setColumnWidth(1, 12000);
        sheet.setColumnWidth(2, 8000);
        sheet.setColumnWidth(3, 10000);
        sheet.setColumnWidth(4, 2000);
        sheet.setColumnWidth(5, 3000);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        footFont.setFontHeightInPoints((short) 10);
        footFont.setItalic(true);

        // compatibility issue
        // footFont.setColor(new XSSFColor(Color.decode("#737373")));

        footStyle.setFont(footFont);
        row = sheet.createRow(x);
        cell = row.createCell(0);
        cell.setCellStyle(headerStyle);
        cell.setCellValue("CLASS");
        cell = row.createCell(1);
        cell.setCellStyle(headerStyle);
        cell.setCellValue("PROFESSOR");
        cell = row.createCell(2);
        cell.setCellStyle(headerStyle);
        cell.setCellValue("DAYS");
        cell = row.createCell(3);
        cell.setCellStyle(headerStyle);
        cell.setCellValue("CLASSROOM");
        cell = row.createCell(4);
        cell.setCellStyle(headerStyle);
        cell.setCellValue("TIME");
        cell = row.createCell(5);
        cell.setCellStyle(headerStyle);
        cell.setCellValue("TIME END");
        x++;
        for (Section section : history) {
            row = sheet.createRow(x);
            row.createCell(0)
               .setCellValue(section.getSectionClass().toString());
            row.createCell(1).setCellValue(section.getProfessor().toString());
            row.createCell(2).setCellValue(section.getMiniDays());
            row.createCell(3).setCellValue(section.getClassroom().toString());
            row.createCell(4).setCellValue(section.getTime().toString());
            row.createCell(5).setCellValue(section.getEndTime().toString());
            x++;
        }
        row = sheet.createRow(x);
        cell = row.createCell(0);
        cell.setCellStyle(footStyle);
        cell.setCellValue("Powered by Poslans Sections Manager");
        return workbook;
    }
}
