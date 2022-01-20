/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

public final class Class {
    private final String code;
    private final String name;
    private final int weight;
    private final int daysPerWeek;
    private final float durationHours;

    Class(
        String code,
        String name,
        int weight,
        int daysPerWeek,
        float durationHours
    ) {
        this.code = code.toUpperCase();
        this.name = name.toUpperCase();
        this.weight = weight;
        this.daysPerWeek = daysPerWeek;
        this.durationHours = durationHours;
    }

    // -------------------- GETTERS -------------------- //
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public int getDaysPerWeek() {
        return daysPerWeek;
    }

    public float getDurationHours() {
        return durationHours;
    }

    @Override
    public String toString() {
        return code + " " + name;
    }
}
