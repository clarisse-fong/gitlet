package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Gitlet {

    private Commit currCommit;
    private StagingArea stagingArea;
    private String head;
    private String currBranch;
    private HashMap<String, String> commitHashMap;
    private static File currBranchFile;
    private static File pwd;
    private static File STAGINGAREA_DIR;
    private static File stagingAreaFile;
    private static File GITLET_DIR;
    private static File BLOBS_DIR;
    private static File COMMITS_DIR;
    private static File BRANCHES_DIR;
    private static File HEAD_FILE;
    private static File commitHashMapFile;

    public Gitlet() {
        pwd = new File(System.getProperty("user.dir"));
        GITLET_DIR = new File(pwd, ".gitlet");
        STAGINGAREA_DIR = new File(GITLET_DIR, "StagingAreaFiles");
        stagingAreaFile = new File(STAGINGAREA_DIR, "StagingArea");
        BLOBS_DIR = new File(GITLET_DIR, "Blobs");
        COMMITS_DIR = new File(GITLET_DIR, "Commits");
        BRANCHES_DIR = new File(GITLET_DIR, "Branches");
        HEAD_FILE = new File(GITLET_DIR, "HEAD");
        currBranchFile = new File(GITLET_DIR, "currBranch");
        commitHashMapFile = new File(GITLET_DIR, "commitHashMap");
    }

    public static void init() {
        /* Checks if a gitlet already exists in the current directory*/
        if (GITLET_DIR.exists()) {
            System.out.println("A gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }

        GITLET_DIR.mkdir();
        BLOBS_DIR.mkdir();
        BRANCHES_DIR.mkdir();
        COMMITS_DIR.mkdir();
        STAGINGAREA_DIR.mkdir();

        StagingArea stagingArea = new StagingArea();
        Commit initCommit = new Commit(null, "initial commit");
        String commitSha = initCommit.serializeMe(COMMITS_DIR);
        String substring = commitSha.substring(0, 5);
        HashMap<String, String> commitHashMap = new HashMap<>();
        commitHashMap.put(substring, commitSha);
        String currBranch = "master";
        String master = commitSha;
        File masterPath = new File(BRANCHES_DIR, "master");
        String head = commitSha;


        try {
            stagingAreaFile.createNewFile();
            HEAD_FILE.createNewFile();
            currBranchFile.createNewFile();
            masterPath.createNewFile();
            commitHashMapFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }

        stagingArea.serializeMe(stagingAreaFile);
        Utils.writeContents(HEAD_FILE, Utils.serializeToHash(head));
        Utils.writeContents(currBranchFile, Utils.serializeToHash("master"));
        Utils.writeContents(masterPath, Utils.serializeToHash(master));
        Utils.writeContents(commitHashMapFile, Utils.serializeToHash(commitHashMap));
    }


    public void add(String fileName) {
        /* If file doesn't exist, then print error and exit out */
        File file = new File(pwd, fileName);
        if (!(file.exists())) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        stagingArea = (StagingArea) Utils.deserialize(stagingAreaFile.toString());
        Blob blob = new Blob(file);
        String blobSha = blob.serializeMe(BLOBS_DIR);

        if (stagingArea.toRemove().containsKey(fileName)) {
            stagingArea.toRemove().remove(fileName);
        }

        if (stagingArea.hash().containsKey(fileName)) {
            stagingArea.hash().put(fileName, blobSha);
        } else {
            head = (String) Utils.deserialize(HEAD_FILE.toString());
            File path = new File(COMMITS_DIR, head);
            currCommit = (Commit) Utils.deserialize(path.toString());
            boolean inCommit = currCommit.hasKey(fileName);

            if (inCommit) {
                if (!currCommit.getValue(fileName).equals(blobSha)) {
                    stagingArea.hash().put(fileName, blobSha);
                }
            } else {
                stagingArea.hash().put(fileName, blobSha);
            }
        }
        stagingArea.serializeMe(stagingAreaFile);
    }

    public void commit(String message) {
        stagingArea = (StagingArea) Utils.deserialize(Utils.join
                (STAGINGAREA_DIR, "StagingArea").toString());

        if (stagingArea.hash().isEmpty() && stagingArea.toRemove().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        head = (String) Utils.deserialize(HEAD_FILE.toString());
        currCommit = (Commit) Utils.deserialize(Utils.join(COMMITS_DIR, head).toString());
        currBranch = (String) Utils.deserialize(currBranchFile.toString());
        commitHashMap = (HashMap<String, String>) (Utils.deserialize(commitHashMapFile.toString()));

        Commit newCommit = new Commit(head, message);
        newCommit.copyParentHashMap(currCommit);
        newCommit.addStagingArea(stagingArea);
        String commitSha = newCommit.serializeMe(COMMITS_DIR);
        head = commitSha;
        String substring = commitSha.substring(0, 5);
        commitHashMap.put(substring, commitSha);


        stagingArea.serializeMe(stagingAreaFile);
        Utils.writeContents(HEAD_FILE, Utils.serializeToHash(head));
        Utils.writeContents(new File(BRANCHES_DIR, currBranch), Utils.serializeToHash(head));
        Utils.writeContents(commitHashMapFile, Utils.serializeToHash(commitHashMap));

    }

    public void rm(String fileName) {
        File file = new File(pwd, fileName);
        stagingArea = (StagingArea) Utils.deserialize(stagingAreaFile.toString());
        head = (String) Utils.deserialize(HEAD_FILE.toString());
        currCommit = (Commit) Utils.deserialize(new File(COMMITS_DIR, head).toString());

        boolean inStaging = stagingArea.hash().containsKey(fileName);
        boolean inCurr = currCommit.hasKey(fileName);

        if (!(file.exists())) {
            if (inStaging) {
                stagingArea.hash().remove(fileName);
            } else if (inCurr) {
                stagingArea.toRemove().put(fileName, currCommit.getValue(fileName));
            }
            stagingArea.serializeMe(stagingAreaFile);
            return;
        }

        byte[] byteArray = Utils.readContents(file);
        String fileSha = Utils.sha1(file.getName(), byteArray);

        if (!(inCurr || inStaging)) {
            System.out.println("No reason to remove the file");
            System.exit(0);
        }
        if (inCurr) {
            Utils.restrictedDelete(file);
            if (inStaging) {
                stagingArea.hash().remove(fileName);
            }
            stagingArea.toRemove().put(fileName, fileSha);

        } else if (inStaging) {
            stagingArea.hash().remove(fileName);
        }
        stagingArea.serializeMe(stagingAreaFile);
    }

    public void log() {
        String commitSha = (String) Utils.deserialize(HEAD_FILE.toString());
        Commit on = (Commit) Utils.deserialize(new File(COMMITS_DIR, commitSha).toString());

        while (true) {
            System.out.println("===");
            System.out.println("Commit " + commitSha);
            System.out.println(on.getDate());
            System.out.println(on.message());
            System.out.println();

            if (on.message().equals("initial commit")) {
                break;
            } else {
                commitSha = on.parentString();
                on = (Commit) Utils.deserialize(new File(COMMITS_DIR, commitSha).toString());
            }
        }
    }

    public void globalLog() {
        for (String sha1Commit : Utils.plainFilenamesIn(COMMITS_DIR)) {
            File path = new File(COMMITS_DIR, sha1Commit);
            Commit on = (Commit) Utils.deserialize(path.toString());

            System.out.println("===");
            System.out.println("Commit " + sha1Commit);
            System.out.println(on.getDate());
            System.out.println(on.message());
            System.out.println();
        }
    }

    public void find(String commitMessage) {
        boolean x = false;
        for (String sha1Commit : Utils.plainFilenamesIn(COMMITS_DIR)) {
            File path = new File(COMMITS_DIR, sha1Commit);
            Commit on = (Commit) Utils.deserialize(path.getPath());

            if (on.message().equals(commitMessage)) {
                System.out.println(sha1Commit);
                x = true;
            }
        }
        if (!x) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public void status() {
        stagingArea = (StagingArea) Utils.deserialize(stagingAreaFile.toString());
        head = (String) Utils.deserialize(HEAD_FILE.toString());
        currBranch = (String) Utils.deserialize(currBranchFile.toString());

        System.out.println("=== Branches ===");
        for (String branch : Utils.plainFilenamesIn(BRANCHES_DIR)) {
            if (branch.equals(currBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        stagingArea.printFiles();
        System.out.println();

        System.out.println("=== Removed Files ===");
        stagingArea.printRemovedFiles();
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ====");
    }

    public void checkoutFile(String fileName) {
        head = (String) Utils.deserialize(HEAD_FILE.toString());
        currCommit = (Commit) Utils.deserialize(Utils.join(COMMITS_DIR, head).toString());

        if (!currCommit.hasKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        File path = new File(pwd, fileName);
        String fileSha = currCommit.getValue(fileName);
        Blob blob = new Blob(path);

        byte[] bytes = Utils.readContents(blob.getFile(BLOBS_DIR, fileSha));
        Utils.writeContents(path, bytes);

    }

    public void checkoutCommit(String commitID, String fileName) {
        commitHashMap = (HashMap<String, String>) (Utils.deserialize(commitHashMapFile.toString()));
        String abbrev = commitID.substring(0, 5);
        if (commitHashMap.containsKey(abbrev)) {
            commitID = commitHashMap.get(abbrev);
        }

        if (!Utils.join(COMMITS_DIR, commitID).exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit commit = (Commit) Utils.deserialize(Utils.join(COMMITS_DIR, commitID).toString());

        if (!(commit.hasKey(fileName))) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        File path = new File(pwd, fileName);
        String fileSha = commit.getValue(fileName);
        Blob blob = new Blob(path);

        byte[] bytes = Utils.readContents(blob.getFile(BLOBS_DIR, fileSha));
        Utils.writeContents(path, bytes);
    }

    public void checkoutBranch(String branchName) {
        head = (String) Utils.deserialize(HEAD_FILE.toString());
        currBranch = (String) Utils.deserialize(currBranchFile.toString());
        currCommit = (Commit) Utils.deserialize(Utils.join(COMMITS_DIR, head).toString());
        stagingArea = (StagingArea) Utils.deserialize(stagingAreaFile.toString());

        if (currBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch");
            System.exit(0);
        }

        File path = Utils.join(BRANCHES_DIR, branchName);
        if (!(path.exists())) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        String newBSha = (String) Utils.deserialize(path.toString());
        Commit givenCom = (Commit) Utils.deserialize(Utils.join(COMMITS_DIR, newBSha).toString());
        String commitSha = givenCom.serializeMe(COMMITS_DIR);


        for (String file : Utils.plainFilenamesIn(pwd)) {
            File filePath = new File(pwd, file);
            if (givenCom.hasKey(file) && (!currCommit.hasKey(file))
                    && Utils.diffFileInPWD(pwd.toString(), file, givenCom.getValue(file))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
                System.exit(0);
            } else if (!givenCom.hasKey(file)) {
                Utils.restrictedDelete(new File(pwd, file));
            }
        }

        for (String file : givenCom.contents().keySet()) {
            File filePath = new File(pwd, file);
            if (!(currCommit.hasKey(file) || stagingArea.hash().containsKey(file)) &&
                    Utils.diffFileInPWD(pwd.toString(), file, givenCom.getValue(file))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
                System.exit(0);
            }
            String fileSha = givenCom.getValue(file);
            byte[] bytes = Utils.readContents(new File(BLOBS_DIR, fileSha));
            Utils.writeContents(filePath, bytes);
        }

        Utils.writeContents(currBranchFile, Utils.serializeToHash(branchName));
        Utils.writeContents(HEAD_FILE, Utils.serializeToHash(commitSha));

        stagingArea = (StagingArea) Utils.deserialize(stagingAreaFile.toString());
        stagingArea.clearMe();

//        for (String file : Utils.plainFilenamesIn(pwd)) {
//            if (!givenCom.hasKey(file)) {
//                Utils.restrictedDelete(new File(pwd, file));
//            }
//        }

    }

    public void branch(String branchName) {
        File path = Utils.join(BRANCHES_DIR, branchName);

        if (path.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        String newBranch = (String) Utils.deserialize(HEAD_FILE.toString());
        Utils.writeContents(path, Utils.serializeToHash(newBranch));
    }

    public void removeBranch(String branchName) {
        File path = Utils.join(BRANCHES_DIR, branchName);

        if (!(path.exists())) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        currBranch = (String) Utils.deserialize(currBranchFile.toString());
        if (branchName.equals(currBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        Utils.restrictedDelete(path);
    }

    public void reset(String commitID) {
        File path = Utils.join(COMMITS_DIR, commitID);
        if (!(path.exists())) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit givenCommit = (Commit) Utils.deserialize(new File(COMMITS_DIR, commitID).toString());

        for (String file : givenCommit.contents().keySet()) {
            File filePath = new File(pwd, file);
            String fileSha = givenCommit.getValue(file);
            byte[] bytes = Utils.readContents(new File(BLOBS_DIR, fileSha));
            Utils.writeContents(filePath, bytes);
        }

        stagingArea = (StagingArea) Utils.deserialize(stagingAreaFile.toString());
        stagingArea.clearMe();
    }

    public void merge(String branchName) {
        head = (String) Utils.deserialize(HEAD_FILE.toString());
        currCommit = (Commit) Utils.deserialize(new File(COMMITS_DIR, head).toString());
        String currBranchName = (String) Utils.deserialize(currBranchFile.toString());
        stagingArea = (StagingArea) Utils.deserialize(stagingAreaFile.toString());

        String gBranchSha = (String) Utils.deserialize(new File(BRANCHES_DIR, branchName).toString());
        Commit gBranchCommit = (Commit) Utils.deserialize(new File(COMMITS_DIR, gBranchSha).toString());

        String cCommitSha = head;
        Commit cCommit = currCommit;
        String gCommitSha = gBranchSha;
        Commit gCommit = gBranchCommit;

        Commit splitPoint = null;
        String splitPointSha = null;

        outerloop:
        while (!cCommit.message().equals("initial commit")) {
            while (!gCommit.message().equals("initial commit")) {
                if (gCommitSha.equals(cCommitSha)) {
                    splitPoint = cCommit;
                    splitPointSha = cCommitSha;
                    break outerloop;
                } else {
                    gCommitSha = gCommit.parentString();
                    gCommit = (Commit) Utils.deserialize(new File(COMMITS_DIR, gCommitSha).toString());
                }
            }
            cCommitSha = cCommit.parentString();
            cCommit = (Commit) Utils.deserialize(new File(COMMITS_DIR, cCommitSha).toString());
            gCommitSha = gBranchSha;
            gCommit = gBranchCommit;
        }
        if (splitPointSha == null) {
            System.out.println("SplitPoint shouldn't be null");
            System.exit(0);
        }

        boolean conflict = false;
        if (splitPointSha.equals(gBranchSha)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPointSha.equals(head)) {
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        } else {
            for (String fileName : splitPoint.contents().keySet()) {
                if ((cCommit.hasKey(fileName) && !Utils.isModified(splitPoint, cCommit, fileName))
                        && !gCommit.hasKey(fileName)) {
                    Utils.restrictedDelete(new File(pwd, fileName));
                    stagingArea.toRemove().put(fileName, splitPoint.getValue(fileName));
                } else if (!cCommit.hasKey(fileName) && gCommit.hasKey(fileName)) {
                    break;
                } else if (Utils.isModified(splitPoint, gCommit, fileName)
                        && !Utils.isModified(splitPoint, cCommit, fileName)) {
                    File path = new File(pwd, fileName);
                    String fileSha = cCommit.getValue(fileName);
                    Blob blob = new Blob(path);

                    byte[] bytes = Utils.readContents(blob.getFile(BLOBS_DIR, fileSha));
                    Utils.writeContents(path, bytes);
                    stagingArea.hash().put(fileName, fileSha);
                } else if (!Utils.isModified(splitPoint, gCommit, fileName)
                        && Utils.isModified(splitPoint, cCommit, fileName)) {
                    break;
                } else {
                    conflict = true;
                    if ((!cCommit.hasKey(fileName)
                            && Utils.isModified(splitPoint, gCommit, fileName))
                            || (!gCommit.hasKey(fileName)
                            && Utils.isModified(splitPoint, cCommit, fileName))) {


                    } else if (Utils.isModified(splitPoint, cCommit, fileName)
                            && Utils.isModified(splitPoint, gCommit, fileName)
                            && Utils.isModified(gCommit, cCommit, fileName)) {
                    }
                }
            }

        }

        for (String fileName : cCommit.contents().keySet()) {
            if (!splitPoint.hasKey(fileName)) {
                if (!gCommit.hasKey(fileName)) {
                    break;
                } else {
                    if (Utils.isModified(cCommit, gCommit, fileName)) {
                        conflict = true;

                    }

                }
            }
        }

        for (String fileName : gCommit.contents().keySet()) {
            if (!(splitPoint.hasKey(fileName) || cCommit.hasKey(fileName))) {
                File path = new File(pwd, fileName);
                String fileSha = cCommit.getValue(fileName);
                Blob blob = new Blob(path);

                byte[] bytes = Utils.readContents(blob.getFile(BLOBS_DIR, fileSha));
                Utils.writeContents(path, bytes);
                stagingArea.hash().put(fileName, fileSha);
            }
        }
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
            System.exit(0);
        } else {
            System.out.println("Merged " + currBranchName + " with " + branchName);
        }
        stagingArea.serializeMe(stagingAreaFile);
    }

}
