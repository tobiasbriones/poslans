/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.controller;

import dev.tobiasbriones.poslans.mu.Strings;
import dev.tobiasbriones.poslans.mu.database.LocalStorageDao;
import dev.tobiasbriones.poslans.mu.database.User;
import dev.tobiasbriones.poslans.mu.ui.MainWindow;
import dev.tobiasbriones.poslans.mu.ui.editing.Campus;
import dev.tobiasbriones.poslans.mu.ui.editing.Career;
import dev.tobiasbriones.poslans.mu.ui.editing.EditingItem;
import engineer.mathsoftware.jdesk.Config;
import engineer.mathsoftware.jdesk.resources.StringResources;
import engineer.mathsoftware.jdesk.ui.dialog.TaskDialog;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

import static dev.tobiasbriones.poslans.mu.ui.controller.MWControllerSQL.*;

public final class MainWindowController implements MainWindow.Callback {
    public static JSONObject loadEmailLogin()
        throws IOException {
        return getEmailJson();
    }

    private final User user;
    private final StringResources sr;

    public MainWindowController(User user, StringResources sr) {
        this.user = user;
        this.sr = sr;
    }

    // -------------------- EMAIL LOGIN WINDOW -------------------- //
    @Override
    public void saveEmailInfo(
        String host,
        int port,
        String email,
        String password
    ) throws IOException {
        final JSONObject data = new JSONObject();
        data.put("host", host);
        data.put("port", port);
        data.put("email", email);
        data.put("password", password);
        // TODO ADD NEW IMPL save encrypted file
    }

    // -------------------- MAIN WINDOW -------------------- //
    // EditingItem called when editing campuses or careers
    @Override
    public List<EditingItem> getCampuses() throws SQLException {
        final List<EditingItem> campuses = new ArrayList<>();
        final String sql = "SELECT name FROM campus";
        try (
            Connection connection = user.connect();
            Statement statement = connection.createStatement()
        ) {
            final ResultSet rs = statement.executeQuery(sql);
            String currentName;

            while (rs.next()) {
                currentName = rs.getString("name");
                campuses.add(new Campus(false, currentName, currentName));
            }
        }
        return campuses;
    }

    @Override
    public List<EditingItem> getCareers() throws SQLException {
        final List<EditingItem> careers = new ArrayList<>();
        final String sql = "SELECT name FROM career";

        try (
            Connection connection = user.connect();
            Statement statement = connection.createStatement()
        ) {
            final ResultSet rs = statement.executeQuery(sql);
            String currentName;

            while (rs.next()) {
                currentName = rs.getString("name");
                careers.add(new Career(false, currentName, currentName));
            }
        }
        return careers;
    }

    // Called when starting new term and career selection is needed
    @Override
    public List<Item> listCampusItems() throws SQLException {
        try (Connection connection = user.connect()) {
            return loadItems("campus", connection);
        }
    }

    @Override
    public List<Item> listCareerItems() throws SQLException {
        try (Connection connection = user.connect()) {
            return loadItems("career", connection);
        }
    }

    @Override
    public LinkedList<String> listCurrentTerms() throws SQLException {
        final LinkedList<String> currentTerms = new LinkedList<>();
        final String sql = "SELECT t.name FROM current_term JOIN term t ON "
                           + "current_term.term_id = t.id";
        try (
            Connection connection = user.connect();
            Statement statement = connection.createStatement()
        ) {
            final ResultSet rs = statement.executeQuery(sql);
            String currentName;

            while (rs.next()) {
                currentName = rs.getString("name");
                currentTerms.add(currentName);
            }
        }
        return currentTerms;
    }

    @Override
    public JSONArray listTerms() throws Exception {
        final URL url = new URL(user.getWebsiteURL()
                                    .toString() + "/term.php");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        final byte[] postDataBytes = "param=list".getBytes(StandardCharsets.UTF_8);
        final StringBuilder builder = new StringBuilder();
        final Charset charset = StandardCharsets.UTF_8;
        final InputStream is;
        final JSONArray list;
        String currentLine;

        connection.setRequestMethod("POST");
        connection.setRequestProperty(
            "Content-Type",
            "application/x-www-form-urlencoded"
        );
        connection.setRequestProperty(
            "Content-Length",
            String.valueOf(postDataBytes.length)
        );
        connection.setDoOutput(true);
        connection.getOutputStream().write(postDataBytes);
        is = connection.getInputStream();
        try (
            BufferedReader br =
                new BufferedReader(new InputStreamReader(
                    is,
                    charset
                ))
        ) {
            while ((currentLine = br.readLine()) != null) {
                builder.append(currentLine);
            }
        }
        try {
            list = new JSONArray(builder.toString());
        }
        catch (Exception e) {
            throw new Exception("Something's wrong at the server side");
        }
        return list;
    }

    @Override
    public boolean isValidUserPassword(String password) {
        if (!user.isSet()) {
            return false;
        }
        return user.getPassword().equals(password);
    }

    @Override
    public void updateCampuses(List<EditingItem> campuses) throws SQLException {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        try (Connection connection = user.connect()) {
            Campus currentCampus;
            connection.setAutoCommit(false);
            try {
                for (EditingItem item : campuses) {
                    currentCampus = (Campus) item;
                    if (currentCampus.isNew() && !item.isDeleted()) {
                        insertNewCampus(connection, currentCampus);
                    }
                    else if (currentCampus.isUpdated()) {
                        updateCampus(connection, currentCampus);
                    }
                    else if (currentCampus.isDeleted()) {
                        deleteCampus(connection, currentCampus);
                    }
                    else {
                        continue;
                    }
                }
            }
            catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
        }
    }

    @Override
    public void updateCareers(List<EditingItem> careers) throws SQLException {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        try (Connection connection = user.connect()) {
            Career currentCareer;
            connection.setAutoCommit(false);
            try {
                for (EditingItem item : careers) {
                    currentCareer = (Career) item;
                    if (currentCareer.isNew() && !currentCareer.isDeleted()) {
                        insertNewCareer(connection, currentCareer);
                    }
                    else if (currentCareer.isUpdated()) {
                        updateCareer(connection, currentCareer);
                    }
                    else if (currentCareer.isDeleted()) {
                        deleteCareer(connection, currentCareer);
                    }
                    else {
                        continue;
                    }
                }
            }
            catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
        }
    }

