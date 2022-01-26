/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.models.career.curriculum;

import dev.tobiasbriones.poslans.mu.models.career.Course;

public final class InvalidCourseRequirementException extends Exception {
    private static final long serialVersionUID = 7641479438904420731L;
    private final Course course;
    private final Course invalidRequirement;

    InvalidCourseRequirementException(Course course, Course invalid) {
        super();
        this.course = course;
        this.invalidRequirement = invalid;
    }

    public Course getCourse() {
        return course;
    }

    public Course getInvalidRequirement() {
        return invalidRequirement;
    }
}
