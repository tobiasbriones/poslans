/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu;

import dev.tobiasbriones.poslans.mu.database.User;
import dev.tobiasbriones.poslans.mu.ui.CareerCoursesWindow;
import dev.tobiasbriones.poslans.mu.ui.controller.CCoursesWController;
import dev.tobiasbriones.poslans.mu.ui.controller.MWController;
import engineer.mathsoftware.jdesk.Window;

final class MWCCallback implements MWController.Callback {
    private final Main main;

    MWCCallback(Main main) {
        this.main = main;
    }

    @Override
    public void openCCACWindow() {
        final User user = main.getUser();
        final CCoursesWCCallback cccCallback = new CCoursesWCCallback(main);
        final CCoursesWController ccwc = new CCoursesWController(
            user,
            cccCallback
        );
        final Window ccacw = new CareerCoursesWindow(main, ccwc);
        main.addWindow(ccacw);
    }
}
