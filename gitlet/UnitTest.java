package gitlet;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/* The suite of all JUnit tests for the gitlet package.
   @author
 */
public class UnitTest {

    @Test
    public void addTest() {
        Gitlet test2 = new Gitlet();
        test2.init();
        Utils.writeContents(new File("test.txt"), new byte[]{});
        Utils.writeContents(new File("test2.txt"), new byte[]{});
        test2.add("test.txt");
        Stage s = Stage.deserialize();
        assertTrue(s.getStagedBlobs().keySet().contains("test.txt"));
        test2.add("test2.txt");
        Stage s2 = Stage.deserialize();
        assertTrue(s2.getStagedBlobs().keySet().contains("test2.txt"));
        test2.status();
        test2.branch("master");
        test2.branch("jie");
        test2.status();
        test2.rmBranch("master");
        test2.rmBranch("jie");
        test2.status();
        delete();

    }


    @Test
    public void commitTest() {
        Main.main("init");
        Utils.writeContents(new File("test.txt"), new byte[]{});
        Utils.writeContents(new File("test2.txt"), new byte[]{1});
        Main.main("add", "test.txt");
        Main.main("commit", "test1");

        Gitlet test = Gitlet.deserialize();
        Commit curr = test.getCurrCommit();
        assertEquals("test1", curr.getMessage());
        assertTrue(curr.getBlobs().containsKey("test.txt"));

        Main.main("add", "test2.txt");
        Main.main("commit", "add test2");

        Gitlet test2 = Gitlet.deserialize();
        Commit curr2 = test2.getCurrCommit();
        assertEquals("add test2", curr2.getMessage());
        System.out.println(curr2.getBlobs().containsKey("test2.txt"));
        System.out.println(curr2.getBlobs().containsKey("test.txt"));
        delete();
    }

    @Test
    public void rmTest() {
        Main.main("init");
        Utils.writeContents(new File("test.txt"), new byte[]{});
        Utils.writeContents(new File("test2.txt"), new byte[]{1});
        Main.main("add", "test.txt");
        Main.main("commit", "test1");
        Main.main("rm", "test.txt");
        Main.main("commit", "removeTest");

        Gitlet test = Gitlet.deserialize();
        Commit curr = test.getCurrCommit();
        assertEquals("removeTest", curr.getMessage());
        assertFalse(curr.getBlobs().containsKey("test.txt"));
        delete();
    }

    @Test
    public void logTest() {
        Main.main("init");
        Utils.writeContents(new File("test.txt"), new byte[]{});
        Utils.writeContents(new File("test2.txt"), new byte[]{1});
        Utils.writeContents(new File("test3.txt"), new byte[]{1});
        Main.main("add", "test.txt");
        Main.main("commit", "test1");
        Main.main("rm", "test.txt");
        Main.main("commit", "removeTest");
        //Main.main("log");
        Main.main("add", "test3.txt");
        Main.main("commit", "some");
        Main.main("global-log");

        delete();
    }

    @Test
    public void addTest2() {
        Main.main("init");
        Utils.writeContents(new File("test.txt"), new byte[]{});
        Utils.writeContents(new File("test2.txt"), new byte[]{1});
        Main.main("add", "test.txt");
        Main.main("commit", "test1");
        Main.main("add", "test2.txt");
        Main.main("commit", "test1");
        Main.main("rm", "test.txt");
        Utils.writeContents(new File("test.txt"), new byte[]{});
        Main.main("commit", "test1");
        Main.main("find", "test");
        delete();
    }

    @Test

    public void checkoutTest() {
        Main.main("init");
        createFile("f.txt", "f");
        createFile("g.txt", "g");
        Main.main("add", "f.txt");
        Main.main("commit", "test1");
        Main.main("add", "g.txt");
        Main.main("commit", "test2");
        createFile("f.txt", "fghijk");
        Main.main("add", "f.txt");
        Main.main("commit", "version 2 f.txt");
        //Main.main("checkout", "--", "f.txt");
        Main.main("test", "test1", "--", "f.txt");
        delete();
    }

    @Test
    public void checkout2Test() {
        Main.main("init");
        createFile("f.txt", "f");
        createFile("g.txt", "g");
        Main.main("add", "f.txt");
        Main.main("commit", "test1");
        Main.main("status");
        Main.main("add", "f.txt");
        Main.main("commit", "test2");
        //Main.main("test", "test1", "--", "g.txt");
        Main.main("checkout", "jhfouewjdpasjnvldsnfpekd", "--", "f.txt");
        Main.main("status");
        createFile("f.txt", "fghijk");
        Main.main("add", "f.txt");
        Main.main("commit", "version 2 f.txt");
        //Main.main("checkout", "--", "f.txt");

        delete();
    }

    @Test
    public void mergeTest() {
        Main.main("init");
        createFile("f.txt", "f");
        createFile("g.txt", "g");
        Main.main("add", "f.txt");
        Main.main("commit", "test1");
        Main.main("branch", "newBranch");
        Main.main("add", "g.txt");
        Main.main("commit", "test2");
        //Main.main("log");

        Main.main("checkout", "newBranch");
        createFile("h.txt", "H");
        Main.main("add", "h.txt");
        Main.main("commit", "test3");

        Main.main("checkout", "master");
        createFile("h.txt", "Hgji");
        Main.main("add", "h.txt");
        //Main.main("status");
        Main.main("commit", "test4");

        Main.main("merge", "newBranch");
        //Main.main("log");
        //Main.main("rm-branch", "newBranch");
        //Main.main("status");
        Main.main("status");
        delete();
    }

