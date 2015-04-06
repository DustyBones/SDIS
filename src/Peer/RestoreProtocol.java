package Peer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        int attempt;
        boolean answered, fail;
        long t0, t1;
        byte[] chunkBuf, buf;
        ArrayList<String[]> chunkInfo, fileInfo, filter;

        try {
            chunkInfo = Util.loadRemoteChunkInfo();
            fileInfo = Util.loadFileInfo();
            file = new File(args[1]);
            if (!Util.fileExists(fileInfo, file)) {
                System.out.println("RestoreProtocol - File was not backed up");
                return;
            }
            if (file.isFile()) {
                System.out.println("RestoreProtocol - File already exists");
                return;
            } else {
                file.createNewFile();
            }

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            restoreSocket = new MulticastSocket(Peer.getMCRport());
            restoreSocket.joinGroup(Peer.getMCRip());
            restoreSocket.setLoopbackMode(true);
            restoreSocket.setSoTimeout(100);
            controlSocket = new MulticastSocket(Peer.getMCport());
            controlSocket.joinGroup(Peer.getMCip());
            controlSocket.setLoopbackMode(true);
            controlSocket.setSoTimeout(100);
            chunkBuf = new byte[64100];
            fileID = Util.filterFiles(fileInfo, file.getName())[1];
            filter = Util.filterChunks(chunkInfo, fileID);
            peerPacket = new DatagramPacket(chunkBuf, chunkBuf.length);
            fail = false;
            for (String[] chunk : filter) {
                buf = buildHeader(chunk).getBytes(StandardCharsets.ISO_8859_1);
                controlPacket = new DatagramPacket(buf, buf.length, Peer.getMCip(), Peer.getMCport());
                answered = false;
                attempt = 0;
                do {
                    controlSocket.send(controlPacket);
                    t0 = System.currentTimeMillis();
                    do {
                        try {
                            restoreSocket.receive(peerPacket);
                            String z = new String(peerPacket.getData(), 0, peerPacket.getLength(), StandardCharsets.ISO_8859_1);
                            int j = z.indexOf("\r\n\r\n");
                            z = new String(peerPacket.getData(), 0, j, StandardCharsets.ISO_8859_1);
                            System.out.println("RestoreProtocol - Received from " + peerPacket.getAddress() + ": " + z);
                            answered = peerAnswered(peerPacket, chunk);
                        } catch (Exception ignore) {
                        }
                        if (answered)
                            break;
                        t1 = System.currentTimeMillis();
                    } while (t1 - t0 < 1000);
                    attempt++;
                } while (!answered && attempt <= 10);
                if (answered) {
                    String s = new String(peerPacket.getData(), 0, peerPacket.getLength(), StandardCharsets.ISO_8859_1);
                    int i = s.indexOf("\r\n\r\n");
                    bos.write(Arrays.copyOfRange(peerPacket.getData(), i + 4, peerPacket.getLength()));
                    bos.flush();
                } else {
                    System.out.println("RestoreProtocol - Unable to restore chunk " + chunk[1] + ". Reverting...");
                    fail = true;
                }
                if (fail)
                    break;
            }
            bos.close();
            controlSocket.close();
            restoreSocket.close();
            if (fail) {
                Files.delete(file.toPath());
            } else {
                System.out.println("RestoreProtocol - Finished");
            }
        } catch (Exception ignore) {
            //e.printStackTrace();
        }
    }

    static String buildHeader(String[] cmd) {
        return "GETCHUNK 1.0 " + cmd[0] + " " + cmd[1] + " \r\n\r\n";
    }

    static boolean peerAnswered(DatagramPacket peerPacket, String[] chunk) {
        String s = new String(peerPacket.getData(), 0, peerPacket.getLength(), StandardCharsets.ISO_8859_1);
        int i = s.indexOf("\r\n\r\n");
        s = new String(peerPacket.getData(), 0, i, StandardCharsets.ISO_8859_1);
        String[] msg = s.split("[ ]+");

        return (msg[0].trim().equals("CHUNK") && msg[2].trim().equals(chunk[0]) && msg[3].trim().equals(chunk[1]));
    }

}
