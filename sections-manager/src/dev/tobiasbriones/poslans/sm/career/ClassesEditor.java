/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public final class ClassesEditor extends Editor<Class> {
    private static final String KEY_CODE = "code";
    private static final String KEY_NAME = "name";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_DAYS = "days";
    private static final String KEY_DURATION = "duration";

    public static JSONObject fromClassToJSON(Class Class) {
        return newClassJSON(
            Class.getCode(),
            Class.getName(),
            Class.getWeight(),
            Class.getDaysPerWeek(),
            Class.getDurationHours()
        );
    }

    public static Class fromJSONToClass(JSONObject classJSON) {
        return new Class(
            classJSON.getString(KEY_CODE),
            classJSON.getString(KEY_NAME),
            classJSON.getInt(KEY_WEIGHT),
            classJSON.getInt(KEY_DAYS),
            classJSON.getFloat(KEY_DURATION)
        );
    }
    private int lastChange;
    private Class change;

    public ClassesEditor() {
        super("classes");
        this.lastChange = -1;
        this.change = null;
    }

    @Override
    protected JSONComparator getComparator() {
        return new JSONComparator() {

            @Override
            public int compare(JSONObject a, JSONObject b) {
                return a.getString(KEY_CODE)
                        .compareToIgnoreCase(b.getString(KEY_CODE));
            }
        };
    }

    @Override
    public void load(List<Class> classes) throws IOException {
        final JSONArray array = getArray();
        try {
            for (int i = 0; i < array.length(); i++) {
                classes.add(fromJSONToClass(array.getJSONObject(i)));
            }
        }
        catch (JSONException e) {
            clear();
            save();
            throw new IOException(
                "Corrupted file, please use a backup if needed.");
        }
    }

    @Override
    public boolean delete(Class Class) {
        final JSONArray array = getArray();
        JSONObject current = null;
        for (int i = 0; i < array.length(); i++) {
            current = array.getJSONObject(i);
            if (current.getString(KEY_CODE).equals(Class.getCode())) {
                lastChange = CHANGE_DELETE;
                change = Class;
                array.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void performLastChange(List<Class> list) {
        switch (lastChange) {
            case CHANGE_CREATE:
                final JSONObject json = getArray().getJSONObject(getSize() - 1);
                final Class createdClass = fromJSONToClass(json);
                list.add(createdClass);
                break;
            case CHANGE_DELETE:
                int i = 0;
                for (dev.tobiasbriones.poslans.sm.career.Class Class : list) {
                    if (Class.getCode().equals(change.getCode())) {
                        list.remove(i);
                        break;
                    }
                    i++;
                }
                break;
        }
        lastChange = -1;
        change = null;
    }

    public void create(
        String code,
        String name,
        int weight,
        int days,
        float duration
    ) {
        lastChange = CHANGE_CREATE;
        getArray().put(newClassJSON(code, name, weight, days, duration));
    }

    private static JSONObject newClassJSON(
        String code,
        String name,
        int weight,
        int days,
        float duration
    ) {
        final JSONObject classJSON = new JSONObject();
        classJSON.put(KEY_CODE, code);
        classJSON.put(KEY_NAME, name);
        classJSON.put(KEY_WEIGHT, weight);
        classJSON.put(KEY_DAYS, days);
        classJSON.put(KEY_DURATION, duration);
        return classJSON;
    }
}
