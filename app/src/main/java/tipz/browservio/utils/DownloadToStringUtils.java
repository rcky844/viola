package tipz.browservio.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadToStringUtils {
    public static String downloadToString(String inUrl) {
        DownloadToString downloadToString = new DownloadToString(inUrl);
        Thread thread = new Thread(downloadToString);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return downloadToString.getValue();
    }

    private static class DownloadToString implements Runnable {
        private volatile String value;
        private final String inUrl;

        private DownloadToString(String inUrl) {
            this.inUrl = inUrl;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(inUrl);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try (InputStream in = new BufferedInputStream(urlConnection.getInputStream())) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    value = result.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getValue() {
            return value;
        }
    }
}