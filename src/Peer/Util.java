package Peer;

import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Random;

public class Util {

    public static boolean fileIsValid(String file) {
        return new File(file).exists();
    }

    public static String getFileID(String file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }
        byte[] hash = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aHash : hash) {
            sb.append(Integer.toString((aHash & 0xff) + 0x100, 16).substring(1));
        }
        fis.close();
        return sb.toString();
    }

    public static boolean fileExists(ArrayList<String[]> list, File file) {
        for (String[] f : list) {
            if (f[0].equals(file.getName()))
                return true;
        }
        return false;
    }

    public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static void wait(int n) {
        long t0, t1;
        t0 = System.currentTimeMillis();
        do {
            t1 = System.currentTimeMillis();
        } while (t1 - t0 < n);
    }

    public static int getRandomInt(int range) {
        Random r = new Random();
        return r.nextInt(range);
    }

    public static ArrayList<String[]> loadRemoteChunkInfo() throws Exception {
        String chunk;
        String[] token;
        ArrayList<String[]> list = new ArrayList<>();
        File fin = new File("remoteChunkInfo.csv");
        if (fin.isFile()) {
            FileInputStream fis = new FileInputStream(fin);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            while ((chunk = br.readLine()) != null) {
                token = chunk.split("[,]");
                list.add(token);
            }
            br.close();
        }
        return list;
    }

    public static void saveRemoteChunkInfo(ArrayList<String[]> list) throws Exception {
        File fout = new File("remoteChunkInfo.csv");
        if (!fout.isFile()) {
            fout.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        for (String[] chunk : list) {
            bw.write(chunk[0] + "," + chunk[1] + "," + chunk[2] + "," + chunk[3] + "," + chunk[4]);
            bw.newLine();
        }
        bw.close();
    }

    public static ArrayList<String[]> loadLocalChunkInfo() throws Exception {
        String chunk;
        String[] token;
        ArrayList<String[]> list = new ArrayList<>();
        File fin = new File("localChunkInfo.csv");
        if (fin.isFile()) {
            FileInputStream fis = new FileInputStream(fin);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            while ((chunk = br.readLine()) != null) {
                token = chunk.split("[,]");
                list.add(token);
            }
            br.close();
        }
        return list;
    }

    public static void saveLocalChunkInfo(ArrayList<String[]> list) throws Exception {
        File fout = new File("localChunkInfo.csv");
        if (!fout.isFile()) {
            fout.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        for (String[] chunk : list) {
            bw.write(chunk[0] + "," + chunk[1] + "," + chunk[2] + "," + chunk[3] + "," + chunk[4]);
            bw.newLine();
        }
        bw.close();
    }

    public static ArrayList<String[]> loadFileInfo() throws Exception {
        String chunk;
        String[] token;
        ArrayList<String[]> list = new ArrayList<>();
        File fin = new File("fileInfo.csv");
        if (fin.isFile()) {
            FileInputStream fis = new FileInputStream(fin);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            while ((chunk = br.readLine()) != null) {
                token = chunk.split("[,]");
                list.add(token);
            }
            br.close();
        }
        return list;
    }

    public static void saveFileInfo(ArrayList<String[]> list) throws Exception {
        File fout = new File("fileInfo.csv");
        if (!fout.isFile())
            fout.createNewFile();
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        for (String[] chunk : list) {
            bw.write(chunk[0] + "," + chunk[1]);
            bw.newLine();
        }
        bw.close();
    }

    public static ArrayList<String[]> filterChunks(ArrayList<String[]> list, String fileID) {
        ArrayList<String[]> filtered = new ArrayList<>();
        for (String[] chunk : list) {
            if (chunk[0].equals(fileID)) {
                filtered.add(chunk);
            }
        }
        return filtered;
    }

    public static String[] filterFiles(ArrayList<String[]> list, String fileName) {
        String[] filtered = new String[2];
        for (String[] file : list) {
            if (file[0].equals(fileName)) {
                filtered = file;
            }
        }
        return filtered;
    }

}
