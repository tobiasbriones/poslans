/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

import dev.tobiasbriones.poslans.sm.ui.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public final class ProfessorSpecializationEditor extends Editor<String> {
    private static final String KEY_NAME = "name";
    private int lastChange;
    private String change;

    public ProfessorSpecializationEditor() {
        super("professor_specializations");
        this.lastChange = -1;
        this.change = null;
    }

    @Override
    protected JSONComparator getComparator() {
        return new JSONComparator() {

            @Override
            public int compare(JSONObject a, JSONObject b) {
                return a.getString(KEY_NAME)
                        .compareToIgnoreCase(b.getString(KEY_NAME));
            }
        };
    }

    @Override
    public void load(List<String> specializations) throws IOException {
        final JSONArray array = getArray();
        try {
            for (int i = 0; i < array.length(); i++) {
                specializations.add(array.getJSONObject(i).getString(KEY_NAME));
            }
        }
        catch (JSONException e) {
            clear();
            save();
            throw new IOException(Strings.CORRUPTED_FILE);
        }
    }

    @Override
    public boolean delete(String specialization) {
        final JSONArray array = getArray();
        JSONObject current = null;
        for (int i = 0; i < array.length(); i++) {
            current = array.getJSONObject(i);
            if (current.getString(KEY_NAME).equals(specialization)) {
                lastChange = CHANGE_DELETE;
                change = specialization;
                array.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void performLastChange(List<String> list) {
        switch (lastChange) {
            case CHANGE_CREATE:
                final String specializationCreated = getArray().getJSONObject(
                    getSize() - 1).getString(KEY_NAME);
                list.add(specializationCreated);
                break;
            case CHANGE_DELETE:
                int i = 0;
                for (String specialization : list) {
                    if (specialization.equals(change)) {
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

    public void create(String specialization) {
        lastChange = CHANGE_CREATE;
        final JSONObject json = new JSONObject();
        json.put(KEY_NAME, specialization.toUpperCase());
        getArray().put(json);
    }
}
