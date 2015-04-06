package Peer;

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RestoreThread extends Thread {
    @Override
    public void run() {
        MulticastSocket restoreSocket, controlSocket;
        DatagramPacket chunkPacket, requestPacket, peerPacket;
        File file;
        FileInputStream fis;
        int time, k;
        long t0, t1;
        byte[] chunkBuf, buf, msg, peerBuf;
        String[] token;
        boolean answered;

        try {
            restoreSocket = new MulticastSocket(Peer.getMCRport());
            restoreSocket.joinGroup(Peer.getMCRip());
            restoreSocket.setLoopbackMode(true);
            restoreSocket.setSoTimeout(100);
            controlSocket = new MulticastSocket(Peer.getMCport());
            controlSocket.joinGroup(Peer.getMCip());
            controlSocket.setLoopbackMode(true);
            controlSocket.setSoTimeout(100);
            buf = new byte[256];
            chunkBuf = new byte[64000];
            peerBuf = new byte[64100];
            requestPacket = new DatagramPacket(buf, buf.length);
            peerPacket = new DatagramPacket(peerBuf, peerBuf.length);

            while (Peer.running) try {
                controlSocket.receive(requestPacket);
                String s = new String(requestPacket.getData(), 0, requestPacket.getLength(), StandardCharsets.ISO_8859_1);
                int i = s.indexOf("\r\n\r\n");
                s = new String(requestPacket.getData(), 0, i, StandardCharsets.ISO_8859_1);
                System.out.println("RestoreThread - Received from " + requestPacket.getAddress() + ": " + s);
                token = s.split("[ ]+");
                if (validRequest(token)) {
                    answered = false;
                    time = Util.getRandomInt(400);
                    t0 = System.currentTimeMillis();
                    file = new File(token[2] + ".part" + token[3]);
                    fis = new FileInputStream(file);
                    k = fis.read(chunkBuf);
                    msg = Util.concatenateByteArrays(buildHeader(token[2], Integer.parseInt(token[3])).getBytes(StandardCharsets.ISO_8859_1), Arrays.copyOfRange(chunkBuf, 0, k));
                    chunkPacket = new DatagramPacket(msg, msg.length, Peer.getMCRip(), Peer.getMCRport());
                    do {
                        try {
                            restoreSocket.receive(peerPacket);
                            String z = new String(peerPacket.getData(), 0, peerPacket.getLength(), StandardCharsets.ISO_8859_1);
                            int j = z.indexOf("\r\n\r\n");
                            z = new String(peerPacket.getData(), 0, j, StandardCharsets.ISO_8859_1);
                            System.out.println("RestoreThread - Received from " + peerPacket.getAddress() + ": " + z);
                            answered = peerAnswered(peerPacket, token);
                        } catch (Exception ignore) {
                        }
                        if (answered)
                            break;
                        t1 = System.currentTimeMillis();
                    } while (t1 - t0 < time);
                    if (!answered) {
                        restoreSocket.send(chunkPacket);
                    }
                    fis.close();
                }
            } catch (Exception ignore) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    String buildHeader(String fileID, int chunkN) {
        return "CHUNK 1.0 " + fileID + " " + chunkN + " \r\n\r\n";
    }

    boolean validRequest(String[] msg) {
        if (msg[0].trim().equals("GETCHUNK")) {
            File file = new File(msg[2].trim() + ".part" + msg[3].trim());
            if (file.isFile())
                return true;
        }
        return false;
    }

    boolean peerAnswered(DatagramPacket peer, String[] requestToken) {
        String s = new String(peer.getData(), 0, peer.getLength(), StandardCharsets.ISO_8859_1);
        String[] peerToken = s.split("[ ]");
        return peerToken[0].trim().equals("CHUNK")
                && peerToken[2].trim().equals(requestToken[2].trim())
                && peerToken[3].trim().equals(requestToken[3].trim());
    }
}
