package Peer;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class ControlThread extends Thread {

    @Override
    public void run() {
        MulticastSocket controlSocket;
        DatagramPacket dataPacket;
        byte[] buf;
        String received;
        try {
            controlSocket = new MulticastSocket(Peer.getMCport());
            controlSocket.joinGroup(Peer.getMCip());
            controlSocket.setLoopbackMode(true);
            controlSocket.setSoTimeout(100);
            buf = new byte[256];
            dataPacket = new DatagramPacket(buf, buf.length);

            while (Peer.running) try {
                controlSocket.receive(dataPacket);
                received = new String(dataPacket.getData(), 0, dataPacket.getLength() - 4, StandardCharsets.ISO_8859_1);
                System.out.print("ControlThread - Received from " + dataPacket.getAddress() + ": " + received);
            } catch (Exception ignore) {
            }
            controlSocket.leaveGroup(Peer.getMCip());
            controlSocket.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}