    @Override
    public void registerProfessors(
        Sheet sheet,
        Sheet passwordsSheet,
        TaskDialog<Sheet> td
    ) throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        int i = 0;
        // Validate sheet
        if (!isValidSheetFormat(
            sheet,
            PROFESSORS_REGISTER_SHEET,
            PROFESSORS_REGISTER_SHEET_HEADER
        )) {
            throw new InvalidExcelSheetFormatException(sr);
        }
        td.update(i);

        try (Connection connection = user.connect()) {
            connection.setAutoCommit(false);
            try {
                Cell currentNameCell;
                Cell currentEmailCell;
                String currentName;
                String currentEmail;
                String currentPassword;
                Row currentPasswordRow;
                for (Row row : sheet) {
                    currentNameCell = row.getCell(0);
                    currentEmailCell = row.getCell(1);
                    currentPasswordRow = passwordsSheet.createRow(i);
                    i++;
                    if (row.getRowNum() == 0) {
                        // Set passwords sheet header
                        currentPasswordRow.createCell(0)
                                          .setCellValue("EMAIL");
                        currentPasswordRow.createCell(1)
                                          .setCellValue("PASSWORD");
                        continue;
                    }
                    // Data validation
                    if (!isSetCell(currentNameCell) || !isSetCell(
                        currentEmailCell)) {
                        throw new NullPointerException();
                    }
                    currentName = currentNameCell.getStringCellValue();
                    currentEmail = currentEmailCell.getStringCellValue();
                    currentPassword = RandomPassword.generate(12);
                    registerProfessor(
                        connection,
                        currentName,
                        currentPassword,
                        currentEmail
                    );
                    currentPasswordRow.createCell(0)
                                      .setCellValue(currentEmail);
                    currentPasswordRow.createCell(1)
                                      .setCellValue(currentPassword);
                    td.update(i - 1);
                }
            }
            catch (NullPointerException e) {
                final String msg = sr.get(Strings.VAL_MISSING_REG_PROFESSORS) + i;
                connection.rollback();
                throw new Exception(msg);
            }
            catch (Exception e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
        }
    }

    @Override
    public void updateProfessors(
        Sheet sheet,
        Sheet passwordsSheet,
        TaskDialog<Sheet> td
    ) throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        int i = 0;
        int k = 0; // For passwords sheet
        // Validate sheet
        if (!isValidSheetFormat(
            sheet,
            PROFESSORS_UPDATE_SHEET,
            PROFESSORS_UPDATE_SHEET_HEADER
        )) {
            throw new InvalidExcelSheetFormatException(sr);
        }
        td.update(i);

        try (Connection connection = user.connect()) {
            connection.setAutoCommit(false);
            try {
                Cell currentNameCell;
                Cell currentEmailCell;
                Cell currentPasswordCell;
                String currentName;
                String currentEmail;
                String currentPassword;
                Row currentPasswordRow;
                for (Row row : sheet) {
                    currentNameCell = row.getCell(0);
                    currentEmailCell = row.getCell(1);
                    currentPasswordCell = row.getCell(2);
                    currentName = null;
                    currentEmail = null;
                    currentPassword = null;
                    i++;
                    if (row.getRowNum() == 0) {
                        currentPasswordRow = passwordsSheet.createRow(k);
                        k++;
                        currentPasswordRow.createCell(0)
                                          .setCellValue("EMAIL");
                        currentPasswordRow.createCell(1)
                                          .setCellValue("PASSWORD");
                        continue;
                    }
                    // Data validation
                    if (!isSetCell(currentNameCell) || !isSetCell(
                        currentEmailCell)) {
                        throw new NullPointerException();
                    }
                    currentName = currentNameCell.getStringCellValue();
                    currentEmail = currentEmailCell.getStringCellValue();
                    // Set new password
                    if (isSetCell(currentPasswordCell)) {
                        currentPassword = RandomPassword.generate(12);
                        currentPasswordRow = passwordsSheet.createRow(k);
                        k++;
                        currentPasswordRow.createCell(0)
                                          .setCellValue(currentEmail);
                        currentPasswordRow.createCell(1)
                                          .setCellValue(currentPassword);
                    }
                    updateProfessor(
                        connection,
                        currentName,
                        currentEmail,
                        currentPassword
                    );
                    td.update(i - 1);
                }
            }
            catch (NullPointerException e) {
                final String msg =
                    sr.get(Strings.VAL_MISSING_UPD_PROFESSORS) + i;
                connection.rollback();
                throw new Exception(msg);
            }
            catch (Exception e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
        }
    }

    @Override
    public void deleteProfessors(Sheet sheet, TaskDialog<Void> td)
        throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        int i = 0;

        // Validate sheet
        if (!isValidSheetFormat(
            sheet,
            PROFESSORS_DELETE_SHEET,
            PROFESSORS_DELETE_SHEET_HEADER
        )) {
            throw new InvalidExcelSheetFormatException(sr);
        }
        td.update(i);
        try (Connection connection = user.connect()) {
            connection.setAutoCommit(false);
            try {
                Cell currentProfessorCell;
                String currentProfessor;
                for (Row row : sheet) {
                    currentProfessorCell = row.getCell(0);
                    i++;
                    if (row.getRowNum() == 0) {
                        continue;
                    }
                    // Data validation
                    if (!isSetCell(currentProfessorCell)) {
                        throw new NullPointerException();
                    }
                    currentProfessor =
                        currentProfessorCell.getStringCellValue();
                    deleteProfessor(connection, currentProfessor);
                    td.update(i - 1);
                }
            }
            catch (NullPointerException e) {
                final String msg =
                    sr.get(Strings.VAL_MISSING_DEL_PROFESSORS) + i;
                connection.rollback();
                throw new Exception(msg);
            }
            catch (Exception e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
        }
    }

    @Override
    public void registerStudents(
        Sheet sheet,
        Sheet passwordsSheet,
        TaskDialog<Sheet> td
    ) throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        final Map<String, Integer> campuses = new HashMap<>();
        final Map<String, Integer> careers = new HashMap<>();
        int i = 0;

