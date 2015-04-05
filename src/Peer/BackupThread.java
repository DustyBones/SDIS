package Peer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BackupThread extends Thread {
    @Override
    public void run() {
        MulticastSocket multiSocket;
        DatagramPacket chunkPacket, ackPacket;
        byte[] buf, ack;
        String received;
        String[] header, body;
        File file;
        FileOutputStream fos;
        BufferedOutputStream bos;
        while (Peer.running) {
            try {
                multiSocket = new MulticastSocket(Peer.getMCBport());
                multiSocket.setSoTimeout(1000);
                multiSocket.joinGroup(Peer.getMCBip());
                buf = new byte[65500];
                chunkPacket = new DatagramPacket(buf, buf.length);

                multiSocket.receive(chunkPacket);
                //TODO filter owwn broadcasts
                if (!InetAddress.getLocalHost().equals(chunkPacket.getAddress())) {
                    received = new String(chunkPacket.getData(), 0, chunkPacket.getLength());
                    header = received.split("[ ]+");
                    body = received.split("\\r\\n\\r\\n");
                    System.out.println("BackupThread - Received from " + chunkPacket.getAddress() + ": " + body[0]);
                    if (header[0].equals("PUTCHUNK")) {
                        if (!(file = new File(header[2] + ".part" + header[3])).isFile()) {
                            file.createNewFile();
                            fos = new FileOutputStream(file);
                            bos = new BufferedOutputStream(fos);
                            bos.write(body[1].getBytes());
                            bos.flush();
                            bos.close();
                        }
                        ack = buildHeader(header).getBytes();
                        ackPacket = new DatagramPacket(ack, ack.length, Peer.getMCBip(), Peer.getMCBport());
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
