package Peer;

import java.io.File;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class DeleteThread extends Thread {

    @Override
    public void run() {

        MulticastSocket controlSocket;
        DatagramPacket dataPacket;
        byte[] buf;
        String received;
        String[] msg;
        int i;
        try {
            controlSocket = new MulticastSocket(Peer.getMCport());
            controlSocket.joinGroup(Peer.getMCip());
            controlSocket.setLoopbackMode(true);
            controlSocket.setSoTimeout(100);
            buf = new byte[256];
            dataPacket = new DatagramPacket(buf, buf.length);

            while (Peer.running) try {
                controlSocket.receive(dataPacket);
                received = new String(dataPacket.getData(), 0, dataPacket.getLength(), StandardCharsets.ISO_8859_1);
                i = received.indexOf("\r\n\r\n");
                received = new String(dataPacket.getData(), 0, i, StandardCharsets.ISO_8859_1);
                msg = received.split("[ ]+");
                if (msg[0].equals("DELETE")) {
                    System.out.println("DeleteThread - Received from " + dataPacket.getAddress() + ": " + received);
                    File[] files = new File(System.getProperty("user.dir")).listFiles();
                    for (File file : files) {
                        String[] fileToken = file.getName().split("[.]");
                        if (fileToken[0].equals(msg[2])) {
                            System.out.println("DeleteThread - " + file.getName() + " erased.");
                            file.delete();
                        }
                    }
                } else if (msg[0].equals("REMOVED")) {
                    System.out.println("DeleteThread - Received from " + dataPacket.getAddress() + ": " + received);
                    File[] files = new File(System.getProperty("user.dir")).listFiles();
                    for (File file : files) {
                        String[] fileToken = file.getName().split("[.]");
                        if (fileToken[0].equals(msg[2])) {
                            System.out.println("DeleteThread - " + file.getName() + " erased.");
                            file.delete();
                        }
                    }
                }
            } catch (Exception ignore) {
            }
            controlSocket.leaveGroup(Peer.getMCip());
            controlSocket.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}

