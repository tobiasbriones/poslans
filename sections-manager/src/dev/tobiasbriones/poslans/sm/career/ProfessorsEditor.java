/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public final class ProfessorsEditor extends Editor<Professor> {
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_TITLE = "title";
    private static final String KEY_SPECIALIZATION = "specialization";

    public static JSONObject fromProfessorToJSON(Professor professor) {
        return newProfessorJSON(
            professor.getId(),
            professor.getName(),
            professor.getTitle(),
            professor.getSpecialization()
        );
    }

    public static Professor fromJSONToProfessor(JSONObject json) {
        return new Professor(
            json.getInt(KEY_ID),
            json.getString(KEY_NAME),
            Professor.Title.values()[json.getInt(KEY_TITLE)],
            json.getString(KEY_SPECIALIZATION)
        );
    }

    private int lastChange;
    private Professor change;

    public ProfessorsEditor() {
        super("professors");
        this.lastChange = -1;
        this.change = null;
    }

    @Override
    protected Editor.JSONComparator getComparator() {
        return new Editor.JSONComparator() {

            @Override
            public int compare(JSONObject a, JSONObject b) {
                return a.getString(KEY_NAME)
                        .compareToIgnoreCase(b.getString(KEY_NAME));
            }
        };
    }

    @Override
    public void load(List<Professor> professors) throws IOException {
        final JSONArray array = getArray();
        try {
            for (int i = 0; i < array.length(); i++) {
                professors.add(fromJSONToProfessor(array.getJSONObject(i)));
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
    public boolean delete(Professor professor) {
        final JSONArray array = getArray();
        JSONObject current = null;
        for (int i = 0; i < array.length(); i++) {
            current = array.getJSONObject(i);
            if (current.getString(KEY_NAME).equals(professor.getName())) {
                lastChange = CHANGE_DELETE;
                change = professor;
                array.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void performLastChange(List<Professor> list) {
        switch (lastChange) {
            case CHANGE_CREATE:
                final Professor professorCreated =
                    fromJSONToProfessor(getArray().getJSONObject(
                        getSize() - 1));
                list.add(professorCreated);
                break;
            case CHANGE_DELETE:
                int i = 0;
                for (Professor professor : list) {
                    if (professor.getName().equals(change.getName())) {
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
        int id,
        String name,
        Professor.Title title,
        String specialization
    ) {
        lastChange = CHANGE_CREATE;
        getArray().put(newProfessorJSON(id, name, title, specialization));
    }

    private static JSONObject newProfessorJSON(
        int id,
        String name,
        Professor.Title title,
        String specialization
    ) {
        final JSONObject professorJSON = new JSONObject();
        professorJSON.put(KEY_ID, id);
        professorJSON.put(KEY_NAME, name);
        professorJSON.put(KEY_TITLE, title.ordinal());
        professorJSON.put(KEY_SPECIALIZATION, specialization);
        return professorJSON;
    }
}
