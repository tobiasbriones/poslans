/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm;

public interface ApplicationMessageReporter {

    public abstract void showInfoMessage(String msg);

    public abstract void showErrorMessage(String msg);

    public abstract void showErrorMessage(String msg, Exception e);

    public abstract void showUnexpectedErrorMessage(String msg, Exception e);
}
