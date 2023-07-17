package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    private final File _file;

    public Blob(File file) {
        _file = file;
    }

    public String serializeMe(File dir) {
        byte[] byteArray = Utils.readContents(_file);
        String sha1Name = Utils.sha1(_file.getName(), byteArray);
        Utils.writeContents(Utils.join(dir, sha1Name), byteArray);
        return sha1Name;
    }

    public File getFile(File blobsDir, String sha1) {
        return Utils.join(blobsDir, sha1);
    }
}
