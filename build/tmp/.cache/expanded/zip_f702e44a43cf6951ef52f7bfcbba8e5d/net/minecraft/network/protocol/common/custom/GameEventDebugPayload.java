package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record GameEventDebugPayload(ResourceKey<GameEvent> gameEventType, Vec3 pos) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, GameEventDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        GameEventDebugPayload::write, GameEventDebugPayload::new
    );
    public static final CustomPacketPayload.Type<GameEventDebugPayload> TYPE = CustomPacketPayload.createType("debug/game_event");

    private GameEventDebugPayload(FriendlyByteBuf pBuffer) {
        this(pBuffer.readResourceKey(Registries.GAME_EVENT), pBuffer.readVec3());
    }

    private void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeResourceKey(this.gameEventType);
        pBuffer.writeVec3(this.pos);
    }

    @Override
    public CustomPacketPayload.Type<GameEventDebugPayload> type() {
        return TYPE;
    }
}