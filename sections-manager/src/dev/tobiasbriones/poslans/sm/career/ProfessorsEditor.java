/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

import dev.tobiasbriones.poslans.sm.career.Professor.Title;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public final class ProfessorsEditor extends Editor<Professor> {
    private static final String KEY_NAME = "name";
    private static final String KEY_TITLE = "title";

    public static JSONObject fromProfessorToJSON(Professor professor) {
        return newProfessorJSON(professor.getName(), professor.getTitle());
    }

    public static Professor fromJSONToProfessor(JSONObject json) {
        return new Professor(
            json.getString(KEY_NAME),
            Title.values()[json.getInt(KEY_TITLE)]
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
    protected JSONComparator getComparator() {
        return (a, b) -> a.getString(KEY_NAME)
                          .compareToIgnoreCase(b.getString(KEY_NAME));
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

    public void create(String name, Title title) {
        lastChange = CHANGE_CREATE;
        getArray().put(newProfessorJSON(name, title));
    }

    private static JSONObject newProfessorJSON(String name, Title title) {
        final JSONObject professorJSON = new JSONObject();
        professorJSON.put(KEY_NAME, name);
        professorJSON.put(KEY_TITLE, title.ordinal());
        return professorJSON;
    }
}
