/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

public final class Professor {
    public enum Title {
        OTHER("Other"),
        LICENTIATE("Licentiate"),
        PHD("PhD"),
        ENGINEER("Engineer"),
        DOCTOR("Doctor of Medicine"),
        ARCHITECT("Architect");

        public static Title fromString(String str) {
            switch (str.toUpperCase()) {
                case "":
                    return OTHER;
                case "OTHER":
                    return OTHER;
                case "LICENTIATE":
                    return LICENTIATE;
                case "PHD":
                    return PHD;
                case "ENGINEER":
                    return ENGINEER;
                case "DOCTOR OF MEDICINE":
                    return DOCTOR;
                case "ARCHITECT":
                    return ARCHITECT;
            }
            return null;
        }

        private final String str;

        Title(String str) {
            this.str = str;
        }

        public String getTitleAbbreviation() {
            switch (str.toUpperCase()) {
                case "":
                    return "";
                case "LICENTIATE":
                    return "LIC";
                case "PHD":
                    return "PhD";
                case "ENGINEER":
                    return "ENG";
                case "DOCTOR":
                    return "MD";
                case "ARCHITECT":
                    return "ARCH";
            }
            return null;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    private final String name;
    private final Title title;

    public Professor(String name, Title title) {
        this.name = name.toUpperCase();
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviationTitleName() {
        return title.getTitleAbbreviation() + " " + name;
    }

    public Title getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return name;
    }
}
