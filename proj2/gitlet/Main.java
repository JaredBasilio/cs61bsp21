package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Jared Basilio
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        String message;
        String fileName;
        String branchName;
        String commitID;
        switch(firstArg) {
        case "init":
            Repository.init();
            break;
        case "add":
            validateNumArgs("add", args, 2);
            fileName = args[1];
            Repository.add(fileName);
            break;
        case "commit":
            validateNumArgs("add", args, 2);
            message = args[1];
            Repository.commit(message);
            break;
        case "rm":
            validateNumArgs("rm", args, 2);
            fileName = args[1];
            Repository.rm(fileName);
            break;
        case "log":
            Repository.log();
            break;
        case "global-log":
            Repository.globalLog();
            break;
        case "find":
            validateNumArgs("find", args, 2);
            message = args[1];
            Repository.find(message);
            break;
        case "status":
            Repository.status();
            break;
        case "checkout":
            if (args.length == 3 && args[1].equals("--")) {
                Repository.checkoutFile(args[2]);
            } else if (args.length == 4 && args[2].equals("--")) {
                Repository.checkoutID(args[1], args[3]);
            } else if (args.length == 2) {
                Repository.checkoutBranch(args[1]);
            } else {
                Utils.exitWithError("Incorrect operands.");
            }
            break;
        case "branch":
            validateNumArgs("branch", args, 2);
            branchName = args[1];
            Repository.branch(branchName);
            break;
        case "rm-branch":
            validateNumArgs("rm-branch", args, 2);
            branchName = args[1];
            Repository.rmbranch(branchName);
            break;
        case "reset":
            validateNumArgs("reset", args, 2);
            commitID = args[1];
            Repository.reset(commitID);
            break;
        case "merge":
            validateNumArgs("merge", args, 2);
            branchName = args[1];
            Repository.merge(branchName);
            break;
        case "add-remote":
            validateNumArgs("reset", args, 3);
            Repository.addRemote(args[1], args[2]);
            break;
        case "rm-remote":
            validateNumArgs("reset", args, 2);
            Repository.rmRemote(args[1]);
            break;
        case "push":
            validateNumArgs("push", args, 3);
            Repository.push(args[1], args[2]);
            break;
        case "fetch":
            validateNumArgs("fetch", args, 3);
            Repository.fetch(args[1], args[2]);
            break;
        case "pull":
            validateNumArgs("pull", args, 3);
            Repository.pull(args[1], args[2]);
            break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
        return;
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @source lab6
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Incorrect operands.", cmd));
        }
    }
}
