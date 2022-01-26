/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.models.career.curriculum;

import dev.tobiasbriones.poslans.mu.models.career.Course;

public final class CourseDuplicationException extends Exception {
    private static final long serialVersionUID = -4778118633603748567L;

    CourseDuplicationException(Course course) {
        super("Course was already added " + course);
    }
}
