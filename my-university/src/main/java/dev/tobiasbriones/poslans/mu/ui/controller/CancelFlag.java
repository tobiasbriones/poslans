/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.controller;

public final class CancelFlag {
    private boolean cancel;

    public CancelFlag() {
        this.cancel = false;
    }

    public boolean isCanceled() {
        return cancel;
    }

    public void set(boolean cancel) {
        this.cancel = cancel;
    }
}
