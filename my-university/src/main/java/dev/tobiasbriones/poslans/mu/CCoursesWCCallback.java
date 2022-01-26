/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu;

import dev.tobiasbriones.poslans.mu.database.User;
import dev.tobiasbriones.poslans.mu.ui.CareerCurriculumWindow;
import dev.tobiasbriones.poslans.mu.ui.controller.CCoursesWController;
import dev.tobiasbriones.poslans.mu.ui.controller.CCurriculumWController;
import engineer.mathsoftware.jdesk.Window;

final class CCoursesWCCallback implements CCoursesWController.Callback {
    private final Main main;

    CCoursesWCCallback(Main main) {
        this.main = main;
    }

    @Override
    public void openCCEW(Window window, int careerId) {
        final User user = main.getUser();
        final CCurriculumWController cCurriculumWController =
            new CCurriculumWController(
            user,
            careerId
        );
        final CareerCurriculumWindow cCurriculumWindow =
            new CareerCurriculumWindow(
            main,
            window,
            cCurriculumWController
        );
        main.addWindow(cCurriculumWindow);
    }
}