        // Validate sheet
        if (!isValidSheetFormat(
            sheet,
            STUDENTS_REGISTER_SHEET,
            STUDENTS_REGISTER_SHEET_HEADER
        )) {
            throw new InvalidExcelSheetFormatException(sr);
        }
        td.update(i);
        try (Connection connection = user.connect()) {
            loadHashMaps(connection, campuses, careers);
            connection.setAutoCommit(false);
            try {
                int currentId;
                Integer currentCampus;
                Integer currentFirstCareer;
                Integer currentSecondCareer;
                Cell currentIdCell;
                Cell currentCampusCell;
                Cell currentFirstCareerCell;
                Cell currentSecondCareerCell;
                Cell currentNameCell;
                Cell currentEmailCell;
                String currentName;
                String currentEmail;
                String currentPassword;
                Row currentPasswordRow;

                for (Row row : sheet) {
                    currentSecondCareer = -1;
                    currentIdCell = row.getCell(0);
                    currentCampusCell = row.getCell(1);
                    currentFirstCareerCell = row.getCell(2);
                    currentSecondCareerCell = row.getCell(3);
                    currentNameCell = row.getCell(4);
                    currentEmailCell = row.getCell(5);
                    currentPasswordRow = passwordsSheet.createRow(i);
                    i++;
                    if (row.getRowNum() == 0) {
                        currentPasswordRow.createCell(0)
                                          .setCellValue("EMAIL");
                        currentPasswordRow.createCell(1)
                                          .setCellValue("PASSWORD");
                        continue;
                    }
                    // Data validation
                    if (!isSetCell(currentIdCell) || !isSetCell(
                        currentCampusCell)
                        || !isSetCell(currentFirstCareerCell) || !isSetCell(
                        currentNameCell)
                        || !isSetCell(currentEmailCell)) {
                        throw new NullPointerException();
                    }
                    currentId = (int) currentIdCell.getNumericCellValue();
                    currentCampus = getIdFromCell(
                        campuses,
                        currentCampusCell
                    );
                    currentFirstCareer = getIdFromCell(
                        careers,
                        currentFirstCareerCell
                    );
                    currentName = currentNameCell.getStringCellValue();
                    currentEmail = currentEmailCell.getStringCellValue();
                    currentPassword = RandomPassword.generate(12);
                    if (isSetCell(currentSecondCareerCell)) {
                        currentSecondCareer = getIdFromCell(
                            careers,
                            currentSecondCareerCell
                        );
                        if (currentSecondCareer == null) {
                            throw new WrongCampusCareerException(sr, i);
                        }
                        if (currentFirstCareer.equals(currentSecondCareer)) {
                            final String msg =
                                sr.get(Strings.SECOND_CAREER_IQUAL_FIRST_CAREER) + i;
                            throw new Exception(msg);
                        }
                    }
                    if (currentCampus == null || currentFirstCareer == null) {
                        throw new WrongCampusCareerException(sr, i);
                    }
                    registerStudent(
                        connection,
                        currentId,
                        currentCampus,
                        currentFirstCareer,
                        currentSecondCareer,
                        currentName,
                        currentPassword,
                        currentEmail
                    );
                    currentPasswordRow.createCell(0)
                                      .setCellValue(currentEmail);
                    currentPasswordRow.createCell(1)
                                      .setCellValue(currentPassword);
                    td.update(i - 1);
                }
            }
            catch (NullPointerException e) {
                final String msg =
                    sr.get(Strings.VAL_MISSING_REG_STUDENTS) + i;
                connection.rollback();
                throw new SQLException(msg);
            }
            catch (Exception e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
        }
    }

    @Override
    public void updateStudents(
        Sheet sheet,
        Sheet passwordsSheet,
        TaskDialog<Sheet> td
    ) throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        final Map<String, Integer> campuses = new HashMap<>();
        final Map<String, Integer> careers = new HashMap<>();
        int i = 0;
        int k = 0; // For passwords sheet

        // Validate sheet
        if (!isValidSheetFormat(
            sheet,
            STUDENTS_UPDATE_SHEET,
            STUDENTS_UPDATE_SHEET_HEADER
        )) {
            throw new InvalidExcelSheetFormatException(sr);
        }
        td.update(i);

