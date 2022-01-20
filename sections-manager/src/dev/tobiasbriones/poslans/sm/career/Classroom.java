/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

public final class Classroom {
    private String building;
    private int classroomNumber;

    Classroom() {
        this("-", -1);
    }

    Classroom(String building, int number) {
        this.building = building.toUpperCase();
        this.classroomNumber = number;
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

    @Override
    public String toString() {
        return "Building " + building + " #" + classroomNumber;
    }
}
