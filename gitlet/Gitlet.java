package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Gitlet extends GitletObject {
    private String currCommitID;
    private Branch currBranch;
    private HashSet<String> allCommits;
    private HashMap<String, Branch> allBranches;
    private HashMap<String, String> recordedBlobs;
    private HashMap<String, String> shortID;

    //public Gitlet() { }

    @Override
    public void serialize() {
        Utils.serialize(this, "gitlet", DIR);
    }

    public static Gitlet deserialize() {
        return (Gitlet) Utils.deserialize("gitlet", DIR);
    }

    public void copy(Gitlet obj) {
        this.currCommitID = obj.currCommitID;
        this.currBranch = obj.currBranch;
        this.allBranches = obj.allBranches;
        this.allCommits = obj.allCommits;
        this.recordedBlobs = obj.recordedBlobs;
        this.shortID = obj.shortID;
    }

    public void init() {
        allCommits = new HashSet<>();
        allBranches = new HashMap<>();
        recordedBlobs = new HashMap<>();
        shortID = new HashMap<>();
        if (DIR.exists()) {
            System.out.print("A gitlet version-control system ");
            System.out.println("already exists in the current directory.");
            return;
        }
        DIR.mkdir();
        Commit newCommit = new Commit();
        currCommitID = newCommit.getID();
        allCommits.add(currCommitID);
        shortID.put(currCommitID.substring(0, 8), currCommitID);
        newCommit.serialize();
        currBranch = new Branch("master", currCommitID);
        allBranches.put(currBranch.getName(), currBranch);
        Stage stage = new Stage();
        stage.stageInit();
        stage.serialize();
        serialize();
    }

    public void add(String fileName) {
        File f = new File(fileName);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blob newBlob = new Blob(f);
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        Commit currCommit = gitlet.currBranch.getCommit();
        Stage currStage = new Stage(Stage.deserialize());
        currStage.deleteRemoved(fileName);
        if (currCommit.getBlobs().size() != 0 && currCommit.getBlobs().containsKey(fileName)
                && currCommit.getBlobs().get(fileName).equals(newBlob.ID)) {
            currStage.serialize();
            serialize();
            return;
        }
        currStage.add(fileName);
        currStage.serialize();
        serialize();
    }

    public void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
        }
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        Commit currCommit = (Commit) Commit.deserialize(currCommitID);
        HashMap<String, String> commitBlobs = currCommit.getBlobs();
        Stage currStage = new Stage(Stage.deserialize());
        HashMap<String, String> newBlobs = currStage.getStagedBlobs();
        if (commitBlobs != null) {
            for (String fileName : commitBlobs.keySet()) {
                if (!newBlobs.containsKey(fileName)
                        && !currStage.getRemovedBlobs().contains(fileName)) {
                    newBlobs.put(fileName, commitBlobs.get(fileName));
                }
            }
        }
        if (commitBlobs.equals(newBlobs)) {
            System.out.println("No changes added to the commit.");
            return;
        }

        Commit newCommit = new Commit(message, currCommitID, newBlobs, currCommit.getOrder() + 1);
        currCommitID = newCommit.getID();
        allCommits.add(currCommitID);
        shortID.put(currCommitID.substring(0, 8), currCommitID);
        newCommit.serialize();
        currBranch.setHead(currCommitID);
        recordedBlobs.putAll(currStage.getStagedBlobs());
        currStage.clear();
        currStage.serialize();
        serialize();
    }

    public void rm(String fileName) {
        //maybe not needed
//        File f = new File(fileName);
//        if (!f.exists()) {
//            System.out.println("File does not exist.");
//            return;
//        }

        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        Commit currCommit = (Commit) Commit.deserialize(currCommitID);
        HashMap<String, String> commitBlobs = currCommit.getBlobs();
        Stage currStage = new Stage(Stage.deserialize());
        HashMap<String, String> stagedBlobs = currStage.getStagedBlobs();
        // situation 1: file is in current commit
        if (commitBlobs.containsKey(fileName)) {
            File f = new File(fileName);
            String blobID = recordedBlobs.remove(fileName);
            //File blob = new File(DIR, blobID);
            Utils.restrictedDelete(f);
            //blob.delete();   //delete the Blob file as well
            if (stagedBlobs.containsKey(fileName)) {
                currStage.unstage(fileName);
            }
            currStage.addRemovedBlobs(fileName);
        } else if (stagedBlobs.containsKey(fileName)) {
            currStage.unstage(fileName);
        } else {
            System.out.println("No reason to remove the file.");
        }
        currStage.serialize();
        serialize();
    }

    public void log() {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        Commit currCommit = (Commit) Commit.deserialize(currCommitID);
        while (currCommit.getParent() != null) {
            currCommit.log();
            currCommit = (Commit) Commit.deserialize(currCommit.getParent());
        }
        currCommit.log();
        serialize();
    }

    public void globalLog() {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        for (String commitID : allCommits) {
            Commit commit = (Commit) Commit.deserialize(commitID);
            commit.log();
        }
        serialize();
    }

    public void find(String commitMsg) {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        boolean flag = true;
        for (String commitID : allCommits) {
            Commit commit = (Commit) Commit.deserialize(commitID);
            if (commit.getMessage().equals(commitMsg)) {
                System.out.println(commit.ID);
                flag = false;
            }
        }
        if (flag) {
            System.out.println("Found no commit with that message.");
        }
        serialize();
    }

    public void status() {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        Stage currStage = new Stage(Stage.deserialize());
        System.out.println("=== Branches ===");
        ArrayList<String> branches = new ArrayList<>();
        String curr = currBranch.getName();
        for (String b : allBranches.keySet()) {
            branches.add(b);
        }
        Collections.sort(branches);
        for (String s : branches) {
            if (s.equals(curr)) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }

        System.out.println("\n=== Staged Files ===");
        HashMap<String, String> stagedBlobs = currStage.getStagedBlobs();
        ArrayList<String> blobs = new ArrayList<String>(stagedBlobs.keySet());
        Collections.sort(blobs);
        for (String b : blobs) {
            System.out.println(b);
        }
        System.out.println("\n=== Removed Files ===");
        ArrayList<String> rmblobs = new ArrayList<String>(currStage.getRemovedBlobs());
        Collections.sort(blobs);
        for (String r : rmblobs) {
            System.out.println(r);
        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        System.out.println("\n=== Untracked Files ===\n");
        currStage.serialize();
        serialize();
    }

    public void branch(String branchName) {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        if (allBranches.keySet().contains(branchName)) {
            System.out.println("A branch with that name already exists.");
        } else {
            Branch newbranch = new Branch(branchName, currCommitID);
            allBranches.put(newbranch.getName(), newbranch);
        }
        serialize();
    }

    public void rmBranch(String branchName) {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        if (!allBranches.keySet().contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else if (branchName.equals(currBranch.getName())) {
            System.out.println("Cannot remove the current branch.");
        } else {
            allBranches.remove(branchName);
        }
        serialize();
    }

    public void checkout(String fileName) {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        if (!getCurrCommit().getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
        } else {
            String hash = getCurrCommit().getBlobs().get(fileName);
            Blob blob = (Blob) Blob.deserialize(hash);
            byte[] content = blob.getContent();
            File f = new File(fileName);
            Utils.writeContents(f, content);
        }
        serialize();
    }

    public void checkoutBranch(String branchName) {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        Stage currStage = new Stage(Stage.deserialize());
        if (!allBranches.keySet().contains(branchName)) {
            System.out.println("No such branch exists.");
        } else if (branchName.equals(currBranch.getName())) {
            System.out.println("No need to checkout the current branch.");
        } else {
            List<String> workFiles = Utils.plainFilenamesIn(System.getProperty("user.dir"));
            Commit checkoutTo = allBranches.get(branchName).getCommit();
            Commit curr = currBranch.getCommit();
            for (String fileName : workFiles) {
                if (checkoutTo.getBlobs().containsKey(fileName)
                        && !curr.getBlobs().containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way; "
                           + "delete it or add it first.");
                    return;
                } else if (!checkoutTo.getBlobs().containsKey(fileName)
                        && curr.getBlobs().containsKey(fileName)) {
                    File f = new File(fileName);
                    Utils.restrictedDelete(f);
                }
            }
            for (String b : checkoutTo.getBlobs().keySet()) {
                String hash = checkoutTo.getBlobs().get(b);
                Blob blob = (Blob) Blob.deserialize(hash);
                byte[] content = blob.getContent();
                File f = new File(b);
                Utils.writeContents(f, content);
            }
            currStage.clear();
            currBranch = allBranches.get(branchName);
            currCommitID = checkoutTo.getID();
        }
        currStage.serialize();
        serialize();
    }

    public void checkout(String commitId, String fileName) {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        if (allCommits != null && commitId.length() == 8) {
            for (String s : allCommits) {
                if (s.startsWith(commitId)) {
                    checkout(s, fileName);
                    return;
                }
            }
        }
        if (!allCommits.contains(commitId)) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit previousCommit = (Commit) Commit.deserialize(commitId);
            if (!previousCommit.getBlobs().containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
            } else {
                String hash = previousCommit.getBlobs().get(fileName);
                Blob blob = (Blob) Blob.deserialize(hash);
                byte[] content = blob.getContent();
                File f = new File(fileName);
                Utils.writeContents(f, content);
            }
        }
        serialize();
    }

    public void reset(String commitId) {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        if (allCommits != null && commitId.length() == 8) {
            for (String s : allCommits) {
                if (s.startsWith(commitId)) {
                    reset(s);
                    return;
                }
            }
        }
        Stage currStage = new Stage(Stage.deserialize());
        if (!allCommits.contains(commitId)) {
            System.out.println("No commit with that id exists.");
        } else {
            List<String> workFiles = Utils.plainFilenamesIn(System.getProperty("user.dir"));
            Commit checkoutTo = (Commit) Commit.deserialize(commitId);
            Commit curr = currBranch.getCommit();
            for (String fileName : workFiles) {
                if (checkoutTo.getBlobs().containsKey(fileName)
                        && !curr.getBlobs().containsKey(fileName)) {
                    System.out.println("There is an untracked file "
                           + "in the way; delete it or add it first.");
                    return;
                } else if (!checkoutTo.getBlobs().containsKey(fileName)
                        && curr.getBlobs().containsKey(fileName)) {
                    File f = new File(fileName);
                    Utils.restrictedDelete(f);
                }
            }
            for (String b : checkoutTo.getBlobs().keySet()) {
                String hash = checkoutTo.getBlobs().get(b);
                Blob blob = (Blob) Blob.deserialize(hash);
                byte[] content = blob.getContent();
                File f = new File(b);
                Utils.writeContents(f, content);
            }
            currStage.clear();
            currCommitID = checkoutTo.getID();
            currBranch.setHead(currCommitID);
        }
        currStage.serialize();
        serialize();
    }

    //if current commit's is direct ancestor of given one,
    // return 1; reverse return -1; else return 0;
    private int directAncestor = 0;

    public void merge(String branchName) {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        Stage currStage = new Stage(Stage.deserialize());
        //check if branch exist:
        if (!allBranches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        //check uncommited changes
        if (!currStage.getStagedBlobs().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        currStage.serialize();
        Commit currCommit = currBranch.getCommit();
        Branch given = allBranches.get(branchName);
        Commit givenCommit = given.getCommit();
        // check if given branch is current branch:
        if (currCommit.getID().equals(givenCommit.getID())) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        List<String> workFiles = Utils.plainFilenamesIn(System.getProperty("user.dir"));
        for (String fileName : workFiles) {
            if (givenCommit.getBlobs().containsKey(fileName)
                    && !currCommit.getBlobs().containsKey(fileName)) {
                System.out.println("There is an untracked "
                        + "file in the way; delete it or add it first.");
                return;
            }
        }
        //first part: check if the situation
        // is fast-forward or is-ancestor: its actually switch
        Commit splitCommit = findSplit(currCommit, givenCommit);
        if (splitCommit.getID().equals(givenCommit.getID())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        } else if (splitCommit.getID().equals(currCommit.getID())) {
            currBranch = given;
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        actualMerge(branchName);
    }

    private void actualMerge(String branchName) {
        Commit currCommit = currBranch.getCommit();
        Branch given = allBranches.get(branchName);
        Commit givenCommit = given.getCommit();
        Commit splitCommit = findSplit(currCommit, givenCommit);
        // actual merging process
        boolean inConflict = false;
        // merge all files that exist in all three places
        HashSet<String> allFileNames = new HashSet<>();
        allFileNames.addAll(splitCommit.getBlobs().keySet());
        allFileNames.addAll(currCommit.getBlobs().keySet());
        allFileNames.addAll(givenCommit.getBlobs().keySet());
        for (String fileName : allFileNames) {
            // if the file is in split commit
            if (splitCommit.getBlobs().containsKey(fileName)) {
                String spFileID = splitCommit.getBlobs().get(fileName);

                // for the file in currCommit
                if (!currCommit.getBlobs().containsKey(fileName)) {
                    if (givenCommit.getBlobs().containsKey(fileName)
                            && !givenCommit.getBlobs().get(fileName).equals(spFileID)) {
                        solveConflict(fileName, given);
                        inConflict = true;
                    }
                } else if (currCommit.getBlobs().get(fileName).equals(spFileID)) {
                    if (!givenCommit.getBlobs().containsKey(fileName)) {
                        removeSpecial(fileName);       //might be fragile
                    } else if (!givenCommit.getBlobs().get(fileName).equals(spFileID)) {
                        stageNewVersion(givenCommit.ID, fileName);
                    }
                } else {
                    if (!givenCommit.getBlobs().containsKey(fileName)) {
                        solveConflict(fileName, given);
                        inConflict = true;
                    } else if (!givenCommit.getBlobs().get(fileName).equals(spFileID)) {
                        solveConflict(fileName, given);
                        inConflict = true;
                    }
                }
            } else {
                if (currCommit.getBlobs().containsKey(fileName)) {
                    if (givenCommit.getBlobs().containsKey(fileName)) {
                        solveConflict(fileName, given);
                        inConflict = true;
                    }
                } else {
                    if (givenCommit.getBlobs().containsKey(fileName)) {
                        stageNewVersion(givenCommit.ID, fileName);
                    }
                }
            }
        }
        if (!inConflict) {
            String msg = "Merged " + currBranch.getName()
                   + " with " + given.getName() + ".";
            nakedCommit(msg);
        } else {
            System.out.println("Encountered a merge conflict.");
        }
        serialize();
    }


    public void solveConflict(String fileName, Branch branch) {
        conflictFile(fileName, branch);
    }

    public void nakedCommit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
        }
        Commit currCommit = (Commit) Commit.deserialize(currCommitID);
        HashMap<String, String> commitBlobs = currCommit.getBlobs();
        Stage currStage = new Stage(Stage.deserialize());
        HashMap<String, String> newBlobs = currStage.getStagedBlobs();
        if (commitBlobs != null) {
            for (String fileName : commitBlobs.keySet()) {
                if (!newBlobs.containsKey(fileName)
                        && !currStage.getRemovedBlobs().contains(fileName)) {
                    newBlobs.put(fileName, commitBlobs.get(fileName));
                }
            }
        }
        if (commitBlobs.equals(newBlobs)) {
            System.out.println("No changes added to the commit.");
            return;
        }

        Commit newCommit = new Commit(message, currCommitID,
                newBlobs, currCommit.getOrder() + 1);
        currCommitID = newCommit.getID();
        allCommits.add(currCommitID);
        shortID.put(currCommitID.substring(0, 8), currCommitID);
        newCommit.serialize();
        currBranch.setHead(currCommitID);
        recordedBlobs.putAll(currStage.getStagedBlobs());
        currStage.clear();
        currStage.serialize();
    }

    public void removeSpecial(String fileName) {
        Commit currCommit = (Commit) Commit.deserialize(currCommitID);
        HashMap<String, String> commitBlobs = currCommit.getBlobs();
        Stage currStage = new Stage(Stage.deserialize());
        HashMap<String, String> stagedBlobs = currStage.getStagedBlobs();
        // situation 1: file is in current commit
        if (commitBlobs.containsKey(fileName)) {
            File f = new File(fileName);
            String blobID = recordedBlobs.remove(fileName);
            //File blob = new File(DIR, blobID);
            Utils.restrictedDelete(f);
            //blob.delete();   //delete the Blob file as well
            if (stagedBlobs.containsKey(fileName)) {
                currStage.unstage(fileName);
            }
            currStage.addRemovedBlobs(fileName);
        } else if (stagedBlobs.containsKey(fileName)) {
            currStage.unstage(fileName);
        } else {
            System.out.println("No reason to remove the file.");
        }
        currStage.serialize();
    }

    public void stageNewVersion(String commitID, String fileName) {
        if (!allCommits.contains(commitID)) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit previousCommit = (Commit) Commit.deserialize(commitID);
            if (!previousCommit.getBlobs().containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
            } else {
                String hash = previousCommit.getBlobs().get(fileName);
                Blob blob = (Blob) Blob.deserialize(hash);
                byte[] content = blob.getContent();
                File f = new File(fileName);
                Utils.writeContents(f, content);
            }
        }
        //ADD
        File f = new File(fileName);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blob newBlob = new Blob(f);
        Commit currCommit = currBranch.getCommit();
        Stage currStage = new Stage(Stage.deserialize());
        currStage.deleteRemoved(fileName);
        if (currCommit.getBlobs().size() != 0
                && currCommit.getBlobs().containsKey(fileName)
                && currCommit.getBlobs().get(fileName).equals(newBlob.ID)) {
            currStage.serialize();
            serialize();
            return;
        }
        currStage.add(fileName);
        currStage.serialize();
    }

    public Commit findSplit(Commit curr, Commit given) {
        if (curr.ID.equals(given.ID)) {
            return curr;
        }
        if (curr.getOrder() > given.getOrder()) {
            Commit parentCommit = (Commit) Commit.deserialize(curr.getParent());
            return findSplit(parentCommit, given);
        } else {
            Commit parentCommit = (Commit) Commit.deserialize(given.getParent());
            return findSplit(parentCommit, curr);
        }
    }

    public void testHelper(String commitName, String fileName) {
        Gitlet gitlet = deserialize();
        this.copy(gitlet);
        for (String commitID : allCommits) {
            Commit commit = (Commit) Commit.deserialize(commitID);
            if (commit.getMessage().equals(commitName)) {
                checkout(commitID.substring(0, 8), fileName);
            } else {
                checkout("");
            }

        }
        serialize();
    }

    public void conflictFile(String filename, Branch other) {
        File file = new File(filename);
        Commit currCommit = currBranch.getCommit();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write("<<<<<<< HEAD\n".getBytes());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }

        if (!file.exists()) {
            Utils.writeContents(file, new byte[]{});
        }
        if (currCommit.getBlobs().keySet().contains(filename)) {
            Blob blob = (Blob) Blob.deserialize(currCommit.getBlobs().get(filename));
            try {
                outputStream.write(blob.getContent());
            } catch (IOException excp) {
                throw new IllegalArgumentException(excp.getMessage());
            }
        }
        try {
            outputStream.write("=======\n".getBytes());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
        if (other.getCommit().getBlobs().keySet().contains(filename)) {
            Blob blob2 = (Blob) Blob.deserialize(other.getCommit().getBlobs().get(filename));
            try {
                outputStream.write(blob2.getContent());
            } catch (IOException excp) {
                throw new IllegalArgumentException(excp.getMessage());
            }
        }
        try {
            outputStream.write(">>>>>>>\n".getBytes());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
        Utils.writeContents(file, outputStream.toByteArray());

    }

    //model
//    public void commit(String message) {
//        Gitlet gitlet = deserialize();
//        this.copy(gitlet);
//        Commit currCommit = (Commit) Commit.deserialize(currCommitID);
//        HashMap<String, String> commitBlobs = currCommit.getBlobs();
//        Stage currStage = new Stage(Stage.deserialize());
//        HashMap<String, String> newBlobs = currStage.getStagedBlobs();
//
//        currStage.serialize();
//        serialize();
//    }
//
    //for test purpose, here are some get methods
    public String getCurrCommitID() {
        return currCommitID;
    }

    public Branch getCurrBranch() {
        return currBranch;
    }

    public Commit getCurrCommit() {
        return currBranch.getCommit();
    }
}
