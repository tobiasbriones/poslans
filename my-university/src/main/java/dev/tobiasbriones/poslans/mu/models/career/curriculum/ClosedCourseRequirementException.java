/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.models.career.curriculum;

import dev.tobiasbriones.poslans.mu.models.career.Course;

public final class ClosedCourseRequirementException extends Exception {
    private static final long serialVersionUID = 7641479438904420731L;
    private final Course course;
    private final Course closedRequirement;

    ClosedCourseRequirementException(Course course, Course closed) {
        super();
        this.course = course;
        this.closedRequirement = closed;
    }

    public Course getCourse() {
        return course;
    }

    public Course getClosedRequirement() {
        return closedRequirement;
    }
}
