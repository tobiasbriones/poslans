/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.editing;

public class Model {
    private final boolean isEditable;
    private State state;

    /**
     * Constructs a new model.
     *
     * @param isEditable flag to set weather this model can be edited. The
     *                   subclass will have to check {@link #isEditable()}
     *                   before setting a field of the model
     */
    public Model(boolean isEditable) {
        this.isEditable = isEditable;
    }

    /**
     * Returns {@code true} if and only if this model is editable.
     *
     * @return {@code true} if and only if this model is editable
     */
    public final boolean isEditable() {
        return isEditable;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
