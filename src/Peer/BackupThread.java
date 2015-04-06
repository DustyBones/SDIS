package Peer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BackupThread extends Thread {
    @Override
    public void run() {
        MulticastSocket backupSocket, controlSocket;
        DatagramPacket chunkPacket, ackPacket;
        byte[] buf, ack, body;
        String received;
        String[] header;
        File file;
        FileOutputStream fos;
        BufferedOutputStream bos;
        try {
            controlSocket = new MulticastSocket(Peer.getMCport());
            controlSocket.joinGroup(Peer.getMCip());
            controlSocket.setLoopbackMode(true);
            controlSocket.setSoTimeout(100);
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
                int i = received.indexOf(System.getProperty("line.separator") + System.getProperty("line.separator"));
                received = new String(chunkPacket.getData(), 0, i, StandardCharsets.ISO_8859_1);
                body = Arrays.copyOfRange(chunkPacket.getData(), i + 4, chunkPacket.getLength());
                System.out.println("BackupThread - Received from " + chunkPacket.getAddress() + ": " + received);
                if (header[0].equals("PUTCHUNK")) {
                    if (!(file = new File(header[2] + ".part" + header[3])).isFile()) {
                        file.createNewFile();
                        fos = new FileOutputStream(file);
                        bos = new BufferedOutputStream(fos);
                        bos.write(body);
                        bos.flush();
                        bos.close();
                    }
                    ack = buildHeader(header).getBytes(StandardCharsets.ISO_8859_1);
                    ackPacket = new DatagramPacket(ack, ack.length, Peer.getMCip(), Peer.getMCport());
                    Util.wait(Util.getRandomInt(400));
                    backupSocket.send(ackPacket);
                }
            } catch (Exception ignore) {
            }
            backupSocket.leaveGroup(Peer.getMCip());
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    String buildHeader(String[] cmd) {
        return "STORED 1.0 " + cmd[2] + " " + Integer.parseInt(cmd[3]) +
                " " + System.getProperty("line.separator") + System.getProperty("line.separator");
    }
}
