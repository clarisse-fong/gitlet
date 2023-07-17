package gitlet;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {
    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
<<<<<<< HEAD
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        Gitlet gitlet = new Gitlet();

        switch (args[0]) {

            case "init":
                Gitlet.init();
                break;
            case "add":
                gitlet.add(args[1]);
                break;
            case "commit":
                if (args[1].equals("")) {
                    System.out.println("Please enter a commit message.");
                }
                gitlet.commit(args[1]);
                break;
            case "rm":
                gitlet.rm(args[1]);
                break;
            case "log":
                gitlet.log();
                break;
            case "global-log":
                gitlet.globalLog();
                break;
            case "find":
                gitlet.find(args[1]);
                break;
            case "status":
                gitlet.status();
                break;
            case "checkout":
                if (args[1].equals("--")) {
                    gitlet.checkoutFile(args[2]);
                    break;
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    gitlet.checkoutCommit(args[1], args[3]);
                    break;
                } else if (args.length == 2) {
                    gitlet.checkoutBranch(args[1]);
                    break;
                }
                break;
            case "branch":
                gitlet.branch(args[1]);
                break;
            case "rm-branch":
                gitlet.removeBranch(args[1]);
                break;
            case "reset":
                gitlet.reset(args[1]);
                break;
            case "merge":
                gitlet.merge(args[1]);
                break;
            default:
                System.out.println("File does not exist.");
=======
        // TODO: YOUR CODE HERE
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
            //exit(how do dis?)
        } else if (args[0] == "init") {
            if (args.length != 1) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            } else if (args[0] == "add") {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String message = args[1];
            } else if (args[0] == "commit") {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String message = args[1];
            } else if (args[0] == "rm") {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String FileName = args[1];
            } else if (args[0] == "log") {
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
            } else if (args[0] == "global-log") {
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
            } else if (args[0] == "find") {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String commitMessage = args[1];
            } else if (args[0] == "status") {
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
            } else if (args[0] == "checkout") {
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
            } else if (args[0] == "branch") {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String branchName = args[1];
            } else if (args[0] == "rm-branch") {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String branchName = args[1];
            } else if (args[0] == "reset") {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String commitID = args[1];
            } else if (args[0] == "merge") {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String branchName = args[1];
            } else {
                System.out.println("No command with that name exists.");
                System.exit(0);
            }
>>>>>>> 5c7b196d8c703f51ffcb5cd412a2d63765357e97
        }
    }
}
