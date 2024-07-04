package io.meowresearch.mcserver.s4;

import java.util.Arrays;
import java.util.List;

public class ServerConfig {

    public static String announcement = "对于 Java 版用户，需要安装 Simple Voice Chat mod 以获取地铁站的最佳体验。";

    public static boolean isMaintenanceMode() {
        String maintenance = System.getenv("SERVER_MAINTENANCE");
        return maintenance != null && maintenance.equalsIgnoreCase("true");
    }

    public static String getMaintenanceMessage() {
        String message = System.getenv("MAINTENANCE_MESSAGE");
        return message != null ? message : "The server is currently undergoing maintenance.";
    }

    public static List<String> getWhitelist() {
        String whitelist = System.getenv("MAINTENANCE_WHITELIST");
        if (whitelist != null && !whitelist.isEmpty()) {
            return Arrays.asList(whitelist.split(","));
        }
        return List.of(); // Return an empty list if whitelist is empty.
    }

    public static String getSecret() {
        return System.getenv("SECRET");
    }

    public static boolean isPlayerWhitelisted(String playerName) {
        return getWhitelist().contains(playerName);
    }
}