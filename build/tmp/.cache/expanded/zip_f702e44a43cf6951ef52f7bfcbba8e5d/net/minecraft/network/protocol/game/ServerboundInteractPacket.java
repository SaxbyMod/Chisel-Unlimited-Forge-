package net.minecraft.network.protocol.game;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ServerboundInteractPacket implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundInteractPacket> STREAM_CODEC = Packet.codec(
        ServerboundInteractPacket::write, ServerboundInteractPacket::new
    );
    private final int entityId;
    private final ServerboundInteractPacket.Action action;
    private final boolean usingSecondaryAction;
    static final ServerboundInteractPacket.Action ATTACK_ACTION = new ServerboundInteractPacket.Action() {
        @Override
        public ServerboundInteractPacket.ActionType getType() {
            return ServerboundInteractPacket.ActionType.ATTACK;
        }

        @Override
        public void dispatch(ServerboundInteractPacket.Handler p_179624_) {
            p_179624_.onAttack();
        }

        @Override
        public void write(FriendlyByteBuf p_179622_) {
        }
    };

    private ServerboundInteractPacket(int pEntityId, boolean pUsingSecondaryAction, ServerboundInteractPacket.Action pAction) {
        this.entityId = pEntityId;
        this.action = pAction;
        this.usingSecondaryAction = pUsingSecondaryAction;
    }

    public static ServerboundInteractPacket createAttackPacket(Entity pEntity, boolean pUsingSecondaryAction) {
        return new ServerboundInteractPacket(pEntity.getId(), pUsingSecondaryAction, ATTACK_ACTION);
    }

    public static ServerboundInteractPacket createInteractionPacket(Entity pEntity, boolean pUsingSecondaryAction, InteractionHand pHand) {
        return new ServerboundInteractPacket(pEntity.getId(), pUsingSecondaryAction, new ServerboundInteractPacket.InteractionAction(pHand));
    }

    public static ServerboundInteractPacket createInteractionPacket(Entity pEntity, boolean pUsingSecondaryAction, InteractionHand pHand, Vec3 pInteractionLocation) {
        return new ServerboundInteractPacket(pEntity.getId(), pUsingSecondaryAction, new ServerboundInteractPacket.InteractionAtLocationAction(pHand, pInteractionLocation));
    }

    private ServerboundInteractPacket(FriendlyByteBuf pBuffer) {
        this.entityId = pBuffer.readVarInt();
        ServerboundInteractPacket.ActionType serverboundinteractpacket$actiontype = pBuffer.readEnum(ServerboundInteractPacket.ActionType.class);
        this.action = serverboundinteractpacket$actiontype.reader.apply(pBuffer);
        this.usingSecondaryAction = pBuffer.readBoolean();
    }

    private void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeVarInt(this.entityId);
        pBuffer.writeEnum(this.action.getType());
        this.action.write(pBuffer);
        pBuffer.writeBoolean(this.usingSecondaryAction);
    }

    @Override
    public PacketType<ServerboundInteractPacket> type() {
        return GamePacketTypes.SERVERBOUND_INTERACT;
    }

    public void handle(ServerGamePacketListener pHandler) {
        pHandler.handleInteract(this);
    }

    @Nullable
    public Entity getTarget(ServerLevel pLevel) {
        return pLevel.getEntityOrPart(this.entityId);
    }

    public boolean isUsingSecondaryAction() {
        return this.usingSecondaryAction;
    }

    public void dispatch(ServerboundInteractPacket.Handler pHandler) {
        this.action.dispatch(pHandler);
    }

    interface Action {
        ServerboundInteractPacket.ActionType getType();

        void dispatch(ServerboundInteractPacket.Handler pHandler);

        void write(FriendlyByteBuf pBuffer);
    }

    static enum ActionType {
        INTERACT(ServerboundInteractPacket.InteractionAction::new),
        ATTACK(p_179639_ -> ServerboundInteractPacket.ATTACK_ACTION),
        INTERACT_AT(ServerboundInteractPacket.InteractionAtLocationAction::new);

        final Function<FriendlyByteBuf, ServerboundInteractPacket.Action> reader;

        private ActionType(final Function<FriendlyByteBuf, ServerboundInteractPacket.Action> pReader) {
            this.reader = pReader;
        }
    }

    public interface Handler {
        void onInteraction(InteractionHand pHand);

        void onInteraction(InteractionHand pHand, Vec3 pInteractionLocation);

        void onAttack();
    }

    static class InteractionAction implements ServerboundInteractPacket.Action {
        private final InteractionHand hand;

        InteractionAction(InteractionHand pHand) {
            this.hand = pHand;
        }

        private InteractionAction(FriendlyByteBuf pBuffer) {
            this.hand = pBuffer.readEnum(InteractionHand.class);
        }

        @Override
        public ServerboundInteractPacket.ActionType getType() {
            return ServerboundInteractPacket.ActionType.INTERACT;
        }

        @Override
        public void dispatch(ServerboundInteractPacket.Handler p_179655_) {
            p_179655_.onInteraction(this.hand);
        }

        @Override
        public void write(FriendlyByteBuf p_179653_) {
            p_179653_.writeEnum(this.hand);
        }
    }

    static class InteractionAtLocationAction implements ServerboundInteractPacket.Action {
        private final InteractionHand hand;
        private final Vec3 location;

        InteractionAtLocationAction(InteractionHand pHand, Vec3 pLocation) {
            this.hand = pHand;
            this.location = pLocation;
        }

        private InteractionAtLocationAction(FriendlyByteBuf pBuffer) {
            this.location = new Vec3((double)pBuffer.readFloat(), (double)pBuffer.readFloat(), (double)pBuffer.readFloat());
            this.hand = pBuffer.readEnum(InteractionHand.class);
        }

        @Override
        public ServerboundInteractPacket.ActionType getType() {
            return ServerboundInteractPacket.ActionType.INTERACT_AT;
        }

        @Override
        public void dispatch(ServerboundInteractPacket.Handler p_179667_) {
            p_179667_.onInteraction(this.hand, this.location);
        }

        @Override
        public void write(FriendlyByteBuf p_179665_) {
            p_179665_.writeFloat((float)this.location.x);
            p_179665_.writeFloat((float)this.location.y);
            p_179665_.writeFloat((float)this.location.z);
            p_179665_.writeEnum(this.hand);
        }
    }
}