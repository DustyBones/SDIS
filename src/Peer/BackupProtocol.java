package Peer;

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class BackupProtocol {
    public static void run(String[] args, boolean chunk) {
        MulticastSocket multicastSocket, controlSocket;
        DatagramPacket chunkPacket, ackPacket;
        File file;
        FileInputStream fis;
        String fileID;
        int chunkN, saved, attempt, timeout;
        long t0, t1;
        byte[] chunkBuf, buf, msg;
        String[] temp;
        ArrayList<String> IPlist;
        ArrayList<String[]> chunkInfo, fileInfo;

        try {
            chunkInfo = (chunk ? Util.loadLocalChunkInfo() : Util.loadRemoteChunkInfo());
            fileInfo = Util.loadFileInfo();
            file = new File(chunk ? args[0] + ".part" + args[1] : args[1]);

            multicastSocket = new MulticastSocket();
            multicastSocket.joinGroup(Peer.getMCBip());
            multicastSocket.setLoopbackMode(true);
            controlSocket = new MulticastSocket(Peer.getMCport());
            controlSocket.joinGroup(Peer.getMCip());
            controlSocket.setLoopbackMode(true);
            fis = new FileInputStream(file);
            fileID = (chunk ? args[0] : Util.getFileID(args[1]));
            chunkN = (chunk ? Integer.parseInt(args[1]) : 0);
            chunkBuf = new byte[64000];
            buf = new byte[100];
            IPlist = new ArrayList<>();
            int k;
            while ((k = fis.read(chunkBuf)) > -1) {
                msg = Util.concatenateByteArrays(buildHeader(fileID, chunkN,
                                (chunk ? args[3] : args[2])).getBytes(StandardCharsets.ISO_8859_1),
                        Arrays.copyOfRange(chunkBuf, 0, k));
                chunkPacket = new DatagramPacket(msg, msg.length, Peer.getMCBip(), Peer.getMCBport());
                ackPacket = new DatagramPacket(buf, buf.length);
                attempt = 1;
                saved = 0;
                IPlist.clear();
                do {
                    timeout = (int) (500 * Math.pow(2, attempt - 1) / Integer.parseInt(args[2]));
                    controlSocket.setSoTimeout(timeout);
                    multicastSocket.send(chunkPacket);
                    t0 = System.currentTimeMillis();
                    do {
                        try {
                            controlSocket.receive(ackPacket);
                            String z = new String(ackPacket.getData(), 0, ackPacket.getLength(), StandardCharsets.ISO_8859_1);
                            int j = z.indexOf("\r\n\r\n");
                            z = new String(ackPacket.getData(), 0, j, StandardCharsets.ISO_8859_1);
                            System.out.println("BackupProtocol - Received from " + ackPacket.getAddress() + ":" +
                                    ackPacket.getPort() + " : " + z);
                            if (validateAcknowledge(ackPacket, IPlist, fileID, chunkN)) {
                                saved++;
                            }
                        } catch (SocketTimeoutException ignore) {
                        }
                        t1 = System.currentTimeMillis();
                    } while ((t1 - t0) < (500 * Math.pow(2, attempt - 1)));
                    attempt++;
                } while (saved < Integer.parseInt(args[2]) && attempt <= 5);
                temp = new String[5];
                temp[0] = fileID;
                temp[1] = chunkN + "";
                temp[2] = k + "";
                temp[3] = args[2];
                temp[4] = saved + "";
                if (chunk) {
                    String[] chunkToRemove = new String[5];
                    for (String[] c : chunkInfo) {
                        if (c[0].equals(temp[0]) && c[1].equals(temp[1]))
                            chunkToRemove = c;
                    }
                    chunkInfo.remove(chunkToRemove);
                }
                chunkInfo.add(temp);
                chunkN++;
            }
            temp = new String[2];
            temp[0] = file.getName();
            temp[1] = fileID;

            fileInfo.add(temp);
            multicastSocket.leaveGroup(Peer.getMCBip());
            multicastSocket.close();
            controlSocket.leaveGroup(Peer.getMCip());
            controlSocket.close();
            fis.close();
            if (chunk) {
                Util.saveLocalChunkInfo(chunkInfo);
            } else {
                Util.saveRemoteChunkInfo(chunkInfo);
                Util.saveFileInfo(fileInfo);
            }
            System.out.println("BackupProtocol - Finished");
        } catch (Exception ignore) {
            //e.printStackTrace();
        }
    }

    static String buildHeader(String fileID, int chunkN, String factor) {
        return "PUTCHUNK 1.0 " + fileID + " " + chunkN + " " + factor + " \r\n\r\n";
    }

    static boolean validateAcknowledge(DatagramPacket ack, ArrayList<String> ip, String fileID, int chunk) {
        boolean exists = false;
        String s = new String(ack.getData(), 0, ack.getLength(), StandardCharsets.ISO_8859_1);
        String[] msg = s.split("[ ]+");

        if (msg[0].trim().equals("STORED") && msg[2].trim().equals(fileID) && Integer.parseInt(msg[3]) == chunk) {
            for (String aIp : ip) {
                if (aIp.equals(ack.getAddress().toString() + ":" + ack.getPort())) {
                    exists = true;
                }
            }
            if (!exists) {
                ip.add(ack.getAddress().toString() + ":" + ack.getPort());
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
