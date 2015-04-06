package Peer;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class DeleteProtocol {
    public static void run(String[] args) {
        MulticastSocket controlSocket;
        DatagramPacket controlPacket;
        String[] file;
        int sent;
        byte[] buf;
        ArrayList<String[]> fileInfo, chunkInfo, filter;

        try {
            chunkInfo = Util.loadChunkInfo();
            fileInfo = Util.loadFileInfo();
            if (!Util.fileExists(fileInfo, args[1])) {
                System.out.println("This file was not backed up.");
                return;
            }
            controlSocket = new MulticastSocket(Peer.getMCport());
            controlSocket.joinGroup(Peer.getMCip());
            controlSocket.setLoopbackMode(true);
            controlSocket.setSoTimeout(100);
            file = Util.filterFiles(fileInfo, args[1]);
            filter = Util.filterChunks(chunkInfo, file[1]);
            buf = buildHeader(file).getBytes(StandardCharsets.ISO_8859_1);
            controlPacket = new DatagramPacket(buf, buf.length, Peer.getMCip(), Peer.getMCport());
            sent = 0;
            while (sent < 5) {
                controlSocket.send(controlPacket);
                sent++;
                Util.wait(1000);
            }
            for (String[] chunk : filter) {
                chunkInfo.remove(chunk);
            }
            fileInfo.remove(file);
            Util.saveFileInfo(fileInfo);
            Util.saveChunkInfo(chunkInfo);
            controlSocket.close();
            System.out.println("Deletion complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String buildHeader(String[] cmd) {
        return "DELETE 1.0 " + cmd[1] + " \r\n\r\n";
    }
}
