package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {
    private String parent;
    private String message;
    private String commitDate;
    private HashMap<String, String> contents;
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public Commit(String parentID, String logMessage) {
        String dateString = format.format(new Date());
        Date date = new Date();
        commitDate = format.format(date);
        parent = parentID;
        message = logMessage;
        contents = new HashMap<>();
    }

    /* Puts all of the currCommit's HashMap(File Names, Sha1ID) into the next commit's HashMap*/
    public void copyParentHashMap(Commit currCommit) {
        currCommit.contents.putAll(contents);
    }

    public void addStagingArea(StagingArea stagingArea) {
        for (String key : stagingArea.hash().keySet()) {
            if (stagingArea.toRemove().containsKey(key)) {
                stagingArea.hash().remove(key);
            } else {
                String sha1Staging = stagingArea.hash().get(key);
                contents.put(key, sha1Staging);
            }
        }
        stagingArea.clearMe();
    }

    public String serializeMe(File commitDir) {
        String sha1Name = Utils.sha1(Utils.serializeToHash(this));
        File newPath = Utils.join(commitDir, sha1Name);
        try {
            newPath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.serialize(this, newPath.toString());
        return sha1Name;
    }

    public boolean hasKey(String fileName) {
        return contents.containsKey(fileName);
    }

    public String getValue(String fileName) {
        return contents.get(fileName);
    }

    public String message() {
        return message;
    }

    public String getDate() {
        return commitDate;
    }

    public String parentString() {
        return parent;
    }

    public HashMap<String, String> contents() {
        return contents;
    }


}
