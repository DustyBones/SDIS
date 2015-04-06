package Peer;

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class BackupProtocol {
    public static void run(String[] args) {
        MulticastSocket backupSocket, controlSocket;
        DatagramPacket chunkPacket, ackPacket;
        File file;
        FileInputStream fis;
        String fileID;
        int chunkN, saved, attempt, timeout;
        long t0, t1;
        byte[] chunkBuf, buf, msg;
        String[] temp;
        ArrayList<InetAddress> IPlist;
        ArrayList<String[]> chunkInfo, fileInfo;

        try {
            chunkInfo = Util.loadChunkInfo();
            fileInfo = Util.loadFileInfo();
            if (Util.fileExists(fileInfo, args[1])) {
                System.out.println("This file or a file with the same name was already backed up.");
                return;
            }
            backupSocket = new MulticastSocket(Peer.getMCBport());
            backupSocket.joinGroup(Peer.getMCBip());
            backupSocket.setLoopbackMode(true);
            controlSocket = new MulticastSocket(Peer.getMCport());
            controlSocket.joinGroup(Peer.getMCip());
            controlSocket.setLoopbackMode(true);
            file = new File(args[1]);
            fis = new FileInputStream(file);
            fileID = Util.getFileID(args[1]);
            chunkN = 0;
            chunkBuf = new byte[64000];
            buf = new byte[100];
            IPlist = new ArrayList<>();
            int k;
            while ((k = fis.read(chunkBuf)) > -1) {
                msg = Util.concatenateByteArrays(buildHeader(fileID, chunkN, args[2]).getBytes(StandardCharsets.ISO_8859_1), Arrays.copyOfRange(chunkBuf, 0, k));
                chunkPacket = new DatagramPacket(msg, msg.length, Peer.getMCBip(), Peer.getMCBport());
                ackPacket = new DatagramPacket(buf, buf.length);
                attempt = 1;
                saved = 0;
                IPlist.clear();
                do {
                    timeout = (int) (500 * Math.pow(2, attempt - 1) / Integer.parseInt(args[2]));
                    controlSocket.setSoTimeout(timeout);
                    backupSocket.send(chunkPacket);
                    t0 = System.currentTimeMillis();
                    do {
                        try {
                            controlSocket.receive(ackPacket);
                            String z = new String(ackPacket.getData(), 0, ackPacket.getLength(), StandardCharsets.ISO_8859_1);
                            int j = z.indexOf("\r\n\r\n");
                            z = new String(ackPacket.getData(), 0, j, StandardCharsets.ISO_8859_1);
                            System.out.println("BackupProtocol - Received from " + ackPacket.getAddress() + ": " + z);
                            if (validateAcknowledge(ackPacket, IPlist, fileID, chunkN)) {
                                saved++;
                            }
                        } catch (SocketTimeoutException ignore) {
                        }
                        t1 = System.currentTimeMillis();
                    } while ((t1 - t0) < (500 * Math.pow(2, attempt - 1)));
                    attempt++;
                } while (saved < Integer.parseInt(args[2]) && attempt <= 5);
                temp = new String[4];
                temp[0] = fileID;
                temp[1] = chunkN + "";
                temp[2] = args[2];
                temp[3] = saved + "";
                chunkInfo.add(temp);
                chunkN++;
            }
            temp = new String[2];
            temp[0] = args[1];
            temp[1] = fileID;
            fileInfo.add(temp);
            backupSocket.leaveGroup(Peer.getMCBip());
            backupSocket.close();
            controlSocket.leaveGroup(Peer.getMCip());
            controlSocket.close();
            fis.close();
            Util.saveChunkInfo(chunkInfo);
            Util.saveFileInfo(fileInfo);
            System.out.println("BackupProtocol - Backup complete.");
        } catch (Exception ignore) {
            //e.printStackTrace();
        }
    }

    static String buildHeader(String fileID, int chunkN, String factor) {
        return "PUTCHUNK 1.0 " + fileID + " " + chunkN + " " + factor + " \r\n\r\n";
    }

    static boolean validateAcknowledge(DatagramPacket ack, ArrayList<InetAddress> ip, String fileID, int chunk) {
        boolean exists = false;
        String s = new String(ack.getData(), 0, ack.getLength(), StandardCharsets.ISO_8859_1);
        String[] msg = s.split("[ ]+");

        if (msg[0].trim().equals("STORED") && msg[2].trim().equals(fileID) && Integer.parseInt(msg[3]) == chunk) {
            for (InetAddress aIp : ip) {
                if (aIp.equals(ack.getAddress())) {
                    exists = true;
                }
            }
            if (!exists) {
                ip.add(ack.getAddress());
                return true;
            } else {
                return false;
            }
        }
        return false;
    }


}
