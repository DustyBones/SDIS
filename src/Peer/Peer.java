package Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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

    //backup test.jpg 1
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
                MulticastSocket multiSocket = new MulticastSocket(Peer.getMCport());
                byte[] buf;
                String input = sc.nextLine();
                buf = input.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, Peer.getMCip(), Peer.getMCport());
                String[] cmd = validateCmd(input);
                switch (cmd[0]) {
                    case "-1":
                        System.out.println("Invalid command or filename, type 'help' for a list of options\n");
                        break;
                    case "0":
                        Peer.running = false;
                        break;
                    case "1":
                        multiSocket.send(packet);
                        BackupProtocol.run(cmd);
                        System.out.println("Backup complete.\n");
                        break;
                    case "2":
                        multiSocket.send(packet);
                        RestoreProtocol.run(cmd);
                        System.out.println("Restoration complete\n");
                        break;
                    case "3":
                        multiSocket.send(packet);
                        DeleteProtocol.run(cmd);
                        System.out.println("Deletion complete.\n");
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
                multiSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
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
        } else if (tokens[0].equals("restore") && tokens.length == 2 && Util.fileIsValid(tokens[1])) {
            tokens[0] = "2";
        } else if (tokens[0].equals("delete") && tokens.length == 2 && Util.fileIsValid(tokens[1])) {
            tokens[0] = "3";
        } else if (tokens[0].equals("help") && tokens.length == 1) {
            tokens[0] = "4";
        } else {
            tokens[0] = "-1";
        }
        //*****************testing only************************
        if (s.equals("1")) {
            tokens = new String[3];
            tokens[0] = "1";
            tokens[1] = "test.txt";
            tokens[2] = "1";
        } else if (s.equals("2")) {
            tokens = new String[3];
            tokens[0] = "1";
            tokens[1] = "test.jpg";
            tokens[2] = "2";
        } else if (s.equals("3")) {
            tokens = new String[3];
            tokens[0] = "1";
            tokens[1] = "CharacterBuilder.zip";
            tokens[2] = "2";
        }
        //****************************************************
        return tokens;
    }
}

