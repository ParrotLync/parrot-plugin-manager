package nl.parrotlync.parrotpluginmanager.common.nexus;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Logger;

public class NexusClient {

    public static String getLatestVersion(String artifact, String auth, Logger logger) {
        String downloadUrl = getDownloadUrl(artifact, auth, logger);
        if (downloadUrl != null) {
            String[] fragments = downloadUrl.split("/");
            return fragments[fragments.length - 1];
        }
        return null;
    }

    public static boolean downloadFile(String artifact, String auth, Logger logger) {
        String downloadUrl = getDownloadUrl(artifact, auth, logger);
        String latestVersion = getLatestVersion(artifact, auth, logger);
        if (downloadUrl != null && latestVersion != null) {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
                connection.setRequestProperty("Authorization", "Basic " + new String(encodedAuth));
                BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                FileOutputStream fileOutput = new FileOutputStream("plugins/" + latestVersion);
                byte[] data = new byte[1024];
                int byteContent ;
                while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                    fileOutput.write(data, 0, byteContent);
                }
                logger.info("Successfully downloaded " + latestVersion);
                return true;
            } catch (Exception e) {
                logger.warning("Couldn't download file: " + latestVersion);
            }
        }
        return false;
    }

    private static String getDownloadUrl(String artifact, String auth, Logger logger) {
        try {
            URL url = new URL("https://nexus.ipictserver.nl/service/rest/v1/search/assets?sort=version&repository=maven-releases&group=nl.parrotlync&name=" + artifact);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + new String(encodedAuth));
            connection.connect();

            if (connection.getResponseCode() == 200) {
                StringBuilder inline = new StringBuilder();
                Scanner scanner = new Scanner(connection.getInputStream());
                while (scanner.hasNext()) {
                    inline.append(scanner.nextLine());
                }
                scanner.close();
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(inline.toString());
                JSONArray array = (JSONArray) json.get("items");
                if (array.size() > 0) {
                    JSONObject result = (JSONObject) array.get(0);
                    return result.get("downloadUrl").toString();
                } else {
                    logger.warning("Artifact " + artifact + " is not available at nexus.");
                    return null;
                }
            } else {
                logger.warning("Received response code " + connection.getResponseCode() + " while fetching downloadUrl for artifact: " + artifact);
                logger.warning("Please check if you provided a valid nexus-auth-string in the config.");
                return null;
            }
        } catch (Exception e) {
            logger.warning("Couldn't fetch downloadUrl for artifact: " + artifact);
            e.printStackTrace();
            return null;
        }
    }
}
