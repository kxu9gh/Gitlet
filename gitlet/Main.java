package gitlet;

import java.util.ArrayList;
import java.util.Arrays;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {

    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        Gitlet gitlet = new Gitlet();
        ArrayList<String> input = new ArrayList<>();
        input.addAll(Arrays.asList(args));
        if (input.size() == 0) {
            System.out.println("Please input a command");
            return;
        }
        String command = input.remove(0);
        String operand = "";
        if (input.size() > 0) {
            operand = input.remove(0);
        }
        switch (command) {
            case "init":
                gitlet.init();
                break;
            case "add":
                gitlet.add(operand);
                break;
            case "commit":
                gitlet.commit(operand);
                break;
            case "status":
                gitlet.status();
                break;
            case "rm":
                gitlet.rm(operand);
                break;
            case "log":
                gitlet.log();
                break;
            case "global-log":
                gitlet.globalLog();
                break;
            case "find":
                gitlet.find(operand);
                break;
            case "branch":
                gitlet.branch(operand);
                break;
            case "rm-branch":
                gitlet.rmBranch(operand);
                break;
            case "checkout":
                if (operand.equals("--")) {
                    gitlet.checkout(input.get(0));
                    break;
                } else if (input.size() > 0 && input.get(0).equals("--")) {
                    gitlet.checkout(operand, input.get(1));
                    break;
                } else if (input.size() == 0) {
                    gitlet.checkoutBranch(operand);
                    break;
                } else {
                    System.out.println("Incorrect operands.");
                    break;
                }
            case "reset":
                gitlet.reset(operand);
                break;
            case "merge":
                gitlet.merge(operand);
                break;
            case "test":
                gitlet.testHelper(operand, input.get(1));
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }

}
