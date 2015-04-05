package Peer;

import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Random;

public class Util {

    public static boolean fileIsValid(String file) {
        if (new File(file).exists())
            return true;
        return false;
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
        return sb.toString();
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

    public static ArrayList<String[]> loadChunkInfo() throws Exception {
        String chunk;
        String[] token;
        ArrayList<String[]> list = new ArrayList<>();
        File fin = new File("chunkInfo.csv");
        if (!fin.isFile())
            fin.createNewFile();
        FileInputStream fis = new FileInputStream(fin);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        while ((chunk = br.readLine()) != null) {
            token = chunk.split("[,]");
            list.add(token);
        }
        return list;
    }

    public static void saveChunkInfo(ArrayList<String[]> list) throws Exception {
        File fout = new File("chunkInfo.csv");
        if (!fout.isFile())
            fout.createNewFile();
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        for (String[] chunk : list) {
            bw.write(chunk[0] + "," + chunk[1] + "," + chunk[2] + "," + chunk[3]);
            bw.newLine();
        }
        bw.close();
    }

    public static ArrayList<String[]> loadFileInfo() throws Exception {
        String chunk;
        String[] token;
        ArrayList<String[]> list = new ArrayList<>();
        File fin = new File("fileInfo.csv");
        if (!fin.isFile())
            fin.createNewFile();
        FileInputStream fis = new FileInputStream(fin);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        while ((chunk = br.readLine()) != null) {
            token = chunk.split("[,]");
            list.add(token);
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

}
