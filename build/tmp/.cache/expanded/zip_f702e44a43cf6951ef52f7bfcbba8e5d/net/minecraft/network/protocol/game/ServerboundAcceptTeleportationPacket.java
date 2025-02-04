package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundAcceptTeleportationPacket implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundAcceptTeleportationPacket> STREAM_CODEC = Packet.codec(
        ServerboundAcceptTeleportationPacket::write, ServerboundAcceptTeleportationPacket::new
    );
    private final int id;

    public ServerboundAcceptTeleportationPacket(int pId) {
        this.id = pId;
    }

    private ServerboundAcceptTeleportationPacket(FriendlyByteBuf pBuffer) {
        this.id = pBuffer.readVarInt();
    }

    private void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeVarInt(this.id);
    }

    @Override
    public PacketType<ServerboundAcceptTeleportationPacket> type() {
        return GamePacketTypes.SERVERBOUND_ACCEPT_TELEPORTATION;
    }

    public void handle(ServerGamePacketListener pHandler) {
        pHandler.handleAcceptTeleportPacket(this);
    }

    public int getId() {
        return this.id;
    }
}