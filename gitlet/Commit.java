package gitlet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Commit extends GitletObject {
    //fields
    private String parent;
    private String message;
    private HashMap<String, String> blobs;
    private String timeStamp;
    private int order;

    public Commit() {
        this("initial commit", null, new HashMap<>(), 0);
    }

    public Commit(String inMessage, String parent, HashMap<String, String> blobs, int order) {
        this.message = inMessage;
        this.parent = parent;
        this.blobs = blobs;
        this.order = order;
        timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        if (blobs.keySet().size() > 0) {
            setID(this.message, this.parent, blobs.toString(), timeStamp);
        } else {
            setID(this.message, timeStamp);
        }
    }

    public void log() {
        System.out.println("===");
        System.out.println("Commit " + this.ID);
        System.out.println(timeStamp);
        System.out.println(message);
        System.out.println();
    }

    public String getID() {
        return this.ID;
    }

    public HashMap<String, String> getBlobs() {
        return this.blobs;
    }

    public int getOrder() {
        return this.order;
    }

    //test purpose: get methods
    public String getMessage() {
        return message;
    }

    public String getParent() {
        return parent;
    }
}
