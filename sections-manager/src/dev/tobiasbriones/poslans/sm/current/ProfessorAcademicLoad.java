/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.current;

import dev.tobiasbriones.poslans.sm.career.Professor;

import java.util.*;

public final class ProfessorAcademicLoad {
    public static Enumeration<ProfessorAcademicLoad> get(
        List<Section> sections,
        List<Professor> professors
    ) {
        final Vector<ProfessorAcademicLoad> list = new Vector<>();
        String name = null;
        int load = 0;
        for (Professor professor : professors) {
            name = professor.getName();
            for (Section section : sections) {
                if (section.getProfessor().getName().equals(name)) {
                    load += section.getSectionClass().getWeight();
                }
            }
            list.addElement(new ProfessorAcademicLoad(name, load));
            load = 0;
        }
        Collections.sort(list, new Comparator<ProfessorAcademicLoad>() {

            @Override
            public int compare(
                ProfessorAcademicLoad o1,
                ProfessorAcademicLoad o2
            ) {
                return (o1.getLoad() < o2.getLoad())
                       ? 1
                       : ((o1.getLoad() > o2.getLoad()) ? -1 : 0);
            }
        });
        return list.elements();
    }
    private final String name;
    private final int load;

    ProfessorAcademicLoad(String name, int load) {
        this.name = name;
        this.load = load;
    }

    public String getName() {
        return name;
    }

    public int getLoad() {
        return load;
    }

    @Override
    public String toString() {
        return name + " has a load of " + load;
    }
}
