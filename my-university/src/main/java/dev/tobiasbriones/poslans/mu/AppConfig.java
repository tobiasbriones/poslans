/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu;

import engineer.mathsoftware.jdesk.Config;
import engineer.mathsoftware.jdesk.io.IOFile;

import java.io.IOException;

public final class AppConfig {
    public static final String[] LANGUAGES_CODES = { "EN", "ES" };
    private static final IOFile CONFIG_FILE = new IOFile("config.properties");
    private static final String LANGUAGE_KEY = "language";

    public static String getLanguage() {
        try {
            return Config.get(CONFIG_FILE, LANGUAGE_KEY, LANGUAGES_CODES[0]);
        }
        catch (IOException e) {
            return LANGUAGES_CODES[0];
        }
    }

    public static void setLanguage(String language) throws IOException {
        Config.save(CONFIG_FILE, LANGUAGE_KEY, language);
    }

    private AppConfig() {}
}
