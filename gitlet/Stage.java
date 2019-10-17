package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class Stage extends GitletObject {
    //fields
    //Name, blob ID
    private HashMap<String, String> stagedBlobs;
    private HashSet<String> removedBlobs;

    public Stage() {
    }

    public Stage(Stage stage) {
        this.stagedBlobs = stage.stagedBlobs;
        this.removedBlobs = stage.removedBlobs;
    }

    public void stageInit() {
        stagedBlobs = new HashMap<>();
        removedBlobs = new HashSet<>();
    }

    public void add(String fileName) {
        File f = new File(fileName);
        Blob newBlob = new Blob(f);
        newBlob.serialize();
        stagedBlobs.put(fileName, newBlob.ID);
        removedBlobs.remove(fileName);
        newBlob.serialize();
    }

    public void addRemovedBlobs(String fileName) {
        removedBlobs.add(fileName);
    }

    public void deleteRemoved(String fileName) {
        removedBlobs.remove(fileName);
    }

    public void unstage(String fileName) {
        String blobID = stagedBlobs.remove(fileName);
        File blob = new File(blobID);
        Utils.restrictedDelete(blob);   //unstage means the blob should no longer be recorded
    }

    public HashMap getStagedBlobs() {
        return stagedBlobs;
    }

    public HashSet<String> getRemovedBlobs() {
        return removedBlobs;
    }

    public void clear() {
        HashSet<String> fileNameSet = new HashSet<>(stagedBlobs.keySet());
        for (String fileName : fileNameSet) {
            unstage(fileName);
        }
        removedBlobs = new HashSet<>();
    }

    @Override
    public void serialize() {
        Utils.serialize(this, "stage", DIR);
    }

    public static Stage deserialize() {
        return (Stage) Utils.deserialize("stage", DIR);
    }


}
