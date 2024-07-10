package io.meowresearch.mcserver.s4.listener;


import io.meowresearch.mcserver.s4.MeowServerS4;
import io.meowresearch.mcserver.s4.ServerConfig;
import io.meowresearch.mcserver.s4.TaskScheduler;
import io.meowresearch.mcserver.s4.networking.ClientCheckPayload;
import io.meowresearch.mcserver.s4.util.DataWriter;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.geysermc.geyser.api.GeyserApi;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerJoinListener {

    private static final Map<String, Long> opLoginAttempts = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> authenticatedOps = new ConcurrentHashMap<>();
    private static final Map<String, Long> waitingAuthenticatedPlayers = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> authenticatedPlayers = new ConcurrentHashMap<>();
    private static final TaskScheduler taskScheduler = new TaskScheduler();
    private static final GeyserApi geyserApi = GeyserApi.api();


    public static void register() {
        // Maintenance Mode
        ServerPlayConnectionEvents.INIT.register((ServerPlayNetworkHandler handler, MinecraftServer server) -> {
            ServerPlayerEntity player = handler.player;
            // Use player's UUID is much better, but it was broken in offline mode.
            if (ServerConfig.isMaintenanceMode() && !ServerConfig.isPlayerWhitelisted(player.getName().getLiteralString())) {
                Text disconnectMessage = Text.literal(ServerConfig.getMaintenanceMessage());
                handler.disconnect(disconnectMessage);
            }
        });

        // Operator & Anti-Cheat Auth
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            String playerName = player.getGameProfile().getName();

            waitingAuthenticatedPlayers.put(playerName, (long) server.getTicks());
            authenticatedPlayers.put(playerName, false);

            player.sendMessage(Text.literal(ServerConfig.announcement).withColor(Colors.YELLOW));

            // Normal Player Auth
            // 15 seconds timeout for auth.
            taskScheduler.scheduleTask(200L, () -> {
                if (geyserApi.connectionByUuid(player.getUuid()) == null && waitingAuthenticatedPlayers.containsKey(playerName) && !authenticatedPlayers.get(playerName)) {
                    long millisecondsInOneYear = 365L * 24 * 60 * 60 * 1000;
                    long millisecondsInFiveYears = 5 * millisecondsInOneYear;
                    server.getPlayerManager().getIpBanList().add(new BannedIpEntry(player.getIp(), new Date(), "Meow Sever SC", new Date(System.currentTimeMillis() + millisecondsInFiveYears), "To join the game, you need to install the Meow Library mod on your client."));
                    if (!player.isDisconnected()) {
                        player.networkHandler.disconnect(Text.literal("To join the game, you need to install the Meow Library mod on your client. Your IP has been banned."));
                    }
                    waitingAuthenticatedPlayers.remove(playerName);
                    authenticatedPlayers.remove(playerName);
                }
            }, server);

            // Operator Auth
            if (server.getPlayerManager().isOperator(player.getGameProfile())) {
                player.sendMessage(Text.literal("You signed-in as a server Operator.").formatted(Formatting.GREEN));
                // Require auth.
                opLoginAttempts.put(playerName, (long) server.getTicks());
                authenticatedOps.put(playerName, false);

                // 15 seconds timeout for auth.
                taskScheduler.scheduleTask(300L, () -> {
                    if (opLoginAttempts.containsKey(playerName) && !authenticatedOps.get(playerName)) {
                        server.getPlayerManager().getIpBanList().add(new BannedIpEntry(player.getIp()));
                        if (!player.isDisconnected()) {
                            player.networkHandler.disconnect(Text.literal("Authentication timed out."));
                        }
                        opLoginAttempts.remove(playerName);
                        authenticatedOps.remove(playerName);
                    }
                }, server);
            }
        }));

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(((message, sender, params) -> {
            if (opLoginAttempts.containsKey(sender.getGameProfile().getName())) {
                if (message.getContent().getLiteralString() != null && message.getContent().getLiteralString().equals(ServerConfig.getSecret())) {
                    opLoginAttempts.remove(sender.getGameProfile().getName());
                    authenticatedOps.put(sender.getGameProfile().getName(), true);
                    sender.sendMessage(Text.literal("Authentication successful!").formatted(Formatting.GREEN), false);
                } else {
                    sender.sendMessage(Text.literal("Invalid Operator Secret.").formatted(Formatting.RED), false);
                }
                return false;
            }
            return true;
        }));

        ServerPlayNetworking.registerGlobalReceiver(ClientCheckPayload.ID, ((payload, context) -> {
            String playerName = context.player().getGameProfile().getName();
            String installedModList = payload.installedModList();
            waitingAuthenticatedPlayers.remove(context.player().getGameProfile().getName());
            authenticatedPlayers.put(context.player().getGameProfile().getName(), true);
            try {
                DataWriter.appendPlayerModInfoToFile(playerName, List.of(installedModList.split(",")));
            } catch (IOException e) {
                MeowServerS4.LOGGER.error(e.getMessage());
            }
        }));
    }


    public static void onServerTick(MinecraftServer server) {
        taskScheduler.tick(server);
    }
}