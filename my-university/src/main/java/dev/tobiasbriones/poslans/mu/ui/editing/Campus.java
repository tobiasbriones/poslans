/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.editing;

public final class Campus extends EditingItem {
    public Campus(boolean isNew, String oldName, String name) {
        super(isNew, oldName, name);
    }

    public Campus(boolean isNew, String oldName, String name, String password) {
        super(isNew, oldName, name);
        setPassword(password);
    }

    @Override
    public String toString() {
        return getName();
    }
}
