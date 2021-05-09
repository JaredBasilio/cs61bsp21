package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.Map;
import java.io.IOException;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Jared Basilio
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**The staging area directory*/
    public static final File STAGINGAREA = join(GITLET_DIR, "stagingArea");

    /** The commits directory in .gitlet*/
    public static final File COMMITS = join(GITLET_DIR, "commits");

    /** The blobs directory in .gitlet*/
    public static final File BLOBS = join(GITLET_DIR, "blobs");

    public static final File DATA = new File(GITLET_DIR, "data");

    private static class Data implements Serializable {
        private Map<String, String> stagedAdd; //creates the hashmap for staged add
        private Map<String, String> stagedRemove; //creates the hashmap for staged remove
        private Map<String, String> branches;
        private Map<String, String> remotes;

        private String currentBranch;
        private String headPointer;

        Data() {
        }

        public void setStagedAdd(Map<String, String> temp) {
            stagedAdd = temp;
        }

        public void setStagedRemove(Map<String, String> temp) {
            stagedRemove = temp;
        }

        public void setBranchList(Map<String, String> temp) {
            branches = temp;
        }

        public void addBranch(String branchName, String uid) {
            branches.put(branchName, uid);
        }

        public void removeBranch(String branchName) {
            branches.remove(branchName);
        }

        public void setHeadPointer(String pointer) {
            headPointer = pointer;
        }

        public Map<String, String> getStagedAdd() {
            return stagedAdd;
        }

        public Map<String, String> getStagedRemove() {
            return stagedRemove;
        }

        public Map<String, String> getBranches() {
            return branches;
        }

        public String getHeadPointer() {
            return headPointer;
        }

        public String getCurrentBranch() {
            return currentBranch;
        }

        public void untrackAdd(String id) {
            stagedAdd.remove(id);
        }

        public void untrackRemove(String id) {
            stagedRemove.remove(id);
        }

        public void setCurrentBranch(String branch) {
            currentBranch = branch;
        }

        public void putStagedAdd(String a, String b) {
            stagedAdd.put(a, b);
        }

        public void putStagedRemove(String a, String b) {
            stagedRemove.put(a, b);
        }

        public void createRemotes() {
            remotes = new TreeMap<>();
        }

        public Map<String, String> getRemotes() {
            return remotes;
        }

        public void addRemote(String name, String directory) {
            remotes.put(name, directory);
        }

        public void removeRemote(String name) {
            remotes.remove(name);
        }

        public void clearStagingArea() {
            for (Map.Entry<String, String> entry : stagedAdd.entrySet()) {
                join(STAGINGAREA, entry.getKey()).delete();
            }
            for (Map.Entry<String, String> entry : stagedRemove.entrySet()) {
                join(STAGINGAREA, entry.getKey()).delete();
            }
            setStagedAdd(new TreeMap<>()); //clears staging area add
            setStagedRemove(new TreeMap<>()); //clears staging area remove
        }

        public void saveData() {
            Utils.writeObject(DATA, this);
        }
    }

    /**
     * initializes the gitlet repository
     * @throws IOException
     */
    public static void init() throws IOException {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        Commit initialCommit = new Commit("initial commit"); //creates the initial commit
        initialCommit.giveUID(Utils.sha1(serialize(initialCommit)));
        STAGINGAREA.mkdir(); //creates the staging area directory
        COMMITS.mkdir(); //creates the commit directory
        BLOBS.mkdir(); //creates the blob directory
        initialCommit.saveCommit();
        DATA.createNewFile();
        Data temp = new Data();
        temp.setBranchList(new TreeMap<>());
        temp.addBranch("master", initialCommit.uid());
        temp.setCurrentBranch("master");
        temp.setHeadPointer(initialCommit.uid());
        temp.setStagedAdd(new TreeMap<>());
        temp.setStagedRemove(new TreeMap<>());
        temp.createRemotes();
        temp.saveData();
    }

    /**
     * adds the file to be staged for commit
     * @param fileName
     */
    public static void add(String fileName) {
        if (!GITLET_DIR.exists()) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        }
        File inFile = join(CWD, fileName);
        if (!inFile.exists()) {
            Utils.exitWithError("File does not exist.");
        }
        byte[] blob = readContents(inFile); //reads as byte for sha1 analysis
        Data temp = readObject(DATA, Data.class);
        String inFileSHA1 = Utils.sha1(blob); //creates the hash1 for the blob
        Commit currentCommit = readObject(join(COMMITS, temp.getHeadPointer()), Commit.class);
        String currentSHA1 = currentCommit.getFile(fileName);
        if (currentSHA1 != null && currentSHA1.equals(inFileSHA1)) { //there is an identical
            if (temp.getStagedAdd().containsKey(fileName)) {
                temp.untrackAdd(fileName);
            }
            if (temp.getStagedRemove().containsKey(fileName)) {
                temp.untrackRemove(fileName);
            }
        } else if (temp.getStagedRemove().containsKey(fileName)) {
            temp.untrackRemove(fileName);
        } else { //neither
            File blobsDestination = join(BLOBS, inFileSHA1);
            File destination = join(STAGINGAREA, fileName);
            Utils.writeContents(blobsDestination, blob);
            Utils.writeContents(destination, blob);
            temp.putStagedAdd(fileName, inFileSHA1);
        }
        temp.saveData();
    }

    /**
     * untracks the file
     * @param fileName
     */
    public static void rm(String fileName) {
        Data temp = readObject(DATA, Data.class);
        Commit currentCommit = readObject(join(COMMITS, temp.getHeadPointer()), Commit.class);
        Map<String, String> stageAdd = temp.getStagedAdd();
        File inFile = join(CWD, fileName);
        Map<String, String> currentCommitFiles = currentCommit.getFiles();
        boolean tracked = currentCommitFiles.containsKey(fileName);
        boolean staged = stageAdd.containsKey(fileName);
        if (staged) { // currently staged for addition case
            if (join(STAGINGAREA, fileName).exists()) {
                join(STAGINGAREA, fileName).delete();
            }
            temp.untrackAdd(fileName); //clears staging area add
        } else if (tracked) { // if in current commit
            File destination = join(STAGINGAREA, fileName); //gets staging area
            temp.putStagedRemove(fileName, currentCommitFiles.get(fileName));
            String blob = currentCommitFiles.get(fileName);
            byte[] fileContents = readContents(join(BLOBS, blob));
            Utils.writeContents(destination, fileContents);
            if (inFile.exists()) {
                inFile.delete();
            }
        } else {
            Utils.exitWithError("No reason to remove the file.");
        }
        temp.saveData();
    }

    /**
     * commits all staged files
     * @param message
     */
    public static void commit(String message) {
        if (!GITLET_DIR.exists()) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        }
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Data temp = readObject(DATA, Data.class);
        String headPointer = temp.getHeadPointer(); //grabs the headpointer, for parent
        File cloneFile = Utils.join(COMMITS, headPointer);
        Commit cloneCommit = readObject(cloneFile, Commit.class);
        Map<String, String> add = temp.getStagedAdd();
        Map<String, String> remove = temp.getStagedRemove();
        if (add.isEmpty() && remove.isEmpty()) {
            Utils.exitWithError("No changes added to the commit.");
        }
        cloneCommit.setDataMulti(add);
        cloneCommit.removeDataMulti(remove); // untracks all the data in stagedRemove in clone
        temp.clearStagingArea();
        cloneCommit.setTimestampCurrent(); //changes the new timestamp to be the current timestamp
        cloneCommit.addParentForCommit(headPointer); //adds the parent to the list
        cloneCommit.setMessage(message); //resets the message to the given message
        cloneCommit.giveUID(Utils.sha1(serialize(cloneCommit)));
        cloneCommit.saveCommit();
        temp.addBranch(temp.getCurrentBranch(), cloneCommit.uid());
        temp.setHeadPointer(cloneCommit.uid()); //sets the headpointer id to the newly made ID
        temp.saveData();
    }

    /**
     * prints out the status of all branches,staged files,removed files,
     * modifications not staged for commit,untracked files
     */
    public static void status() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        Data temp = readObject(DATA, Data.class);
        Map<String, String> a = temp.getStagedAdd();
        Map<String, String> b = temp.getStagedRemove();
        System.out.println("=== Branches ===");
        Map<String, String> branches = temp.getBranches();
        String currentBranch = temp.getCurrentBranch();
        for (Map.Entry<String, String> branch: branches.entrySet()) {
            String branchName = branch.getKey();
            if (branchName.equals(currentBranch)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (Map.Entry<String, String> entry : a.entrySet()) {
            System.out.println(entry.getKey());
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (Map.Entry<String, String> entry : b.entrySet()) {
            System.out.println(entry.getKey());
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        File currentCommit = join(COMMITS, temp.getHeadPointer());
        Commit commit = readObject(currentCommit, Commit.class);
        for (Map.Entry<String, String> entry : commit.getFiles().entrySet()) {
            String file = readContentsAsString(join(BLOBS, entry.getValue()));
            if (!join(CWD, entry.getKey()).exists() && !b.containsKey(entry.getKey())) {
                System.out.println(entry.getKey() + " (deleted)");
            } else if (join(CWD, entry.getKey()).exists()) {
                String contents = readContentsAsString(join(CWD, entry.getKey()));
                boolean tracked = a.containsKey(entry.getKey());
                if (!file.equals(contents) && !a.containsKey(entry.getKey())) {
                    System.out.println(entry.getKey() + " (modified)");
                }
            }
        }
        for (Map.Entry<String, String> entry: a.entrySet()) {
            if (!join(CWD, entry.getKey()).exists()) {
                System.out.println(entry.getKey() + " (deleted)");
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String item : plainFilenamesIn(CWD)) {
            if (join(CWD, item).isFile() && !a.containsKey(item)
                    && !b.containsKey(item) && !commit.getFiles().containsKey(item)) {
                System.out.println(item);
            }
        }
        System.out.println();
    }

    /**
     * prints the log following the commit and the first parent
     */
    public static void log() {
        Data temp = readObject(DATA, Data.class);
        String localPoint = temp.getHeadPointer();
        while (localPoint != null) {
            File headCommit = Utils.join(COMMITS, localPoint);
            Commit commitCheck = readObject(headCommit, Commit.class);
            ArrayList<String> parents = commitCheck.getParents();
            System.out.println("===");
            System.out.println("commit " + commitCheck.uid());
            if (commitCheck.isMerged()) {
                String parentsString = "";
                for (String parent : commitCheck.getParents()) {
                    parentsString = parentsString + " " + parent.substring(0, 7);
                }
                System.out.println("Merge:" + parentsString);
            }
            System.out.println("Date: " + commitCheck.timeStamp());
            System.out.println(commitCheck.message());
            System.out.println();
            if (parents.size() == 0) {
                localPoint = null;
            } else {
                localPoint = parents.get(0);
            }
        }
    }

    /**
     * prints out the log for all commits in any order
     */
    public static void globalLog() {
        for (String item : plainFilenamesIn(COMMITS)) {
            File fileCurrent = Utils.join(COMMITS, item);
            Commit commitCheck = readObject(fileCurrent, Commit.class);
            System.out.println("===");
            System.out.println("commit " + commitCheck.uid());
            if (commitCheck.isMerged()) {
                String parentsString = "";
                for (String parent : commitCheck.getParents()) {
                    parentsString = parentsString + " " + parent.substring(0, 7);
                }
                System.out.println("Merge:" + parentsString);
            }
            System.out.println("Date: " + commitCheck.timeStamp());
            System.out.println(commitCheck.message());
            System.out.println();
        }
    }

    /**
     * finds all commits with that message
     * @param message
     */
    public static void find(String message) {
        boolean hasMessage = false;
        for (String item : plainFilenamesIn(COMMITS)) {
            File fileCurrent = Utils.join(COMMITS, item);
            Commit commit = readObject(fileCurrent, Commit.class);
            if (commit.message().equals(message)) {
                System.out.println(commit.uid());
                hasMessage = true;
            }
        }
        if (!hasMessage) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /**
     * Takes the version of the file as it exists in
     * the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * @param fileName - the file you want to checkout
     */
    public static void checkoutFile(String fileName) {
        Data temp = readObject(DATA, Data.class);
        String headPointer = temp.getHeadPointer();
        File headCommit = Utils.join(COMMITS, headPointer); //goes to head pointer
        Commit commitCheck = readObject(headCommit, Commit.class); //reads headpointer commit
        if (commitCheck.getFile(fileName) == null) { //if does not exist in previous commit
            Utils.exitWithError("File does not exist in that commit.");
        }
        String blobID = commitCheck.getFile(fileName);
        File version = join(BLOBS, blobID);
        byte[] contents = readContents(version);
        Utils.writeContents(Utils.join(CWD, fileName), contents);
    }

    /**
     * the checkout command should modify the working
     * directory to change the file to the version in the commit based on the id
     * go from the commit id to the commit object
     * from the commit object change the filename to the contents of the file in the directory
     *
     * when should this be stored?
     * how should this be stored?
     *      -file?
     *      -instance var?(what data structure?)
     * @param fileName
     */
    public static void checkoutID(String id, String fileName) {
        Map<String, String> abbreviated = abbreviated(id.length());
        if (abbreviated.get(id) == null) {
            Utils.exitWithError("No commit with that id exists.");
        }
        File commit = Utils.join(COMMITS, abbreviated.get(id));
        Commit commitCheck = readObject(commit, Commit.class);
        Map<String, String> filesPresent = commitCheck.getFiles();
        if (!filesPresent.containsKey(fileName)) {
            Utils.exitWithError("File does not exist in that commit.");
        }
        byte[] contents = readContents(join(BLOBS, filesPresent.get(fileName)));
        Utils.writeContents(Utils.join(CWD, fileName), contents);
    }

    /**
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist.
     * Also, at the end of this command, the given branch will now be
     * considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present in the
     * checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch
     * @param branchName
     */
    public static void checkoutBranch(String branchName) {
        Data temp = readObject(DATA, Data.class);
        Map<String, String> branches = temp.getBranches();
        String currentBranch = temp.getCurrentBranch();
        Map<String, String> stagedRemove = temp.getStagedRemove();
        Map<String, String> stagedAdd = temp.getStagedAdd();
        if (!branches.containsKey(branchName)) {
            Utils.exitWithError("No such branch exists.");
        }
        if (branchName.equals(currentBranch)) {
            Utils.exitWithError("No need to checkout the current branch.");
        }
        String branchID = branches.get(branchName);
        String currentBranchID = branches.get(currentBranch);
        File commit = Utils.join(COMMITS, branchID);
        File currentCommit = Utils.join(COMMITS, currentBranchID);
        Commit branchCheck = readObject(commit, Commit.class);
        Commit currentBranchCheck = readObject(currentCommit, Commit.class);
        overWrite(currentBranchCheck, branchCheck, stagedAdd, stagedRemove);
        for (Map.Entry<String, String> entry: currentBranchCheck.getFiles().entrySet()) {
            if (!branchCheck.getFiles().containsKey(entry.getKey())) {
                join(CWD, entry.getKey()).delete();
            }
        }
        for (Map.Entry<String, String> entry : branchCheck.getFiles().entrySet()) {
            String fileName = entry.getKey();
            String blobID = entry.getValue();
            Utils.writeContents(Utils.join(CWD, fileName), readContents(join(BLOBS, blobID)));
        } //WRITES THE FILES
        temp.removeBranch(branchName);
        temp.addBranch(branchName, branchID);
        temp.setCurrentBranch(branchName);
        temp.setHeadPointer(branchID);
        temp.clearStagingArea();
        temp.saveData();
    }

    /**
     * adds a new branch
     * @param branchName
     */
    public static void branch(String branchName) {
        Data temp = readObject(DATA, Data.class);
        Map<String, String> branches = temp.getBranches();
        if (branches.containsKey(branchName)) {
            Utils.exitWithError("A branch with that name already exists");
        }
        String currentBranchUID = branches.get(temp.getCurrentBranch());
        temp.addBranch(branchName, currentBranchUID);
        temp.saveData();
    }

    /**
     * removes a given branch
     * @param branchName
     */
    public static void rmbranch(String branchName) {
        Data temp = readObject(DATA, Data.class);
        Map<String, String> branches = temp.getBranches();
        String currentBranch = temp.getCurrentBranch();
        if (!branches.containsKey(branchName)) {
            Utils.exitWithError("A branch with that name does not exist.");
        } else if (currentBranch.equals(branchName)) {
            Utils.exitWithError("Cannot remove the current branch.");
        }
        temp.removeBranch(branchName);
        temp.saveData();
    }

    public static void reset(String id) {
        File commit = join(COMMITS, id);
        Data temp = readObject(DATA, Data.class);
        Map<String, String> stagedAdd = temp.getStagedAdd();
        Map<String, String> stagedRemove = temp.getStagedRemove();
        Map<String, String> branches = temp.getBranches();
        if (!commit.exists()) {
            Utils.exitWithError("No commit with that id exists");
        }
        File currentCommit = join(COMMITS, branches.get(temp.getCurrentBranch()));
        Commit commitCheck = readObject(commit, Commit.class); //given id commit
        Commit current = readObject(currentCommit, Commit.class); //current branch commit
        Map<String, String> files = commitCheck.getFiles();
        Map<String, String> currentFiles = current.getFiles();
        overWrite(current, commitCheck, stagedAdd, stagedRemove);
        for (Map.Entry<String, String> entry : currentFiles.entrySet()) {
            if (!files.containsKey(entry.getKey())) {
                join(CWD, entry.getKey()).delete();
            }
        }
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String fileName = entry.getKey();
            checkoutID(id, fileName);
        }
        String tempBranch = temp.getCurrentBranch();
        temp.clearStagingArea();
        temp.removeBranch(tempBranch);
        temp.addBranch(tempBranch, id);
        temp.setHeadPointer(id); //moves the current branch's head to that commit node
        temp.saveData();
    }

    /**
     * merges two commits
     * @param branchName
     */
    public static void merge(String branchName) {
        Data temp = readObject(DATA, Data.class);
        Map<String, String> branches = temp.getBranches();
        Map<String, String> stagedRemove = temp.getStagedRemove();
        Map<String, String> stagedAdd = temp.getStagedAdd();
        String currentBranch = temp.getCurrentBranch();
        boolean mergeConflict = false;
        if (!stagedRemove.isEmpty() || !stagedAdd.isEmpty()) {
            Utils.exitWithError("You have uncommitted changes.");
        }
        File fileCurrent = join(COMMITS, branches.get(currentBranch));
        if (!branches.containsKey(branchName)) {
            Utils.exitWithError("A branch with that name does not exist.");
        }
        File fileUSER = join(COMMITS, branches.get(branchName));
        Commit commitCurrent = readObject(fileCurrent, Commit.class);
        if (temp.getCurrentBranch().equals(branchName)) {
            Utils.exitWithError("Cannot merge a branch with itself.");
        }
        Commit commitUser = readObject(fileUSER, Commit.class);
        overWrite(commitCurrent, commitUser, stagedAdd, stagedRemove);
        String splitPoint = findSplitPoint(bfs(commitUser), bfs(commitCurrent));
        if (splitPoint.equals(commitUser.uid())) {
            Utils.exitWithError("Given branch is an ancestor of the current branch.");
        }
        if (splitPoint.equals(commitCurrent.uid())) {
            checkoutBranch(branchName);
            Utils.exitWithError("Current branch fast-forwarded.");
        }
        Commit splitPointCommit = readObject(join(COMMITS, splitPoint), Commit.class);
        Map<String, String> currentBranchFiles = commitCurrent.getFiles();
        Map<String, String> givenBranchFiles = commitUser.getFiles();
        Map<String, String> splitPointFiles = splitPointCommit.getFiles();
        for (Map.Entry<String, String> entry : splitPointFiles.entrySet()) {
            String split = entry.getValue(); //gets the content of splitpoint
            String current = "";
            String given = "";
            String fileName = entry.getKey();
            if (currentBranchFiles.containsKey(fileName)) {
                current = currentBranchFiles.get(fileName); //gets fileName sha1 for currentBranch
                currentBranchFiles.remove(fileName);
            }
            if (givenBranchFiles.containsKey(fileName)) {
                given = givenBranchFiles.get(fileName); //gets fileName sha1 for givenBranch
                givenBranchFiles.remove(fileName);
            }
            if (current.equals(given)) {
                continue;
            } else if (given.equals("") && current.equals(split)) {
                rm(fileName);
            } else if (current.equals("") && given.equals(split)) {
                continue;
            } else if (current.equals(split)) {
                checkoutID(branches.get(branchName), fileName);
                add(fileName);
            } else if (given.equals(split)) {
                continue;
            } else {
                mergeConflict = true;
                mergeConflict(current, given, fileName);
            }
        }
        for (Map.Entry<String, String> entry: givenBranchFiles.entrySet()) {
            String fileName = entry.getKey();
            if (!splitPointFiles.containsKey(fileName)) {
                checkoutID(branches.get(branchName), fileName);
                add(fileName);
            }
        }
        if (checkNonSplitFiles(givenBranchFiles, currentBranchFiles)) {
            mergeConflict = true;
        }
        if (checkNonSplitFiles(currentBranchFiles, givenBranchFiles)) {
            mergeConflict = true;
        }
        mergeCommit("Merged " + branchName + " into " + currentBranch + ".", branchName);
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * adds a remote directory
     * @param remoteName
     * @param remoteDirectory
     */
    public static void addRemote(String remoteName, String remoteDirectory) {
        Data temp = readObject(DATA, Data.class);
        Map<String, String> remotes = temp.getRemotes();
        if (remotes.containsKey(remoteName)) {
            Utils.exitWithError("A remote with that name already exists.");
        }
        temp.addRemote(remoteName, remoteDirectory.replace("/", java.io.File.separator));
        temp.saveData();
    }

    /**
     * removes a remote directory
     * @param remoteName
     */
    public static void rmRemote(String remoteName) {
        Data temp = readObject(DATA, Data.class);
        Map<String, String> remotes = temp.getRemotes();
        if (!remotes.containsKey(remoteName)) {
            Utils.exitWithError("A remote with that name does not exist.");
        }
        temp.removeRemote(remoteName);
        temp.saveData();
    }

    /**
     * Attempts to append the current branch’s
     * commits to the end of the given branch at the given remote.
     * @param remoteName
     * @param remoteBranchName
     */
    public static void push(String remoteName, String remoteBranchName) {
        Data temp = readObject(DATA, Data.class);
        Map<String, String> remotes = temp.getRemotes();
        if (!remotes.containsKey(remoteName) || !join(remotes.get(remoteName)).exists()) {
            Utils.exitWithError("Remote directory not found.");
        }
        String remoteDirectory = remotes.get(remoteName);
        Map<String, String> branches = temp.getBranches();
        String currentBranchUID = branches.get(temp.getCurrentBranch());
        Commit headLocal = readObject(join(COMMITS, currentBranchUID), Commit.class);
        List<String> recentCommits = bfs(headLocal);
        Data remoteData = readObject(join(remoteDirectory, "data"), Data.class);
        String remoteHead = remoteData.getBranches().get(remoteBranchName);
        if (!recentCommits.contains(remoteHead)) {
            Utils.exitWithError("Please pull down remote changes before pushing.");
        }
        Commit commitRemoteHead = readObject(join(remoteDirectory,
                "commits" + java.io.File.separator + remoteHead), Commit.class);
        Queue<String> q = new PriorityQueue<String>();
        ArrayList<String> marked = new ArrayList<>();
        q.add(headLocal.uid());
        marked.add(headLocal.uid());
        writeObject(join(remoteDirectory, "commits"), commitRemoteHead);
        String leastFound = "";
        while (!q.isEmpty()) {
            String v = q.poll();
            Commit vCommit = readObject(join(COMMITS, v), Commit.class);
            if (vCommit.getParents() == null) {
                continue;
            }
            ArrayList<String> parents = vCommit.getParents();
            if (parents.contains(commitRemoteHead.uid())) {
                leastFound = v;
                break;
            }
            for (String item: parents) {
                if (!marked.contains(item)) {
                    q.add(item);
                    marked.add(item);
                    Commit commitItem = readObject(join(COMMITS, item), Commit.class);
                    for (Map.Entry<String, String> entry: commitItem.getFiles().entrySet()) {
                        byte[] blob = readContents(join(BLOBS, entry.getValue()));
                        writeObject(join(remoteDirectory, java.io.File.separator
                                + "blobs" + java.io.File.separator + entry.getValue()), blob);
                    }
                    writeObject(join(remoteDirectory, java.io.File.separator + "commits"
                            + java.io.File.separator + commitItem.uid()), commitItem);
                }
            }
        }
        Commit lastCommit = readObject(join(COMMITS, leastFound), Commit.class);
        lastCommit.addParentForCommit(commitRemoteHead.uid());
        writeObject(join(remoteDirectory, java.io.File.separator
                + "commits" + java.io.File.separator + lastCommit.uid()), lastCommit);
        remoteData.removeBranch(remoteBranchName);
        remoteData.addBranch(remoteBranchName, headLocal.uid());
        remoteData.setHeadPointer(headLocal.uid());
        remoteData.saveData();
    }

    /**
     * Brings down commits from
     * the remote Gitlet repository into the local Gitlet repository
     * @param remoteName
     * @param remoteBranchName
     */
    public static void fetch(String remoteName, String remoteBranchName) {
        Data temp = readObject(DATA, Data.class);
        String previousBranch = temp.getCurrentBranch();
        Map<String, String> remotes = temp.getRemotes();
        if (!remotes.containsKey(remoteName) || !join(remotes.get(remoteName)).exists()) {
            Utils.exitWithError("Remote directory not found.");
        }
        String remoteDirectory = remotes.get(remoteName);
        Data remoteData = readObject(join(remoteDirectory, "data"), Data.class);
        if (!remoteData.getBranches().containsKey(remoteBranchName)) {
            Utils.exitWithError("That remote does not have that branch.");
        }
        String newName = remoteName + java.io.File.separator + remoteBranchName;
        if (!temp.getBranches().containsKey(newName)) {
            branch(newName);
        }
        temp.setCurrentBranch(newName);
        for (String commitName : plainFilenamesIn(join(remoteDirectory, "commits"))) {
            if (!join(COMMITS, commitName).exists()) {
                File commitsFolder = join(remoteDirectory, "commits");
                Commit commitItem = readObject(join(commitsFolder, commitName), Commit.class);
                for (Map.Entry<String, String> entry: commitItem.getFiles().entrySet()) {
                    if (!join(BLOBS, entry.getValue()).exists()) {
                        byte[] blob = readContents(join(join(remoteDirectory, "blobs"),
                                entry.getValue()));
                        writeObject(join(BLOBS, entry.getValue()), blob);
                    }
                }
                writeObject(join(COMMITS, commitItem.uid()), commitItem);
            }
        }
        temp.addBranch(newName, remoteData.getBranches().get(remoteBranchName));
        temp.setCurrentBranch(previousBranch);
        temp.setHeadPointer(remoteData.getBranches().get(remoteBranchName));
    }

    public static void pull(String remoteName, String remoteBranchName) {
        Data temp = readObject(DATA, Data.class);
        fetch(remoteName, remoteBranchName);
        merge(temp.getCurrentBranch());
    }

    /**
     * helper method to check the nonsplit files
     * @param main
     * @param sub
     * @return boolean if mergeConflict occured
     */
    private static boolean checkNonSplitFiles(Map<String, String> main, Map<String, String>  sub) {
        boolean mergeConflict = false;
        for (Map.Entry<String, String> entry: main.entrySet()) {
            String fileName = entry.getKey();
            String current = "";
            String given = "";
            if (sub.containsKey(entry.getKey())
                    && !entry.getValue().equals(sub.get(entry.getKey()))) {
                mergeConflict = true;
                mergeConflict(current, given, fileName);
            }
        }
        return mergeConflict;
    }
    /**
     * creates a merge conflict
     * @param current
     * @param given
     * @param fileName
     */
    private static void mergeConflict(String current, String given, String fileName) {
        String currentContents = "";
        String givenContents = "";
        if (!current.equals("") && join(BLOBS, current).exists()) {
            currentContents = Utils.readContentsAsString(join(BLOBS, current));
        }
        if (!given.equals("") && join(BLOBS, given).exists()) {
            givenContents = Utils.readContentsAsString(join(BLOBS, given));
        }
        String content = "<<<<<<< HEAD\n"
                + currentContents
                + "=======\n"
                + givenContents
                + ">>>>>>>\n";
        Utils.writeContents(join(CWD, fileName), content);
        add(fileName);
    }

    /**
     * abbreviates all the commits to fit the given length
     * @param length
     * @return
     */
    private static Map<String, String> abbreviated(int length) {
        Map<String, String> abbreviationMap = new HashMap<>();
        for (String item : plainFilenamesIn(COMMITS)) {
            String key = item.substring(0, length);
            abbreviationMap.put(key, item);
        }
        return abbreviationMap;
    }

    /**
     * commit with multiple parents when merge is successfull
     * @param message
     * @param other
     */
    private static void mergeCommit(String message, String other) {
        if (!GITLET_DIR.exists()) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        }
        Data temp = readObject(DATA, Data.class);
        String headPointer = temp.getHeadPointer(); //grabs the headpointer, for parent
        File cloneFile = Utils.join(COMMITS, headPointer);
        Commit cloneCommit = readObject(cloneFile, Commit.class);
        Map<String, String> add = temp.getStagedAdd();
        Map<String, String> remove = temp.getStagedRemove();
        Map<String, String> branches = temp.getBranches();
        File otherFile = Utils.join(COMMITS, branches.get(other));
        cloneCommit.setDataMulti(add);
        cloneCommit.removeDataMulti(remove); // untracks all the data in stagedRemove in clone
        cloneCommit.setTimestampCurrent(); //changes the new timestamp to be the current timestamp
        cloneCommit.addParentForCommit(branches.get(temp.getCurrentBranch()));
        cloneCommit.addParent(branches.get(other)); //adds the parent to the list
        cloneCommit.setMerged();
        cloneCommit.setMessage(message); //resets the message to the given message
        cloneCommit.giveUID(Utils.sha1(serialize(cloneCommit)));
        cloneCommit.saveCommit();
        temp.clearStagingArea();
        temp.removeBranch(temp.currentBranch);
        temp.addBranch(temp.getCurrentBranch(), cloneCommit.uid());
        temp.setHeadPointer(cloneCommit.uid());
        temp.saveData();
    }

    private static String findSplitPoint(List<String> list1, List<String> list2) {
        for (String items: list1) {
            if (list2.contains(items)) {
                return items;
            }
        }
        return null;
    }

    /**
     * finds the least common ancestor of two commits or null if there is none
     * @param commit
     * @return LCA or null
     */
    private static List<String> bfs(Commit commit) {
        Queue<String> q = new PriorityQueue<String>();
        ArrayList<String> marked = new ArrayList<>();
        q.add(commit.uid());
        marked.add(commit.uid());
        while (!q.isEmpty()) {
            String v = q.poll();
            Commit vCommit = readObject(join(COMMITS, v), Commit.class);
            if (vCommit.getParents() == null) {
                continue;
            }
            for (String item: vCommit.getParents()) {
                if (!marked.contains(item)) {
                    q.add(item);
                    marked.add(item);
                }
            }
        }
        return marked;
    }

    /**
     * checks if a file will be overwritten by the other commit since it is not tracked
     * @param current
     * @param other
     * @param stagedAdd
     * @param stagedRemove
     */
    private static void overWrite(Commit current, Commit other,
                                  Map<String, String> stagedAdd, Map<String, String> stagedRemove) {
        Map<String, String> otherFiles = other.getFiles();
        Map<String, String> currentFiles = current.getFiles();
        for (Map.Entry<String, String> entry: otherFiles.entrySet()) {
            String key = entry.getKey();
            if (join(CWD, key).exists() && !currentFiles.containsKey(key)
                    && !stagedAdd.containsKey(key) && !stagedRemove.containsKey(key)) {
                Utils.exitWithError("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
    }
}
