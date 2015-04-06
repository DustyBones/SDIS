package Peer;

import java.io.File;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class DeleteProtocol {
    public static void run(String[] args) {
        MulticastSocket controlSocket;
        DatagramPacket controlPacket;
        File file;
        String[] fileFilter;
        int sent;
        byte[] buf;
        ArrayList<String[]> fileInfo, chunkInfo, filter;

        try {
            chunkInfo = Util.loadRemoteChunkInfo();
            fileInfo = Util.loadFileInfo();
            file = new File(args[1]);
            if (!Util.fileExists(fileInfo, file)) {
                System.out.println("DeleteProtocol - File was not backed up");
                return;
            }
            controlSocket = new MulticastSocket(Peer.getMCport());
            controlSocket.joinGroup(Peer.getMCip());
            controlSocket.setLoopbackMode(true);
            controlSocket.setSoTimeout(100);
            fileFilter = Util.filterFiles(fileInfo, file.getName());
            filter = Util.filterChunks(chunkInfo, fileFilter[1]);
            buf = buildHeader(fileFilter).getBytes(StandardCharsets.ISO_8859_1);
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
            fileInfo.remove(fileFilter);
            Util.saveFileInfo(fileInfo);
            Util.saveRemoteChunkInfo(chunkInfo);
            controlSocket.close();
            System.out.println("DeleteProtocol - Finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String buildHeader(String[] cmd) {
        return "DELETE 1.0 " + cmd[1] + " \r\n\r\n";
    }
}
