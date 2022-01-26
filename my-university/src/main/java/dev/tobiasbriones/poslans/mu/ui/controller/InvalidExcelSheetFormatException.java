/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.controller;

import dev.tobiasbriones.poslans.mu.Strings;
import engineer.mathsoftware.jdesk.resources.StringResources;

public final class InvalidExcelSheetFormatException extends Exception {
    private static final long serialVersionUID = -1907488742661216149L;

    public InvalidExcelSheetFormatException(StringResources sr) {
        super(sr.get(Strings.INVALID_SHEET_FORMAT));
    }
}
