/*
 * Copyright (c) 2018 Tobias Briones. All rights reserved.
 */

DROP DATABASE IF EXISTS university;

CREATE DATABASE university COLLATE utf8mb4_unicode_ci;

USE university;

CREATE TABLE campus (
    id       SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name     VARCHAR(70)       NOT NULL,
    password CHAR(60)          NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (name)
) ENGINE = INNODB;

CREATE TABLE career_classification (
    id          SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name        VARCHAR(70)       NOT NULL,
    description TEXT              NOT NULL,
    PRIMARY KEY (id)
) ENGINE = INNODB;

CREATE TABLE career (
    id                SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    classification_id SMALLINT UNSIGNED,
    name              VARCHAR(70)       NOT NULL,
    password          CHAR(60)          NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (classification_id)
        REFERENCES career_classification (id)
        ON DELETE SET NULL,
    UNIQUE (name)
) ENGINE = INNODB;

CREATE TABLE course (
    id        INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    career_id SMALLINT UNSIGNED NOT NULL,
    code      VARCHAR(25)       NOT NULL,
    name      VARCHAR(70)       NOT NULL,
    credits   TINYINT UNSIGNED  NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (career_id)
        REFERENCES career (id)
        ON DELETE CASCADE,
    UNIQUE (code)
) ENGINE = INNODB;

