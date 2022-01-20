/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.current;

public final class Time {
    public static Time fromString(String str) {
        if (str == null) {
            return null;
        }
        final int hour;
        final int minute;
        final Time time;
        try {
            if (str.contains(":")) {
                final String hourStr = str.substring(0, str.indexOf(":"));
                final String minuteStr = str.substring(
                    str.indexOf(":") + 1,
                    str.length()
                );
                hour = Integer.parseInt(hourStr);
                minute = Integer.parseInt(minuteStr);
            }
            else {
                hour = Integer.parseInt(str);
                minute = 0;
            }
            time = new Time(hour, minute);
        }
        catch (NumberFormatException | InvalidTimeException e) {
            System.out.println(e);
            return null;
        }
        return time;
    }

    private int hour;
    private int minute;

    public Time() {
        this(0, 0);
    }

    public Time(int hour, int minute) {
        set(hour, minute);
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    @Override
    public String toString() {
        final String h = (hour > 9)
                         ? String.valueOf(hour)
                         : "0" + String.valueOf(hour);
        final String m = (minute > 9)
                         ? String.valueOf(minute)
                         : "0" + String.valueOf(minute);
        return h + ":" + m;
    }

    public void set(int hour, int minute) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new InvalidTimeException(hour, minute);
        }
        this.hour = hour;
        this.minute = minute;
    }

    // Checks overlapping of this time in the given interval, returns true
    // iff they're disjoint (exclusive from endTime)
    public boolean hasOverlap(Time endTime0, Time startTime1, Time endTime1) {
        final int n0 = (hour * 60) + minute;
        final int n1 = (endTime0.getHour() * 60) + endTime0.getMinute();
        final int n2 = (startTime1.getHour() * 60) + startTime1.getMinute();
        final int n3 = (endTime1.getHour() * 60) + endTime1.getMinute();
        return ((n2 >= n0 && n2 < n1) || (n3 > n0 && n3 <= n1)) ? true : false;
    }
}
