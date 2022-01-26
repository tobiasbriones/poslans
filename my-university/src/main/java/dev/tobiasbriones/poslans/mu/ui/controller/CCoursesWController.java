/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.controller;

import dev.tobiasbriones.poslans.mu.database.User;
import dev.tobiasbriones.poslans.mu.models.career.Course;
import dev.tobiasbriones.poslans.mu.ui.CareerCoursesWindow;
import engineer.mathsoftware.jdesk.Window;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class CCoursesWController implements CareerCoursesWindow.Controller {
    public interface Callback {
        void openCCEW(Window window, int careerId);
    }

    private final User user;
    private final Callback callback;
    private final LinkedHashSet<Course> changes;
    private int careerInEdition;

    public CCoursesWController(User user, Callback callback) {
        this.changes = new LinkedHashSet<>();
        this.callback = callback;
        this.user = user;
        this.careerInEdition = -1;
    }

    // -------------------- EDITING CONTROLLER -------------------- //
    @Override
    public boolean setEditingCareer(int careerInEdition) {
        if (hasChanges()) {
            return false;
        }
        this.careerInEdition = careerInEdition;
        return true;
    }

    @Override
    public boolean addToCourseChanges(Course change) {
        if (careerInEdition == -1) {
            return false;
        }
        changes.add(change);
        return true;
    }

    @Override
    public boolean hasChanges() {
        return !changes.isEmpty();
    }

    @Override
    public void saveChanges() throws SQLException {
        if (careerInEdition == -1) {
            throw new SQLException("Career not selected");
        }
        final String insertSQL = "INSERT INTO course(career_id, code, name, "
                                 + "credits) VALUES(?, ?, ?, ?)";
        final String updateSQL = "UPDATE course SET code = ?, name = ?, "
                                 + "credits = ? WHERE id = ?";
        final String deleteSQL = "DELETE FROM course WHERE id = ?";
        String currentSQL;

        try (Connection connection = user.connect()) {
            connection.setAutoCommit(false);
            for (Course course : changes) {
                switch (course.getState()) {
                    case DELETE:
                        currentSQL = deleteSQL;
                        break;
                    case EDITED:
                        currentSQL = updateSQL;
                        break;
                    case NEW:
                        currentSQL = insertSQL;
                        break;
                    default:
                        continue;
                }
                try (
                    PreparedStatement ps = connection.prepareStatement(currentSQL)
                ) {
                    switch (course.getState()) {
                        case DELETE:
                            ps.setInt(1, course.getId());
                            break;
                        case EDITED:
                            ps.setString(1, course.getCode());
                            ps.setString(2, course.getName());
                            ps.setInt(3, course.getCredits());
                            ps.setInt(4, course.getId());
                            break;
                        case NEW:
                            ps.setInt(1, course.getCareerId());
                            ps.setString(2, course.getCode());
                            ps.setString(3, course.getName());
                            ps.setInt(4, course.getCredits());
                            break;
                        default:
                            continue;
                    }
                    ps.executeUpdate();
                }
                catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }
            connection.commit();
        }
        changes.clear();
        careerInEdition = -1;
    }

    @Override
    public void discardChanges() {
        careerInEdition = -1;
        changes.clear();
    }

    // -------------------- CONTROLLER -------------------- //
    @Override
    public void openCCEW(Window window) {
        if (careerInEdition == -1) {
            return;
        }
        callback.openCCEW(window, careerInEdition);
    }

    @Override
    public List<Item> listCareerItems() throws SQLException {
        try (Connection connection = user.connect()) {
            return MWController.loadItems("career", connection);
        }
    }

    @Override
    public List<Course> listCareerCourses(Item career) throws SQLException {
        final List<Course> courses = new ArrayList<>();
        final int careerId = career.id;

        try (Connection connection = user.connect()) {
            loadCourses(connection, careerId, courses, true);
        }
        return courses;
    }

    @Override
    public boolean isValidUserPassword(String password) {
        return user.getPassword().equals(password);
    }

    static void loadCourses(
        Connection connection,
        int careerId,
        List<Course> courses,
        boolean isEditable
    ) throws SQLException {
        final String sql = "SELECT * FROM course WHERE career_id = " + careerId;
        try (Statement statement = connection.createStatement()) {
            final ResultSet rs = statement.executeQuery(sql);
            int currentId;
            String currentCode;
            String currentName;
            int currentCredits;
            Course currentCourse;

            while (rs.next()) {
                currentId = rs.getInt("id");
                currentCode = rs.getString("code");
                currentName = rs.getString("name");
                currentCredits = rs.getInt("credits");
                if (isEditable) {
                    currentCourse = new Course(careerId, currentId);
                    currentCourse.set(currentCode, currentName, currentCredits);
                }
                else {
                    currentCourse = new Course(
                        careerId,
                        currentId,
                        currentCode,
                        currentName,
                        currentCredits
                    );
                }
                courses.add(currentCourse);
            }
        }
    }
}
