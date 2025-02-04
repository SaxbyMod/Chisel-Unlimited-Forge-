package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDelayPacket implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetBorderWarningDelayPacket> STREAM_CODEC = Packet.codec(
        ClientboundSetBorderWarningDelayPacket::write, ClientboundSetBorderWarningDelayPacket::new
    );
    private final int warningDelay;

    public ClientboundSetBorderWarningDelayPacket(WorldBorder pWorldBorder) {
        this.warningDelay = pWorldBorder.getWarningTime();
    }

    private ClientboundSetBorderWarningDelayPacket(FriendlyByteBuf pBuffer) {
        this.warningDelay = pBuffer.readVarInt();
    }

    private void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeVarInt(this.warningDelay);
    }

    @Override
    public PacketType<ClientboundSetBorderWarningDelayPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_BORDER_WARNING_DELAY;
    }

    public void handle(ClientGamePacketListener p_179263_) {
        p_179263_.handleSetBorderWarningDelay(this);
    }

    public int getWarningDelay() {
        return this.warningDelay;
    }
}