/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.current;

public final class InvalidTimeException extends RuntimeException {
    private static final long serialVersionUID = 2298195749418597905L;

    InvalidTimeException() {
        // TODO Auto-generated constructor stub
    }

    InvalidTimeException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    InvalidTimeException(Throwable arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    InvalidTimeException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    InvalidTimeException(
        String arg0,
        Throwable arg1,
        boolean arg2,
        boolean arg3
    ) {
        super(arg0, arg1, arg2, arg3);
        // TODO Auto-generated constructor stub
    }

    InvalidTimeException(int hour, int minute) {
        super("Invalid time config " + hour + ":" + minute);
    }
}
