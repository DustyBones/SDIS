package Peer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;

public class RestoreProtocol {
    public static void run(String[] args) {
        MulticastSocket restoreSocket, controlSocket;
        DatagramPacket controlPacket, peerPacket;
        File file;
        FileOutputStream fos;
        BufferedOutputStream bos;
        String fileID;
        boolean answered;
        long t0, t1;
        byte[] chunkBuf, buf;
        ArrayList<String[]> chunkInfo, fileInfo, filter;

        try {
            chunkInfo = Util.loadChunkInfo();
            fileInfo = Util.loadFileInfo();
            if (!fileExists(fileInfo, args[1])) {
                System.out.println("This file was not backed up.");
                return;
            }
            file = new File(args[1]);
            if (file.isFile()) {
                System.out.println("This file already exists.");
                return;
            } else {
                file.createNewFile();
            }

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            restoreSocket = new MulticastSocket(Peer.getMCBport());
            restoreSocket.joinGroup(Peer.getMCBip());
            restoreSocket.setLoopbackMode(true);
            restoreSocket.setSoTimeout(100);
            controlSocket = new MulticastSocket(Peer.getMCport());
            controlSocket.joinGroup(Peer.getMCip());
            controlSocket.setLoopbackMode(true);
            controlSocket.setSoTimeout(100);
            chunkBuf = new byte[64100];
            fileID = Util.filterFiles(fileInfo, args[1])[1];
            filter = Util.filterChunks(chunkInfo, fileID);
            peerPacket = new DatagramPacket(chunkBuf, chunkBuf.length);
            for (String[] chunk : filter) {
                buf = buildHeader(chunk).getBytes();
                controlPacket = new DatagramPacket(buf, buf.length, Peer.getMCip(), Peer.MCport);
                controlSocket.send(controlPacket);
                answered = false;
                t0 = System.currentTimeMillis();
                do {
                    try {
                        restoreSocket.receive(peerPacket);
                        if (answered = peerAnswered(peerPacket, chunk))
                            break;
                    } catch (Exception ignore) {
                    }
                    t1 = System.currentTimeMillis();
                } while (t1 - t0 < 30000);
                if (answered) {
                    String s = new String(peerPacket.getData(), 0, peerPacket.getLength());
                    int i = s.indexOf("\r\n\r\n");
                    bos.write(Arrays.copyOfRange(peerPacket.getData(), i + 4, peerPacket.getLength()));
                    bos.flush();
                } else {
                    System.out.println("Unable to restore chunk " + chunk[1] + ". Reverting...");
                    file.deleteOnExit();
                }
            }
            bos.close();
            fos.close();
            controlSocket.close();
            restoreSocket.close();
        } catch (Exception ignore) {
            //e.printStackTrace();
        }
        System.out.println("Restoration complete.");
    }

    static String buildHeader(String[] cmd) {
        return "GETCHUNK 1.0 " + cmd[0] + " " + cmd[1] + " \r\n\r\n";
    }

    static boolean peerAnswered(DatagramPacket peerPacket, String[] chunk) {
        String s = new String(peerPacket.getData(), 0, peerPacket.getLength());
        int i = s.indexOf("\r\n\r\n");
        s = new String(peerPacket.getData(), 0, i);
        String[] msg = s.split("[ ]+");

        return (msg[0].trim().equals("CHUNK") && msg[2].trim().equals(chunk[0]) && msg[3].trim().equals(chunk[1]));
    }

    static boolean fileExists(ArrayList<String[]> list, String fileName) {
        for (String[] file : list) {
            if (file[0].equals(fileName))
                return true;
        }
        return false;
    }

}
