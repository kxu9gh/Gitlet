package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob extends GitletObject implements Serializable {
    //fields
    private byte[] content;
    private String name;

    public Blob(File f) {
        content = Utils.readContents(f);
        name = f.getName();
        setID(content, name);
    }

    public byte[] getContent() {
        return this.content;
    }

}
