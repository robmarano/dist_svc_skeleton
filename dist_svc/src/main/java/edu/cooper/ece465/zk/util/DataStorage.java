package edu.cooper.ece465.zk.util;

import edu.cooper.ece465.zk.model.File;

import java.util.ArrayList;
import java.util.List;

/** @author "Bikas Katwal" 26/03/19 */
public final class DataStorage {

    private static List<File> personList = new ArrayList<>();

    public static List<File> getFileListFromStorage() {
        return personList;
    }

    public static void setFile(File file) {
        personList.add(file);
    }

    private DataStorage() {}
}