CREATE TABLE course_node (
    career_id        SMALLINT UNSIGNED   NOT NULL,
    course_id        INT UNSIGNED        NOT NULL,
    level            TINYINT UNSIGNED    NOT NULL,
    required_courses TEXT                NOT NULL,
    next_courses     TEXT                NOT NULL,
    is_mandatory     TINYINT(1) UNSIGNED NOT NULL,
    INDEX (level),
    FOREIGN KEY (career_id)
        REFERENCES career (id)
        ON DELETE CASCADE,
    FOREIGN KEY (course_id)
        REFERENCES course (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE professor (
    id          INT UNSIGNED NOT NULL AUTO_INCREMENT,
    name        VARCHAR(70)  NOT NULL,
    password    CHAR(60)     NOT NULL,
    email       VARCHAR(150) NOT NULL,
    personal_id BIGINT       DEFAULT NULL,
    title       VARCHAR(35)  DEFAULT NULL,
    studies_at  VARCHAR(255) DEFAULT NULL,
    titles      VARCHAR(255) DEFAULT NULL,
    description VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (email)
) ENGINE = INNODB;

CREATE TABLE professor_career_conversation (
    id           INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    campus_id    SMALLINT UNSIGNED NOT NULL,
    career_id    SMALLINT UNSIGNED NOT NULL,
    professor_id INT UNSIGNED      NOT NULL,
    last_message VARCHAR(50)      DEFAULT NULL,
    unread       TINYINT UNSIGNED DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (campus_id)
        REFERENCES campus (id),
    FOREIGN KEY (career_id)
        REFERENCES career (id)
        ON DELETE CASCADE,
    FOREIGN KEY (professor_id)
        REFERENCES professor (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE professor_career_message (
    conversation_id INT UNSIGNED NOT NULL,
    sent_date       DATETIME     NOT NULL,
    content         TEXT         NOT NULL,
    is_media        TINYINT(1) UNSIGNED DEFAULT 0,
    FOREIGN KEY (conversation_id)
        REFERENCES professor_career_conversation (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE professor_scientific_research (
    id           INT UNSIGNED NOT NULL AUTO_INCREMENT,
    professor_id INT UNSIGNED NOT NULL,
    name         VARCHAR(70)  NOT NULL,
    description  VARCHAR(255) NOT NULL,
    tags         VARCHAR(150) NOT NULL,
    link         VARCHAR(512) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (professor_id)
        REFERENCES professor (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE student (
    id            BIGINT UNSIGNED NOT NULL,
    name          VARCHAR(70)     NOT NULL,
    password      CHAR(60)        NOT NULL,
    email         VARCHAR(150)    NOT NULL,
    date_of_birth DATE         DEFAULT NULL,
    phone         VARCHAR(20)  DEFAULT NULL,
    skills        VARCHAR(150) DEFAULT NULL,
    languages     VARCHAR(150) DEFAULT NULL,
    UNIQUE (id),
    UNIQUE (email)
) ENGINE = INNODB;

CREATE TABLE student_career_conversation (
    id           INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    campus_id    SMALLINT UNSIGNED NOT NULL,
    career_id    SMALLINT UNSIGNED NOT NULL,
    student_id   BIGINT UNSIGNED   NOT NULL,
    last_message VARCHAR(50)      DEFAULT NULL,
    unread       TINYINT UNSIGNED DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (campus_id)
        REFERENCES campus (id),
    FOREIGN KEY (career_id)
        REFERENCES career (id)
        ON DELETE CASCADE,
    FOREIGN KEY (student_id)
        REFERENCES student (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE student_career_message (
    conversation_id INT UNSIGNED NOT NULL,
    sent_date       DATETIME     NOT NULL,
    content         TEXT         NOT NULL,
    is_media        TINYINT(1) UNSIGNED DEFAULT 0,
    FOREIGN KEY (conversation_id)
        REFERENCES student_career_conversation (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE student_conversation (
    id           INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    student1_id  BIGINT UNSIGNED NOT NULL,
    student2_id  BIGINT UNSIGNED NOT NULL,
    last_message VARCHAR(50)      DEFAULT NULL,
    unread       TINYINT UNSIGNED DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (student1_id)
        REFERENCES student (id)
        ON DELETE CASCADE,
    FOREIGN KEY (student2_id)
        REFERENCES student (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE student_enroll (
    student_id BIGINT UNSIGNED   NOT NULL,
    campus_id  SMALLINT UNSIGNED NOT NULL,
    career_id  SMALLINT UNSIGNED NOT NULL,
    FOREIGN KEY (student_id)
        REFERENCES student (id)
        ON DELETE CASCADE,
    FOREIGN KEY (campus_id)
        REFERENCES campus (id)
        ON DELETE CASCADE,
    FOREIGN KEY (career_id)
        REFERENCES career (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE student_message (
    conversation_id INT UNSIGNED NOT NULL,
    sent_date       DATETIME     NOT NULL,
    content         TEXT         NOT NULL,
    is_media        TINYINT(1) UNSIGNED DEFAULT 0,
    FOREIGN KEY (conversation_id)
        REFERENCES student_conversation (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE student_scientific_research (
    id          INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    student_id  BIGINT UNSIGNED NOT NULL,
    name        VARCHAR(70)     NOT NULL,
    description VARCHAR(255)    NOT NULL,
    tags        VARCHAR(150)    NOT NULL,
    link        VARCHAR(512)    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (student_id)
        REFERENCES student (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE student_professor_conversation (
    id           INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    professor_id INT UNSIGNED    NOT NULL,
    student_id   BIGINT UNSIGNED NOT NULL,
    last_message VARCHAR(50)      DEFAULT NULL,
    unread       TINYINT UNSIGNED DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (professor_id)
        REFERENCES professor (id)
        ON DELETE CASCADE,
    FOREIGN KEY (student_id)
        REFERENCES student (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE student_professor_message (
    conversation_id INT UNSIGNED NOT NULL,
    sent_date       DATETIME     NOT NULL,
    content         TEXT         NOT NULL,
    is_media        TINYINT(1) UNSIGNED DEFAULT 0,
    FOREIGN KEY (conversation_id)
        REFERENCES student_professor_conversation (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE term (
    id          SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)       NOT NULL,
    open_date   DATETIME          NOT NULL,
    finish_date DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (name)
) ENGINE = INNODB;

CREATE TABLE current_term (
    term_id   SMALLINT UNSIGNED NOT NULL,
    campus_id SMALLINT UNSIGNED NOT NULL,
    career_id SMALLINT UNSIGNED NOT NULL,
    FOREIGN KEY (term_id)
        REFERENCES term (id)
        ON DELETE CASCADE,
    FOREIGN KEY (campus_id)
        REFERENCES campus (id)
        ON DELETE CASCADE,
    FOREIGN KEY (career_id)
        REFERENCES career (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE term_chat_group (
    id            SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    term_id       SMALLINT UNSIGNED NOT NULL,
    professor_id  INT UNSIGNED      NOT NULL,
    name          VARCHAR(50)       NOT NULL,
    creation_date DATE              NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (term_id)
        REFERENCES term (id),
    FOREIGN KEY (professor_id)
        REFERENCES professor (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE term_chat_group_member (
    id         INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    group_id   SMALLINT UNSIGNED NOT NULL,
    student_id BIGINT UNSIGNED   NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (group_id)
        REFERENCES term_chat_group (id)
        ON DELETE CASCADE,
    FOREIGN KEY (student_id)
        REFERENCES student (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE term_chat_group_message (
    group_id     SMALLINT UNSIGNED NOT NULL,
    professor_id INT UNSIGNED,
    student_id   BIGINT UNSIGNED,
    sent_date    DATE              NOT NULL,
    content      TEXT              NOT NULL,
    is_media     TINYINT(1) UNSIGNED DEFAULT 0,
    FOREIGN KEY (group_id)
        REFERENCES term_chat_group (id)
        ON DELETE CASCADE,
    FOREIGN KEY (professor_id)
        REFERENCES professor (id)
        ON DELETE CASCADE,
    FOREIGN KEY (student_id)
        REFERENCES student (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE student_current_term_enroll (
    student_id BIGINT UNSIGNED   NOT NULL,
    course_id  INT UNSIGNED      NOT NULL,
    term_id    SMALLINT UNSIGNED NOT NULL,
    grade      TINYINT UNSIGNED DEFAULT NULL,
    FOREIGN KEY (student_id)
        REFERENCES student (id)
        ON DELETE CASCADE,
    FOREIGN KEY (course_id)
        REFERENCES course (id)
        ON DELETE CASCADE,
    FOREIGN KEY (term_id)
        REFERENCES term (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE student_curriculum (
    student_id BIGINT UNSIGNED   NOT NULL,
    course_id  INT UNSIGNED      NOT NULL,
    term_id    SMALLINT UNSIGNED NOT NULL,
    grade      TINYINT UNSIGNED  NOT NULL,
    FOREIGN KEY (student_id)
        REFERENCES student (id)
        ON DELETE CASCADE,
    FOREIGN KEY (course_id)
        REFERENCES course (id)
        ON DELETE CASCADE,
    FOREIGN KEY (term_id)
        REFERENCES term (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE university_info (
    id               TINYINT UNSIGNED DEFAULT 1,
    name             VARCHAR(70)      DEFAULT '',
    description      VARCHAR(500)     DEFAULT '',
    phones           VARCHAR(500)     DEFAULT NULL,
    email            VARCHAR(150)     DEFAULT '',
    campus_addresses TEXT             DEFAULT NULL,
    webpage          VARCHAR(300)     DEFAULT ''
) ENGINE = MYISAM;

CREATE TABLE university_message (
    id        SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    sent_date DATETIME          NOT NULL,
    content   TEXT              NOT NULL,
    is_media  TINYINT(1) UNSIGNED DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE = INNODB;

CREATE TABLE user_chat_group (
    id            INT UNSIGNED NOT NULL AUTO_INCREMENT,
    name          VARCHAR(50)  NOT NULL,
    creation_date DATE         NOT NULL,
    PRIMARY KEY (id)
) ENGINE = INNODB;

CREATE TABLE user_chat_group_member (
    id           INT UNSIGNED NOT NULL AUTO_INCREMENT,
    group_id     INT UNSIGNED NOT NULL,
    professor_id INT UNSIGNED,
    student_id   BIGINT UNSIGNED,
    PRIMARY KEY (id),
    FOREIGN KEY (group_id)
        REFERENCES user_chat_group (id)
        ON DELETE CASCADE,
    FOREIGN KEY (professor_id)
        REFERENCES professor (id)
        ON DELETE CASCADE,
    FOREIGN KEY (student_id)
        REFERENCES student (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE user_chat_group_message (
    group_id     INT UNSIGNED NOT NULL,
    professor_id INT UNSIGNED,
    student_id   BIGINT UNSIGNED,
    sent_date    DATE         NOT NULL,
    content      TEXT         NOT NULL,
    is_media     TINYINT(1) UNSIGNED DEFAULT 0,
    FOREIGN KEY (group_id)
        REFERENCES user_chat_group (id)
        ON DELETE CASCADE,
    FOREIGN KEY (professor_id)
        REFERENCES professor (id)
        ON DELETE CASCADE,
    FOREIGN KEY (student_id)
        REFERENCES student (id)
        ON DELETE CASCADE
) ENGINE = INNODB;

CREATE TABLE student_first_career (
    id              INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    student_id      BIGINT UNSIGNED   NOT NULL,
    campus_id       SMALLINT UNSIGNED NOT NULL,
    career_id       SMALLINT UNSIGNED NOT NULL,
    student_classes VARCHAR(5000),
    PRIMARY KEY (id),
    FOREIGN KEY (student_id)
        REFERENCES student (id),
    FOREIGN KEY (campus_id)
        REFERENCES campus (id),
    FOREIGN KEY (career_id)
        REFERENCES career (id)
) ENGINE = INNODB;

CREATE TABLE student_second_career (
    id              INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    student_id      BIGINT UNSIGNED   NOT NULL,
    campus_id       SMALLINT UNSIGNED NOT NULL,
    career_id       SMALLINT UNSIGNED NOT NULL,
    student_classes VARCHAR(5000),
    PRIMARY KEY (id),
    FOREIGN KEY (student_id)
        REFERENCES student (id),
    FOREIGN KEY (campus_id)
        REFERENCES campus (id),
    FOREIGN KEY (career_id)
        REFERENCES career (id)
) ENGINE = INNODB;
