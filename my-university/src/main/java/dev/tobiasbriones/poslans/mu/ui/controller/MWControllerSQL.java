/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.controller;

final class MWControllerSQL {
    static final String INSERT_CAMPUS_SQL = "INSERT INTO campus(name, "
                                            + "password) VALUES(?, ?)";
    static final String DELETE_CAMPUS_SQL = "DELETE FROM campus WHERE name = ?";
    static final String INSERT_CAREER_SQL = "INSERT INTO career(name, "
                                            + "password) VALUES(?, ?)";
    static final String DELETE_CAREER_SQL = "DELETE FROM career WHERE name = ?";
    static final String INSERT_PROFESSOR_SQL = "INSERT INTO professor(name, "
                                               + "password, email) VALUES(?, "
                                               + "?, ?)";
    static final String DELETE_PROFESSOR_SQL = "DELETE FROM professor WHERE "
                                               + "name = ?";
    static final String INSERT_STUDENT_SQL = "INSERT INTO student(id, name, "
                                             + "password, email) VALUES(?, ?,"
                                             + " ?, ?)";
    static final String INSERT_STUDENT_FC_SQL = "INSERT INTO "
                                                + "student_first_career"
                                                + "(student_id, campus_id, "
                                                + "career_id, "
                                                + "student_classes) VALUES(?,"
                                                + " ?, ?, ?)";
    static final String INSERT_STUDENT_SC_SQL = "INSERT INTO "
                                                + "student_second_career"
                                                + "(student_id, campus_id, "
                                                + "career_id, "
                                                + "student_classes) VALUES(?,"
                                                + " ?, ?, ?)";
    static final String DELETE_STUDENT_SQL = "DELETE FROM student WHERE id = ?";
    static final String INSERT_TERM_SQL = "INSERT INTO current_term(name, "
                                          + "open_date) VALUES(?, ?)";
    static final String SELECT_TERM_ID_SQL = "SELECT id FROM current_term "
                                             + "WHERE name = ?";
    static final String SELECT_TERM_ID_DATE_SQL = "SELECT id, open_date FROM "
                                                  + "current_term WHERE name "
                                                  + "= ?";
    static final String INSERT_TERM_CAREER_SQL = "INSERT INTO "
                                                 + "current_term_result"
                                                 + "(term_id, campus_id, "
                                                 + "career_id) VALUES(?, ?, ?)";
    static final String SELECT_PROFESSORS_SQL = "SELECT name, title, email "
                                                + "FROM professor";
    static final String SELECT_STUDENTS_SQL = "SELECT id, campus_id, "
                                              + "first_career_id, "
                                              + "second_career_id, name, "
                                              + "email, phone FROM student";
    static final String PROFESSORS_REGISTER_SHEET = "PROFESSORS TO REGISTER";
    static final String PROFESSORS_UPDATE_SHEET = "PROFESSORS TO UPDATE";
    static final String PROFESSORS_DELETE_SHEET = "PROFESSORS TO DELETE";
    static final String STUDENTS_REGISTER_SHEET = "STUDENTS TO REGISTER";
    static final String STUDENTS_UPDATE_SHEET = "STUDENTS TO UPDATE";
    static final String STUDENTS_DELETE_SHEET = "STUDENTS TO DELETE";
    static final String EMPTY_STUDENT_CURRICULUM = "{'passed':[], "
                                                   + "'current':[], "
                                                   + "'next':[], 'failed':[]}";
    static final String[] PROFESSORS_REGISTER_SHEET_HEADER = {
        "NAME",
        "EMAIL"
    };
    static final String[] PROFESSORS_UPDATE_SHEET_HEADER = {
        "NAME",
        "NEW EMAIL",
        "GENERATE NEW PASSWORD? Leave empty if the answer is NO"
    };
    static final String[] PROFESSORS_DELETE_SHEET_HEADER = { "NAME" };
    static final String[] STUDENTS_REGISTER_SHEET_HEADER = {
        "STUDENT ID",
        "CAMPUS",
        "FIRST CAREER",
        "SECOND CAREER",
        "NAME",
        "EMAIL"
    };
    static final String[] STUDENTS_UPDATE_SHEET_HEADER = {
        "STUDENT ID",
        "NEW CAMPUS",
        "NEW FIRST CAREER",
        "NEW SECOND CAREER",
        "NEW NAME",
        "NEW EMAIL",
        "GENERATE NEW PASSWORD? Leave empty if the answer is NO"
    };
    static final String[] STUDENTS_DELETE_SHEET_HEADER = { "STUDENT ID" };

    private MWControllerSQL() {}
}
