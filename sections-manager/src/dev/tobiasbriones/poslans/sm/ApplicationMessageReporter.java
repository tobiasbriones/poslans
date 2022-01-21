/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm;

public interface ApplicationMessageReporter {

    void showInfoMessage(String msg);

    void showErrorMessage(String msg);

    void showErrorMessage(String msg, Exception e);

    void showUnexpectedErrorMessage(String msg, Exception e);
}
