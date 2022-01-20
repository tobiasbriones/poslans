/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.current;

import dev.tobiasbriones.poslans.sm.career.*;
import dev.tobiasbriones.poslans.sm.career.Class;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class SectionsEditor {
    private static final String DATA_DIRECTORY = "sections-manager/data";
    private static final String CURRENT_SECTIONS_FILE_PATH = DATA_DIRECTORY + "/current";
    private static final String KEY_CLASS = "class";
    private static final String KEY_DAYS = "days";
    private static final String KEY_CLASSROOM = "classroom";
    private static final String KEY_TIME = "time";
    private static final String KEY_PROFESSOR = "professor";
    private JSONArray array;

    public SectionsEditor() {
        this.array = null;
    }

    public boolean isLoaded() {
        return array != null;
    }

    public int getSize() {
        return (array != null) ? array.length() : -1;
    }

    public void load() throws IOException {
        check();
        final Path path = Paths.get(".", CURRENT_SECTIONS_FILE_PATH);
        final String str = new String(Files.readAllBytes(path));
        try {
            this.array = new JSONArray(str);
        }
        catch (JSONException e) {
            clear();
            save();
            throw new IOException(
                "Corrupted file, please use a backup if needed and restart "
                + "the app.");
        }
    }

    public void load(List<Section> sections) throws IOException {
        try {
            for (int i = 0; i < array.length(); i++) {
                sections.add(newSection(array.getJSONObject(i)));
            }
        }
        catch (JSONException e) {
            clear();
            save();
            throw new IOException(
                "Corrupted file, please use a backup if needed and restart "
                + "the app.");
        }
    }

    public Section openSection(
        Class Class,
        int[] days,
        Classroom classroom,
        Time time,
        Professor professor
    ) {
        if (!isLoaded()) {
            throw new RuntimeException("Sections file hasn't been loaded yet.");
        }
        final JSONObject sectionJSON = new JSONObject();
        final JSONObject timeJSON = new JSONObject();
        timeJSON.put("hour", time.getHour());
        timeJSON.put("minute", time.getMinute());
        sectionJSON.put(KEY_CLASS, ClassesEditor.fromClassToJSON(Class));
        sectionJSON.put(KEY_DAYS, new JSONArray(days));
        sectionJSON.put(
            KEY_CLASSROOM,
            ClassroomsEditor.fromClassroomToJSON(classroom)
        );
        sectionJSON.put(KEY_TIME, timeJSON);
        sectionJSON.put(
            KEY_PROFESSOR,
            ProfessorsEditor.fromProfessorToJSON(professor)
        );
        array.put(sectionJSON);
        return new Section(Class, days, classroom, time, professor);
    }

    public void editSection(
        int position, Class Class, int[] days, Classroom classroom, Time time,
        Professor professor
    ) {
        if (!isLoaded()) {
            throw new RuntimeException("Sections file hasn't been loaded yet.");
        }
        final JSONObject sectionJSON = array.getJSONObject(position);
        final JSONObject timeJSON = new JSONObject();
        timeJSON.put("hour", time.getHour());
        timeJSON.put("minute", time.getMinute());
        sectionJSON.put(KEY_CLASS, ClassesEditor.fromClassToJSON(Class));
        sectionJSON.put(KEY_DAYS, new JSONArray(days));
        sectionJSON.put(
            KEY_CLASSROOM,
            ClassroomsEditor.fromClassroomToJSON(classroom)
        );
        sectionJSON.put(KEY_TIME, timeJSON);
        sectionJSON.put(
            KEY_PROFESSOR,
            ProfessorsEditor.fromProfessorToJSON(professor)
        );
    }

    public boolean delete(int position) {
        return array.remove(position) != null;
    }

    public void clear() {
        array = new JSONArray();
    }

    public void save() throws IOException {
        final String jsonStr;
        if (array == null) {
            throw new IOException("File hasn't been loaded.");
        }
        array = sort(array);
        jsonStr = array.toString();
        FileWriter fw = null;
        if (jsonStr == null) {
            throw new IOException(
                "An error happened when getting data, it could've been an "
                + "invalid number.");
        }
        try {
            fw = new FileWriter(CURRENT_SECTIONS_FILE_PATH);
            fw.write(jsonStr);
            fw.flush();
        }
        finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    private void check() throws IOException {
        final File data = new File(DATA_DIRECTORY);
        final File file = new File(CURRENT_SECTIONS_FILE_PATH);
        if (!data.exists() || !data.isDirectory()) {
            if (!data.mkdir()) {
                throw new IOException("Error creating directory " + data);
            }
        }
        if (!file.exists() || !file.isFile()) {
            if (!file.createNewFile()) {
                throw new IOException("Error creating file " + file);
            }
            array = new JSONArray();
            save();
        }
    }

    private static Section newSection(JSONObject sectionJSON) {
        final JSONArray daysArray = sectionJSON.getJSONArray(KEY_DAYS);
        final int[] days = new int[daysArray.length()];
        for (int i = 0; i < days.length; i++) {
            days[i] = daysArray.getInt(i);
        }
        return new Section(
            ClassesEditor.fromJSONToClass(sectionJSON.getJSONObject(KEY_CLASS)),
            days,
            ClassroomsEditor.fromJSONToClassroom(sectionJSON.getJSONObject(
                KEY_CLASSROOM)),
            new Time(
                sectionJSON.getJSONObject(KEY_TIME).getInt("hour"),
                sectionJSON.getJSONObject(KEY_TIME).getInt("minute")
            ),
            ProfessorsEditor.fromJSONToProfessor(sectionJSON.getJSONObject(
                KEY_PROFESSOR))
        );
    }

    private static JSONArray sort(JSONArray array) {
        final JSONArray sortedArray = new JSONArray();
        final List<JSONObject> values = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            values.add(array.getJSONObject(i));
        }
        Collections.sort(values, (sectionA, sectionB) -> {
            final int order;
            try {
                final JSONObject a = sectionA.getJSONObject(KEY_TIME);
                final JSONObject b = sectionB.getJSONObject(KEY_TIME);
                final int timeA = (a.getInt("hour") * 60) + a.getInt("minute");
                final int timeB = (b.getInt("hour") * 60) + b.getInt("minute");
                order = (timeA < timeB) ? -1 : ((timeA > timeB) ? 1 : 0);
            }
            catch (JSONException e) {
                return -1;
            }
            return order;
        });
        for (int i = 0; i < values.size(); i++) {
            sortedArray.put(values.get(i));
        }
        return sortedArray;
    }
}
