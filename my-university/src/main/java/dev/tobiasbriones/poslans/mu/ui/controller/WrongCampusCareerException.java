/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui.controller;

import dev.tobiasbriones.poslans.mu.Strings;
import engineer.mathsoftware.jdesk.resources.StringResources;

class WrongCampusCareerException extends Exception {
    private static final long serialVersionUID = 2133528227065077249L;

    WrongCampusCareerException(StringResources sr, int row) {
        super(sr.get(Strings.WRONG_CAMPUS_CAREER) + row);
    }
}
