/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

public final class Class {
    private final String code;
    private final String name;
    private final int weight;
    private final int daysPerWeek;
    private final float durationHours;
    private final String tag;

    Class(
        String code,
        String name,
        int weight,
        int daysPerWeek,
        float durationHours,
        String tag
    ) {
        this.code = code.toUpperCase();
        this.name = name.toUpperCase();
        this.weight = weight;
        this.daysPerWeek = daysPerWeek;
        this.durationHours = durationHours;
        this.tag = tag.toUpperCase();
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

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return code + " " + name;
    }
}
