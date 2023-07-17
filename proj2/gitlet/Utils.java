package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;


/* Assorted utilities.
   @author P. N. Hilfinger */
class Utils<T> {

    /* SHA-1 HASH VALUES. */

    /* Returns the SHA-1 hash of the concatenation of VALS, which may be any
       mixture of byte arrays and Strings. */
    static String sha1(Object... vals) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Object val : vals) {
                if (val instanceof byte[]) {
                    md.update((byte[]) val);
                } else if (val instanceof String) {
                    md.update(((String) val).getBytes(StandardCharsets.UTF_8));
                } else {
                    throw new IllegalArgumentException("improper type to sha1");
                }
            }
            Formatter result = new Formatter();
            for (byte b : md.digest()) {
                result.format("%02x", b);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException excp) {
            throw new IllegalArgumentException("System does not support SHA-1");
        }
    }

    /* Returns the SHA-1 hash of the concatenation of the strings in VALS. */
    static String sha1(List<Object> vals) {
        return sha1(vals.toArray(new Object[vals.size()]));
    }

    /* Returns if the sha1's are equal to each other */
    static boolean sha1Equals(String sha1, String sha2) {
        return sha1 == sha2;
    }
    /* FILE DELETION */

    /* Deletes FILE if it exists and is not a directory.  Returns true if FILE
       was deleted, and false otherwise.  Refuses to delete FILE and throws
       IllegalArgumentException unless the directory designated by FILE also
       contains a directory named .gitlet. */
    static boolean restrictedDelete(File file) {
        if (!(new File(file.getParentFile(), ".gitlet")).isDirectory()) {
            throw new IllegalArgumentException("not .gitlet working directory");
        }
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }

    /* Deletes the file named FILE if it exists and is not a directory. Returns
       true if FILE was deleted, and false otherwise. Refuses to delete FILE and
       throws IllegalArgumentException unless the directory designated by FILE
       also contains a directory named .gitlet. */
    static boolean restrictedDelete(String file) {
        return restrictedDelete(new File(file));
    }

    /* READING AND WRITING FILE CONTENTS */

    /* Return the entire contents of FILE as a byte array. FILE must be a normal
       file. Throws IllegalArgumentException in case of problems. */
    static byte[] readContents(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /* Write the entire contents of BYTES to FILE, creating or overwriting it as
       needed. Throws IllegalArgumentException in case of problems. */
    static void writeContents(File file, byte[] bytes) {
        try {
            if (file.isDirectory()) {
                throw
                        new IllegalArgumentException("cannot overwrite directory");
            }
            Files.write(file.toPath(), bytes);
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /* OTHER FILE UTILITIES */

    /* Return the concatenation of FIRST and OTHERS into a File designator,
       analogous to the {@link java.nio.file.Paths.#get(String, String[])}
       method. */
    static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }

    /* Return the concatenation of FIRST and OTHERS into a File designator,
       analogous to the {@link java.nio.file.Paths.#get(String, String[])}
       method. */
    static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }

    /* DIRECTORIES */

    /* Filter out all but plain files. */
    private static final FilenameFilter PLAIN_FILES = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return new File(dir, name).isFile();
        }
    };

    /* Returns a list of the names of all plain files in the directory DIR, in
       lexicographic order as Java Strings. Returns null if DIR does not denote
       a directory. */
    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    /* Returns a list of the names of all plain files in the directory DIR, in
       lexicographic order as Java Strings. Returns null if DIR does not denote
       a directory. */
    static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }

    /* Takes in an Object and a String FileName and puts the serialized Byte of the
     * object into the FileName */
    static void serialize(Object obj, String fileName) {
        File outFile = new File(fileName);
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(obj);
            out.close();
        } catch (IOException excp) {
            System.out.print("error");
        }
    }

    static Object deserialize(String fileName) {
        Object obj;
        File inFile = new File(fileName);
        try {
            ObjectInputStream inp = new ObjectInputStream((new FileInputStream(inFile)));
            obj = inp.readObject();
            inp.close();
            return obj;
        } catch (IOException | ClassNotFoundException excp) {
            return null;
        }
    }


    static byte[] serializeToHash(Object obj) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(stream);
            objectStream.writeObject(obj);
            objectStream.close();
            return stream.toByteArray();
        } catch (IOException excp) {
            System.out.println("Internal error serializing commit.");
            System.exit(0);

        }
        return null;
    }

    static boolean isModified(Commit oldCommit, Commit newCommit, String fileName) {
        String oldSha = oldCommit.getValue(fileName);
        String newSha = newCommit.getValue(fileName);
        return !oldSha.equals(newSha);
    }

    static boolean diffFileInPWD(String pwd, String fileName, String otherFileSha) {
        File path = new File(pwd, fileName);
        if (path.exists()) {
            byte[] bytes = readContents(path);
            String fileSha = sha1(fileName, bytes);
            if (fileSha.equals(otherFileSha)) {
                return false;
            }
            return true;
        }
        return true;
    }
}
