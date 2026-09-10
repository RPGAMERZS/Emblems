package com.emblems;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.YamlConfiguration;

public class LicenseManager {

    private final EmblemsPlugin plugin;
    private static final String SERVER_URL = new String(new char[]{'h','t','t','p','s',':','/','/','l','i','c','e','n','s','e','.','m','i','n','e','v','i','a','.','c','l','u','b'});
    private static final String TEAM_ID = new String(new char[]{'8','8','9','9','b','4','3','0','-','f','6','d','5','-','4','0','5','3','-','9','b','5','b','-','5','d','0','b','7','7','8','b','f','7','4','f'});
    private static final String PRODUCT_ID = new String(new char[]{'b','f','8','e','b','c','6','1','-','a','6','d','d','-','4','5','9','4','-','8','d','6','9','-','3','f','9','e','a','2','3','6','1','e','6','e'});

    public LicenseManager(EmblemsPlugin plugin) {
        this.plugin = plugin;
    }

    public LicenseResult verify() {
        String key = getKeyFromLicenseFile();
        if (key == null || key.trim().isEmpty()) {
            return new LicenseResult(false, "No license key configured! Create license.yml and add your key. Get one at https://discord.gg/vNWpjVeGf5");
        }

        try {
            String apiUrl = SERVER_URL + "/api/v1/client/teams/" + TEAM_ID + "/verification/verify";
            String jsonBody = "{\"licenseKey\":\"" + escapeJson(key) + "\",\"productId\":\"" + PRODUCT_ID + "\"}";

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();

            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            String responseBody = response.toString();

            if (responseCode == 429) {
                return new LicenseResult(false, "Rate limited. Try again later.");
            }

            boolean valid = responseBody.contains("\"valid\":true") || responseBody.contains("\"valid\": true");

            if (valid) {
                return new LicenseResult(true, "License verified successfully!");
            } else {
                String details = extractField(responseBody, "details");
                String code = extractField(responseBody, "code");
                String error = code.isEmpty() ? details : code + " - " + details;
                return new LicenseResult(false, "License invalid: " + error);
            }

        } catch (Exception e) {
            return new LicenseResult(false, "Failed to connect to license server: " + e.getMessage());
        }
    }

    private String getKeyFromLicenseFile() {
        File licenseFile = new File(plugin.getDataFolder(), "license.yml");
        if (!licenseFile.exists()) {
            plugin.saveResource("license.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(licenseFile);
        return yaml.getString("license-key", "");
    }

    private String extractField(String json, String field) {
        String search = "\"" + field + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            search = "\"" + field + "\": \"";
            start = json.indexOf(search);
        }
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end);
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static class LicenseResult {
        private final boolean valid;
        private final String message;

        public LicenseResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}

