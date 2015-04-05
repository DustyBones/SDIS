package Peer;

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

public class BackupProtocol {
    public static void run(String[] args) {
        MulticastSocket backupSocket, controlSocket;
        DatagramPacket chunkPacket, ackPacket;
        File file;
        FileInputStream fis;
        String fileID, received;
        int chunkN = 0, saved, attempt, timeout = 200;
        long t0, t1;
        byte[] chunkBuf, buf, msg;
        ArrayList<InetAddress> IPlist;

        try {
            backupSocket = new MulticastSocket(Peer.getMCBport());
            backupSocket.joinGroup(Peer.getMCBip());
            controlSocket = new MulticastSocket(Peer.getMCport());
            controlSocket.setSoTimeout(timeout);
            controlSocket.joinGroup(Peer.getMCip());
            file = new File(args[1]);
            fis = new FileInputStream(file);
            fileID = Util.getFileID(args[1]);
            chunkBuf = new byte[64000];
            buf = new byte[100];
            IPlist = new ArrayList<>();
            int k;
            while ((k = fis.read(chunkBuf)) > -1) {
                msg = Util.concatenateByteArrays(buildHeader(fileID, chunkN, args[2]).getBytes(), Arrays.copyOfRange(chunkBuf, 0, k));
                chunkPacket = new DatagramPacket(msg, msg.length, Peer.getMCBip(), Peer.getMCBport());
                ackPacket = new DatagramPacket(buf, buf.length);
                chunkN++;
                attempt = 1;
                saved = 0;
                IPlist.clear();
                do {
                    backupSocket.send(chunkPacket);
                    t0 = System.currentTimeMillis();
                    do {
                        try {
                            controlSocket.receive(ackPacket);
                            if (!InetAddress.getLocalHost().equals(ackPacket.getAddress())) {
                                received = new String(ackPacket.getData(), 0, ackPacket.getLength());
                                System.out.println("BackupProtocol - Received from " + ackPacket.getAddress() + ": " + received);
                                if (validateAcknowledge(ackPacket, IPlist, fileID, chunkN)) {
                                    saved++;
                                }
                            }
                        } catch (SocketTimeoutException ignore) {
                            System.out.println("nothing received");
                        }
                        t1 = System.currentTimeMillis();
                    } while ((t1 - t0) < (500 * attempt));
                    attempt++;
                } while (saved < Integer.parseInt(args[2]) && attempt <= 5);
                //TODO repeat until factor has been reached
                //TODO save IPaddress from ack peers
            }
            backupSocket.leaveGroup(Peer.getMCBip());
            backupSocket.close();
            controlSocket.leaveGroup(Peer.getMCip());
            controlSocket.close();
            fis.close();
        } catch (Exception ignore) {
            //e.printStackTrace();
        }
    }

    static String buildHeader(String fileID, int chunkN, String factor) {
        return "PUTCHUNK 1.0" + " " + fileID + " " + chunkN + " " + factor + " \r\n\r\n";
    }

    static boolean validateAcknowledge(DatagramPacket ack, ArrayList<InetAddress> ip, String fileID, int chunk) {
        boolean exists = false;
        String s = new String(ack.getData(), 0, ack.getLength());
        String[] msg = s.split("[ ]+");
        if (msg[0].equals("STORED") && msg[2].equals(fileID) && Integer.parseInt(msg[3]) == chunk) {
            for (InetAddress aIp : ip) {
                if (aIp.equals(ack.getAddress())) {
                    exists = true;
                }
            }
            if (!exists) {
                ip.add(ack.getAddress());
            }
            return true;
        }
        return false;
    }
}
