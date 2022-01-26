/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.models.career;

import dev.tobiasbriones.poslans.mu.Strings;
import dev.tobiasbriones.poslans.mu.ui.editing.FieldValidationException;
import dev.tobiasbriones.poslans.mu.ui.editing.Model;
import dev.tobiasbriones.poslans.mu.ui.editing.State;
import engineer.mathsoftware.jdesk.resources.StringResources;

public final class Course extends Model {
    public static void validate(
        StringResources sr,
        String code,
        String name,
        int credits
    ) throws FieldValidationException {
        if(code.length() > 25) throw new FieldValidationException(sr.get(Strings.VALIDATION_COURSE_CODE));
        if(name.length() > 70) throw new FieldValidationException(sr.get(Strings.VALIDATION_COURSE_NAME));
        if(credits < 0 || credits > 255) throw new FieldValidationException(sr.get(Strings.VALIDATION_COURSE_CREDITS));
    }

    private final int careerId;
    private final int id;
    private String code;
    private String name;
    private int credits;
    private State state;

    public Course(int id, int careerId, String code, String name, int credits) {
        super(true);
        this.careerId = careerId;
        this.id = id;
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.state = State.NONE;
    }

    public Course(int careerId, int id) {
        super(true);
        this.careerId = careerId;
        this.id = id;
        this.code = null;
        this.name = null;
        this.credits = -1;
        this.state = State.NONE;
    }

    public Course(int careerId) {
        this(careerId, -1);
        this.state = State.NEW;
    }

    public int getCareerId() {
        return careerId;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getCredits() {
        return credits;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return ((code != null && name != null) ? code + " " + name : "");
    }

    public void set(String code, String name, int credits) {
        this.code = code;
        this.name = name;
        this.credits = credits;
    }
}
