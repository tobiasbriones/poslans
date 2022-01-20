/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm;

import javax.swing.*;

abstract class Application implements ApplicationMessageReporter {
    Application() {}

    protected abstract JFrame getWindow();

    // -------------------- MESSAGE REPORTER -------------------- //
    @Override
    public void showInfoMessage(String msg) {
        JOptionPane.showMessageDialog(
            getWindow(),
            msg,
            "Information",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void showErrorMessage(String msg) {
        JOptionPane.showMessageDialog(
            getWindow(),
            msg,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
    public void showErrorMessage(String msg, Exception e) {
        final String errorMsg = msg + "\n" + e.getMessage();
        JOptionPane.showMessageDialog(
            getWindow(),
            errorMsg,
            "Info",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void showUnexpectedErrorMessage(String msg, Exception e) {
        final String message = "An unexpected error occurred, if the problem "
                               + "persists please "
                               + "use the restore tool or contact us.\n"
                               + msg + "\n" + e.getMessage();
        JOptionPane.showMessageDialog(
            getWindow(),
            message,
            "Unexpected error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