        try (Connection connection = user.connect()) {
            loadHashMaps(connection, campuses, careers);
            connection.setAutoCommit(false);
            try {
                int currentId;
                Integer currentCampus;
                Integer currentFirstCareer;
                Integer currentSecondCareer;
                Cell currentIdCell;
                Cell currentCampusCell;
                Cell currentFirstCareerCell;
                Cell currentSecondCareerCell;
                Cell currentNameCell;
                Cell currentEmailCell;
                Cell currentNewPasswordCell;
                String currentName;
                String currentEmail;
                String currentPassword;
                Row currentPasswordRow;
                for (Row row : sheet) {
                    currentIdCell = row.getCell(0);
                    currentCampusCell = row.getCell(1);
                    currentFirstCareerCell = row.getCell(2);
                    currentSecondCareerCell = row.getCell(3);
                    currentNameCell = row.getCell(4);
                    currentEmailCell = row.getCell(5);
                    currentNewPasswordCell = row.getCell(6);
                    currentId = -1;
                    currentCampus = -1;
                    currentFirstCareer = -1;
                    currentSecondCareer = -1;
                    currentName = null;
                    currentEmail = null;
                    currentPassword = null;
                    i++;
                    if (row.getRowNum() == 0) {
                        currentPasswordRow = passwordsSheet.createRow(k);
                        k++;
                        currentPasswordRow.createCell(0)
                                          .setCellValue("EMAIL");
                        currentPasswordRow.createCell(1)
                                          .setCellValue("PASSWORD");
                        continue;
                    }
                    // Data validation
                    if (!isSetCell(currentIdCell)) {
                        throw new NullPointerException();
                    }
                    // At least one value must be set
                    if (!isSetCell(currentCampusCell) && !isSetCell(
                        currentFirstCareerCell)
                        && !isSetCell(currentSecondCareerCell) && !isSetCell(
                        currentNameCell)
                        && !isSetCell(currentEmailCell) && !isSetCell(
                        currentNewPasswordCell)) {
                        throw new NullPointerException();
                    }
                    currentId = (int) currentIdCell.getNumericCellValue();
                    if (isSetCell(currentCampusCell)) {
                        currentCampus = getIdFromCell(
                            campuses,
                            currentCampusCell
                        );
                        if (currentCampus == null) {
                            throw new WrongCampusCareerException(sr, i);
                        }
                    }
                    if (isSetCell(currentFirstCareerCell)) {
                        currentFirstCareer = getIdFromCell(
                            campuses,
                            currentFirstCareerCell
                        );
                        if (currentFirstCareer == null) {
                            throw new WrongCampusCareerException(sr, i);
                        }
                    }
                    if (isSetCell(currentSecondCareerCell)) {
                        currentSecondCareer = getIdFromCell(
                            campuses,
                            currentSecondCareerCell
                        );
                        if (currentSecondCareer == null) {
                            throw new WrongCampusCareerException(sr, i);
                        }
                        if (currentFirstCareer.equals(currentSecondCareer)) {
                            final String msg =
                                sr.get(Strings.SECOND_CAREER_IQUAL_FIRST_CAREER) + i;
                            throw new Exception(msg);
                        }
                    }
                    if (isSetCell(currentNameCell)) {
                        currentName = currentNameCell.getStringCellValue();
                    }
                    if (isSetCell(currentEmailCell)) {
                        currentEmail =
                            currentEmailCell.getStringCellValue();
                    }
                    if (isSetCell(currentNewPasswordCell)) {
                        currentPassword = RandomPassword.generate(12);
                        currentPasswordRow = passwordsSheet.createRow(k);
                        k++;
                        if (currentEmail == null) {
                            final String msg =
                                sr.get(Strings.PWD_UPDATE_BUT_NO_EMAIL_SET) + i;
                            throw new Exception(msg);
                        }
                        currentPasswordRow.createCell(0)
                                          .setCellValue(currentEmail);
                        currentPasswordRow.createCell(1)
                                          .setCellValue(currentPassword);
                    }
                    updateStudent(
                        connection,
                        currentId,
                        currentCampus,
                        currentFirstCareer,
                        currentSecondCareer,
                        currentName,
                        currentPassword,
                        currentEmail
                    );
                    td.update(i - 1);
                }
            }
            catch (NullPointerException e) {
                final String msg =
                    sr.get(Strings.VAL_MISSING_UPD_STUDENTS) + i;
                connection.rollback();
                throw new SQLException(msg);
            }
            catch (Exception e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
        }
    }

    @Override
    public void deleteStudents(Sheet sheet, TaskDialog<Void> td)
        throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        int i = 0;

        // Validate sheet
        if (!isValidSheetFormat(
            sheet,
            STUDENTS_DELETE_SHEET,
            STUDENTS_DELETE_SHEET_HEADER
        )) {
            throw new InvalidExcelSheetFormatException(sr);
        }
        td.update(i);

