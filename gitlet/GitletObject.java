package gitlet;

import java.io.File;
import java.io.Serializable;

public class GitletObject implements Serializable {
    protected String ID;
    protected static final File DIR = new File(".gitlet");

    public void serialize() {
        Utils.serialize(this, ID, DIR);
    }
    public static GitletObject deserialize(String id) {
        return (GitletObject) Utils.deserialize(id, DIR);
    }

    protected void setID(Object... vals) {
        this.ID = Utils.sha1(vals);
    }
}
