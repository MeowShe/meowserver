package io.meowresearch.mcserver.s4.listener;


import io.meowresearch.mcserver.s4.MeowServerS4;
import io.meowresearch.mcserver.s4.ServerConfig;
import io.meowresearch.mcserver.s4.TaskScheduler;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerJoinListener {

    private static final Map<String, Long> opLoginAttempts = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> authenticatedOps = new ConcurrentHashMap<>();
    private static final TaskScheduler taskScheduler = new TaskScheduler();

    public static void register() {
        ServerPlayConnectionEvents.INIT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> {
            ServerPlayerEntity player = handler.player;
            // Use player's UUID is much better, but it was broken in offline mode.
            if (ServerConfig.isMaintenanceMode() && !ServerConfig.isPlayerWhitelisted(player.getName().getLiteralString())) {
                Text disconnectMessage = Text.literal(ServerConfig.getMaintenanceMessage());
                handler.disconnect(disconnectMessage);
                MeowServerS4.LOGGER.info("Prevented player join: {}", player.getUuid().toString());
            }
        });

        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            player.sendMessage(Text.literal(ServerConfig.announcement).withColor(Colors.YELLOW));
            if (server.getPlayerManager().isOperator(player.getGameProfile())) {
                player.sendMessage(Text.literal("You signed-in as a Server Operator.").withColor(Colors.GREEN));
                // Required auth.
                opLoginAttempts.put(player.getGameProfile().getName(), (long) server.getTicks());
                authenticatedOps.put(player.getGameProfile().getName(), false);

                // 15 seconds timeout.
                taskScheduler.scheduleTask(300L, () -> {
                    if (opLoginAttempts.containsKey(player.getGameProfile().getName()) && !authenticatedOps.get(player.getGameProfile().getName())) {
                        if (!player.isDisconnected()) {
                            player.networkHandler.disconnect(Text.literal("Authentication timed out."));
                        }
                        opLoginAttempts.remove(player.getGameProfile().getName());
                        authenticatedOps.remove(player.getGameProfile().getName());
                        server.getPlayerManager().getIpBanList().add(new BannedIpEntry(player.getIp()));
                    }
                }, server);
            }
        }));

        ServerMessageEvents.CHAT_MESSAGE.register(((message, sender, params) -> {
            MeowServerS4.LOGGER.info(message.getContent().getLiteralString());
            if (opLoginAttempts.containsKey(sender.getGameProfile().getName())) {
                if (message.getContent().getLiteralString() != null && message.getContent().getLiteralString().equals(ServerConfig.getSecret())) {
                    opLoginAttempts.remove(sender.getGameProfile().getName());
                    authenticatedOps.put(sender.getGameProfile().getName(), true);
                    sender.sendMessage(Text.literal("Authentication successful!").formatted(Formatting.GREEN), false);
                } else {
                    sender.sendMessage(Text.literal("Invalid Operator Secret.").formatted(Formatting.RED), false);
                }
            }
        }));
    }

    public static void onServerTick(MinecraftServer server) {
        taskScheduler.tick(server);
    }
}