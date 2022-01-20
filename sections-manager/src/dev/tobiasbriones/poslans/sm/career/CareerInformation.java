/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class CareerInformation {
    private static final String DATA_DIRECTORY = "data";
    private static final String FILE_PATH = DATA_DIRECTORY + "/info";
    private static final String KEY_UNIVERSITY = "university";
    private static final String KEY_CAREER = "career";

    public static void saveInformation(String university, String career) throws
                                                                         IOException {
        final JSONObject json = new JSONObject();
        FileWriter fw = null;
        check();
        json.put(KEY_UNIVERSITY, university);
        json.put(KEY_CAREER, career);
        try {
            fw = new FileWriter(FILE_PATH);
            fw.write(json.toString());
            fw.flush();
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    public static void loadInformation(String[] careerInfo) throws IOException {
        check();
        final Path path = Paths.get(".", FILE_PATH);
        final String str = new String(Files.readAllBytes(path));
        final JSONObject json;
        try {
            json = new JSONObject(str);
            careerInfo[0] = json.getString(KEY_UNIVERSITY);
            careerInfo[1] = json.getString(KEY_CAREER);
        }
        catch (JSONException e) {
            careerInfo[0] = "";
            careerInfo[1] = "";
        }
    }

    private static void check() throws IOException {
        final File data = new File(DATA_DIRECTORY);
        final File file = new File(FILE_PATH);
        if (!data.exists() || !data.isDirectory()) {
            if (!data.mkdir()) {
                throw new IOException("Error creating directory " + data);
            }
        }
        if (!file.exists() || !file.isFile()) {
            if (!file.createNewFile()) {
                throw new IOException("Error creating file " + file);
            }
        }
    }
}
