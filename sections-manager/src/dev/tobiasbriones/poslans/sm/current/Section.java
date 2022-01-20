/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.current;

import dev.tobiasbriones.poslans.sm.career.Class;
import dev.tobiasbriones.poslans.sm.career.Classroom;
import dev.tobiasbriones.poslans.sm.career.Professor;

public final class Section implements Days {
    /**
     * Take into account the following facts: - The hour is the same for all the
     * days - If the class is updated or changed, it will need to be deleted and
     * created again - Time is military format
     */
    private final dev.tobiasbriones.poslans.sm.career.Class Class;
    private final int[] days;
    private final Classroom classroom;
    private final Time time;
    private final Time endTime;
    private final Professor professor;

    Section(
        Class Class,
        int[] days,
        Classroom classroom,
        Time time,
        Professor professor
    ) {
        this.Class = Class;
        this.days = days;
        this.classroom = classroom;
        this.time = time;
        this.endTime = endTime();
        this.professor = professor;
    }

    // -------------------- GETTERS -------------------- //
    public Class getSectionClass() {
        return Class;
    }

    public int[] getDays() {
        return days;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public Time getTime() {
        return time;
    }

    public Time getEndTime() {
        return endTime;
    }

    public Professor getProfessor() {
        return professor;
    }

    public String getMiniDays() {
        String str = "";
        int i = 0;
        for (int day : days) {
            str += MINI_DAYS[day];
            if (i < days.length - 1) {
                str += ", ";
            }
            i++;
        }
        return str;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(Class);
        sb.append("\n");
        for (int day : days) {
            sb.append(DAYS[day]);
            sb.append(", ");
        }
        sb.append("\n");
        sb.append(classroom);
        sb.append(" ");
        sb.append(time);
        sb.append(" ");
        sb.append(professor);
        return sb.toString();
    }

    // -------------------- PUBLIC METHODS -------------------- //
    // overlap iff (same hour & same day) & (same classroom | same professor)
    public boolean overlaps(Section section) {
        final boolean timeOverlap = timeOverlap(section);
        final boolean dayOverlap = dayOverlap(section);
        final boolean classRoomOverlap = classRoomOverlap(section);
        final boolean professorOverlap = professorOverlap(section);
        return ((timeOverlap && dayOverlap) && (classRoomOverlap || professorOverlap))
               ? true
               : false;
    }

    // -------------------- PRIVATE METHODS -------------------- //
    private Time endTime() {
        int hour;
        int minute;
        float duration = Class.getDurationHours();
        if (duration == ((int) duration)) {
            hour = time.getHour() + ((int) duration);
            minute = time.getMinute();
        }
        else {
            final int h = (int) Math.floor(duration);
            final int m = (int) Math.floor((duration - (float) h) * 60.0f);
            hour = time.getHour() + h;
            minute = time.getMinute() + m;
            if (minute > 59) {
                hour += (int) Math.floor(minute / 60);
                minute = Math.floorMod(minute, 60);
            }
        }
        if (hour >= 24) {
            hour -= 24;
        }
        return new Time(hour, minute);
    }

    private boolean timeOverlap(Section section) {
        return section.getTime().hasOverlap(section.endTime(), time, endTime);
    }

    private boolean dayOverlap(Section section) {
        for (int sectionDay : section.getDays()) {
            for (int day : days) {
                if (sectionDay == day) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean classRoomOverlap(Section section) {
        return (
                   section.getClassroom().getBuilding()
                          .equals(classroom.getBuilding())
                   && section.getClassroom()
                             .getClassroomNumber() == classroom.getClassroomNumber()
               ) ? true : false;
    }

    private boolean professorOverlap(Section section) {
        return section.getProfessor().getName().equals(professor.getName());
    }
}
