package Peer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RestoreThread extends Thread {
    @Override
    public void run() {
        MulticastSocket multiSocket;
        DatagramPacket dataPacket;
        byte[] buf;
        String received;
        while (Peer.running) {
            try {
                multiSocket = new MulticastSocket(Peer.getMCport());
                multiSocket.setSoTimeout(1000);
                multiSocket.joinGroup(Peer.getMCip());
                buf = new byte[256];
                dataPacket = new DatagramPacket(buf, buf.length);

                multiSocket.receive(dataPacket);

                if (!InetAddress.getLocalHost().equals(dataPacket.getAddress())) {
                    received = new String(dataPacket.getData(), 0, dataPacket.getLength());
                    // System.out.println("RestoreThread - Received from " + dataPacket.getAddress() + ": " + received);
                }
                multiSocket.leaveGroup(Peer.getMCip());
                multiSocket.close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }
}