    @Test
    public void testConflict() {
        Main.main("init");
        createFile("f.txt", "f");
        createFile("g.txt", "g");
        Main.main("add", "f.txt");
        Main.main("add", "g.txt");
        Main.main("commit", "two files");
        Main.main("branch", "other");
        createFile("h.txt", "wug2.txt");
        Main.main("add", "h.txt");
        Main.main("rm", "g.txt");
        createFile("f.txt", "wug2.txt");
        Main.main("add", "f.txt");
        Main.main("commit", "Add h.txt, remove g.txt, and change f.txt");
        Main.main("checkout", "other");
        createFile("f.txt", "notwug.txt");
        Main.main("add", "f.txt");
        createFile("k.txt", "wug3.txt");
        Main.main("add", "k.txt");
        Main.main("commit", "Add k.txt and modify f.txt");
        Main.main("checkout", "master");
        Main.main("merge", "other");
        //Main.main("log");

        Main.main("status");
        delete();

    }

    @Test
    public void merge2Test() {
        Main.main("init");
        createFile("f.txt", "f");
        createFile("g.txt", "g");
        Main.main("add", "f.txt");
        Main.main("commit", "test1");
        Main.main("branch", "newBranch");
        Main.main("add", "g.txt");
        Main.main("commit", "test2");
        //Main.main("log");

        Main.main("checkout", "newBranch");
        createFile("h.txt", "H");
        Main.main("add", "h.txt");
        Main.main("commit", "test3");

        Main.main("checkout", "master");
        //createFile("h.txt", "Hgji");
        //Main.main("add", "h.txt");
        //Main.main("status");
        //Main.main("commit", "test4");

        Main.main("merge", "newBranch");
        //Main.main("log");
        //Main.main("rm-branch", "newBranch");
        //Main.main("status");
        delete();
    }

    @Test
    public void mergeRemoveTest() {
        Main.main("init");
        createFile("f.txt", "f");
        createFile("g.txt", "g");
        Main.main("add", "f.txt");
        Main.main("commit", "test1");
        Main.main("add", "g.txt");
        Main.main("commit", "test2");
        Main.main("branch", "given");
        //Main.main("log");

        Main.main("checkout", "given");
        //createFile("h.txt", "H");
        Main.main("rm", "f.txt");
        Main.main("commit", "test3");

        Main.main("checkout", "master");
        createFile("h.txt", "H");
        Main.main("add", "h.txt");
        Main.main("commit", "test4");
        Main.main("merge", "given");
        Main.main("log");
        delete();
    }

    @Test
    public void branchTest() {
        Main.main("init");
        createFile("f.txt", "f");
        createFile("g.txt", "g");
        Main.main("add", "f.txt");
        Main.main("commit", "test1");
        Main.main("branch", "newBranch");
        Main.main("add", "g.txt");
        Main.main("commit", "test2");
        //Main.main("log");

        Main.main("checkout", "newBranch");
        createFile("h.txt", "H");
        Main.main("add", "h.txt");
        Main.main("commit", "test3");

        Main.main("checkout", "master");
        createFile("h.txt", "Hgji");
        Main.main("add", "h.txt");
        Main.main("commit", "test4");

        Main.main("checkout", "newBranch");
        //Main.main("log");
        //Main.main("rm-branch", "newBranch");
        //Main.main("status");
        delete();
    }

    @Test
    public void checkoutbranchTest() {
        Main.main("init");
        Main.main("checkout", "master");
        Main.main("checkout", "jie");
        Main.main("branch", "jie");
        Main.main("checkout", "jie");
        Main.main("status");
//        Utils.writeContents(new File("test.txt"), new byte[]{});
//        Utils.writeContents(new File("test2.txt"), new byte[]{1});
//        Main.main("add", "test.txt");
//        Main.main("commit", "test1");
//        Main.main("add", "test2.txt");
//        Main.main("commit", "test1");
//        Main.main("checkout", "--", "test.txt");
//        Main.main("checkout", "--", "test3.txt");
//        Utils.writeContents(new File("test.txt"), new byte[]{});
//        Main.main("commit", "test1");
//        Main.main("find", "test");
        delete();
    }

    public void delete() {
        File directory = new File(".").getAbsoluteFile();
        for (File file : directory.listFiles()) {
            if (file.getName().contains(".txt")
                    && !file.getName().equals(".txt")) {
                Utils.restrictedDelete(file);
            }
        }
        File dir = new File(".gitlet");
        if (dir.exists()) {
            for (String fileName: dir.list()) {
                File currFile = new File(dir, fileName);
                int prev = dir.list().length;
                currFile.delete();
                while (dir.list().length == prev) {
                    continue;
                }
            }
            dir.delete();
        }
    }

    public void createFile(String filename, String contents) {
        byte[] bytestuff = contents.getBytes();
        Utils.writeContents(new File(filename), bytestuff);
    }
}


