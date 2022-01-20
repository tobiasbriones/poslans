/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm;

import org.json.JSONArray;
import org.json.JSONException;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

final class AppConfig {
    private static final String APP_CONFIG_FILE = "sections-manager/properties.prop";
    private static final String APP_CONFIG_PROP_FILTER = "filter";

    private AppConfig() {}

    static void checkFile() {
        final File file = new File(APP_CONFIG_FILE);
        if (!file.exists() || !file.isFile()) {
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }

    static void saveNoFilter() throws IOException {
        final Properties properties = new Properties();
        properties.load(new FileInputStream(new File(APP_CONFIG_FILE)));
        properties.put(APP_CONFIG_PROP_FILTER, new JSONArray().toString());
        properties.store(new FileOutputStream(new File(APP_CONFIG_FILE)), null);
    }

    static void saveFilter(List<String> filter) throws IOException {
        final Properties properties = new Properties();
        properties.load(new FileInputStream(new File(APP_CONFIG_FILE)));
        properties.put(
            APP_CONFIG_PROP_FILTER,
            new JSONArray(filter).toString()
        );
        properties.store(new FileOutputStream(new File(APP_CONFIG_FILE)), null);
    }

    static JSONArray loadFilter() throws IOException {
        final Properties properties = new Properties();
        properties.load(new FileInputStream(new File(APP_CONFIG_FILE)));
        try {
            return new JSONArray(properties.getProperty(
                APP_CONFIG_PROP_FILTER,
                "[]"
            ));
        }
        catch (JSONException e) {
            saveNoFilter();
        }
        return new JSONArray();
    }
}
