/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.models.career.curriculum;

import dev.tobiasbriones.poslans.mu.models.career.Course;

public final class CourseRequirementsDuplicationException extends Exception {
    private static final long serialVersionUID = -4778118633603748567L;
    private final Course course;
    private final Course duplicatedRequirement;

    CourseRequirementsDuplicationException(Course course, Course duplicated) {
        super();
        this.course = course;
        this.duplicatedRequirement = duplicated;
    }

    public Course getCourse() {
        return course;
    }

    public Course getDuplicatedRequirement() {
        return duplicatedRequirement;
    }
}
