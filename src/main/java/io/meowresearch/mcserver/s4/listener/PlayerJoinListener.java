package io.meowresearch.mcserver.s4.listener;


import io.meowresearch.mcserver.s4.MeowServerS4;
import io.meowresearch.mcserver.s4.ServerConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;

public class PlayerJoinListener {

    public static void register() {
        ServerPlayConnectionEvents.INIT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> {
            if (ServerConfig.isMaintenanceMode()) {
                MeowServerS4.LOGGER.info("Prevented player join because maintenance.");
                Text disconnectMessage = Text.literal(ServerConfig.getMaintenanceMessage());
                handler.disconnect(disconnectMessage);
            }
        });
    }
}