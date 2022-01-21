/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.current;

// Anti-pattern!
public interface Days {
    int MONDAY = 0;

    int TUESDAY = 1;

    int WEDNESDAY = 2;

    int THURSDAY = 3;

    int FRIDAY = 4;

    int SATURDAY = 5;

    String[] DAYS = {
        "Monday", "Tuesday", "Wednesday",
        "Thursday", "Friday", "Saturday"
    };

    String[] MINI_DAYS = {
        "Mon",
        "Tue",
        "Wed",
        "Thu",
        "Fri",
        "Sat"
    };

    static int fromMiniDayStringToDay(String miniDay) {
        switch (miniDay.trim().toUpperCase()) {
            case "MON":
                return 0;
            case "TUE":
                return 1;
            case "WED":
                return 2;
            case "THU":
                return 3;
            case "FRI":
                return 4;
            case "SAT":
                return 5;
        }
        return -1;
    }
}
