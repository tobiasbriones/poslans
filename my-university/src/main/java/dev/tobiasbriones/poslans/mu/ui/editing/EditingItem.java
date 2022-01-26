/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.editing;

public class EditingItem {
    private final boolean isNew;
    private final String oldName;
    private String name;
    private String password;
    private boolean isUpdated;
    private boolean isDeleted;

    public EditingItem(boolean isNew, String oldName, String name) {
        this.isNew = isNew;
        this.oldName = oldName;
        this.name = name;
        this.password = null;
        this.isUpdated = false;
        this.isDeleted = false;
    }

    public final boolean isNew() {
        return isNew;
    }

    public final String getOldName() {
        return oldName;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
        if (!isNew && !isDeleted) {
            this.isUpdated = true;
        }
    }

    public final String getPassword() {
        return password;
    }

    public final void setPassword(String password) {
        this.password = password;
        if (!isNew && !isDeleted) {
            this.isUpdated = true;
        }
    }

    public final boolean isUpdated() {
        return isUpdated;
    }

    public final boolean isDeleted() {
        return isDeleted;
    }

    public final void setDeleted() {
        this.isUpdated = false;
        this.isDeleted = true;
    }
}
