/*
 * Copyright (c) 2022 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.database;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LocalStorageDao {
    public LocalStorageDao() {}

    public String load(Path path) throws IOException {
        return Files.readAllLines(path).get(0);
    }

    public void save(JSONObject object, Path path) throws IOException {
        save(object.toString(), path);
    }

    public void save(String str, Path path) throws IOException {
        Files.write(path, str.getBytes(StandardCharsets.UTF_8));
    }
}
