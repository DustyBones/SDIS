package Peer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class Peer {
    public static volatile boolean running = true;
    protected static InetAddress MCip;
    protected static int MCport;
    protected static InetAddress MCBip;
    protected static int MCBport;
    protected static InetAddress MCRip;
    protected static int MCRport;

    public static InetAddress getMCip() {
        return MCip;
    }

    public static int getMCport() {
        return MCport;
    }

    public static InetAddress getMCBip() {
        return MCBip;
    }

    public static int getMCBport() {
        return MCBport;
    }

    public static int getMCRport() {
        return MCRport;
    }

    public static InetAddress getMCRip() {
        return MCRip;
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            MCip = InetAddress.getByName("225.0.0.1");
            MCport = 9001;
            MCBip = InetAddress.getByName("225.0.0.1");
            MCBport = 9002;
            MCRip = InetAddress.getByName("225.0.0.1");
            MCRport = 9003;
        } else if (args.length == 6) {
            MCip = InetAddress.getByName(args[0]);
            MCport = Integer.parseInt(args[1]);
            MCBip = InetAddress.getByName(args[2]);
            MCBport = Integer.parseInt(args[3]);
            MCRip = InetAddress.getByName(args[4]);
            MCRport = Integer.parseInt(args[5]);
        } else {
            System.out
                    .println("Wrong number of arguments. Expected \"Server <MCip> <MCport> <MCBip> <MCBport> <MCRip> <MCRport>\"");
            return;
        }

        new ControlThread().start();
        new BackupThread().start();
        new RestoreThread().start();

        Scanner sc = new Scanner(System.in);
        while (Peer.running) {
            try {
                String input = sc.nextLine();
                String[] cmd = validateCmd(input);
                switch (cmd[0]) {
                    case "-1":
                        System.out.println("Invalid command or filename, type 'help' for a list of options");
                        break;
                    case "0":
                        Peer.running = false;
                        break;
                    case "1":
                        BackupProtocol.run(cmd);
                        break;
                    case "2":
                        RestoreProtocol.run(cmd);
                        System.out.println("Restoration complete");
                        break;
                    case "3":
                        DeleteProtocol.run(cmd);
                        System.out.println("Deletion complete.");
                        break;
                    case "4":
                        System.out.println("Possible operations:\n" +
                                "\tbackup <filename> <replication factor>\n" +
                                "\trestore <filename>\n" +
                                "\tdelete <filename>\n" +
                                "\texit\n");
                        break;
                    default:
                        return;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        sc.close();
    }

    public static String[] validateCmd(String s) throws Exception {
        String[] tokens = s.split("[ ]+");
        if (tokens[0].equals("exit")) {
            tokens[0] = "0";
        } else if (tokens[0].equals("backup") && tokens.length == 3 && Util.fileIsValid(tokens[1])
                && Integer.parseInt(tokens[2]) > 0 && Integer.parseInt(tokens[2]) < 10) {
            tokens[0] = "1";
        } else if (tokens[0].equals("restore") && tokens.length == 2) {
            tokens[0] = "2";
        } else if (tokens[0].equals("delete") && tokens.length == 2) {
            tokens[0] = "3";
        } else if (tokens[0].equals("help") && tokens.length == 1) {
            tokens[0] = "4";
        } else {
            tokens[0] = "-1";
        }
        //*****************testing only************************
        switch (s) {
            case "1":
                tokens = new String[3];
                tokens[0] = "1";
                tokens[1] = "test.txt";
                tokens[2] = "1";
                break;
            case "2":
                tokens = new String[3];
                tokens[0] = "1";
                tokens[1] = "test.jpg";
                tokens[2] = "2";
                break;
            case "3":
                tokens = new String[3];
                tokens[0] = "1";
                tokens[1] = "CharacterBuilder.zip";
                tokens[2] = "1";
                break;
            case "4":
                tokens = new String[3];
                tokens[0] = "2";
                tokens[1] = "test.txt";
                break;
            case "5":
                tokens = new String[3];
                tokens[0] = "2";
                tokens[1] = "test.jpg";
                break;
            case "6":
                tokens = new String[3];
                tokens[0] = "2";
                tokens[1] = "CharacterBuilder.zip";
                break;
        }
        //****************************************************
        return tokens;
    }
}

