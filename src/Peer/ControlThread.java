package Peer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ControlThread extends Thread {

    @Override
    public void run() {
        MulticastSocket multiSocket;
        DatagramPacket dataPacket;
        byte[] buf;
        String received;
        while (Peer.running) {
            try {
                multiSocket = new MulticastSocket(Peer.getMCport());
                multiSocket.setSoTimeout(100);
                multiSocket.joinGroup(Peer.getMCip());
                buf = new byte[256];
                dataPacket = new DatagramPacket(buf, buf.length);

                multiSocket.receive(dataPacket);

                received = new String(dataPacket.getData(), 0, dataPacket.getLength());
                if (!InetAddress.getLocalHost().equals(dataPacket.getAddress())) {
                    System.out.println("ControlThread - Received from " + dataPacket.getAddress() + ": " + received);
                } else {
                    System.out.println("ControlThread - Sent from " + dataPacket.getAddress() + ": " + received);
                }
                multiSocket.leaveGroup(Peer.getMCip());
                multiSocket.close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }
}
