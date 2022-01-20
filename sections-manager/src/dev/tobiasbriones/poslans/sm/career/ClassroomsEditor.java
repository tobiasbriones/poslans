/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.career;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public final class ClassroomsEditor extends Editor<Classroom> {
    private static final String KEY_BUILDING = "building";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_DESCRIPTION = "description";

    public static JSONObject fromClassroomToJSON(Classroom classroom) {
        return newClassroomJSON(
            classroom.getBuilding(),
            classroom.getClassroomNumber(),
            classroom.getDescription()
        );
    }

    public static Classroom fromJSONToClassroom(JSONObject json) {
        return new Classroom(
            json.getString(KEY_BUILDING),
            json.getInt(KEY_NUMBER),
            json.getString(KEY_DESCRIPTION)
        );
    }

    private int lastChange;
    private Classroom change;

    public ClassroomsEditor() {
        super("classrooms");
        this.lastChange = -1;
        this.change = null;
    }

    @Override
    protected Editor.JSONComparator getComparator() {
        return (a, b) -> {
            final String ba = a.getString(KEY_BUILDING);
            final String bb = b.getString(KEY_BUILDING);
            final String na = a.get(KEY_NUMBER).toString();
            final String nb = b.get(KEY_NUMBER).toString();
            return (ba + na).compareToIgnoreCase(bb + nb);
        };
    }

    @Override
    public void load(List<Classroom> classrooms) throws IOException {
        final JSONArray array = getArray();
        try {
            for (int i = 0; i < array.length(); i++) {
                classrooms.add(fromJSONToClassroom(array.getJSONObject(i)));
            }
        }
        catch (JSONException e) {
            clear();
            save();
            throw new IOException("Corrupted file, please use a backup if needed.");
        }
    }

    @Override
    public boolean delete(Classroom classroom) {
        final JSONArray array = getArray();
        JSONObject current = null;
        for (int i = 0; i < array.length(); i++) {
            current = array.getJSONObject(i);
            if (current.getString(KEY_BUILDING)
                       .equals(classroom.getBuilding()) && current.getInt(
                KEY_NUMBER) == classroom.getClassroomNumber()
            ) {
                lastChange = CHANGE_DELETE;
                change = classroom;
                array.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void performLastChange(List<Classroom> list) {
        switch (lastChange) {
            case CHANGE_CREATE:
                final JSONObject json =
                    getArray().getJSONObject(getArray().length() - 1);
                final Classroom createdClassroom = fromJSONToClassroom(json);
                list.add(createdClassroom);
                break;
            case CHANGE_DELETE:
                int i = 0;
                for (Classroom classroom : list) {
                    if (classroom.getBuilding()
                                 .equals(change.getBuilding()) && classroom.getClassroomNumber() == change.getClassroomNumber()
                    ) {
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
        String building,
        int classroomNumber,
        String description
    ) {
        lastChange = CHANGE_CREATE;
        getArray().put(newClassroomJSON(
            building,
            classroomNumber,
            description
        ));
    }

    private static JSONObject newClassroomJSON(
        String building,
        int classroomNumber,
        String description
    ) {
        final JSONObject classroomJSON = new JSONObject();
        classroomJSON.put(KEY_BUILDING, building);
        classroomJSON.put(KEY_NUMBER, classroomNumber);
        classroomJSON.put(KEY_DESCRIPTION, description);
        return classroomJSON;
    }
}
