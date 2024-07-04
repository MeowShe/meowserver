package io.meowresearch.mcserver.s4;

public class ServerConfig {

    public static boolean isMaintenanceMode() {
        String maintenance = System.getenv("SERVER_MAINTENANCE");
        return maintenance != null && maintenance.equalsIgnoreCase("true");
    }

    public static String getMaintenanceMessage() {
        String message = System.getenv("MAINTENANCE_MESSAGE");
        return message != null ? message : "The server is currently undergoing maintenance.";
    }
}