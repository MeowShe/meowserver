package io.meowresearch.mcserver.s4.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientCheckPayload(String installedModList) implements CustomPayload {
    public static final Identifier PACKET_CLIENT_CHECK = Identifier.of("meow", "client_check");
    public static final CustomPayload.Id<ClientCheckPayload> ID = new CustomPayload.Id<>(PACKET_CLIENT_CHECK);
    public static final PacketCodec<RegistryByteBuf, ClientCheckPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, ClientCheckPayload::installedModList, ClientCheckPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