        try (Connection connection = user.connect()) {
            connection.setAutoCommit(false);
            try {
                Cell currentIdCell;
                int currentId;
                for (Row row : sheet) {
                    currentIdCell = row.getCell(0);
                    i++;
                    if (row.getRowNum() == 0) {
                        continue;
                    }
                    // Data validation
                    if (!isSetCell(currentIdCell)) {
                        throw new NullPointerException();
                    }
                    currentId = (int) currentIdCell.getNumericCellValue();
                    deleteStudent(connection, currentId);
                    td.update(i - 1);
                }
            }
            catch (NullPointerException e) {
                final String msg =
                    sr.get(Strings.VAL_MISSING_DEL_STUDENTS) + i;
                connection.rollback();
                throw new Exception(msg);
            }
            catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
        }
    }

    @Override
    public void sendPasswords(
        Sheet passwordsSheet,
        TaskDialog<Void> td,
        CancelFlag cancelFlag
    ) throws Exception {
        String currentEmail;
        String currentPassword;
        int i = 0;
        td.update(i);

        try {
            final JSONObject emailData = loadEmailLogin();
            for (Row row : passwordsSheet) {
                if (cancelFlag.isCanceled()) {
                    break;
                }
                if (row.getRowNum() == 0) {
                    continue;
                }
                i++;
                currentEmail = row.getCell(0).getStringCellValue();
                currentPassword = row.getCell(1).getStringCellValue();
                sendPwdEmail(emailData, currentPassword, currentEmail);
                td.update(i);
            }
        }
        catch (FileNotFoundException | JSONException e) {
            final String msg = sr.get(Strings.PROVIDE_EMAIL_INFO);
            throw new Exception(msg);
        }
        catch (NullPointerException e) {
            final String msg = "Corrupted file.";
            throw new Exception(msg);
        }
    }

    @Override
    public void downloadProfessors(
        File file,
        TaskDialog<Void> td,
        CancelFlag cancelFlag
    ) throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }

        try (
            Connection connection = user.connect();
            Statement statement = connection.createStatement()
        ) {
            final ResultSet resultSet = statement.executeQuery(SELECT_PROFESSORS_SQL);
            if (cancelFlag.isCanceled()) {
                return;
            }
            try (Workbook workbook = new XSSFWorkbook()) {
                final Sheet sheet = workbook.createSheet("PROFESSORS");
                final CellStyle headerStyle = workbook.createCellStyle();
                final XSSFFont font =
                    ((XSSFWorkbook) workbook).createFont();
                Cell header;
                Row currentRow;
                int i = 0;
                sheet.setColumnWidth(0, 16000);
                sheet.setColumnWidth(1, 8000);
                sheet.setColumnWidth(2, 12000);
                font.setBold(true);
                headerStyle.setFont(font);
                currentRow = sheet.createRow(i);
                header = currentRow.createCell(0);
                header.setCellStyle(headerStyle);
                header.setCellValue("NAME");
                header = currentRow.createCell(1);
                header.setCellStyle(headerStyle);
                header.setCellValue("TITLE");
                header = currentRow.createCell(2);
                header.setCellStyle(headerStyle);
                header.setCellValue("EMAIL");
                i++;
                while (resultSet.next()) {
                    currentRow = sheet.createRow(i);
                    if (cancelFlag.isCanceled()) {
                        return;
                    }
                    currentRow.createCell(0)
                              .setCellValue(resultSet.getString("name"));
                    currentRow.createCell(1)
                              .setCellValue(resultSet.getString("title"));
                    currentRow.createCell(2)
                              .setCellValue(resultSet.getString("email"));
                    i++;
                }
                if (cancelFlag.isCanceled()) {
                    return;
                }
                workbook.write(new FileOutputStream(file));
            }
        }
    }

    @Override
    public void downloadStudents(
        File file,
        TaskDialog<Void> td,
        CancelFlag cancelFlag
    ) throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        final HashMap<String, Integer> campuses = new HashMap<>();
        final HashMap<String, Integer> careers = new HashMap<>();

        try (
            Connection connection = user.connect();
            Statement statement = connection.createStatement()
        ) {
            final ResultSet resultSet = statement.executeQuery(SELECT_STUDENTS_SQL);
            if (cancelFlag.isCanceled()) {
                return;
            }
            loadHashMaps(connection, campuses, careers);

            try (Workbook workbook = new XSSFWorkbook()) {
                final Map<Integer, String> campusesInv = getInverseMap(campuses);
                final Map<Integer, String> careersInv = getInverseMap(careers);
                final Sheet sheet = workbook.createSheet("STUDENTS");
                final CellStyle headerStyle = workbook.createCellStyle();
                final XSSFFont font = ((XSSFWorkbook) workbook).createFont();
                Cell header;
                Row currentRow;
                String currentCampus;
                String currentCareer;
                String currentSecondCareer;
                Integer currentPhone;
                int i = 0;
                sheet.setColumnWidth(0, 6000);
                sheet.setColumnWidth(1, 8000);
                sheet.setColumnWidth(2, 8000);
                sheet.setColumnWidth(3, 8000);
                sheet.setColumnWidth(4, 16000);
                sheet.setColumnWidth(5, 8000);
                sheet.setColumnWidth(6, 8000);
                font.setBold(true);
                headerStyle.setFont(font);
                currentRow = sheet.createRow(i);
                header = currentRow.createCell(0);
                header.setCellStyle(headerStyle);
                header.setCellValue("STUDENT ID");
                header = currentRow.createCell(1);
                header.setCellStyle(headerStyle);
                header.setCellValue("CAMPUS");
                header = currentRow.createCell(2);
                header.setCellStyle(headerStyle);
                header.setCellValue("FIRST CAREER");
                header = currentRow.createCell(3);
                header.setCellStyle(headerStyle);
                header.setCellValue("SECOND CAREER");
                header = currentRow.createCell(4);
                header.setCellStyle(headerStyle);
                header.setCellValue("NAME");
                header = currentRow.createCell(5);
                header.setCellStyle(headerStyle);
                header.setCellValue("EMAIL");
                header = currentRow.createCell(6);
                header.setCellStyle(headerStyle);
                header.setCellValue("PHONE");
                i++;
                if (cancelFlag.isCanceled()) {
                    return;
                }
                while (resultSet.next()) {
                    currentRow = sheet.createRow(i);
                    currentCampus = campusesInv.get(resultSet.getInt(
                        "campus_id"));
                    currentCareer = careersInv.get(resultSet.getInt(
                        "first_career_id"));
                    currentSecondCareer = careersInv.get(resultSet.getInt(
                        "second_career_id"));
                    currentPhone = (resultSet.getInt("phone") != 0)
                                   ? resultSet.getInt("phone")
                                   : null;
                    currentRow.createCell(0)
                              .setCellValue(resultSet.getInt("id"));
                    currentRow.createCell(1).setCellValue(currentCampus);
                    currentRow.createCell(2).setCellValue(currentCareer);
                    currentRow.createCell(3)
                              .setCellValue(currentSecondCareer);
                    currentRow.createCell(4)
                              .setCellValue(resultSet.getString("name"));
                    currentRow.createCell(5)
                              .setCellValue(resultSet.getString("email"));
                    if (currentPhone != null) {
                        currentRow.createCell(6).setCellValue(currentPhone);
                    }
                    i++;
                    if (cancelFlag.isCanceled()) {
                        return;
                    }
                }
                if (cancelFlag.isCanceled()) {
                    return;
                }
                workbook.write(new FileOutputStream(file));
            }
        }
    }

    @Override
    public void saveLoginKey(File file, String keyUserPassword)
        throws Exception {
        if (!user.isSet() || keyUserPassword == null) {
            throw new Exception("Not connected");
        }
        // TODO ADD NEW IMPL this used to be encrypted, move to some JWT
        //  approach now
        Config.save(file.toPath(), "user", user.getUser());
    }

    @Override
    public void startNewTerm(String name, JSONArray targets) throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        try (Connection connection = user.connect()) {
            final int termId;
            final Savepoint savepoint;
            connection.setAutoCommit(false);
            savepoint = connection.setSavepoint();
            // Insert the record
            try (
                PreparedStatement ps = connection.prepareStatement(
                    INSERT_TERM_SQL)
            ) {
                final Date date = new Date();
                ps.setString(1, name);
                ps.setDate(2, new java.sql.Date(date.getTime()));
                ps.executeUpdate();
            }
            catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
            // Retrieve term ID
            try (
                final PreparedStatement ps = connection.prepareStatement(
                    SELECT_TERM_ID_SQL)
            ) {
                final ResultSet resultSet;
                ps.setString(1, name);
                resultSet = ps.executeQuery();
                if (resultSet.next()) {
                    termId = resultSet.getInt("id");
                }
                else {
                    throw new SQLException("Not created");
                }
            }
            catch (SQLException e) {
                connection.rollback(savepoint);
                throw e;
            }
            // Able careers to access to it
            for (int i = 0; i < targets.length(); i++) {
                try (
                    PreparedStatement ps = connection.prepareStatement(
                        INSERT_TERM_CAREER_SQL)
                ) {
                    ps.setInt(1, termId);
                    ps.setInt(2, targets.getJSONObject(i).getInt("campus"));
                    ps.setInt(3, targets.getJSONObject(i).getInt("career"));
                    ps.executeUpdate();
                }
                catch (SQLException e) {
                    connection.rollback(savepoint);
                    throw e;
                }
            }
            connection.commit();
        }
    }

    @Override
    public void finishTerm(String term) throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        final String selectSQL = "SELECT campus_id, career_id, results "
                                 + "FROM "
                                 + "current_term_result WHERE term_id = ?";
        final String deleteSQL = "DELETE FROM current_term WHERE name = ?";
        final int termId;
        final java.sql.Date termOpenDate;

        try (Connection connection = user.connect()) {
            // Get term id
            try (PreparedStatement ps = connection.prepareStatement(SELECT_TERM_ID_DATE_SQL)) {
                ps.setString(1, term);
                final ResultSet resultSet = ps.executeQuery();

                if (resultSet.next()) {
                    termId = resultSet.getInt("id");
                    termOpenDate = resultSet.getDate("open_date");
                }
                else {
                    throw new Exception("No term");
                }
            }
            // Save the history
            try (PreparedStatement ps = connection.prepareStatement(selectSQL)) {
                ps.setInt(1, termId);
                final HashMap<Integer, String> campuses = new HashMap<>();
                final HashMap<Integer, String> careers = new HashMap<>();
                final ResultSet resultSet = ps.executeQuery();
                final JSONArray termData = new JSONArray();
                JSONObject currentCareerData;
                String currentCampus;
                String currentCareer;
                String currentDataStr;

                try (Statement statement = connection.createStatement()) {
                    final ResultSet rs = statement.executeQuery("SELECT id, name FROM campus");
                    while (rs.next()) {
                        campuses.put(rs.getInt("id"), rs.getString("name"));
                    }
                }
                try (Statement statement = connection.createStatement()) {
                    final ResultSet rs = statement.executeQuery("SELECT id, name FROM career");
                    while (rs.next()) {
                        careers.put(rs.getInt("id"), rs.getString("name"));
                    }
                }

                while (resultSet.next()) {
                    currentCareerData = new JSONObject();
                    currentCampus = campuses.get(resultSet.getInt("campus_id"));
                    currentCareer = careers.get(resultSet.getInt("career_id"));
                    currentDataStr = resultSet.getString("results");
                    currentCareerData.put("campus", currentCampus);
                    currentCareerData.put("career", currentCareer);
                    if (currentDataStr != null) {
                        currentCareerData.put(
                            "data",
                            new JSONObject(currentDataStr)
                        );
                    }
                    termData.put(currentCareerData);
                }
                // Upload
                uploadResults(term, termOpenDate, termData);
            }
            // Delete the record
            try (PreparedStatement ps = connection.prepareStatement(deleteSQL)) {
                ps.setString(1, term);
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void downloadTermFile(String download, File saveDir)
        throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        final URL url =
            new URL(user.getWebsiteURL() + "/terms/" + download.replace(
                " ",
                "%20"
            ));
        try (
            ReadableByteChannel rbc =
                Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(new File(
                saveDir,
                download
            ))
        ) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    @Override
    public void deleteTermFile(String delete) throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        final URL url = new URL(user.getWebsiteURL()
                                    .toString() + "/term.php");
        final HttpURLConnection connection =
            (HttpURLConnection) url.openConnection();
        final byte[] postDataBytes = ("param=delete&file=" + delete)
            .getBytes(
                StandardCharsets.UTF_8);
        final StringBuilder builder = new StringBuilder();
        final Charset charset = StandardCharsets.UTF_8;
        final InputStream is;
        String currentLine;
        connection.setRequestMethod("POST");
        connection.setRequestProperty(
            "Content-Type",
            "application/x-www-form-urlencoded"
        );
        connection.setRequestProperty(
            "Content-Length",
            String.valueOf(postDataBytes.length)
        );
        connection.setDoOutput(true);
        connection.getOutputStream().write(postDataBytes);
        is = connection.getInputStream();

        try (
            BufferedReader br = new BufferedReader(
                new InputStreamReader(is, charset)
            )
        ) {
            while ((currentLine = br.readLine()) != null) {
                builder.append(currentLine);
            }
        }
        if (!builder.toString().equals("OK")) {
            throw new Exception(builder.toString());
        }
    }

    @Override
    public void logout() throws IOException {
        final File loginFile = new File("user.key");
        final File emailFile = new File("email.key");
        if (!loginFile.delete()) {
            throw new IOException("Couldn't delete login file.");
        }
        emailFile.delete();
        System.exit(0);
    }

    private void uploadResults(
        String term,
        java.sql.Date termOpenDate,
        JSONArray termData
    ) throws Exception {
        if (!user.isSet()) {
            throw new SQLException("Not connected");
        }
        final URL url = new URL(user.getWebsiteURL()
                                    .toString() + "/term.php");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        final String name = term + " " + termOpenDate;
        final String params = "param=term&name=" + name + "&data=" + termData.toString();
        final byte[] postDataBytes = params.getBytes(StandardCharsets.UTF_8);
        final StringBuilder builder = new StringBuilder();
        final Charset charset = StandardCharsets.UTF_8;
        final InputStream is;
        String currentLine;
        connection.setRequestMethod("POST");
        connection.setRequestProperty(
            "Content-Type",
            "application/x-www-form-urlencoded"
        );
        connection.setRequestProperty(
            "Content-Length",
            String.valueOf(postDataBytes.length)
        );
        connection.setDoOutput(true);
        connection.getOutputStream().write(postDataBytes);
        is = connection.getInputStream();
        try (
            BufferedReader br = new BufferedReader(
                new InputStreamReader(
                    is,
                    charset
                )
            )
        ) {
            while ((currentLine = br.readLine()) != null) {
                builder.append(currentLine);
            }
        }
        if (!builder.toString().equals("OK")) {
            throw new Exception(builder.toString());
        }
    }

    private void registerStudent(
        Connection connection, int id, int campus, int career,
        int secondCareer,
        String name, String password, String email
    ) throws SQLException {
        // Register in tables: student, student_first_career,
        // student_second_career (if needed)
        final PreparedStatement ps = connection.prepareStatement(
            INSERT_STUDENT_SQL);
        final PreparedStatement ps1 = connection.prepareStatement(
            INSERT_STUDENT_FC_SQL);
        final String hashedPassword = BCrypt.hashpw(
            password,
            BCrypt.gensalt()
        );
        ps.setInt(1, id);
        ps.setString(2, name);
        ps.setString(3, hashedPassword);
        ps.setString(4, email);
        ps.executeUpdate();
        ps.close();
        ps1.setInt(1, id);
        ps1.setInt(2, campus);
        ps1.setInt(3, career);
        ps1.setString(4, EMPTY_STUDENT_CURRICULUM);
        ps1.executeUpdate();
        ps1.close();
        if (secondCareer != -1) {
            final PreparedStatement ps2 = connection.prepareStatement(INSERT_STUDENT_SC_SQL);
            ps2.setInt(1, id);
            ps2.setInt(2, campus);
            ps2.setInt(3, secondCareer);
            ps2.setString(4, EMPTY_STUDENT_CURRICULUM);
            ps2.executeUpdate();
            ps2.close();
        }
    }

    private void updateStudent(
        Connection connection, int id, int campus, int career,
        int secondCareer,
        String name, String password, String email
    ) throws SQLException {
        String sql = "UPDATE student SET ";
        int currentParam = 1;
        if (campus != -1) {
            sql += "campus_id = ?, ";
        }
        if (career != -1) {
            sql += "career_id = ?, ";
        }
        if (secondCareer != -1) {
            sql += "second_career_id = ?, ";
        }
        if (name != null) {
            sql += "name = ?, ";
        }
        if (password != null) {
            sql += "password = ?, ";
        }
        if (email != null) {
            sql += "email = ?, ";
        }
        if (sql.charAt(sql.length() - 2) == ',') {
            sql = sql.substring(0, sql.length() - 2);
        }
        sql += " WHERE id = ?";
        final PreparedStatement ps = connection.prepareStatement(sql);
        if (campus != -1) {
            ps.setInt(currentParam, campus);
            currentParam++;
        }
        if (career != -1) {
            ps.setInt(currentParam, career);
            currentParam++;
        }
        if (secondCareer != -1) {
            ps.setInt(currentParam, secondCareer);
            currentParam++;
        }
        if (name != null) {
            ps.setString(currentParam, name);
            currentParam++;
        }
        if (password != null) {
            final String hasedPassword = BCrypt.hashpw(
                password,
                BCrypt.gensalt()
            );
            ps.setString(currentParam, hasedPassword);
            currentParam++;
        }
        if (email != null) {
            ps.setString(currentParam, email);
            currentParam++;
        }
        ps.setInt(currentParam, id);
        ps.executeUpdate();
        ps.close();
    }

    private void sendPwdEmail(
        JSONObject emailInfo,
        String userPassword,
        String to
    ) throws JSONException,
             MessagingException {
        final Properties properties = System.getProperties();
        final String subject = sr.get(Strings.PWD_EMAIL_SUBJECT);
        final String msg1 = sr.get(Strings.PWD_EMAIL_MSG_1);
        final String msg2 = sr.get(Strings.PWD_EMAIL_MSG_2);
        final String message = msg1 + userPassword + msg2;
        final String host = emailInfo.getString("host");
        final String user = emailInfo.getString("email");
        final String password = emailInfo.getString("password");
        final int port = emailInfo.getInt("port");
        final Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.user", user);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.startssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.port", port);
        properties.put(
            "mail.smtp.socketFactory.class",
            "javax.net.ssl.SSLSocketFactory"
        );
        properties.put("mail.smtp.socketFactory.fallback", "false");
        final Session session = Session.getDefaultInstance(
            properties,
            authenticator
        );
        final MimeMessage mime = new MimeMessage(session);
        final Transport transport = session.getTransport("smtps");
        mime.setFrom(new InternetAddress(user));
        mime.addRecipient(
            Message.RecipientType.TO,
            new InternetAddress(to)
        );
        mime.setSubject(subject);
        mime.setContent(message, "text/html");
        transport.connect(host, port, user, password);
        transport.sendMessage(mime, mime.getAllRecipients());
        transport.close();
    }

    private static boolean isSetCell(Cell cell) {
        return cell != null && cell.getCellType() != CellType.BLANK;
    }

    private static boolean isStringCell(Cell cell) {
        return isSetCell(cell) && cell.getCellType() == CellType.STRING;
    }

    private static Integer getIdFromCell(
        Map<String, Integer> map, Cell
        cell
    ) {
        return map.get(cell.getStringCellValue().toUpperCase());
    }

    private static boolean isValidSheetFormat(
        Sheet sheet,
        String validSheetName,
        String... validColumns
    ) {
        if (sheet.getPhysicalNumberOfRows() < 1) {
            return false;
        }
        final Row headerRow = sheet.getRow(0);
        Cell currentHeaderCell;
        if (!sheet.getSheetName().equals(validSheetName)) {
            return false;
        }
        for (int i = 0; i < validColumns.length; i++) {
            currentHeaderCell = headerRow.getCell(i);
            if (!isStringCell(currentHeaderCell)) {
                return false;
            }
            if (!currentHeaderCell.getStringCellValue()
                                  .equals(validColumns[i])) {
                return false;
            }
        }
        return true;
    }

    private static void loadHashMaps(
        Connection connection,
        Map<String, Integer> campuses,
        Map<String, Integer> careers
    ) throws SQLException {
        final String sql1 = "SELECT id, name FROM campus";
        final String sql2 = "SELECT id, name FROM career";

        try (Statement statement = connection.createStatement()) {
            final ResultSet rs = statement.executeQuery(sql1);
            int currentId = -1;
            String currentName = null;
            while (rs.next()) {
                currentId = rs.getInt("id");
                currentName = rs.getString("name");
                campuses.put(currentName.toUpperCase(), currentId);
            }
        }

        try (Statement statement = connection.createStatement()) {
            final ResultSet rs = statement.executeQuery(sql2);
            int currentId;
            String currentName;
            while (rs.next()) {
                currentId = rs.getInt("id");
                currentName = rs.getString("name");
                careers.put(currentName.toUpperCase(), currentId);
            }
        }
    }

    private static Map<Integer, String> getInverseMap(
        HashMap<String,
            Integer> map
    ) {
        return map.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getValue,
            Map.Entry::getKey
        ));
    }

    private static void insertNewCampus(
        Connection connection,
        Campus campus
    ) throws SQLException {
        final String password = campus.getPassword();
        final String hashedPassword = BCrypt.hashpw(
            password,
            BCrypt.gensalt()
        );
        final PreparedStatement ps = connection.prepareStatement(INSERT_CAMPUS_SQL);
        ps.setString(1, campus.getName());
        ps.setString(2, hashedPassword);
        ps.executeUpdate();
        ps.close();
    }

    private static void updateCampus(
        Connection connection,
        Campus campus
    ) throws SQLException {
        final String sql;
        final PreparedStatement ps;
        // Don't update password, keep the same
        if (campus.getPassword() == null) {
            sql = "UPDATE campus SET name = ? WHERE name = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, campus.getName());
            ps.setString(2, campus.getOldName());
            ps.executeUpdate();
        }
        // Do update the password
        else {
            sql = "UPDATE campus SET name = ?, password = ? WHERE name = ?";
            ps = connection.prepareStatement(sql);
            final String password = campus.getPassword();
            final String hashedPassword = BCrypt.hashpw(
                password,
                BCrypt.gensalt()
            );
            ps.setString(1, campus.getName());
            ps.setString(2, hashedPassword);
            ps.setString(3, campus.getOldName());
            ps.executeUpdate();
        }
        ps.close();
    }

    private static void deleteCampus(
        Connection connection,
        Campus campus
    ) throws SQLException {
        final PreparedStatement ps = connection.prepareStatement(
            DELETE_CAMPUS_SQL);
        ps.setString(1, campus.getName());
        ps.executeUpdate();
        ps.close();
    }

    private static void insertNewCareer(
        Connection connection,
        Career career
    ) throws SQLException {
        final String password = career.getPassword();
        final String hashedPassword = BCrypt.hashpw(
            password,
            BCrypt.gensalt()
        );
        final PreparedStatement ps = connection.prepareStatement(
            INSERT_CAREER_SQL);
        ps.setString(1, career.getName());
        ps.setString(2, hashedPassword);
        ps.executeUpdate();
        ps.close();
    }

    private static void updateCareer(
        Connection connection,
        Career career
    ) throws SQLException {
        final String sql;
        final PreparedStatement ps;
        // Don't update password, keep the same
        if (career.getPassword() == null) {
            sql = "UPDATE career SET name = ? WHERE name = ?";
            ps = connection.prepareStatement(sql);
            ps.setString(1, career.getName());
            ps.setString(2, career.getOldName());
            ps.executeUpdate();
        }
        // Do update the password
        else {
            sql = "UPDATE career SET name = ?, password = ? WHERE name = ?";
            ps = connection.prepareStatement(sql);
            final String password = career.getPassword();
            final String hashedPassword = BCrypt.hashpw(
                password,
                BCrypt.gensalt()
            );
            ps.setString(1, career.getName());
            ps.setString(2, hashedPassword);
            ps.setString(3, career.getOldName());
            ps.executeUpdate();
        }
        ps.close();
    }

    private static void deleteCareer(
        Connection connection,
        Career career
    ) throws SQLException {
        final PreparedStatement ps = connection.prepareStatement(
            DELETE_CAREER_SQL);
        ps.setString(1, career.getName());
        ps.executeUpdate();
        ps.close();
    }

    private static ArrayList<Item> loadItems(
        String table,
        Connection connection
    ) throws SQLException {
        final ArrayList<Item> items = new ArrayList<>();
        final String sql = "SELECT id, name FROM " + table;
        try (final Statement statement = connection.createStatement()) {
            final ResultSet rs = statement.executeQuery(sql);
            int currentId = -1;
            String currentName = null;
            while (rs.next()) {
                currentId = rs.getInt("id");
                currentName = rs.getString("name");
                items.add(new Item(currentId, currentName));
            }
        }
        return items;
    }

    private static void registerProfessor(
        Connection connection, String name, String email,
        String password
    ) throws SQLException {
        final PreparedStatement ps = connection.prepareStatement(
            INSERT_PROFESSOR_SQL);
        final String hasedPassword = BCrypt.hashpw(
            password,
            BCrypt.gensalt()
        );
        ps.setString(1, name);
        ps.setString(2, hasedPassword);
        ps.setString(3, email);
        ps.executeUpdate();
        ps.close();
    }

    private static void updateProfessor(
        Connection connection, String name, String email,
        String password
    ) throws SQLException {
        final String sql1 = "UPDATE professor SET email = ? WHERE name = ?";
        final String sql2 = "UPDATE professor SET email = ?, password = ? "
                            + "WHERE name = ?";
        if (password == null) {
            final PreparedStatement ps = connection.prepareStatement(sql1);
            ps.setString(1, email);
            ps.setString(2, name);
            ps.executeUpdate();
            ps.close();
        }
        else {
            final PreparedStatement ps = connection.prepareStatement(sql2);
            ps.setString(1, email);
            ps.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            ps.setString(3, name);
            ps.executeUpdate();
            ps.close();
        }
    }

    private static void deleteProfessor(
        Connection connection,
        String name
    ) throws SQLException {
        final PreparedStatement ps = connection.prepareStatement(
            DELETE_PROFESSOR_SQL);
        ps.setString(1, name);
        ps.executeUpdate();
        ps.close();
    }

    private static void deleteStudent(Connection connection, int id) throws
                                                                     SQLException {
        final PreparedStatement ps = connection.prepareStatement(
            DELETE_STUDENT_SQL);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    private static JSONObject getEmailJson() throws IOException {
        final LocalStorageDao dao = new LocalStorageDao();
        final String str = dao.load(Paths.get("email.json"));
        System.out.println(str);
        // TODO TEST
        return new JSONObject(str);
    }

    private static final class RandomPassword {
        private static final String CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        private static SecureRandom getSecureRandom() {
            try {
                return SecureRandom.getInstanceStrong();
            }
            catch (NoSuchAlgorithmException e) {
            }
            return null;
        }

        private static String generate(int length) {
            final SecureRandom sr = getSecureRandom();
            final char[] chars = new char[length];
            int currentRandom;
            for (int i = 0; i < length; i++) {
                if (sr != null) {
                    currentRandom = sr.nextInt(length);
                }
                else {
                    currentRandom = (int) (Math.random() * length);
                }
                chars[i] = CHARS.charAt(currentRandom);
            }
            return new String(chars);
        }
    }
}
