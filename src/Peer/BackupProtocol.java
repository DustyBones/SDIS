package Peer;

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BackupProtocol {
    public static void run(String[] args) {
        MulticastSocket multiSocket;
        DatagramPacket chunkPacket, ackPacket;
        File file;
        FileInputStream fis;
        String fileID, received;
        int chunkN = 0, saved, attempt, timeout = 100;
        byte[] chunkBuf, buf;

        try {
            multiSocket = new MulticastSocket(Peer.getMCBport());
            multiSocket.joinGroup(Peer.getMCBip());
            file = new File(args[1]);
            fis = new FileInputStream(file);
            fileID = Util.getFileID(args[1]);
            chunkBuf = new byte[65000];
            buf = new byte[65000];

            while (fis.read(chunkBuf) > -1) {
                byte[] msg = Util.concatenateByteArrays(buildHeader(fileID, chunkN, args[2]).getBytes(), chunkBuf);
                chunkPacket = new DatagramPacket(msg, msg.length, Peer.getMCBip(), Peer.getMCBport());
                ackPacket = new DatagramPacket(buf, buf.length);
                chunkN++;
                attempt = 0;
                saved = 0;
                multiSocket.send(chunkPacket);
                multiSocket.setSoTimeout(timeout * attempt);
                Util.wait(Util.getRandomInt(1000));
                multiSocket.receive(ackPacket);
                if (!InetAddress.getLocalHost().equals(ackPacket.getAddress())) {
                    received = new String(ackPacket.getData(), 0, ackPacket.getLength());
                    System.out.println("BackupProtocol - Received from " + ackPacket.getAddress() + ": " + received);
                }

                //TODO repeat until factor has been reached
                //TODO save IPaddress from ack peers
            }
            multiSocket.leaveGroup(Peer.getMCBip());
            multiSocket.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    static String buildHeader(String fileID, int chunkN, String factor) {
        return "PUTCHUNK 1.0" + " " + fileID + " " + chunkN + " " + factor + " \r\n\r\n";
    }

    static boolean validateAcknowledge(String s) {
        String[] msg = s.split("[ ]+");
        return msg[0].equals("STORED");
    }
}
