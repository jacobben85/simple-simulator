package com.univision.deportes.simulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class UrlFileReader {

    private static Map<String, String> processorInstances = new HashMap<>();
    private ExecutorService asyncExecutor;

    UrlFileReader() {
        this.asyncExecutor = Executors.newFixedThreadPool(10);
    }

    public void run() throws IOException, InterruptedException {

        InputStream stream = this.getClass().getResourceAsStream("/urls.txt");
        BufferedReader br = null;
        String line = "";

        try {

            br = new BufferedReader(new InputStreamReader(stream));
            while ((line = br.readLine()) != null) {
                final String finalLine = line;

                while (hasToWait()) {
                    Thread.sleep(2000);
                }

                this.asyncExecutor.execute(() -> process(finalLine));
            }

        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }

        System.out.println("Done");
    }

    private static void process(String line) {
        try {
            processorInstances.put(md5(line), line);
            String postUrl = "http://sports.dev.y.univision.com/feeds/xml-team-backfile";
            Long sleep = 500L;

            PostToLocal postToLocal = new PostToLocal();
            postToLocal.setSleepTime(sleep);
            postToLocal.setPostUrl(postUrl);
            postToLocal.fetchLinksAndProcess(line, false);
            processorInstances.remove(md5(line));
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }
    }

    protected boolean hasToWait() {
        if (processorInstances.size() > 9) {
            return true;
        }
        return false;
    }

    public static String md5(String string) {
        String md5String = null;
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(string));
            md5String = String.format("%032x", new BigInteger(1, md5.digest()));
        } catch (NoSuchAlgorithmException e) {
        }
        return md5String;
    }
}
