/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

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

abstract class Editor<T> {
    static final int CHANGE_CREATE = 0;
    static final int CHANGE_DELETE = 1;
    private static final String DATA_DIRECTORY = "sections-manager/data/careers";

    interface JSONComparator {
        int compare(JSONObject a, JSONObject b);
    }

    private final String filePath;
    private JSONArray array;

    Editor(String fileName) {
        this.filePath = DATA_DIRECTORY + "/" + fileName;
        this.array = null;
    }

    final JSONArray getArray() {
        return array;
    }

    public final boolean isLoaded() {
        return array != null;
    }

    final int getSize() {
        return (array != null) ? array.length() : -1;
    }

    protected abstract JSONComparator getComparator();

    public final void load() throws Exception {
        check();
        final Path path = Paths.get(".", filePath);
        final String str = new String(Files.readAllBytes(path));
        try {
            this.array = new JSONArray(str);
        }
        catch (JSONException e) {
            clear();
            save();
            throw new Exception("Corrupted file, please use a backup if needed.");
        }
    }

    public final void clear() {
        array = new JSONArray();
    }

    public final void save() throws IOException {
        if (!isLoaded()) {
            throw new IOException("File hasn't been loaded.");
        }
        final String jsonStr;
        jsonStr = sort(array, getComparator()).toString();
        FileWriter fw = null;
        if (jsonStr == null) {
            throw new IOException(
                "An error happened when getting data, it could've been an "
                + "invalid number.");
        }
        try {
            fw = new FileWriter(filePath);
            fw.write(jsonStr);
            fw.flush();
        }
        finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    protected final void check() throws IOException {
        final File data = new File(DATA_DIRECTORY);
        final File file = new File(filePath);
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

    public abstract void load(List<T> list) throws IOException;

    public abstract boolean delete(T delete);

    public abstract void performLastChange(List<T> list);

    private static JSONArray sort(JSONArray array, JSONComparator comparator) {
        final List<JSONObject> values = new ArrayList<>();
        final JSONArray sortedArray = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            values.add(array.getJSONObject(i));
        }
        Collections.sort(values, (a, b) -> comparator.compare(a, b));
        for (int i = 0; i < values.size(); i++) {
            sortedArray.put(values.get(i));
        }
        return sortedArray;
    }
}
