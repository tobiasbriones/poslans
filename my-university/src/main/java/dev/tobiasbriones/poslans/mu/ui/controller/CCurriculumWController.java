/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.controller;

import dev.tobiasbriones.poslans.mu.database.User;
import dev.tobiasbriones.poslans.mu.models.career.Course;
import dev.tobiasbriones.poslans.mu.models.career.curriculum.CareerCurriculum;
import dev.tobiasbriones.poslans.mu.ui.CareerCurriculumWindow;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public final class CCurriculumWController implements CareerCurriculumWindow.Controller {
    private final User user;
    private final int careerId;
    private final Map<String, Course> courses;

    public CCurriculumWController(User user, int careerId) {
        this.user = user;
        this.careerId = careerId;
        this.courses = new HashMap<>();
    }

    @Override
    public void load() throws SQLException {
        courses.clear();
        try (
            Connection connection = user.connect();
            Statement statement = connection.createStatement()
        ) {
            final String sql =
                "SELECT * FROM course WHERE career_id = " + careerId;
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
                currentCourse = new Course(
                    careerId,
                    currentId,
                    currentCode,
                    currentName,
                    currentCredits
                );
                courses.put(currentCode, currentCourse);
            }
        }
    }

    @Override
    public CareerCurriculum retrieveCurriculum() throws SQLException {
        try (
            Connection connection = user.connect();
            Statement statement = connection.createStatement()
        ) {
            final String sql = "SELECT course_id, level, required_courses, "
                               + "next_courses FROM course_node WHERE "
                               + "career_id = " + careerId;
            final ResultSet resultSet = statement.executeQuery(sql);
            return CareerCurriculum.newCurriculum("", courses, resultSet);
        }
    }

    @Override
    public void saveCurriculum() throws SQLException {
        // TODO Auto-generated method stub
    }

    @Override
    public Map<String, Course> getCourses() {
        return java.util.Collections.unmodifiableMap(courses);
    }
}
