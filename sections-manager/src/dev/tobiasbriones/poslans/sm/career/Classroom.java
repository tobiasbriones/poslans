/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

public final class Classroom {
    private final String description;
    private String building;
    private int classroomNumber;

    Classroom() {
        this("-", -1, "-");
    }

    Classroom(String building, int number, String description) {
        this.building = building.toUpperCase();
        this.classroomNumber = number;
        this.description = description;
    }

    // -------------------- GETTERS -------------------- //
    public String getBuilding() {
        return building;
    }

    // -------------------- SETTERS -------------------- //
    public void setBuilding(String building) {
        this.building = building;
    }

    public int getClassroomNumber() {
        return classroomNumber;
    }

    public void setClassroomNumber(int classroomNumber) {
        this.classroomNumber = classroomNumber;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Building " + building + " #" + classroomNumber;
    }
}
