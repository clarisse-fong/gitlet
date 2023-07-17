package gitlet;

import java.util.Arrays;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class StagingArea implements Serializable {
    private HashMap<String, String> hash;
    private HashMap<String, String> toRemove;


    public StagingArea() {
        hash = new HashMap<>();
        toRemove = new HashMap<>();
    }

    public void clearMe() {
        hash = new HashMap<>();
        toRemove = new HashMap<>();
    }

    public HashMap<String, String> hash() {
        return hash;
    }

    public HashMap<String, String> toRemove() {
        return toRemove;
    }


    public void serializeMe(File stagingAreaFile) {
        Utils.writeContents(stagingAreaFile, Utils.serializeToHash(this));
    }

    public void printFiles() {
        String[] strs = hash.keySet().toArray(new String[hash.size()]);
        Arrays.sort(strs);
        for (String file : strs) {
            System.out.println(file);
        }
    }

    public void printRemovedFiles() {
        String[] strs = toRemove.keySet().toArray(new String[toRemove.size()]);
        Arrays.sort(strs);
        for (String file : strs) {
            System.out.println(file);
        }
    }

}
