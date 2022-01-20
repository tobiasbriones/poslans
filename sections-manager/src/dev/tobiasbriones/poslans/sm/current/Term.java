/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.current;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Term {
    private static final String DATA_DIRECTORY = "sections-manager/data";
    private static final String FILE_PATH = DATA_DIRECTORY + "/term";

    public static void set(String name) throws IOException {
        FileWriter fw = null;
        check();
        try {
            fw = new FileWriter(FILE_PATH);
            fw.write(name);
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

    public static String load() throws IOException {
        check();
        return new String(Files.readAllBytes(Paths.get(".", FILE_PATH)));
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
