/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.current;

import java.io.File;

public final class HistoryItem {
    private final File file;

    public HistoryItem(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
