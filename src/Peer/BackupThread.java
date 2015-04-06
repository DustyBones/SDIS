package Peer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class BackupThread extends Thread {
    @Override
    public void run() {
        MulticastSocket backupSocket, multicastSocket;
        DatagramPacket chunkPacket, ackPacket, peerAckPacket;
        byte[] buf, ack, peerAck, body;
        String received;
        String[] header;
        File file;
        FileOutputStream fos;
        BufferedOutputStream bos;
        int saved, timeout;
        long t0, t1;
        ArrayList<String[]> localChunkInfo;
        ArrayList<String> ip;
        try {
            multicastSocket = new MulticastSocket();
            multicastSocket.joinGroup(Peer.getMCip());
            multicastSocket.setLoopbackMode(true);
            multicastSocket.setSoTimeout(100);
            backupSocket = new MulticastSocket(Peer.getMCBport());
            backupSocket.joinGroup(Peer.getMCBip());
            backupSocket.setLoopbackMode(true);
            backupSocket.setSoTimeout(100);
            buf = new byte[64100];
            chunkPacket = new DatagramPacket(buf, buf.length);
            while (Peer.running) try {
                backupSocket.receive(chunkPacket);
                received = new String(chunkPacket.getData(), 0, chunkPacket.getLength(), StandardCharsets.ISO_8859_1);
                header = received.split("[ ]+");
                int i = received.indexOf("\r\n\r\n");
                received = new String(chunkPacket.getData(), 0, i, StandardCharsets.ISO_8859_1);
                body = Arrays.copyOfRange(chunkPacket.getData(), i + 4, chunkPacket.getLength());
                System.out.println("BackupThread  - Received from " + chunkPacket.getAddress().toString() + ":" +
                        chunkPacket.getPort() + " | " + received);
                if (header[0].equals("PUTCHUNK")) {
                    ip = new ArrayList<>();
                    saved = 1;
                    localChunkInfo = Util.loadLocalChunkInfo();
                    if (!(file = new File(header[2] + ".part" + header[3])).isFile()) {
                        file.createNewFile();
                        fos = new FileOutputStream(file);
                        bos = new BufferedOutputStream(fos);
                        bos.write(body);
                        bos.flush();
                        bos.close();
                        String[] newChunk = new String[5];
                        newChunk[0] = header[2];
                        newChunk[1] = header[3];
                        newChunk[2] = body.length + "";
                        newChunk[3] = header[4];
                        newChunk[4] = "";
                        localChunkInfo.add(newChunk);
                    }
                    timeout = Util.getRandomInt(400);
                    ack = buildHeader(header).getBytes(StandardCharsets.ISO_8859_1);
                    ackPacket = new DatagramPacket(ack, ack.length, Peer.getMCip(), Peer.getMCport());
                    peerAck = new byte[256];
                    peerAckPacket = new DatagramPacket(peerAck, peerAck.length);
                    t0 = System.currentTimeMillis();
                    do {
                        boolean exists = false;
                        try {
                            multicastSocket.receive(peerAckPacket);
                            if (peerAckPacket.getData().equals(ackPacket.getData())) {
                                for (String aIp : ip) {
                                    if (aIp.equals(peerAckPacket.getAddress().toString() + ":" + peerAckPacket.getPort())) {
                                        exists = true;
                                    }
                                }
                                if (!exists) {
                                    ip.add(peerAckPacket.getAddress().toString() + ":" + peerAckPacket.getPort());
                                    saved++;
                                }
                            }
                        } catch (Exception ignore) {
                        }
                        t1 = System.currentTimeMillis();
                        if (t1 - t0 < timeout)
                            multicastSocket.send(ackPacket);
                    } while (t1 - t0 < 500);
                    for (String[] chunk : localChunkInfo) {
                        if (chunk[0].equals(header[2]) && chunk[1].equals(header[3]))
                            chunk[4] = saved + "";
                    }
                    Util.saveLocalChunkInfo(localChunkInfo);
                }
            } catch (Exception ignore) {
            }
            backupSocket.leaveGroup(Peer.getMCip());
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    String buildHeader(String[] cmd) {
        return "STORED 1.0 " + cmd[2] + " " + Integer.parseInt(cmd[3]) + " \r\n\r\n";
    }
}
