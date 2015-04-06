package Peer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class Peer {
    protected static volatile boolean running = true;
    private static InetAddress MCip;
    private static int MCport;
    private static InetAddress MCBip;
    private static int MCBport;
    private static InetAddress MCRip;
    private static int MCRport;

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

        new BackupThread().start();
        new RestoreThread().start();
        new DeleteThread().start();

        Scanner sc = new Scanner(System.in);
        while (Peer.running) {
            try {
                String input = sc.nextLine();
                String[] cmd = validateCmd(input);
                switch (Integer.parseInt(cmd[0])) {
                    case -1:
                        System.out.println("Invalid command or filename, type 'help' for a list of options");
                        break;
                    case 0:
                        Peer.running = false;
                        break;
                    case 1:
                        BackupProtocol.run(cmd, false);
                        break;
                    case 2:
                        RestoreProtocol.run(cmd);
                        break;
                    case 3:
                        DeleteProtocol.run(cmd);
                        break;
                    case 4:
                        ReclaimProtocol.run();
                        break;
                    case 5:
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
        } else if (tokens[0].equals("reclaim") && tokens.length == 1) {
            tokens[0] = "4";
        } else if (tokens[0].equals("help") && tokens.length == 1) {
            tokens[0] = "5";
        } else {
            tokens[0] = "-1";
        }
        return tokens;
    }
}

