package Peer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class BackupThread extends Thread {
    @Override
    public void run() {
        MulticastSocket multiSocket;
        DatagramPacket chunkPacket, ackPacket;
        byte[] buf, ack, body;
        String received;
        String[] header;
        File file;
        FileOutputStream fos;
        BufferedOutputStream bos;
        while (Peer.running) {
            try {
                multiSocket = new MulticastSocket(Peer.getMCBport());
                multiSocket.setSoTimeout(1000);
                multiSocket.joinGroup(Peer.getMCBip());
                buf = new byte[64100];
                chunkPacket = new DatagramPacket(buf, buf.length);

                multiSocket.receive(chunkPacket);

                if (!InetAddress.getLocalHost().equals(chunkPacket.getAddress())) {
                    received = new String(chunkPacket.getData(), 0, 86);
                    header = received.split("[ ]+");
                    body = Arrays.copyOfRange(chunkPacket.getData(), 86, chunkPacket.getLength());
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
                        ack = buildHeader(header).getBytes();
                        ackPacket = new DatagramPacket(ack, ack.length, Peer.getMCip(), Peer.getMCport());
                        Util.wait(Util.getRandomInt(400));
                        multiSocket.send(ackPacket);
                    }
                }
                multiSocket.leaveGroup(Peer.getMCip());
                multiSocket.close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    String buildHeader(String[] cmd) {
        return "STORED 1.0" + " " + cmd[2] + " " + Integer.parseInt(cmd[3]) + " \r\n\r\n";
    }
}
