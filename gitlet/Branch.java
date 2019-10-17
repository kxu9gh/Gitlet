package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {
    private String name;

    private String head;

    public Branch(String name, String head) {
        this.name = name;
        this.head = head;
    }

    public String getName() {
        return this.name;
    }

    public String getHead() {
        return this.head;
    }

    public void setHead(String id) {
        this.head = id;
    }
    public Commit getCommit() {
        return (Commit) Commit.deserialize(this.head);
    }
}
