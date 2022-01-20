/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

import java.util.ArrayList;
import java.util.List;

public final class CareerDataHolder {
    private final List<Class> classes;
    private final List<Professor> professors;
    private final List<Classroom> classrooms;

    public CareerDataHolder() {
        this.classes = new ArrayList<>();
        this.professors = new ArrayList<>();
        this.classrooms = new ArrayList<>();
    }

    public boolean isEmpty() {
        return classes.isEmpty() && professors.isEmpty() && classrooms.isEmpty();
    }

    public List<Class> getClasses() {
        return classes;
    }

    public List<Professor> getProfessors() {
        return professors;
    }

    public List<Classroom> getClassrooms() {
        return classrooms;
    }

    public void clear() {
        classes.clear();
        professors.clear();
        classrooms.clear();
    }
}
